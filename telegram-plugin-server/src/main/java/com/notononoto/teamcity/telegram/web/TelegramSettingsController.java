package com.notononoto.teamcity.telegram.web;

import com.notononoto.teamcity.telegram.config.TelegramSettings;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Plugin servlet. Should handle writing of settings.
 */
public class TelegramSettingsController extends BaseController {

  /** Settings manager */
  private final TelegramSettingsManager settingsManager;

  public TelegramSettingsController(@NotNull WebControllerManager webManager,
                                    @NotNull TelegramSettingsManager settingsManager) {
    webManager.registerController("/saveTelegramSettings.html", this);
    this.settingsManager = settingsManager;
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response) throws Exception {
    if (isPost(request)) {
      // looks ugly. Maybe we should implement BaseFormXmlController.
      // Right now it looks like overhead. Why we can't use NotifierSettingsTab
      // as usual Jetbrains notification plugin?
      new AjaxRequestProcessor().processRequest(request, response,
          (req, resp, xmlResponse) -> handle(req));
    }
    return null;
  }

  private void handle(HttpServletRequest request) {
    String botName = request.getParameter(TelegramSettingsPage.BOT_NAME);
    String botToken = request.getParameter(TelegramSettingsPage.BOT_TOKEN);
    TelegramSettings settings = new TelegramSettings(botName, botToken);
    if (isValidSettings(settings)) {
      settingsManager.saveConfiguration(settings);
      getOrCreateMessages(request).addMessage("msg", "Settings successfully saved");
    } else {
      getOrCreateMessages(request).addMessage("msg", "Please fill all fields");
    }
  }

  private boolean isValidSettings(@NotNull TelegramSettings settings) {
    if (!isValidString(settings.getBotName())) {
      return false;
    }
    if (!isValidString(settings.getBotToken())) {
      return false;
    }
    return true;
  }

  private boolean isValidString(String str) {
    return str != null && !str.isEmpty();
  }
}
