package com.notononoto.teamcity.telegram;

/** Telegram bot description */
public class BotInfo {

  /** Name of bot */
  private final String name;
  /** Telegram username for bot */
  private final String username;

  public BotInfo(String name, String username) {
    this.name = name;
    this.username = username;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }
}
