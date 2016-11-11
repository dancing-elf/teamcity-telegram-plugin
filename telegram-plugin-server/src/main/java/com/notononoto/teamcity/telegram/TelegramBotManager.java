package com.notononoto.teamcity.telegram;

import com.intellij.openapi.diagnostic.Logger;
import com.notononoto.teamcity.telegram.config.TelegramSettings;
import com.pengrad.telegrambot.GetUpdatesListener;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Manage telegram bot state and concurrent access
 * It's don't good to manage state... At first sight we
 * should move sending code to sendMessage method of TelegramNotificator
 * class. But it's impossible because bot should be active always
 * because new users must send at least one message to bot. Otherwise
 * bot will not be able to send a messages to this users because Telegram
 * prohibits it.
 */
public class TelegramBotManager {

  private static final Logger LOG = Loggers.SERVER;

  /** Plugin settings */
  private TelegramSettings settings;
  /** Request executor */
  private TelegramBot bot;
  /**
   * {@link #reloadIfNeeded} and {@link #sendMessage} can have a race.
   * {@link #sendMessage} can be invoked from many threads, so using
   * read-write lock.
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Reload bot if settings changed
   * @param newSettings updated user settings
   */
  public void reloadIfNeeded(@NotNull TelegramSettings newSettings) {
    if (!isBotChanged(newSettings, settings)) {
      LOG.debug("Telegram bot token has not changed");
      return;
    }
    LOG.debug("New telegram bot token is received: " +
        StringUtil.truncateStringValueWithDotsAtEnd(newSettings.getBotToken(), 6));
    lock.writeLock().lock();
    try {
      this.settings = newSettings;
      cleanupBot();
      if (settings.getBotToken() != null) {
        bot = createBot(settings.getBotToken());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Send message to client
   * @param chatId client identifier
   * @param message text to send
   */
  public void sendMessage(long chatId, @NotNull String message) {
    lock.readLock().lock();
    try {
      if (bot != null) {
        bot.execute(new SendMessage(chatId, message));
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  public void destroy() {
    lock.writeLock().lock();
    try {
      cleanupBot();
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void cleanupBot() {
    if (bot != null) {
      bot.removeGetUpdatesListener();
      // make cleanup visible to all methods
      bot = null;
    }
  }

  private boolean isBotChanged(@NotNull TelegramSettings newSettings,
                               @Nullable TelegramSettings oldSettings) {
    return oldSettings == null ||
        !Objects.equals(newSettings.getBotToken(), oldSettings.getBotToken());
  }

  private TelegramBot createBot(@NotNull String botToken) {
    TelegramBot bot = TelegramBotAdapter.build(botToken);
    bot.setGetUpdatetsListener(updates -> {
      for (Update update: updates) {
        Message message = update.message();
        Long chatId = message.chat().id();
        SendMessage msg = new SendMessage(chatId,
            "Hello! You chat id is '" + chatId + "'.\n" +
                "If you want to receive notifications about Teamcity events " +
                "please add this chat id in your Teamcity settings");
        bot.execute(msg);
      }
      return GetUpdatesListener.CONFIRMED_UPDATES_ALL;
    });
    return bot;
  }
}
