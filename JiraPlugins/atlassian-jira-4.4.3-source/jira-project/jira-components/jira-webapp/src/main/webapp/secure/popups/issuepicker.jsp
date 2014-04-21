<%@ taglib uri="webwork" prefix="ww" %> <%@ taglib uri="webwork" prefix="ui" %>
<html>
<head>
    <meta content="popup" name="decorator"/>
</head>
<body class="type-a">
<div class="content intform" id="issue-picker-popup-open">
    <script type="text/javascript">

    function populateForm(val)
    {
        callbackToMainWindow([{
            value: val,
            label: AJS.$("a[rel=" + val + "]").attr("data-label")
        }]);

        return false;
    }

    // populate the parent form with multiple values
    function populateFormMultiple()
    {
        var val, value = [], counter;
        if (document.issues.issuekey.length > 0)
        {
            // get all the selected checkboxes
            for (counter=0; counter<document.issues.issuekey.length; counter++)
            {
                if (document.issues.issuekey[counter].checked)
                {
                    val = document.issues.issuekey[counter].value;
                    value.push({
                        value: val,
                        label: AJS.$("a[rel=" + val + "]").attr("data-label")
                    });
                }
            }
        }
        else if (document.issues.issuekey.checked)
        {
            val = document.issues.issuekey.value;

            value.push({
                value: val,
                label: AJS.$("a[rel=" + val + "]").attr("data-label")
            });

        }

        callbackToMainWindow(value);
        return false;
    }

    function callbackToMainWindow(value)
    {
        var path, callback, i;

        path = ["jira", "issuepicker", "callback"];

        callback = window.opener;
        for (i = 0; callback && i < path.length; i++) {
            callback = callback[path[i]];
        }

        value = JSON.stringify(value);

        if (callback){
            callback(value);
        }

        window.close();
    }

    // Provide hover and click effect to entire table rows.
    // Usage:
    // <table class="grid">
    //   <tr href="somelink.jsp" onmouseover="rowHover(this)">
    //   ...
    function rowHover(row) {

        AJS.$(row).addClass('rowHover');
        AJS.$(row).mouseout(function() {
            AJS.$(row).removeClass('rowHover');
        });
    }
    </script>

    <table width="100%">
        <tr >
            <td width=1% nowrap>
                <strong><ww:text name="'issuepicker.name'"/> </strong>
            </td>
            <td valign=middle align="right" nowrap>

                <ww:if test="mode == 'recent'">
                    <span class="picker-label-recent-active"><ww:text name="'issuepicker.recent.issues'"/></span>
                </ww:if>
                <ww:else>
                    <a href="<ww:url page="IssuePicker.jspa">
                        <ww:param name="'mode'" value="'recent'" />
                        <ww:param name="'currentIssue'" value="/currentIssue"/>
                        <ww:param name="'singleSelectOnly'" value="/singleSelectOnly"/>
                        <ww:param name="'showSubTasks'" value="/showSubTasks"/>
                        <ww:param name="'showSubTasksParent'" value="/showSubTasksParent"/>
                        <ww:if test="/selectedProjectId">
                            <ww:param name="'selectedProjectId'" value="/selectedProjectId"/>
                        </ww:if>
                        </ww:url>" title="<ww:text name="'issuepicker.recent.issues.desc'"/>"
                            class="picker-recent-link"><ww:text name="'issuepicker.recent.issues'"/></a>
                </ww:else>
                | <span title="<ww:text name="'issuepicker.search.filter.desc'"/>"><ww:text name="'issuepicker.search.filter'"/></span>
            </td>
            <td width=1% nowrap>
                <form name="selectFilter">
                        <select name="searchRequestId" onChange="submit()">
                            <option value="-1"><ww:text name="'issuepicker.select.value'"/></option>

                            <ww:iterator value="/availableFilters">
                                <option value="<ww:property value="./id"/>" <ww:if test="./id == /searchRequestId && mode=='search'">SELECTED</ww:if>>
                                    <ww:property value="./name"/></option>
                            </ww:iterator>
                        </select>

                        <input type="hidden" name="mode" value="search">
                        <input type="hidden" name="currentIssue" value="<ww:property value="/currentIssue"/>">
                        <input type="hidden" name="singleSelectOnly" value="<ww:property value="/singleSelectOnly"/>">
                        <input type="hidden" name="showSubTasks" value="<ww:property value="/showSubTasks"/>">
                        <input type="hidden" name="showSubTasksParent" value="<ww:property value="/showSubTasksParent"/>">
                        <ww:if test="/selectedProjectId">
                            <input type="hidden" name="selectedProjectId" value="<ww:property value="/selectedProjectId"/>">
                        </ww:if>
                </form>
            </td>
        </tr>
    </table>

    <form name="issues">
        <hr/>
        <ww:if test="mode == 'recent'">
            <div id="recent-issues">
                <ww:property value="/userHistoryIssues" >
                    <ww:if test=". && size > 0">
                        <strong><ww:text name="'issuepicker.issues.viewed'"/></strong>
                        <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                    </ww:if>
                    <ww:else>
                        <strong><ww:text name="'issuepicker.noissues.viewed'"/></strong>
                    </ww:else>
                </ww:property>
            </div>
        <hr/>
        <%-- See if the user has a current search.  If they do - show them the first 50 issues in that search --%>
            <div id="current-issues">
                <ww:property value="/browsableIssues" >
                    <ww:if test=". && size > 0">
                    <strong><ww:text name="'issuepicker.current.search.issues'"/></strong>
                    <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                    </ww:if>
                    <ww:else>
                    <strong><ww:text name="'issuepicker.current.search.noissues'"/></strong>
                    </ww:else>
                </ww:property>
            </div>
        </ww:if>

        <ww:elseIf test="mode == 'search'">
            <%-- Return the first 50 results from the selected filter. --%>
            <div id="filter-issues">
            <ww:property value="/searchRequestIssues" >
                <ww:if test=". && size > 0">
                    <strong><ww:text name="'issuepicker.search.issues'"/>&nbsp;<em><ww:property value="/searchRequestName" /></em>:</strong>
                    <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                </ww:if>
                <ww:else>
                    <strong><ww:text name="'issuepicker.search.noissues'"/>&nbsp;<em><ww:property value="/searchRequestName" /></em>.</strong>
                </ww:else>
            </ww:property>
            </div>
        </ww:elseIf>
    </form>
</div>
</body>
</html>
