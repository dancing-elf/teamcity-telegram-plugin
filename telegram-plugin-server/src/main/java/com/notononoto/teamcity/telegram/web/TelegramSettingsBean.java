package com.notononoto.teamcity.telegram.web;

import com.notononoto.teamcity.telegram.config.TelegramSettings;
import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.controllers.StateField;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Telegram settings used on client side
 */
public class TelegramSettingsBean extends RememberState {

  @StateField
  private String botToken;
  /** Bot state */
  @StateField
  private boolean paused;
  @StateField
  private boolean useProxy;
  @StateField
  private String proxyServer;
  @StateField
  private String proxyPort;
  @StateField
  private String proxyUsername;
  @StateField
  private String proxyPassword;

  public TelegramSettingsBean(@NotNull TelegramSettings settings) {
    this.botToken = settings.getBotToken();
    this.paused = settings.isPaused();
    this.useProxy = settings.isUseProxy();
    this.proxyServer = settings.getProxyServer();
    this.proxyPort = settings.getProxyPort() == null ?
        "" : Integer.toString(settings.getProxyPort());
    this.proxyUsername = settings.getProxyUsername();
    this.proxyPassword = settings.getProxyPassword();
    rememberState();
  }

  public String getBotToken() {
    return botToken;
  }

  public void setBotToken(String botToken) {
    this.botToken = botToken;
  }

  public String getHexEncodedPublicKey() {
    return RSACipher.getHexEncodedPublicKey();
  }

  public String getEncryptedBotToken() {
    return StringUtil.isEmpty(botToken) ? "" : RSACipher.encryptDataForWeb(botToken);
  }

  public void setEncryptedBotToken(String encrypted) {
    this.botToken = RSACipher.decryptWebRequestData(encrypted);
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public boolean isUseProxy() {
    return useProxy;
  }

  public void setUseProxy(boolean useProxy) {
    this.useProxy = useProxy;
  }

  public String getProxyServer() {
    return proxyServer;
  }

  public void setProxyServer(String proxyServer) {
    this.proxyServer = proxyServer;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(String proxyPort) {
    this.proxyPort = proxyPort;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  public String getEncryptedProxyPassword() {
    return StringUtil.isEmpty(proxyPassword) ? "" : RSACipher.encryptDataForWeb(proxyPassword);
  }

  public void setEncryptedProxyPassword(String encrypted) {
    this.proxyPassword = RSACipher.decryptWebRequestData(encrypted);
  }

  public TelegramSettings toSettings() {
    TelegramSettings settings = new TelegramSettings();
    settings.setBotToken(botToken);
    settings.setPaused(paused);
    settings.setUseProxy(useProxy);
    settings.setProxyServer(proxyServer);
    settings.setProxyPort(StringUtil.isEmpty(proxyPort) ? null : Integer.valueOf(proxyPort));
    settings.setProxyUsername(proxyUsername);
    settings.setProxyPassword(proxyPassword);
    return settings;
  }
}
