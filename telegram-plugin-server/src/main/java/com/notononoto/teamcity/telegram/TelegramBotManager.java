package com.notononoto.teamcity.telegram;

import com.intellij.openapi.diagnostic.Logger;
import com.notononoto.teamcity.telegram.config.TelegramSettings;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetMeResponse;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;


/**
 * Manage telegram bot state and concurrent access
 * It's don't good to manage state... At first sight we
 * should move sending code to sendMessage method of TelegramNotificator
 * class. But it's impossible because bot should be active always
 * because new users must send at least one message to bot. Otherwise
 * bot will not be able to send a messages to this users because Telegram
 * prohibits it.
 * We use synchronized methods in this class. Of course, read-write lock
 * can get best performance, but it's not important here.
 */
public class TelegramBotManager {

  private static final Logger LOG = Loggers.SERVER;

  /**
   * Plugin settings
   */
  private TelegramSettings settings;
  /**
   * Request executor
   */
  private volatile TelegramBot bot;

  /**
   * Reload bot if settings changed
   *
   * @param newSettings updated user settings
   */
  public synchronized void reloadIfNeeded(@NotNull TelegramSettings newSettings) {
    if (Objects.equals(newSettings, settings)) {
      LOG.debug("Telegram bot token settings has not changed");
      return;
    }
    LOG.debug("New telegram bot token is received: " +
        StringUtil.truncateStringValueWithDotsAtEnd(newSettings.getBotToken(), 6));
    this.settings = newSettings;
    cleanupBot();
    if (settings.getBotToken() != null && !settings.isPaused()) {
      TelegramBot newBot = createBot(settings);
      addUpdatesListener(newBot);
      bot = newBot;
    }
  }

  /**
   * Send message to client
   *
   * @param chatId  client identifier
   * @param message text to send
   */
  public synchronized void sendMessage(long chatId, @NotNull String message) {
    if (bot != null) {
      bot.execute(buildMsg(chatId, message));
    }
  }

  @Nullable
  public BotInfo requestDescription(@NotNull TelegramSettings settings) {
    TelegramBot bot = createBot(settings);
    GetMeResponse response = bot.execute(new GetMe());
    User user = response.user();
    if (user == null) {
      return null;
    }
    return new BotInfo(user.firstName(), user.username());
  }

  public synchronized void destroy() {
    cleanupBot();
  }

  private void cleanupBot() {
    if (bot != null) {
      bot.removeGetUpdatesListener();
      // make cleanup visible to all methods
      bot = null;
    }
  }

  private TelegramBot createBot(@NotNull TelegramSettings settings) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (settings.isUseProxy()) {
      builder.proxy(new Proxy(Proxy.Type.HTTP,
          new InetSocketAddress(settings.getProxyServer(), settings.getProxyPort())));
      if (!StringUtils.isEmpty(settings.getProxyUsername()) &&
          !StringUtils.isEmpty(settings.getProxyPassword())) {
        builder.proxyAuthenticator((route, response) -> {
          String credential =
              Credentials.basic(settings.getProxyUsername(), settings.getProxyPassword());
          return response.request().newBuilder()
              .header("Proxy-Authorization", credential)
              .build();
        });
      }
    }
    return new TelegramBot(settings.getBotToken());
  }

  private void addUpdatesListener(TelegramBot bot) {
    bot.setUpdatesListener(updates -> {
      for (Update update : updates) {
        Message message = update.message();
        if (message == null) {
          continue;
        }
        Long chatId = message.chat().id();
        SendMessage msg = buildMsg(chatId,
            "Hello! Your chat id is '" + chatId + "'.\n" +
                "If you want to receive notifications about Teamcity events " +
                "please add this chat id in your Teamcity settings");
        bot.execute(msg);
      }
      return UpdatesListener.CONFIRMED_UPDATES_ALL;
    });
  }

  private SendMessage buildMsg(Long chatId, String message) {
    SendMessage sendMessage = new SendMessage(chatId, message);
    ParseMode parseMode = settings.getParseMode().getParseMode();
    if (parseMode != null) {
      sendMessage.parseMode(parseMode);
    }
    return sendMessage;
  }
}
