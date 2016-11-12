package com.notononoto.teamcity.telegram.config;

import java.util.Objects;


/** Plugin settings */
public class TelegramSettings {

  /** Bot's token */
  private final String botToken;
  /** Bot state */
  private final boolean paused;

  public TelegramSettings(String botToken, boolean paused) {
    this.botToken = botToken;
    this.paused = paused;
  }

  public String getBotToken() {
    return botToken;
  }

  public boolean isPaused() {
    return paused;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TelegramSettings that = (TelegramSettings) o;
    return paused == that.paused &&
        Objects.equals(botToken, that.botToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(botToken, paused);
  }
}
