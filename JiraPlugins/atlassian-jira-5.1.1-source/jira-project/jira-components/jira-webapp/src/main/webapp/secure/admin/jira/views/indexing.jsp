<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.jira.util.JiraDateUtils'" id="dateUtils" />
<html>
<head>
	<title><ww:text name="'admin.indexing.jira.indexing'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="indexing"/>
</head>
<body>

<ww:if test="indexing == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">IndexReIndex.jspa</page:param>
        <page:param name="submitId">reindex_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.indexing.reindex'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.indexing.reindexing'"/></page:param>
        <page:param name="helpURL">searchindex</page:param>
        <%--    <page:param name="helpDescription">with Indexing</page:param>--%>
        <page:param name="instructions">

        <ww:if test="reindexTime > 0">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.indexing.reindexing.was.successful'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                            <ww:param name="'value2'"><strong><ww:property value="@dateUtils/formatTime(reindexTime)" /></strong></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
            <p>
                <ww:text name="'admin.indexing.to.reindex.click.the.button'"/>
                <ww:text name="'admin.indexing.optimize.url.title'">
                    <ww:param name="'value0'"><a href="IndexOptimize!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.indexing.johnson.desc'"/><br/>
                        <ww:text name="'admin.indexing.this.may.take.a.while'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>

            <%-- TODO - until we have an All Tasks Page this is disabled --%>
            <%--<ww:if test="anyLiveTasks == true">--%>
                <%--<ui:component template="taskwarning.jsp"/>--%>
            <%--</ww:if>--%>
        </page:param>
        <ww:if test="/hasSystemAdminPermission == true">
            <ui:component template="paths/radio-indexing-config.jsp" label="text('setup.indexpath.label')" name="'indexPath'"/>
        </ww:if>
        <ww:else>
            <ui:component label="text('admin.indexing.search.index.path')" template="label.jsp">
                <ui:param name="'description'">
                    <ww:text name="'admin.import.index.no.permission.note'">
                        <ww:param name="'value0'"><strong></ww:param>
                        <ww:param name="'value1'"></strong></ww:param>
                    </ww:text>
                </ui:param>
                <ui:param name="'value'">
                    <ww:property value="indexPath"/>
                </ui:param>
            </ui:component>
        </ww:else>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="action">IndexActivate.jspa</page:param>
        <page:param name="width">100%</page:param>
        <ww:if test="/hasSystemAdminPermission == true">
            <page:param name="submitId">activate_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.common.words.activate'"/></page:param>
        </ww:if>
        <page:param name="title">
            <ww:text name="'admin.indexing.is.currently.off'">
                <ww:param name="'value0'"><span class="status-inactive"></ww:param>
                <ww:param name="'value1'"></span></ww:param>
            </ww:text>
        </page:param>
        <page:param name="helpURL">searchindex</page:param>
        <%--	<page:param name="helpDescription">with Indexing</page:param>--%>
        <page:param name="description">

            <ww:if test="/hasSystemAdminPermission == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'admin.indexing.to.activate'"/><br/>
                            <ww:text name="'admin.indexing.this.may.take.a.while'">
                                <ww:param name="'value0'"><strong></ww:param>
                                <ww:param name="'value1'"></strong></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'admin.import.index.disable.contact.sysadmin'">
                                <ww:param name="'value0'"><strong></ww:param>
                                <ww:param name="'value1'"></strong></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>

            </ww:else>
        </page:param>

        <ww:if test="/hasSystemAdminPermission == true">
            <ui:component template="paths/radio-indexing-config.jsp" label="text('setup.indexpath.label')" name="'indexPath'"/>

            <script language="javascript" type="text/javascript">
                window.onload = function()
                {
                    jQuery("#indexPathOption_CUSTOM").toggleField("#indexPath")
                }
            </script>
        </ww:if>
    </page:applyDecorator>
</ww:else>
</body>
</html>
