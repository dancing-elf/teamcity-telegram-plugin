
<%@ include file="/include.jsp"%>

<script type="text/javascript">
    function saveSettings(form) {
        var botName = form.botName.value;
        var botToken = form.botToken.value;
        BS.ajaxRequest($('settingsForm').action, {
            parameters: 'bot_name=' + botName + '&bot_token=' + botToken,
            onComplete: function(transport) {
                if (transport.responseXML) {
                    $('settingsContainer').refresh();
                }
            }
        });
    }
</script>

<bs:refreshable containerId="settingsContainer" pageUrl="${pageUrl}">
    <c:url var="url" value="/saveTelegramSettings.html"/>
    <form action="${url}" id="settingsForm" method="POST" >
        <table class="runnerFormTable">
            <tbody>
            <tr>
                <th><label for="botName">Bot name: <span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                <td><input type="text" name="botName" value="${bot_name}" class="textField" /></td>
            </tr>
            <tr>
                <th><label for="botToken">Bot token: <span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                <td><input type="text" name="botToken" value="${bot_token}" class="textField"/></td>
            </tr>
            </tbody>
        </table>
        <input type="button" value="Save" class="btn btn_primary submitButton" onClick="saveSettings(this.form)"/>
    </form>
    <bs:messages key="msg"/>
</bs:refreshable>