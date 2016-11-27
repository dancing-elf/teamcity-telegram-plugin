<%@ include file="/include.jsp" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="telegramSettings"
             scope="request"
             type="com.notononoto.teamcity.telegram.web.TelegramSettingsBean"/>
<bs:linkCSS dynamic="${true}">
    /css/admin/adminMain.css
    /css/admin/serverConfig.css
    /plugins/telegram-plugin/css/telegramSettings.css
</bs:linkCSS>
<bs:linkScript>
    /js/bs/testConnection.js
    /plugins/telegram-plugin/js/telegramSettings.js
</bs:linkScript>
<script type="text/javascript">
    $j(function() {
        Telegram.SettingsForm.setupEventHandlers();
    });
</script>

<c:url var="url" value="/telegram/notifierSettings.html"/>
<div id="settingsContainer">
    <form action="${url}" method="post" onsubmit="return Telegram.SettingsForm.submitSettings()" autocomplete="off">
        <div class="editNotificatorSettingsPage">
            <c:choose>
                <c:when test="${telegramSettings.paused}">
                    <div class="headerNote">
                        <span class="icon icon16 build-status-icon build-status-icon_paused"></span>
                        The notifier is <strong>disabled</strong>. All telegram notifications are suspended&nbsp;&nbsp;
                        <a class="btn btn_mini" href="#" id="enable-btn">Enable</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="enableNote">
                        The notifier is <strong>enabled</strong>&nbsp;&nbsp;
                        <a class="btn btn_mini" href="#" id="disable-btn">Disable</a>
                    </div>
                </c:otherwise>
            </c:choose>

            <bs:messages key="settingsSaved"/>
            <table class="runnerFormTable">
                <tr>
                    <th><label for="botToken">Bot token: <l:star/></label></th>
                    <td>
                        <forms:passwordField name="botToken"
                                             encryptedPassword="${telegramSettings.encryptedBotToken}"/>
                        <span class="error" id="errorBotToken"></span>
                    </td>
                </tr>
                <tr class="groupingTitle">
                    <td colspan="2">Proxy settings</td>
                </tr>
                <tr>
                    <th><label for="useProxy">Use proxy: </label></th>
                    <td><forms:checkbox name="useProxy" checked="${telegramSettings.useProxy}"/>
                </tr>
                <tr>
                    <th><label for="proxyServer">Server: </label></th>
                    <td>
                        <forms:textField name="proxyServer" value="${telegramSettings.proxyServer}"/>
                        <span class="smallNote">Optional. Provide if proxy enabled.</span>
                        <span class="error" id="errorProxyServer"></span>
                    </td>
                </tr>
                <tr>
                    <th><label for="proxyPort">Port: </label></th>
                    <td>
                        <forms:textField name="proxyPort" value="${telegramSettings.proxyPort}"/>
                        <span class="smallNote">Optional. Provide if proxy enabled.</span>
                        <span class="error" id="errorProxyPort"></span>
                    </td>
                </tr>
                <tr>
                    <th><label for="proxyUsername">Username: </label></th>
                    <td>
                        <forms:textField name="proxyUsername" value="${telegramSettings.proxyUsername}"/>
                        <span class="smallNote">Optional. Provide if proxy enabled and requires authentication.</span>
                    </td>
                </tr>
                <tr>
                    <th><label for="proxyPassword">Password: </label></th>
                    <td>
                        <forms:passwordField name="proxyPassword"
                                             encryptedPassword="${telegramSettings.encryptedProxyPassword}"/>
                        <span class="smallNote">Optional. Provide if proxy enabled and requires authentication.</span>
                    </td>
                </tr>
                <tr class="noBorder">
                    <td colspan="2">
                        The templates for Telegram notifications
                        <a target="_blank"
                           href="<bs:helpUrlPrefix/>/Customizing+Notifications"
                           showdiscardchangesmessage="false">
                            can be customized
                        </a>.
                    </td>
                </tr>
            </table>

            <div class="saveButtonsBlock">
                <forms:submit type="submit" label="Save"/>
                <forms:submit id="testConnection" type="button" label="Test connection"/>
                <input type="hidden" id="submitSettings" name="submitSettings" value="store"/>
                <input type="hidden" id="publicKey" name="publicKey"
                       value="<c:out value='${telegramSettings.hexEncodedPublicKey}'/>"/>
                <forms:saving/>
            </div>
        </div>
    </form>
</div>

<bs:dialog dialogId="testConnectionDialog"
           title="Test Connection"
           closeCommand="BS.TestConnectionDialog.close();"
           closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails" class="mono"></div>
</bs:dialog>
<forms:modified/>

<script type="text/javascript">
    (function($) {
        var sendAction = function(enable) {
            $.post("${url}?action=" + (enable ? 'enable' : 'disable'), function() {
                BS.reload(true);
            });
            // looks like Teamcity should support very old browsers so don't use
            // event.preventDefault() but return boolean from click event...
            return false;
        };

        $("#enable-btn").click(function() {
            return sendAction(true);
        });

        $("#disable-btn").click(function() {
            if (!confirm("Telegram notifications will not be sent until enabled. Disable the notifier?"))
                return false;
            return sendAction(false);
        });
    })(jQuery);
</script>
