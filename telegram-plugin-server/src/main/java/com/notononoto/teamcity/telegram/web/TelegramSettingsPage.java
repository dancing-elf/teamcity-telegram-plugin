package com.notononoto.teamcity.telegram.web;

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

  // Name of settings property in web request/response.
  public static final String SETTINGS_NAME = "telegramSettings";
  // Plugin name in url
  public static final String PLUGIN_NAME = "telegram";


  private final TelegramSettingsManager settingsManager;

  public TelegramSettingsPage(@NotNull PagePlaces places,
                              @NotNull PluginDescriptor descriptor,
                              @NotNull TelegramSettingsManager settingsManager) {
    super(places);
    setPluginName(PLUGIN_NAME);
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
    model.put(SETTINGS_NAME, new TelegramSettingsBean(settingsManager.getSettings()));
  }
}
