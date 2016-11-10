package com.notononoto.teamcity.telegram;

import com.intellij.openapi.diagnostic.Logger;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.VcsRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/** Telegram notifier */
public class TelegramNotificator extends NotificatorAdapter {

  private static final Logger LOG = Logger.getInstance(TelegramNotificator.class.getName());

  /** User input field at notification rules tab */
  private static List<UserPropertyInfo> USER_PROPERTIES = Collections.singletonList(
      new UserPropertyInfo("telegram-chat-id", "Telegram chat id"));
  private final TelegramSettingsManager settingsManager;


  public TelegramNotificator(@NotNull NotificatorRegistry registry,
                             @NotNull TelegramSettingsManager settingsManager) {
    this.settingsManager = settingsManager;
    registry.register(this, USER_PROPERTIES);
  }

  @NotNull
  @Override
  public String getNotificatorType() {
    return "Telegram notifier";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "telegram";
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
    LOG.info(message);
    LOG.info(settingsManager.toString());
  }
}