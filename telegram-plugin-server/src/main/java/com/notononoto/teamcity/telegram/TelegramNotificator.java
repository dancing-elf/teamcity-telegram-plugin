package com.notononoto.teamcity.telegram;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.vcs.VcsRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


/** Telegram notifier */
public class TelegramNotificator extends NotificatorAdapter {

  /**
   * In {@link Loggers} doesn't exists NOTIFIER entry. Package renaming to
   * jetbrains.buildServer something very strange. But maybe it's required...
   * So don't use this log here too active...
   */
  private static final Logger LOG = Loggers.SERVER;

  /** Inner property name */
  private static final String CHAT_ID_PROP = "telegram-chat-id";
  /** Notificator type */
  private static final String NOTIFICATOR_TYPE = "telegram";

  /** Property key description */
  public static final PropertyKey TELEGRAM_PROP_KEY =
      new NotificatorPropertyKey(NOTIFICATOR_TYPE, CHAT_ID_PROP);

  /** User input field at notification rules tab */
  private static List<UserPropertyInfo> USER_PROPERTIES = Collections.singletonList(
      new UserPropertyInfo(CHAT_ID_PROP, "Telegram chat id", null,
          (UserPropertyValidator) (propertyValue, editee, currentUserData) ->
              StringUtil.isNumber(propertyValue) ? null : "Chat id should be a number"));


  /** Telegram bot manager */
  private final TelegramBotManager botManager;


  public TelegramNotificator(@NotNull NotificatorRegistry registry,
                             @NotNull TelegramBotManager botManager) {
    this.botManager = botManager;
    registry.register(this, USER_PROPERTIES);
  }

  @NotNull
  @Override
  public String getNotificatorType() {
    return NOTIFICATOR_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Telegram Notifier";
  }

  @Override
  public void notifyBuildStarted(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is started");
  }

  @Override
  public void notifyBuildSuccessful(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is successful");
  }

  @Override
  public void notifyBuildFailed(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is failed");
  }

  @Override
  public void notifyBuildFailedToStart(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is failed to start");
  }

  @Override
  public void notifyLabelingFailed(@NotNull Build build, @NotNull VcsRoot root,
                                   @NotNull Throwable exception, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is labeling failed");
  }

  @Override
  public void notifyBuildFailing(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    sendNotification(users, build.getFullName() + " is build failing");
  }

  @Override
  public void notifyBuildProbablyHanging(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleChanged(@NotNull SBuildType buildType, @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleAssigned(@NotNull SBuildType buildType, @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleChanged(@Nullable TestNameResponsibilityEntry oldValue,
                                       @NotNull TestNameResponsibilityEntry newValue,
                                       @NotNull SProject project,
                                       @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleAssigned(@Nullable TestNameResponsibilityEntry oldValue,
                                        @NotNull TestNameResponsibilityEntry newValue,
                                        @NotNull SProject project,
                                        @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleChanged(@NotNull Collection<TestName> testNames,
                                       @NotNull ResponsibilityEntry entry,
                                       @NotNull SProject project,
                                       @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyResponsibleAssigned(@NotNull Collection<TestName> testNames,
                                        @NotNull ResponsibilityEntry entry,
                                        @NotNull SProject project,
                                        @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyBuildProblemResponsibleAssigned(@NotNull Collection<BuildProblemInfo> buildProblems,
                                                    @NotNull ResponsibilityEntry entry,
                                                    @NotNull SProject project,
                                                    @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyBuildProblemResponsibleChanged(@NotNull Collection<BuildProblemInfo> buildProblems,
                                                   @NotNull ResponsibilityEntry entry,
                                                   @NotNull SProject project,
                                                   @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyTestsMuted(@NotNull Collection<STest> tests,
                               @NotNull MuteInfo muteInfo,
                               @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyTestsUnmuted(@NotNull Collection<STest> tests,
                                 @NotNull MuteInfo muteInfo,
                                 @Nullable SUser user,
                                 @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyBuildProblemsMuted(@NotNull Collection<BuildProblemInfo> buildProblems,
                                       @NotNull MuteInfo muteInfo,
                                       @NotNull Set<SUser> users) {

  }

  @Override
  public void notifyBuildProblemsUnmuted(@NotNull Collection<BuildProblemInfo> buildProblems,
                                         @NotNull MuteInfo muteInfo,
                                         @Nullable SUser user,
                                         @NotNull Set<SUser> users) {

  }

  private void sendNotification(@NotNull Set<SUser> users, @NotNull String message) {
    LOG.debug("Send to telegram message: " +
        StringUtil.truncateStringValueWithDotsAtEnd(message, 80));
    collectChatIds(users).forEach(chatId -> {
      try {
        botManager.sendMessage(chatId, message);
      } catch (Exception ex) {
        LOG.warnAndDebugDetails("Can't send message to chatId='" + chatId + "'", ex);
      }
    });
  }

  /**
   * @param users telegram users
   * @return users ids without duplicates
   */
  private List<Long> collectChatIds(@NotNull Set<SUser> users) {
    return users.stream().
        map(user -> user.getPropertyValue(TELEGRAM_PROP_KEY)).
        filter(Objects::nonNull).
        map(Long::parseLong).
        distinct().
        collect(Collectors.toList());
  }
}