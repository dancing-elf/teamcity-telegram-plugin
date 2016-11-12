package com.notononoto.teamcity.telegram.web;

import com.intellij.openapi.util.text.StringUtil;
import com.notononoto.teamcity.telegram.TelegramNotificator;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import jetbrains.buildServer.notification.NotificationRulesManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.users.UserNotFoundException;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Extension of user's main page for "Telegram Notifier" part
 */
public class UserTelegramSettingsExtension extends SimplePageExtension {

  private final NotificationRulesManager rulesManager;
  private final UserModel userModel;
  private final TelegramSettingsManager settingsManager;

  public UserTelegramSettingsExtension(@NotNull WebControllerManager manager,
                                       @NotNull NotificationRulesManager rulesManager,
                                       @NotNull UserModel userModel,
                                       @NotNull PluginDescriptor descriptor,
                                       @NotNull TelegramSettingsManager settingsManager) {
    super(manager);
    this.rulesManager = rulesManager;
    this.userModel = userModel;
    this.settingsManager = settingsManager;

    setPluginName(TelegramSettingsPage.PLUGIN_NAME);
    setIncludeUrl(descriptor.getPluginResourcesPath("userTelegramSettings.jsp"));
    // This extension required by two places. Don't looks clear
    // but works...
    setPlaceId(PlaceId.NOTIFIER_SETTINGS_FRAGMENT);
    register();
    setPlaceId(PlaceId.MY_SETTINGS_NOTIFIER_SECTION);
    register();
  }

  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return "/profile.html".equals(WebUtil.getPathWithoutContext(request)) ||
        getPluginName().equals(request.getParameter("notificatorType"));
  }

  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    SUser user = SessionUser.getUser(request);
    String userIdStr = request.getParameter("userId");
    if (userIdStr != null) {
      long userId = Long.parseLong(userIdStr);
      user = userModel.findUserById(userId);
      if (user == null) {
        throw new UserNotFoundException(userId, "User with id " + userIdStr + " does not exist");
      }
    }

    boolean telegramNotConfigured = true;
    if (rulesManager.isRulesWithEventsConfigured(user.getId(), this.getPluginName())) {
      String chatId = user.getPropertyValue(TelegramNotificator.TELEGRAM_PROP_KEY);
      telegramNotConfigured = StringUtil.isEmpty(chatId);
    }

    model.put("showTelegramNotConfiguredWarning", telegramNotConfigured);
    model.put("showTelegramPausedWarning", settingsManager.getSettings().isPaused());
  }
}
