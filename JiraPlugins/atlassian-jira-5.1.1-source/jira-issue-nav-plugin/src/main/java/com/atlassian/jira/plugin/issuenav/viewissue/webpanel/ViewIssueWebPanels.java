package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a REST resource that renders WebPanels on the view issue page and returns them in a JSON structure
 * corresponding to:
 * <pre>
 *     {
 *         leftPanels: {
 *             details-module: "detailsModuleHtml",
 *             ...
 *         },
 *         rightPanels: {...},
 *         infoPanels: {...}
 *     }
 * </pre>
 * <p/>
 * This code exists in a webwork action since some fields require a webwork context in order to be properly rendered. *
 *
 * @since v5.1
 */
public class ViewIssueWebPanels extends JiraWebActionSupport implements OperationContext
{
    private static final String JSON = "json";
    
    private Long id;
    private final IssueService issueService;
    private final WebPanelMapperUtil webPanelMapperUtil;
    private ErrorCollection errors;
    private IssueWebPanelsBean issueWebPanelsBean;

    public ViewIssueWebPanels(final IssueService issueService,
            final WebPanelMapperUtil webPanelMapperUtil)
    {
        this.issueService = issueService;
        this.webPanelMapperUtil = webPanelMapperUtil;
    }

    @Override
    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), id);
        if (!result.isValid())
        {
            this.errors = ErrorCollection.of(result.getErrorCollection());
            final Reason worstReason = Reason.getWorstReason(result.getErrorCollection().getReasons());
            ActionContext.getResponse().setStatus(worstReason == null ? 400 : worstReason.getHttpStatusCode());
            return JSON;
        }

        this.issueWebPanelsBean = webPanelMapperUtil.create(result.getIssue(), this);

        return JSON;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getJson()
    {
        final Object objectToMarshall;
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        if (this.errors != null)
        {
            objectToMarshall = this.errors;
        }
        else
        {
            objectToMarshall = this.issueWebPanelsBean;
        }

        return marshaller.marshal(objectToMarshall);
    }

    @Override
    public Map getFieldValuesHolder()
    {
        return new HashMap();
    }

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }
}
