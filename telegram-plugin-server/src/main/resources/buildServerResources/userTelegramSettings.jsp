<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="forms" tagdir="/WEB-INF/tags/forms"

%><jsp:useBean id="showTelegramNotConfiguredWarning" type="java.lang.Boolean" scope="request"
/><jsp:useBean id="showTelegramPausedWarning" type="java.lang.Boolean" scope="request"

/><c:choose
><c:when test="${showTelegramPausedWarning}"
><forms:attentionComment additionalClasses="attentionCommentNotifier">Notification rules will not work because Telegram notifier is disabled.</forms:attentionComment
></c:when
><c:when test="${showTelegramNotConfiguredWarning}"
><forms:attentionComment additionalClasses="attentionCommentNotifier">Notification rules will not work until you set up your Telegram chat id.</forms:attentionComment
></c:when
></c:choose>