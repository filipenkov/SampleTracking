<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.cvsmodules.code.repositories'"/></title>
    <meta name="admin.active.section" content="admin_plugins_menu/source_control"/>
    <meta name="admin.active.tab" content="cvs_modules"/>
</head>
<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.menu.globalsettings.cvs.modules'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">cvs_integration</page:param>
    <p>
    <ww:text name="'admin.cvsmodules.description'"/>
    </p>
    <ww:if test="/systemAdministrator == true">
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.cvsmodules.add.new.cvs.module'">
                    <ww:param name="'value0'"><a id="add_cvs_module" href="AddRepository!default.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.errors.must.be.sys.admin.to.create'">
                        <ww:param name="'value0'"> </ww:param>
                        <ww:param name="'value1'"> </ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:else>
</page:applyDecorator>

<ww:if test="repositories/size == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.cvsmodules.no.modules.configured'"/></p>
        </aui:param>
    </aui:component>
</ww:if>
<ww:else>
    <table class="aui aui-table-rowhover" id="cvs_modules_table">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.words.details'"/>
                </th>
                <th>
                    <ww:text name="'common.concepts.projects'"/>
                </th>
                <ww:if test="/systemAdministrator == true">
                <th width="10%">
                    <ww:text name="'common.words.operations'"/>
                </th>
                </ww:if>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="repositories" status="'status'">
            <tr>
                <td>
                    <b><ww:property value="name"/></b>
                    <ww:if test="description">
                        <div class="description"><ww:property value="description"/></div>
                    </ww:if>
                </td>
                <td>
                    <b><ww:text name="'admin.cvsmodules.log.file.path'"/></b>:
                    <ww:if test="./cvsLogFilePath">
                         <ww:property value="./cvsLogFilePath" /><br>
                    </ww:if>
                    <ww:else>
                        <span class="status-innactive"><ww:text name="'admin.cvsmodules.not.set'"/></span><br>
                    </ww:else>
                    <b><ww:text name="'admin.cvsmodules.cvs.root'"/></b>: <ww:property value="./cvsRoot" /><br>
                    <b><ww:text name="'admin.cvsmodules.module.name'"/></b>: <ww:property value="./moduleName" /><br>
                    <b><ww:text name="'admin.cvsmodules.log.retrieval'"/></b>: <ww:if test="./fetchLog == true"><ww:text name="'admin.cvsmodules.periodic.retrieval'"/></ww:if><ww:else><ww:text name="'admin.cvsmodules.log.needs.manual.update'"/></ww:else><br>
                    <b><ww:text name="'admin.cvsmodules.cvs.timeout'"/></b>: <ww:property value="./cvsTimeoutStringInSeconds" /> seconds<br>
                    <ww:property value="/viewCVSBaseUrl(.)">
                        <b><ww:text name="'admin.cvsmodules.viewcvs.url'"/></b>: <ww:property />
                        <ww:if test=". != ''">
                            <br><b><ww:text name="'admin.cvsmodules.viewcvs.root.param'"/></b>: <ww:property value="/viewCVSRootParameter(..)" />
                        </ww:if>
                    </ww:property>
                </td>
                <td>
                    <ww:if test="/projects(.) != null && /projects(.)/empty == false">
                        <ul>
                        <ww:iterator value="/projects(.)">
                            <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </td>
                <ww:if test="/systemAdministrator == true">
                <td>
                    <ul class="operations-list">
                        <li><a id="edit_<ww:property value="id"/>" href="UpdateRepository!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a id="test_<ww:property value="id"/>" href="<ww:url value="'RepositoryTest.jspa'"><ww:param name="'id'" value="id"/></ww:url>"><ww:text name="'admin.common.words.test'"/></a></li>
                        <ww:if test="/deletable(.) == true">
                            <li><a id="delete_<ww:property value="id"/>" href="DeleteRepository!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
                        </ww:if>
                    </ul>
                </td>
                </ww:if>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:else>

</body>
</html>
