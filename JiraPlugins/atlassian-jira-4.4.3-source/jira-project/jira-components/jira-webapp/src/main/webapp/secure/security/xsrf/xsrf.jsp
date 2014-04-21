<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <ww:if test="/sessionExpired == true">
        <title><ww:text name="'xsrf.error.session.expired.title'"/></title>
    </ww:if>
    <ww:else>
        <title><ww:text name="'xsrf.error.title'"/></title>
    </ww:else>
    <meta content="message" name="decorator" />
</head>
<body>
<%--here so func tests work and attach file dialog. DO NOT DELETE! --%>
<!-- SecurityTokenMissing -->
<div class="content-body jira-error">
<ww:if test="/sessionExpired == true">
    <h1><ww:text name="'xsrf.error.session.expired.title'"/></h1>
</ww:if>
<ww:else>
    <h1><ww:text name="'xsrf.error.title'"/></h1>
</ww:else>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
        <ww:if test="/noRequestParameters == true">
            <p>
                <ww:text name="'xsrf.retry.noparameters'"/>
            </p>
            <p>
                <ww:text name="'xsrf.retry.noparameters.helpmsg'">
                    <ww:param name="'value0'"><a target="newwindow" href="<ww:property value="@helpUtil/helpPath('xsrf_emptypostbug')/url"/>"></ww:param>
                    <ww:param name="'value1'"><ww:property value="@helpUtil/helpPath('xsrf_emptypostbug')/title"/></ww:param>
                    <ww:param name="'value2'"></a></ww:param>
                </ww:text>
            </p>
        </ww:if>
        <ww:if test="/sessionExpired == true">
            <p>
                <ww:text name="'xsrf.info.session.expired.line.1'"/>
            </p>
            <p>
                <ww:text name="'xsrf.info.session.expired.line.2'">
                    <ww:param name="'value0'"><ww:property value="@maxInactiveIntervalMinutes"/></ww:param>
                </ww:text>
            </p>
        </ww:if>
        <ww:else>
            <p>
                <ww:text name="'xsrf.info.line.1'"/>
            </p>

            <p>
                <ww:text name="'xsrf.info.line.2'"/>
            </p>
        </ww:else>
        <p>
            <ww:text name="'xsrf.error.offending.action'"/>:
            <ww:property value="/action/actionName"/><ww:if test="/action/commandName != null">!<ww:property value="/action/commandName"/></ww:if>.jspa
        </p>
        <ww:if test="@loggedin == true">
            <ww:if test="/noRequestParameters == false">
                <p>
                    <ww:text name="'xsrf.retry.note1'"/>
                </p>
                <p>
                    <em><ww:text name="'xsrf.retry.note2'"/></em>
                </p>
                <form id="atl_token_retry_form" action="<ww:property value="/requestURL"/>" method="<ww:property value="/requestMethod"/>">
                    <ww:iterator value="/requestParameters">
                        <ww:iterator value="./value">
                            <%--dont use the original atl_token--%>
                            <ww:if test="../key != 'atl_token'">
                                <ww:component name="../key" value="." template="hidden.jsp"/>
                            </ww:if>
                        </ww:iterator>
                    </ww:iterator>
                    <ww:component name="'atl_token'" value="@xsrfToken" template="hidden.jsp"/>
                    <input type="submit" name="atl_token_retry_button" value="<ww:text name="'xsrf.retry.button'"/>"/>
                </form>
                <%-- evil javascript that replaces the tokens on the page --%>
                <script type="text/javascript">jira.xsrf.updateTokenOnPage("<ww:property value="@xsrfToken"/>")</script>
            </ww:if>
        </ww:if>
        <ww:else>
            <p>
                <ww:text name="'xsrf.login.text'">
                    <ww:param name="'value0'"><a href="<ww:property value="@contextpath"/>/login.jsp"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
            <h4><ww:text name="'xsrf.notlogin.captured.params'"/></h4>
            <ul class="item-details">
                <ww:iterator value="/requestParameters">
                    <ww:iterator value="./value">
                        <%--dont use the original atl_token--%>
                        <ww:if test="../key != 'atl_token'">
                            <li>
                                <dl>
                                    <dt><ww:property value="../key"/></dt>
                                    <dd><ww:property value="."/></dd>
                                </dl>
                            </li>
                        </ww:if>
                    </ww:iterator>
                </ww:iterator>
            </ul>
        </ww:else>
   </aui:param>
</aui:component>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'titleText'"><ww:text name="'xsrf.info.admin.1'"/></aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:text name="'xsrf.info.admin.2'">
                <ww:param name="'value0'"><a href="<ww:property value="@helpUtil/helpPath('xsrf')/url"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </aui:param>
</aui:component>
</div>
</body>
</html>
