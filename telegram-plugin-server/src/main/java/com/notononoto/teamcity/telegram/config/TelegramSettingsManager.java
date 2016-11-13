package com.notononoto.teamcity.telegram.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.notononoto.teamcity.telegram.TelegramBotManager;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.ChangeObserver;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/** Settings manager */
public class TelegramSettingsManager implements ChangeListener {

  private static final Logger LOG = Loggers.SERVER;

  /** Fields names in xml */
  private static final String BOT_TOKEN_ATTR = "bot-token";
  private static final String PAUSE_ATTR = "paused";

  private static final String CONFIG_FILE_NAME = "telegram-config.xml";

  /** Configuration file */
  private final Path configFile;
  /** Configuration directory */
  private final Path configDir;
  /** Check file system changes */
  private final ChangeObserver changeObserver;
  /** Telegram bot manager */
  private final TelegramBotManager botManager;
  /** Plugin settings */
  private TelegramSettings settings;

  public TelegramSettingsManager(@NotNull ServerPaths paths,
                                 @NotNull TelegramBotManager botManager)
      throws JDOMException, IOException {

    configDir = Paths.get(paths.getConfigDir()).resolve("_notifications").
        resolve("telegram");
    configFile = configDir.resolve(CONFIG_FILE_NAME);
    this.botManager = botManager;

    initResources();
    reloadConfiguration();

    changeObserver = new FileWatcher(configFile.toFile());
    changeObserver.setSleepingPeriod(10000L);
    changeObserver.registerListener(this);
    changeObserver.start();
  }

  @Override
  public void changeOccured(String requestor) {
    try {
      reloadConfiguration();
    } catch (IOException | JDOMException ex) {
      throw new RuntimeException(ex);
    }
  }

  @NotNull
  public TelegramSettings getSettings() {
    return settings;
  }

  @NotNull
  public Path getSettingsDir() {
    return configDir;
  }

  /**
   * Save configuration on disk
   * @param newSettings {@link TelegramSettings}
   */
  public synchronized void saveConfiguration(@NotNull TelegramSettings newSettings) {
    changeObserver.runActionWithDisabledObserver(() ->
        FileUtil.processXmlFile(configFile.toFile(), (root) -> {
          String rawToken = newSettings.getBotToken();
          String token = StringUtil.isEmptyOrSpaces(rawToken) ?
              rawToken : EncryptUtil.scramble(rawToken);
          root.setAttribute(BOT_TOKEN_ATTR, token);
          root.setAttribute(PAUSE_ATTR, Boolean.toString(newSettings.isPaused()));
        }));
    settings = newSettings;
    botManager.reloadIfNeeded(settings);
  }

  private synchronized void reloadConfiguration() throws JDOMException, IOException {
    LOG.info("Loading configuration file: " + configFile);
    Document document = JDOMUtil.loadDocument(configFile.toFile());

    Element rootElement = document.getRootElement();
    String token = rootElement.getAttributeValue(BOT_TOKEN_ATTR);
    if (!StringUtil.isEmptyOrSpaces(token)) {
      token = EncryptUtil.unscramble(token);
    }
    boolean pause = Boolean.parseBoolean(rootElement.getAttributeValue(PAUSE_ATTR));

    settings = new TelegramSettings(token, pause);
    botManager.reloadIfNeeded(settings);
  }

  private void initResources() {
    try {
      Files.createDirectories(configDir);
      copyResourceIfNotExists(configDir, CONFIG_FILE_NAME);
      copyResourceIfNotExists(configDir, "telegram-config.dtd");
      copyResourceIfNotExists(configDir, "build_failed.ftl");
      copyResourceIfNotExists(configDir, "build_failed_to_start.ftl");
      copyResourceIfNotExists(configDir, "build_failing.ftl");
      copyResourceIfNotExists(configDir, "build_probably_hanging.ftl");
      copyResourceIfNotExists(configDir, "build_problem_responsibility_assigned_to_me.ftl");
      copyResourceIfNotExists(configDir, "build_problem_responsibility_changed.ftl");
      copyResourceIfNotExists(configDir, "build_problems_muted.ftl");
      copyResourceIfNotExists(configDir, "build_problems_unmuted.ftl");
      copyResourceIfNotExists(configDir, "build_started.ftl");
      copyResourceIfNotExists(configDir, "build_successful.ftl");
      copyResourceIfNotExists(configDir, "build_type_responsibility_assigned_to_me.ftl");
      copyResourceIfNotExists(configDir, "build_type_responsibility_changed.ftl");
      copyResourceIfNotExists(configDir, "common.ftl");
      copyResourceIfNotExists(configDir, "labeling_failed.ftl");
      copyResourceIfNotExists(configDir, "multiple_test_responsibility_assigned_to_me.ftl");
      copyResourceIfNotExists(configDir, "multiple_test_responsibility_changed.ftl");
      copyResourceIfNotExists(configDir, "mute.ftl");
      copyResourceIfNotExists(configDir, "responsibility.ftl");
    } catch (IOException ex) {
      LOG.error("Failed to create telegram plugin config directory", ex);
    }
  }

  private void copyResourceIfNotExists(@NotNull Path configDir, @NotNull String name) {
    FileUtil.copyResourceIfNotExists(this.getClass(),
        "/telegram_templates/" + name, configDir.resolve(name).toFile());
  }
}
