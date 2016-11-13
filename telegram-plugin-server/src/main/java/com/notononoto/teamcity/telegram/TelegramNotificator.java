package com.notononoto.teamcity.telegram;

import com.intellij.openapi.diagnostic.Logger;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import freemarker.core.Environment;
import freemarker.template.*;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.notification.TemplateMessageBuilder;
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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
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

  /** Name of message variable in FreeMarker context after template execution */
  private static final String FREE_MARKER_MSG_KEY = "message";
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
  /** FreeMarker message builder */
  private final TemplateMessageBuilder messageBuilder;
  /** Templates files config dir */
  private final Configuration freeMarkerConfig;

  public TelegramNotificator(@NotNull NotificatorRegistry registry,
                             @NotNull TelegramSettingsManager settingsManager,
                             @NotNull TelegramBotManager botManager,
                             @NotNull TemplateMessageBuilder messageBuilder)
      throws IOException {
    this.botManager = botManager;
    this.messageBuilder = messageBuilder;
    // Plugin will not work if that statement fails, so don't suppress exception here
    freeMarkerConfig = createFreeMarkerConfig(settingsManager.getSettingsDir());
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
    Map<String, Object> props = messageBuilder.getBuildStartedMap(build, users);
    sendNotification(props, users, "build_started");
  }

  @Override
  public void notifyBuildSuccessful(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.getBuildSuccessfulMap(build, users);
    sendNotification(props, users, "build_successful");
  }

  @Override
  public void notifyBuildFailed(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.getBuildFailedMap(build, users);
    sendNotification(props, users, "build_failed");
  }

  @Override
  public void notifyBuildFailedToStart(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.getBuildFailedToStartMap(build, users);
    sendNotification(props, users, "build_failed_to_start");
  }

  @Override
  public void notifyLabelingFailed(@NotNull Build build, @NotNull VcsRoot root,
                                   @NotNull Throwable exception, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.
        getLabelingFailedMap((SBuild)build, root, exception, users);
    sendNotification(props, users, "labeling_failed");
  }

  @Override
  public void notifyBuildFailing(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.getBuildFailedMap(build, users);
    sendNotification(props, users, "build_failed");
  }

  @Override
  public void notifyBuildProbablyHanging(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.getBuildProbablyHangingMap(build, users);
    sendNotification(props, users, "build_probably_hanging");
  }

  @Override
  public void notifyResponsibleChanged(@NotNull SBuildType buildType, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.
        getBuildTypeResponsibilityChangedMap(buildType, users);
    sendNotification(props, users, "build_type_responsibility_changed");
  }

  @Override
  public void notifyResponsibleAssigned(@NotNull SBuildType buildType, @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.
        getBuildTypeResponsibilityAssignedMap(buildType, users);
    sendNotification(props, users, "build_type_responsibility_assigned_to_me");
  }

  @Override
  public void notifyResponsibleChanged(@Nullable TestNameResponsibilityEntry oldValue,
                                       @NotNull TestNameResponsibilityEntry newValue,
                                       @NotNull SProject project,
                                       @NotNull Set<SUser> users) {
    Map<String, Object> props = messageBuilder.
        getTestResponsibilityChangedMap(newValue, oldValue, project, users);
    sendNotification(props, users, "test_responsibility_changed");
  }

  @Override
  public void notifyResponsibleAssigned(@Nullable TestNameResponsibilityEntry oldValue,
                                        @NotNull TestNameResponsibilityEntry newValue,
                                        @NotNull SProject project,
                                        @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getTestResponsibilityAssignedMap(newValue, oldValue, project, users);
    this.sendNotification(root, users, "test_responsibility_assigned_to_me");
  }

  @Override
  public void notifyResponsibleChanged(@NotNull Collection<TestName> testNames,
                                       @NotNull ResponsibilityEntry entry,
                                       @NotNull SProject project,
                                       @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getTestResponsibilityAssignedMap(testNames, entry, project, users);
    this.sendNotification(root, users, "multiple_test_responsibility_changed");
  }

  @Override
  public void notifyResponsibleAssigned(@NotNull Collection<TestName> testNames,
                                        @NotNull ResponsibilityEntry entry,
                                        @NotNull SProject project,
                                        @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getTestResponsibilityChangedMap(testNames, entry, project, users);
    this.sendNotification(root, users, "multiple_test_responsibility_assigned_to_me");
  }

  @Override
  public void notifyBuildProblemResponsibleAssigned(@NotNull Collection<BuildProblemInfo> buildProblems,
                                                    @NotNull ResponsibilityEntry entry,
                                                    @NotNull SProject project,
                                                    @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getBuildProblemsResponsibilityAssignedMap(buildProblems, entry, project, users);
    this.sendNotification(root, users, "build_problem_responsibility_assigned_to_me");
  }

  @Override
  public void notifyBuildProblemResponsibleChanged(@NotNull Collection<BuildProblemInfo> buildProblems,
                                                   @NotNull ResponsibilityEntry entry,
                                                   @NotNull SProject project,
                                                   @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getBuildProblemsResponsibilityAssignedMap(buildProblems, entry, project, users);
    this.sendNotification(root, users, "build_problem_responsibility_changed");
  }

  @Override
  public void notifyTestsMuted(@NotNull Collection<STest> tests,
                               @NotNull MuteInfo muteInfo,
                               @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getTestsMutedMap(tests, muteInfo, users);
    this.sendNotification(root, users, "tests_muted");
  }

  @Override
  public void notifyTestsUnmuted(@NotNull Collection<STest> tests,
                                 @NotNull MuteInfo muteInfo,
                                 @Nullable SUser user,
                                 @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getTestsUnmutedMap(tests, muteInfo, user, users);
    this.sendNotification(root, users, "tests_unmuted");
  }

  @Override
  public void notifyBuildProblemsMuted(@NotNull Collection<BuildProblemInfo> buildProblems,
                                       @NotNull MuteInfo muteInfo,
                                       @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getBuildProblemsMutedMap(buildProblems, muteInfo, users);
    this.sendNotification(root, users, "build_problems_muted");
  }

  @Override
  public void notifyBuildProblemsUnmuted(@NotNull Collection<BuildProblemInfo> buildProblems,
                                         @NotNull MuteInfo muteInfo,
                                         @Nullable SUser user,
                                         @NotNull Set<SUser> users) {
    Map<String, Object> root = messageBuilder.
        getBuildProblemsMutedMap(buildProblems, muteInfo, users);
    this.sendNotification(root, users, "build_problems_unmuted");
  }

  /**
   * Send notifications to telegram users
   * @param props template parameters
   * @param users users to send messages
   * @param templateName template name
   */
  private void sendNotification(@NotNull Map<String, Object> props,
                                @NotNull Set<SUser> users,
                                @NotNull String templateName) {
    String message;
    try (StringWriter writer = new StringWriter()) {
      Template template = freeMarkerConfig.getTemplate(templateName + ".ftl");
      Environment env = template.createProcessingEnvironment(props, writer, null);
      env.process();
      if (!env.getKnownVariableNames().contains(FREE_MARKER_MSG_KEY)) {
        LOG.warn("Can't extract message from template. Message will not be sended");
        return;
      }
      message = env.getVariable(FREE_MARKER_MSG_KEY).toString();
    } catch (IOException | TemplateException ex) {
      LOG.error("Can't execute template '" + templateName + ".ftl': ", ex);
      return;
    }

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

  private Configuration createFreeMarkerConfig(@NotNull Path configDir) throws IOException {
    Configuration cfg = new Configuration();
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setDirectoryForTemplateLoading(configDir.toFile());
    cfg.setTemplateUpdateDelay(TeamCityProperties.getInteger(
        "teamcity.notification.template.update.interval", 60));
    return cfg;
  }
}