package com.notononoto.teamcity.telegram.config;

/** Plugin settings */
public class TelegramSettings {

  /** Name of bot */
  private final String botName;
  /** Bot's token */
  private final String botToken;

  public TelegramSettings(String botName, String botToken) {
    this.botName = botName;
    this.botToken = botToken;
  }

  public String getBotName() {
    return botName;
  }

  public String getBotToken() {
    return botToken;
  }
}
