package com.notononoto.teamcity.telegram.web;

import com.notononoto.teamcity.telegram.config.TelegramSettings;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/** Information about page rendering and reading of settings */
public class TelegramSettingsPage extends AdminPage {

  // Names of settings properties in web request/response. Maybe
  // in future they shout be placed in separate class or enum...
  public static final String BOT_NAME = "bot_name";
  public static final String BOT_TOKEN = "bot_token";


  private final TelegramSettingsManager settingsManager;

  public TelegramSettingsPage(@NotNull PagePlaces places,
                              @NotNull PluginDescriptor descriptor,
                              @NotNull TelegramSettingsManager settingsManager) {
    super(places);
    setPluginName("telegram");
    setTabTitle("Telegram Notifier");
    setIncludeUrl(descriptor.getPluginResourcesPath("telegramSettings.jsp"));
    setPosition(PositionConstraint.after("email", "jabber"));
    register();
    this.settingsManager = settingsManager;
  }

  @NotNull
  @Override
  public String getGroup() {
    return SERVER_RELATED_GROUP;
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return super.isAvailable(request) &&
        checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);
    TelegramSettings settings = settingsManager.getSettings();
    model.put(BOT_NAME, settings.getBotName());
    model.put(BOT_TOKEN, settings.getBotToken());
  }
}
