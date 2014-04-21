<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'iconpicker.title'"/></title>
</head>

<body>
    <h3><ww:text name="'iconpicker.title'"/></h3>
    <p>
        <ww:text name="'iconpicker.choose.icon'">
            <ww:param name="'value0'"><b><ww:property value="text(fieldName)"/></b></ww:param>
        </ww:text>
    </p>
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th><ww:text name="'iconpicker.label.icon'"/></th>
                <th><ww:text name="'iconpicker.label.filename'"/></th>
                <th>
                    <ww:text name="'iconpicker.label.associatedissueconstant'">
                        <ww:param name="'value0'"><ww:property value="text(fieldName)"/></ww:param>
                    </ww:text>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="./iconUrls(./fieldType)" status="'status'">
            <ww:property value="image(.)">
                <tr onmouseover="rowHover(this)"onclick="select('<ww:property value="."/>');">
                    <td><img src="<%= request.getContextPath() %><ww:property value="."/>"></td>
                    <td><ww:property value="."/></td>
                    <td nowrap>
                        <ww:if test="/associatedImages(.)/empty == false">
                            <ul>
                            <ww:iterator value="associatedImages(.)" status="'iteratorStatus'">
                                <li><ww:property value="./nameTranslation"/></li>
                            </ww:iterator>
                            </ul>
                        </ww:if>
                    </td>
                </tr>
            </ww:property>
        </ww:iterator>
        </tbody>
    </table>

    <ww:text name="'iconpicker.enter.image.path'">
        <ww:param name="'value0'"><span class="warning"></ww:param>
        <ww:param name="'value1'"></span></ww:param>
    </ww:text>

    <form name="submitter">
        <table width="95%" cellpadding="3" cellspacing="1" align="center">
            <tr>
                <ui:textfield label="'IconURL'" name="'iconurl'" size="'60'" theme="'single'">
                    <ui:param name="'description'"><ww:text name="'admin.common.phrases.relative.to.jira'"/></ui:param>
                    <ui:param name="'onkeypress'">return checkkeypressed(event);</ui:param>
                </ui:textfield>
            </tr>
            <tr>
                <td colspan="2" class="fullyCentered jiraformfooter" >
                    <input type="button" name="Update" value="<ww:text name="'common.words.update'"/>" onclick="update();"/>
                    <input type="button" value="<ww:text name="'common.words.cancel'"/>" name="cancel" onclick="closeit();"/>
                </td>
            </tr>
        </table>
    </form>

    <fieldset class="hidden parameters">
        <input type="hidden" title="formName" value="<ww:property value="formName"/>"/>
    </fieldset>
    <script type="text/javascript">
        function rowHover(row)
        {
            row.oldClassName = row.className;
            row.className = 'rowHover';
            row.onmouseout = function()
            {
                this.className = this.oldClassName;
            }
        }

        function select(value)
    	{
            var openerForm = opener.document.forms[AJS.params.formName];
    		var openerEl = openerForm.elements['iconurl'];
            openerEl.value = value;
            window.close();
        }

        function update()
        {
            var openerForm = opener.document.forms[AJS.params.formName];
            var openerEl = openerForm.elements['iconurl'];
            openerEl.value = document.forms['submitter'].elements['iconurl'].value;
            window.close();
            return false;
        }

        function closeit()
        {
            window.close();
        }

        function checkkeypressed(event)
        {
            if (event.keyCode == '13')
                return update();
        }
    </script>


</body>
</html>
