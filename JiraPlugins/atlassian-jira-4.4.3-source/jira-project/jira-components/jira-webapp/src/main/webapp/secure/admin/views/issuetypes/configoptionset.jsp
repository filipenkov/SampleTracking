<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:if test="/schemeId != null">
<ww:property id="command" value="'Modify'" />
</ww:if>
<ww:else>
<ww:property id="command" value="'Add'" />
</ww:else>

<%-- The page is used for the manageable option object --%>
<ww:property value="/manageableOption" >
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
	<title><ww:text name="'admin.issuesettings.issuetypes.issue.type.scheme'">
	    <ww:param name="'value0'"><ww:property value="@command" /> <ww:property value="title" /></ww:param>
	</ww:text></title>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:editissuetypescheme");
%>

</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.issuesettings.form.title'">
        <ww:param name="'value0'"><ww:property value="@command" /></ww:param>
        <ww:param name="'value1'"><ww:property value="title" /></ww:param>
    </ww:text> &mdash; <ww:property value="/configScheme/name"/></page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <page:param name="instructions">
        <p>
            <ww:text name="'admin.issuesettings.you.can.configure'">
                <ww:param name="'value0'"><ww:property value="title" /></ww:param>
            </ww:text>
        </p>
        <ww:if test="/configScheme/global == true">
            <aui:component template="auimessage.jsp" theme="'aui'" name="'name'" value="'value'" label="'label'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.issuesettings.note.editing.global.scheme'" />
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>

        <ww:if test="/projectId && /schemeId == null">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.issuesettings.this.scheme.will.be.automatically.selected'">
                            <ww:param name="'value0'"><strong><ww:property value="/project/string('name')" /></strong></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>

        <%@ include file="/includes/admin/javascriptrequired.jsp"%>
    </page:param>
    <page:param name="action">ConfigureOptionSchemes.jspa</page:param>
    <page:param name="submitName"><ww:text name="'common.words.save'"/></page:param>
    <page:param name="submitId">submitSave</page:param>
    <page:param name="submitClassName">standard</page:param>
    <ww:if test="/schemeId">
    <page:param name="buttons">
        <input class="toolbar-trigger" type="reset" value="<ww:text name="'admin.common.words.reset'"/>" onclick="window.location='<ww:url value="'ConfigureOptionSchemes!default.jspa'" ><ww:param name="'fieldId'" value="/fieldId" /><ww:param name="'schemeId'" value="/schemeId" /><ww:param name="'returnUrl'" value="/returnUrl" /></ww:url>';return false;"/>
    </page:param>
    </ww:if>
    <page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>

    <ui:component template="multihidden.jsp">
        <ui:param name="'fields'">schemeId,fieldId,projectId</ui:param>
    </ui:component>

    <ui:textfield label="text('admin.issuesettings.scheme.name')" name="'name'" >
        <ui:param name="'description'"><ww:text name="'admin.issuesettings.name.for.scheme'"/></ui:param>
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'cssId'">name</ui:param>
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'description'" size="40">
        <ui:param name="'cssId'">description</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.issuesettings.description.for.scheme'"/>
        </ui:param>
        <ui:param name="'class'">standardField</ui:param>
    </ui:textfield>

    <ui:select label="text('admin.issuesettings.default.issue.type')" name="'defaultOption'"
               list="/allOptions" listKey="'id'" listValue="'name'" >
        <ui:param name="'summary'">description</ui:param>
        <ui:param name="'headerrow'"><ww:text name="'common.words.none'"/></ui:param>
        <ui:param name="'headervalue'"></ui:param>
        <ui:param name="'optionIcon'">imagePath</ui:param>
        <ui:param name="'optionTitle'">description</ui:param>
        <ui:param name="'description'"><ww:text name="'admin.issuesettings.select.default.issue.types'">
            <ww:param name="'value0'"><ww:property value="title" /></ww:param>
        </ww:text></ui:param>
    </ui:select>

