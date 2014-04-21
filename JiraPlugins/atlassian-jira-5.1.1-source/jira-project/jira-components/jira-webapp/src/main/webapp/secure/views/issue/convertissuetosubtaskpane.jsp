<%@ taglib uri="webwork" prefix="ww" %>
<%
    final String IMG_DONE = request.getContextPath() + "/images/icons/bullet_done.gif";
    final String IMG_NOT_DONE = request.getContextPath() + "/images/icons/bullet_notdone.gif";
    final String IMG_IN_PROGRESS = request.getContextPath() + "/images/icons/bullet_inprogress.gif";
%>

<ww:if test="errorMessages/empty == true">
    <ol class="steps">
        <ww:if test="currentStep == 1">
            <li class="current"><ww:text name="textKey('step1.title')"/></li>
        </ww:if>
        <ww:elseIf test="currentStep > 1">
            <li class="done">
                <a href="<ww:property value="/actionPrefix"/>.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step1.title')"/></a>

                <ww:if test="/issue/subTask == false">
                    <br/>
                    <ww:text name="textKey('step1.parentissue')">
                        <ww:param name="'value0'"><strong><ww:property value="/parentIssueKey"/></strong></ww:param>
                    </ww:text>
                </ww:if>
                <br/>
                <ww:text name="textKey('step1.issuetype')">
                    <ww:param name="'value0'"><strong><ww:property value="/targetIssue/issueTypeObject/nameTranslation(.)"/></strong></ww:param>
                </ww:text>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step1.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 2">
            <li class="current"><ww:text name="textKey('step2.title')"/></li>
        </ww:if>
        <ww:elseIf test="/currentStep > 2">
            <li class="done">
                <ww:if test="/statusChangeRequired == true">
                    <a href="<ww:property value="/actionPrefix"/>SetIssueType.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step2.title')"/></a>
                </ww:if>
                <ww:else>
                    <ww:text name="textKey('step2.title')"/>
                </ww:else>
                <br/>
                <ww:text name="textKey('step2.status')">
                    <ww:param name="'value0'"><strong><ww:property value="/targetIssue/statusObject/nameTranslation(.)"/></strong></ww:param>
                </ww:text>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step2.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 3">
            <li class="current"><ww:text name="textKey('step3.title')"/></li>
        </ww:if>
        <ww:elseIf test="/currentStep > 3">
            <li class="done">
                <a href="<ww:property value="/actionPrefix"/>SetStatus.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step3.title')"/></a>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step3.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 4">
            <li class="current"><ww:text name="textKey('step4.title')"/></li>
        </ww:if>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step4.title')"/></li>
        </ww:else>
        
    </ol>
</ww:if>
<ww:else>
    <div style="padding: 4px">
        <ww:if test="/issue">
            <ww:text name="textKey('error.exit.issue')">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/browse/<ww:property value="/issue/key" />"></ww:param>
                <ww:param name="'value1'"><ww:property value="/issue/key" /></ww:param>
                <ww:param name="'value2'"></a></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="textKey('error.exit.dashboard')">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </ww:else>
    </div>
</ww:else>