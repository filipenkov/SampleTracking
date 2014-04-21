<%--
  -- workflowinfobox.jsp
  --
  -- This is used to display information about a draft/active workflow that is being edited and provides
  -- a link to jump from the draft to the active and vice versa.  This requires a getWorkflow() method to be
  -- available by the calling webwork action (see /workflow below).
  -- Information being displayed also depends on a getStep() or getTransition() method being available.  If no
  -- step and no transition can be found, no information about who last edited the page will be displayed.
  --%>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:if test="/workflow/systemWorkflow == false && /workflow/active == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:if test="/workflow/draftWorkflow == false">
                    <ww:text name="'admin.workflow.infobox.viewing.active'"/>
                    <ww:if test="/workflow/hasDraftWorkflow == true">
                        <ww:text name="'admin.workflow.infobox.edit.draft'">
                            <ww:param name="'value0'"><a id="view_draft_workflow" href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="'draft'"/><ww:param name="'workflowName'" value="/workflow/name"/></ww:url>"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.workflow.infobox.create.draft'">
                            <ww:param name="'value0'"><a id="create_draft_workflow" href="<ww:url page="CreateDraftWorkflow.jspa"><ww:param name="'draftWorkflowName'" value="/workflow/name"/></ww:url>"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </ww:else>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.workflow.infobox.editing.draft.view.original'">
                        <ww:param name="'value0'"><a id="view_live_workflow" href="<ww:url page="ViewWorkflowSteps.jspa"><ww:param name="'workflowMode'" value="'live'"/><ww:param name="'workflowName'" value="/workflow/name"/></ww:url>"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                        <ww:param name="'value2'"><a id="publish_draft_workflow" href="<ww:url page="PublishDraftWorkflow!default.jspa"><ww:param name="'workflowMode'" value="'draft'"/><ww:param name="'workflowName'" value="/workflow/name"/></ww:url>"></ww:param>
                    </ww:text>
                    <ww:if test="/step != null && /stepWithoutTransitionsOnDraft(/step/id) == true">
                        <br/>
                        <ww:text name="'admin.workflowtransitions.add.transition.draft.step.without.transition'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:property value="step/name" /></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                        </ww:text>
                    </ww:if>
                </ww:else>
            </p>
        </aui:param>
    </aui:component>
</ww:if>
