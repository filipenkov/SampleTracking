<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'popups.duedate.duedateselector'"/></title>
</head>
<body>
    <fieldset class="hidden parameters">
        <input type="hidden" title="previousFieldName" value="<ww:property value="previousFieldName"/>"/>
        <input type="hidden" title="nextFieldName" value="<ww:property value="nextFieldName"/>"/>
    </fieldset>

    <script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/includes/js/stringUtil.js"></script>
    <script language="JavaScript" type="text/javascript">
        function populateForm(previousFieldValue, nextFieldValue)
        {
            var openerPrevEl = opener.document.getElementById(AJS.params.previousFieldName);
            openerPrevEl.value = previousFieldValue;
            var openerNextEl = opener.document.getElementById(AJS.params.nextFieldName);
            openerNextEl.value = nextFieldValue;
            window.close();
        }

        function handleOverdue()
        {
            populateForm('', '0');
        }

        function handleEndDateDaysAgo()
        {
            var notNumberString = "<ww:text name="'popups.duedate.validation.notnumber'" />";
            var duedateForm = document.forms['jiraform'];
            var daysoverdue = duedateForm.elements['daysoverdue'].value;
            if (isNaN(daysoverdue)) {
                var p = new RegExp("\\{0\\}", "g");
                alert(notNumberString.replace(p, daysoverdue));
                return false;
            } else if (daysoverdue < 0) {
                alert("<ww:text name="'popups.duedate.validation.daysoverduenegative'"/>");
                return false;
            } else if (trim(daysoverdue) == '') {
                alert("<ww:text name="'popups.duedate.validation.daysmissing'"/>");
                return false;
            }

            var nextField = '-'+daysoverdue+'d';
            populateForm('', nextField);
        }

        function handleDueInNext()
        {
            var notNumberString = new String("<ww:text name="'popups.duedate.validation.notnumber'" />");
            var duedateForm = document.forms['jiraform'];
            var dueinnext = duedateForm.elements['dueinnext'].value;
            if (isNaN(dueinnext)) {
                var p = new RegExp("\\{0\\}", "g");
                alert(notNumberString.replace(p, dueinnext));
                return false;
            } else if (trim(dueinnext) == '') {
                alert("<ww:text name="'popups.duedate.validation.daysmissing'"/>");
                return false;
            } else if (dueinnext < 0) {
                alert("<ww:text name="'popups.duedate.validation.dueinnextnegative'"/>");
                return false;
            }
            var nextField = dueinnext+'d';
            var prevField;
            var selectedIndex =duedateForm.elements['andoverdue'].selectedIndex;
            if (selectedIndex == 0) {
                prevField = '0';
            } else {
                prevField = "";
            }
            populateForm(prevField, nextField);
        }

        function handleGeneric()
        {
            var duedateForm = document.forms['jiraform'];
            var previous = duedateForm.elements['previous'].value;
            var next = duedateForm.elements['next'].value;
            populateForm(previous, next);
        }

        function selectRowRadioBox(rowID)
        {

            var radio = document.forms['jiraform'].elements['selectedType'];
            switch (rowID) {
                case "row1":
                    radio[0].checked = true;
                break;
                case "row2":
                    radio[1].checked = true;
                break;
            case "row3":
                    radio[2].checked = true;
                break;
            case "row4":
                    radio[3].checked = true;
                break;
            }
        }

        function colourRow(rowID)
        {
            if (document.layers)
            {
                //this browser == "NN4"
                //do nothing as NN4's implementation of CSS is no good
                return;
            }
            if (document.all)
            {
                //this browser == "ie"
                resetColour();
                document.all[rowID].className='selectedRow';
            }
            if (!document.all && document.getElementById)
            {
                //this browser == "NN6"
                resetColour();
                document.getElementById(rowID).className='selectedRow';
            }
        }

        function resetColour()
        {
            if (document.layers)
            {
                //this browser == "NN4"
                //do nothing as NN4's implementation of CSS is no good
                return;
            }
            if (document.all)
            {
                //this browser == "ie"
                document.all['row1'].className='unselectedRow';
                document.all['row2'].className='unselectedRow';
                document.all['row3'].className='unselectedRow';
                document.all['row4'].className='unselectedRow';
            }
            if (!document.all && document.getElementById)
            {
                //this browser == "NN6"
                document.getElementById('row1').className='unselectedRow';
                document.getElementById('row2').className='unselectedRow';
                document.getElementById('row3').className='unselectedRow';
                document.getElementById('row4').className='unselectedRow';
            }
        }

        function selectRow(rowID)
        {
            colourRow(rowID);
            selectRowRadioBox(rowID);
        }

        function submitForm()
        {
            var radio = document.forms['jiraform'].elements['selectedType'];
            if (radio[0].checked) {
                return handleOverdue();
            } else if (radio[1].checked) {
                return handleEndDateDaysAgo();
            } else if (radio[2].checked) {
                return handleDueInNext();
            } else if (radio[3].checked) {
                return handleGeneric();
            } else {
                window.close();
            }
        }
    </script>

    <page:applyDecorator name="jiraform">
		<page:param name="action">null</page:param>
        <page:param name="class">ajs-dirty-warning-exempt</page:param>
		<page:param name="onsubmit">return submitForm();</page:param>
		<page:param name="title"><ww:text name="'popups.duedate.duedateselector'"/></page:param>
		<page:param name="description">
	        <ww:text name="'popups.duedate.description'"/>
		</page:param>
		<page:param name="width">100%</page:param>
		<page:param name="columns">3</page:param>
		<page:param name="autoSelectFirst">false</page:param>
		<page:param name="leftButtons">
            <input class="aui-button" type="submit" value="   <ww:text name="'popups.duedate.ok'"/>   ">
        </page:param>
		<page:param name="buttons">
            <input class="aui-button" type="button" value="<ww:text name="'common.words.cancel'"/>" onclick="window.close();">
        </page:param>

            <tr>
                <td>
                    <input type="radio" name="selectedType" value="TYPE_OVERDUE value="TYPE_OVERDUE" onclick="selectRow('row1');" <ww:if test="selectedType == 'TYPE_OVERDUE'"> checked</ww:if> />
                </td>
                <td id="row1" onClick="selectRow('row1')" class="<ww:if test="selectedType == 'TYPE_OVERDUE'">selectedRow</ww:if><ww:else>unselectedRow</ww:else>">
                        <ww:text name="'popups.duedate.nowoverdue'"/>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="radio" name="selectedType" value="END_DATE_DAYS_AGO" onclick="selectRow('row2');" <ww:if test="selectedType == 'END_DATE_DAYS_AGO'"> checked</ww:if>/>
                </td>
                <td id="row2" onClick="selectRow('row2')" class="<ww:if test="selectedType == 'END_DATE_DAYS_AGO'">selectedRow</ww:if><ww:else>unselectedRow</ww:else>">
                        <ww:text name="'popups.duedate.morethandaysoverdue'">
                            <ww:param name="'value0'"><input type="text" name="daysoverdue" size="4" onClick="selectRow('row2')" value="<ww:property value="endDateDaysAgo"/>"></ww:param>
                        </ww:text>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="radio" name="selectedType" value="TYPE_DUEINNEXT" onclick="selectRow('row3');" <ww:if test="selectedType == 'TYPE_DUEINNEXT'"> checked</ww:if> />
                </td>
                <td id="row3" onClick="selectRow('row3')" class="<ww:if test="selectedType == 'TYPE_DUEINNEXT'">selectedRow</ww:if><ww:else>unselectedRow</ww:else>">
                        <ww:text name="'popups.duedate.dueinnext'">
                            <ww:param name="'value0'"><input type="text" name="dueinnext" size="4" onClick="selectRow('row3')" value="<ww:property value="dueInNext"/>" /></ww:param>
                            <ww:param name="'value1'">
                                <select name="andoverdue" onClick="selectRow('row3')" >
                                    <option value="andnot" <ww:if test="andOverdue == 'AND_NOT'">selected</ww:if>><ww:text name="'popups.duedate.andnot'"/></option>
                                    <option value="and" <ww:if test="andOverdue == 'AND'">selected</ww:if>><ww:text name="'popups.duedate.and'"/></option>
                                </select>
                            </ww:param>
                        </ww:text>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="radio" name="selectedType" value="TYPE_GENERIC" onclick="selectRow('row4');" <ww:if test="selectedType == 'TYPE_GENERIC'"> checked</ww:if> />
                </td>
                <td id="row4" onClick="selectRow('row4')" class="<ww:if test="selectedType == 'TYPE_GENERIC'">selectedRow</ww:if><ww:else>unselectedRow</ww:else>">
                        <ww:text name="'popups.duedate.inrange'">
                            <ww:param name="'value0'"><input type="text" name="previous" size="3" onClick="selectRow('row4')" value="<ww:property value="previousFieldValue"/>" /></ww:param>
                            <ww:param name="'value1'"><input type="text" name="next" size="3" onClick="selectRow('row4')" value="<ww:property value="nextFieldValue"/>" /></ww:param>
                        </ww:text>
                        <br><small><ww:text name="'popups.duedate.inrange.description'">
                            <ww:param name="'value0'">1w 2d 5h 30m</ww:param>
                            <ww:param name="'value1'"><strong>w</strong></ww:param>
                            <ww:param name="'value2'"><strong>d</strong></ww:param>
                            <ww:param name="'value3'"><strong>h</strong></ww:param>
                            <ww:param name="'value4'"><strong>m</strong></ww:param>
                            <ww:param name="'value5'"><strong>-1w 3d</strong></ww:param>
                            <ww:param name="'value6'"><strong>-1d</strong></ww:param>
                        </ww:text></small>
                </td>
            </tr>
    </page:applyDecorator>
</body>
</html>
