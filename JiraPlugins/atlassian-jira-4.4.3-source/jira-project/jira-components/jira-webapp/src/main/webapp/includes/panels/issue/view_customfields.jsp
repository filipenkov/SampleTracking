<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<div id="customfieldmodule">
    <ww:if test="/fieldScreenRenderTabs/size > 1">
        <%-- Show tab headings --%>
        <div class="tabwrap tabs2">
            <ul id="customfield-tabs" class="tabs horizontal">
            <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
                <li id="tabCell<ww:property value="@status/count" />" <ww:if test="@status/count == /selectedTab">class="active"</ww:if>>
                    <a rel="<ww:property value="@status/count"/>" href="#"><strong><ww:property value="./name" /></strong></a>
                </li>
            </ww:iterator>
           </ul>
        </div>
    </ww:if>

    <%-- Show the actual tabs with their fields --%>
    <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
    <ul id="tabCellPane<ww:property value="@status/count"/>" class="property-list<ww:if test="@status/count != /selectedTab"> hidden</ww:if><ww:if test="/fieldScreenRenderTabs/size > 1"> pl-tab</ww:if>">
        <%-- Show tab's fields --%>
        <ww:iterator value="./fieldScreenRenderLayoutItems">
            <ww:property value="./orderableField">
                <%-- If changing this, see if http://confluence.atlassian.com/display/JIRACOM/Displaying+Custom+Fields+with+no+value needs updating --%>
                <ww:if test="./value(/issueObject) != null && ./customFieldType/descriptor/viewTemplateExists != false"><%-- don't display custom fields with no values --%>
                    <li id="rowFor<ww:property value="./id" />" class="item">
                        <div class="wrap">
                            <strong title="<ww:property value="name" />" class="name"><ww:property value="name" />:</strong>
                            <div id="<ww:property value="./id" />-val" class="value type-<ww:property value="./customFieldType/descriptor/key" /><ww:if test="./customFieldType/descriptor/key == 'textarea' && ./value(/issueObject)/length() > 255"> twixified</ww:if>">
                                <ww:property value="/customFieldHtml(../fieldLayoutItem,., /issueObject)" escape="false" />
                            </div>
                        </div>
                    </li>
                </ww:if>
            </ww:property>
        </ww:iterator>
    </ul>
    </ww:iterator>
</div>
