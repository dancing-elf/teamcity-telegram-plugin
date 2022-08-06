package com.notononoto.teamcity.telegram.web;

import com.intellij.openapi.diagnostic.Logger;
import com.notononoto.teamcity.telegram.BotInfo;
import com.notononoto.teamcity.telegram.TelegramBotManager;
import com.notononoto.teamcity.telegram.config.TelegramSettings;
import com.notononoto.teamcity.telegram.config.TelegramSettingsManager;
import jetbrains.buildServer.controllers.*;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Plugin servlet. Should handle writing of settings.
 */
public class TelegramSettingsController extends BaseFormXmlController {

  private static final Logger LOG = Loggers.SERVER;

  private static final String REQUEST_TYPE_PROP_NAME = "submitSettings";

  /** Settings manager */
  private final TelegramSettingsManager settingsManager;
  /** Bot manager */
  private final TelegramBotManager botManager;

  public TelegramSettingsController(@NotNull WebControllerManager webManager,
                                    @NotNull TelegramSettingsManager settingsManager,
                                    @NotNull TelegramBotManager botManager) {
    webManager.registerController("/telegram/notifierSettings.html", this);
    this.settingsManager = settingsManager;
    this.botManager = botManager;
  }

  @Override
  protected ModelAndView doGet(@NotNull HttpServletRequest request,
                               @NotNull HttpServletResponse response) {
    return null;
  }

  @Override
  protected void doPost(@NotNull HttpServletRequest request,
                        @NotNull HttpServletResponse response,
                        @NotNull Element xmlResponse) {

    String action = request.getParameter("action");
    if (action != null) {
      // We use action because we don't want to save another fields.
      // But this reset another form fields (javascript takes it).
      // It's very strange behaviour but this is Teamcity standard
      // behavior... Maybe in future we should change javascript code.
      boolean pause = "disable".equals(action);
      changePauseState(pause);
      return;
    }

    if (PublicKeyUtil.isPublicKeyExpired(request)) {
      PublicKeyUtil.writePublicKeyExpiredError(xmlResponse);
      return;
    }

    TelegramSettingsBean bean = new TelegramSettingsBean(settingsManager.getSettings());
    FormUtil.bindFromRequest(request, bean);
    if (isStoreInSessionRequest(request)) {
      XmlResponseUtil.writeFormModifiedIfNeeded(xmlResponse, bean);
    } else {
      ActionErrors errors = validate(bean);
      if (errors.hasNoErrors()) {
        if (isTestConnectionRequest(request)) {
          String testResult = testSettings(bean.toSettings());
          XmlResponseUtil.writeTestResult(xmlResponse, testResult);
        } else {
          settingsManager.saveConfiguration(bean.toSettings());
          FormUtil.removeAllFromSession(request.getSession(), bean.getClass());
          writeRedirect(xmlResponse, request.getContextPath() +
              "/admin/admin.html?item=" + TelegramSettingsPage.PLUGIN_NAME);
        }
      }
      writeErrors(xmlResponse, errors);
    }
  }

  private void changePauseState(boolean pause) {
    TelegramSettings oldSettings = settingsManager.getSettings();
    TelegramSettings newSettings = new TelegramSettings(oldSettings);
    newSettings.setPaused(pause);
    settingsManager.saveConfiguration(newSettings);
  }

  private String testSettings(@NotNull TelegramSettings settings) {
    try {
      BotInfo info = botManager.requestDescription(settings);
      if (info == null) {
        return "Can't find data about bot with requested token";
      }
      return "bot name: " + info.getName() + "\n" +
             "bot username: " + info.getUsername();
    } catch (Exception ex) {
      LOG.info("Can't send test message to Telegram: ", ex);
      return "Can't send test message to Telegram:\n" +
          ExceptionUtil.getDisplayMessage(ex);
    }
  }

  private ActionErrors validate(@NotNull TelegramSettingsBean settings) {
    ActionErrors errors = new ActionErrors();
    if (StringUtil.isEmptyOrSpaces(settings.getBotToken())) {
      errors.addError("emptyBotToken", "Bot token must not be empty");
    }
    if (settings.isUseProxy()) {
      if (StringUtils.isEmpty(settings.getProxyServer())) {
        errors.addError("emptyProxyServer", "Proxy server must not be empty");
      }
      if (StringUtils.isEmpty(settings.getProxyPort())) {
        errors.addError("emptyProxyPort", "Proxy port must not be empty");
      }
    }
    String port = settings.getProxyPort();
    if (!StringUtils.isEmpty(port) &&
        (!StringUtil.isNumber(port) || Integer.parseInt(port) < 1 || Integer.parseInt(port) > 65535)) {
      errors.addError("badProxyPort", "Proxy port must be integer between 1 and 65535");
    }
    return errors;
  }

  private boolean isStoreInSessionRequest(HttpServletRequest request) {
    return "storeInSession".equals(request.getParameter(REQUEST_TYPE_PROP_NAME));
  }

  private boolean isTestConnectionRequest(HttpServletRequest request) {
    return "testConnection".equals(request.getParameter(REQUEST_TYPE_PROP_NAME));
  }
}
