package com.notononoto.teamcity.telegram.web;

import com.notononoto.teamcity.telegram.config.TelegramSettings;
import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.controllers.StateField;
import org.jetbrains.annotations.NotNull;

/**
 * Telegram settings used on client side
 */
public class TelegramSettingsBean extends RememberState {

  /** Bot's token */
  @StateField
  private String botToken;
  /** Bot state */
  @StateField
  private boolean paused;

  public TelegramSettingsBean(@NotNull TelegramSettings settings) {
    this.botToken = settings.getBotToken();
    this.paused = settings.isPaused();
    rememberState();
  }

  public String getBotToken() {
    return botToken;
  }

  public void setBotToken(String botToken) {
    this.botToken = botToken;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }
}
