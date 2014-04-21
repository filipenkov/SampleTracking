<%@ page import="com.atlassian.jira.ManagerFactory,com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<h2><ww:text name="'login.welcome.to'"/> <%= TextUtils.htmlEncode(ManagerFactory.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_TITLE))%></h2>
<%@ include file="/includes/loginform.jsp" %>
