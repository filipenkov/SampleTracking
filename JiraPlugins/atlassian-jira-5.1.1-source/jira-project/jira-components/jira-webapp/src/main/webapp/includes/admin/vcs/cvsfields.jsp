<script language="JavaScript">
    function processSelection(value)
    {
        document.forms['jiraform'].elements['password'].disabled = value;
    }
</script>

<ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
    <ui:param name="'description'"><ww:text name="'admin.cvsmodules.name.description'"/></ui:param>
    <ui:param name="'mandatory'">true</ui:param>
</ui:textfield>
<ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

<tr>
    <td colspan=2 class="sub-heading"><b><ww:text name="'admin.cvsmodules.cvs.module.details'"/></b></td>
</tr>

<ui:textfield label="text('admin.cvsmodules.cvs.root')" name="'cvsRoot'" size="'60'">
    <ui:param name="'description'">
        <ww:text name="'admin.cvsmodules.cvs.root.description'"/>
    </ui:param>
    <ui:param name="'mandatory'">true</ui:param>
</ui:textfield>

<ui:textfield label="text('admin.cvsmodules.module.name')" name="'moduleName'" size="'60'">
    <ui:param name="'description'">
        <ww:text name="'admin.cvsmodules.module.name.description'"/>
    </ui:param>
    <ui:param name="'mandatory'">true</ui:param>
</ui:textfield>

<tr>
    <td class="fieldLabelArea">
        <ww:text name="'admin.cvsmodules.log.retrieval'"/><span class="icon icon-required"><span>(<ww:text name="'common.forms.requiredfields'"/>)</span></span>
    </td>
    <td class="fieldValueArea">
        <input class="radio" id="fetchLogTrue" type="radio" value="true" name="fetchLog" onclick="processSelection(false);" <ww:if test="/fetchLog == true">checked</ww:if>><label for="fetchLogTrue"><ww:text name="'admin.cvsmodules.automatically.retrieve.the.cvs.log'"/></label>
        &nbsp;
        <input class="radio" id="fetchLogFalse" type="radio" value="false" name="fetchLog" onclick="processSelection(true);" <ww:if test="/fetchLog == false">checked</ww:if>><label for="fetchLogFalse"><ww:text name="'admin.cvsmodules.update.log.manually'"/></label>
    </td>
</tr>


<ui:textfield label="text('admin.cvsmodules.log.file.path')" name="'logFilePath'" size="'60'">
    <ui:param name="'description'">
        <ww:text name="'admin.cvsmodules.log.retrieval.description'">
            <ww:param name="'value0'"><%=System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "cvs-module1.log"%></ww:param>
            <ww:param name="'value1'"><br></ww:param>
        </ww:text>
    </ui:param>
    <ui:param name="'mandatory'">true</ui:param>
</ui:textfield>

<ui:textfield label="text('admin.cvsmodules.cvs.timeout')" name="'timeout'" size="'20'">
    <ui:param name="'description'">
        <ww:text name="'admin.cvsmodules.cvs.timeout.description'"/>    
    </ui:param>
    <ui:param name="'mandatory'">true</ui:param>
</ui:textfield>

<ui:password label="text('common.words.password')" name="'password'">
    <ui:param name="'description'">
    <ui:param name="'disabled'"><ww:if test="/fetchLog == false">true</ww:if><ww:else>false</ww:else></ui:param>
        <ww:text name="'admin.cvsmodules.password.description'"/>
    </ui:param>
</ui:password>

<tr>
    <td colspan=2 class="sub-heading"><b><ww:text name="'admin.cvsmodules.viewcvs.details'"/></b></td>
</tr>

<ui:textfield label="text('admin.cvsmodules.base.url')" name="'repositoryBrowserURL'" size="'60'">
    <ui:param name="'description'"><ww:text name="'admin.cvsmodules.base.url.description'">
        <ww:param name="'value0'"><a href="http://viewcvs.sourceforge.net"></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text></ui:param>
</ui:textfield>

<ui:textfield label="text('admin.cvsmodules.root.parameter')" name="'repositoryBrowserRootParam'" size="'30'">
    <ui:param name="'description'">
        <ww:text name="'admin.cvsmodules.root.parameter.description'"/>
    </ui:param>
</ui:textfield>