<tr><td colspan="2">
    <p>
    <ww:text name="'admin.issuesettings.change.order.by.drag.drop'">
        <ww:param name="'value0'"><strong></ww:param>
        <ww:param name="'value1'"></strong></ww:param>
    </ww:text>
    <ww:if test="/allowEditOptions == true">
        <ww:text name="'admin.issuesettings.similarly.drag.drop.to.remove'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text>
    </ww:if>
    </p>

    
    <div id="optionsContainer" class="ab-drag-wrap">
        
        <div id="left" class="ab-drag-container">
            <h4>
                <ww:text name="'admin.issuesettings.issuetypes.for.current.scheme'">
                    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
                </ww:text>
            </h4>
            <div class="ab-items">
                <ww:if test="/allowEditOptions == true">
                    <a class="ab-all" href="#" id="selectedOptionsRemoveAll">
                        <ww:text name="'admin.issuesettings.remove.all'"/>
                    </a>
                </ww:if>
                <ul id="selectedOptions" class="grabable" style="min-height:<ww:property value="/maxHeight" />px;">
                    <ww:iterator value="/optionsForScheme" status="'status'">
                        <li id="selectedOptions_<ww:property value="./id" />">
                            <span class="icon icon-vgrabber"></span>
                            <span class="icon" style="background-image:url('<ww:url value="./imagePath" />')"></span>
                            <span>
                                <ww:property value="./name" /> <ww:if test="./subTask == true"><span class="smallgrey">(<ww:text name="'admin.issuesettings.sub.task'"/>)</span></ww:if>
                            </span>
                        </li>
                    </ww:iterator>
                </ul>
            </div>
        </div>

    <ww:if test="/allowEditOptions == true">
        <div id="right" class="ab-drag-container">
            <h4>
                <ww:text name="'admin.issuesettings.available.issue.types'">
                    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
                </ww:text>
            </h4>
            <div class="ab-items">
                <a class="ab-all" href="#" id="selectedOptionsAddAll">
                    <ww:text name="'admin.issuesettings.add.all'"/>
                </a>
                <ul id="availableOptions" class="grabable" style="min-height:<ww:property value="/maxHeight" />px;">
                    <ww:iterator value="/availableOptions" status="'status'">
                        <li id="availableOptions_<ww:property value="./id" />">
                            <span class="icon icon-vgrabber"></span>
                            <span class="icon" style="background-image:url('<ww:url value="./imagePath" />')"></span>
                            <span>
                                <ww:property value="./name" /> <ww:if test="./subTask == true"><span class="smallgrey">(<ww:text name="'admin.issuesettings.sub.task'"/>)</span></ww:if>
                            </span>
                        </li>
                    </ww:iterator>
                </ul>
            </div>
        </div>
    </ww:if>

    </div>

<script type="text/javascript">
<ww:if test="/allowEditOptions == true">
    jira.app.editIssueTypeScheme.allowEditOptions = true;
</ww:if>
</script>

</td></tr>
</page:applyDecorator>

<ww:if test="/allowEditOptions == true">
    <fieldset class="hidden parameters">
        <input type="hidden" title="fieldId" value="<ww:property value="fieldId"/>"/>
    </fieldset>
    <script type="text/javascript">
        var openWindow = function()
        {
            var vWinUsers = window.open('<%= request.getContextPath() %>/secure/popups/IconPicker.jspa?fieldType=' + AJS.params.fieldId +
                    '&formName=addConstantForm','IconPicker', 'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
            vWinUsers.opener = self;
            vWinUsers.focus();
        }
    </script>
    <br/>
    <page:applyDecorator name="jiraform">
        <page:param name="formName">addConstantForm</page:param>
        <page:param name="action">ConfigureOptionSchemes!addConstant.jspa</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="submitId">submitAdd</page:param>
        <page:param name="title"><ww:text name="'admin.issuesettings.add.new'">
            <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
        </ww:text></page:param>
        <page:param name="instructions"><ww:text name="'admin.issuesettings.quickly.add'">
            <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
        </ww:text></page:param>
        <page:param name="submitClassName">constant</page:param>
        <page:param name="submitAccessKey">A</page:param>

        <ui:textfield label="text('common.words.name')" name="'constantName'" >
            <ui:param name="'mandatory'">true</ui:param>
            <ui:param name="'class'">standardField</ui:param>
        </ui:textfield>

        <ui:textfield label="text('common.words.description')" name="'constantDescription'">
            <ui:param name="'class'">standardField</ui:param>
        </ui:textfield>

        <%@ include file="/secure/admin/views/issuetypes/typeicon.jsp"%>

    </page:applyDecorator>
</ww:if>

<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>

</body>
</html>
</ww:property>
