package com.notononoto.teamcity.telegram.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.ChangeObserver;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Settings manager */
public class TelegramSettingsManager implements ChangeListener {

  private static final Logger LOG = Logger.getInstance(TelegramSettingsManager.class.getName());

  /** Fields names in xml */
  private static final String BOT_NAME_ATTR = "bot-name";
  private static final String BOT_TOKEN_ATTR = "bot-token";

  /** Configuration file */
  private final Path configFile;
  /** Check file system changes */
  private final ChangeObserver changeObserver;
  /** Plugin settings */
  private TelegramSettings settings;

  public TelegramSettingsManager(@NotNull ServerPaths paths) {
    configFile = Paths.get(paths.getConfigDir() + "_notifications").
        resolve("telegram").resolve("telegram-config.xml");
    reloadConfiguration();

    changeObserver = new FileWatcher(configFile.toFile());
    changeObserver.setSleepingPeriod(10000L);
    changeObserver.registerListener(this);
    changeObserver.start();
  }

  @Override
  public void changeOccured(String requestor) {
    this.reloadConfiguration();
  }

  public TelegramSettings getSettings() {
    return settings;
  }

  /**
   * Save configuration on disk
   * @param newSettings {@link TelegramSettings}
   */
  public void saveConfiguration(@NotNull TelegramSettings newSettings) {
    changeObserver.runActionWithDisabledObserver(() ->
        FileUtil.processXmlFile(configFile.toFile(), (root) -> {
          root.setAttribute(BOT_NAME_ATTR, newSettings.getBotName());
          root.setAttribute(BOT_TOKEN_ATTR, newSettings.getBotToken());
        }));
    settings = newSettings;
  }

  private void reloadConfiguration() {
    LOG.info("Loading configuration file: " + configFile);
    Document document = parseFile(configFile);
    // Don't kill all system. Telegram don't enough important.
    if (document == null) {
      return;
    }
    Element rootElement = document.getRootElement();
    settings = new TelegramSettings(
        rootElement.getAttributeValue(BOT_NAME_ATTR),
        rootElement.getAttributeValue(BOT_TOKEN_ATTR));
  }

  private Document parseFile(Path configFile) {
    try {
      return JDOMUtil.loadDocument(configFile.toFile());
    } catch (JDOMException | IOException ex) {
      LOG.error("Failed to parse telegram configuration file: " + configFile, ex);
      return null;
    }
  }
}
