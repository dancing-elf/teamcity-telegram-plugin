package com.notononoto.teamcity.telegram.web;

import com.notononoto.teamcity.telegram.config.TelegramSettings;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestTelegramSettingsBean {

  @Test
  public void testToSettings() {
    TelegramSettingsBean bean = new TelegramSettingsBean(new TelegramSettings());
    bean.setBotToken("123123");
    bean.setPaused(true);
    bean.setUseProxy(true);
    bean.setProxyServer("localhost");
    bean.setProxyPort("345");
    bean.setProxyUsername("user");
    bean.setProxyPassword("123");

    TelegramSettings settings = bean.toSettings();
    assertEquals("123123", settings.getBotToken());
    assertTrue(settings.isPaused());
    assertTrue(settings.isUseProxy());
    assertEquals("localhost", settings.getProxyServer());
    assertEquals(Integer.valueOf(345), settings.getProxyPort());
    assertEquals("user", settings.getProxyUsername());
    assertEquals("123", settings.getProxyPassword());
  }
}
