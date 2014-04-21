package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.plugin.issuenav.pigsty.OpsbarBeanBuilder;
import com.atlassian.jira.plugin.issuenav.viewissue.webpanel.IssueWebPanelsBean;
import com.atlassian.jira.plugin.issuenav.viewissue.webpanel.WebPanelMapperUtil;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.OpsbarBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import org.apache.commons.httpclient.HttpStatus;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.plugin.issuenav.action.ActionUtils.*;

/**
 * A webwork action to produce JSON with field edit html.  This is an action and not a REST resource mainly because our
 * fields API is still so heavily tied to webwork.  All methods on this action should return JSON content.
 *
 * @since 5.0
 */
public class IssueAction extends BaseEditAction
{
    private final UserIssueHistoryManager userIssueHistoryManager;
    private final WebPanelMapperUtil webPanelMapperUtil;

    /// Used by OpsbarBeanBuilder
    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final SimpleLinkManager simpleLinkManager;
    private final EventPublisher eventPublisher;

    private boolean singleFieldEdit = false;
    private IssueService.UpdateValidationResult updateValidationResult;

    // Return values
    private IssueFields fields;

    public IssueAction(final IssueService issueService, final UserIssueHistoryManager userIssueHistoryManager,
                       final FieldHtmlFactory fieldHtmlFactory,
                       final WebPanelMapperUtil webPanelMapperUtil, PluginAccessor pluginAccessor,
                       JiraAuthenticationContext authContext, IssueManager issueManager, SimpleLinkManager simpleLinkManager,
                       EventPublisher eventPublisher)
    {
        super(issueService, fieldHtmlFactory);

        this.userIssueHistoryManager = userIssueHistoryManager;
                this.webPanelMapperUtil = webPanelMapperUtil;
        this.pluginAccessor = pluginAccessor;
        this.authContext = authContext;
        this.issueManager = issueManager;
        this.simpleLinkManager = simpleLinkManager;
        this.eventPublisher = eventPublisher;
    }

    public String doDefault() throws Exception
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), issueId);
        final Issue issue = result.getIssue();
        if (!result.isValid() || result.getIssue() == null)
        {
            this.fields = new IssueFields(getXsrfToken(), ErrorCollection.of(result.getErrorCollection()));
            setErrorReturnCode(getLoggedInUser());
            return JSON;
        }

        populateIssueFields(issue, false, ErrorCollection.of(result.getErrorCollection()));

        return JSON;
    }

    private void populateIssueFields(Issue issue, boolean retainValues, ErrorCollection errorCollection)
    {
        long start = System.nanoTime();

        List<FieldHtmlBean> editFields;
        if (issueService.isEditable(issue, getLoggedInUser()))
        {
            editFields = fieldHtmlFactory.getEditFields(getLoggedInUser(), this, this, issue, retainValues);
        }
        else
        {
            editFields = Collections.emptyList();
        }
        long fieldEnd = System.nanoTime();

        OpsbarBean opsbarBean = new OpsbarBeanBuilder(issue, getApplicationProperties(), simpleLinkManager, authContext, issueManager, pluginAccessor).build();
        IssueBean issueBean = new IssueBean(issue, issue.getProjectObject(), issue.getStatusObject(), opsbarBean);
        long issueEnd = System.nanoTime();

        IssueWebPanelsBean panels = webPanelMapperUtil.create(issue, this);
        long panelsEnd = System.nanoTime();

        eventPublisher.publish(new IssueRenderTime(fieldEnd - start, issueEnd - fieldEnd, panelsEnd - issueEnd));

        fields = new IssueFields(editFields, getXsrfToken(), errorCollection, issueBean, panels);
    }

    protected void doValidation()
    {
        ActionContext.getResponse().setContentType("application/json");

        final IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), issueId);
        final Issue issue = result.getIssue();
        if (!result.isValid() || result.getIssue() == null)
        {
            addErrorCollection(result.getErrorCollection());
            this.fields = new IssueFields(getXsrfToken(), ErrorCollection.of(result.getErrorCollection()));
            setErrorReturnCode(getLoggedInUser());
            return;
        }

        final IssueInputParameters issueInputParameters = new IssueInputParametersImpl(ActionContext.getParameters());
        if (singleFieldEdit)
        {
            issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(singleFieldEdit, true);
        }
        updateValidationResult = issueService.validateUpdate(getLoggedInUser(), issue.getId(), issueInputParameters);
        setFieldValuesHolder(updateValidationResult.getFieldValuesHolder());
        if (!updateValidationResult.isValid())
        {
            addErrorCollection(updateValidationResult.getErrorCollection());
            populateIssueFields(issue, true, ErrorCollection.of(updateValidationResult.getErrorCollection()));
            setErrorReturnCode(getLoggedInUser());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            IssueService.IssueResult result = issueService.update(getLoggedInUser(), updateValidationResult);
            result = issueService.getIssue(getLoggedInUser(), getIssueId());
            Issue issue = result.getIssue();

            if (!result.isValid())
            {
                addErrorCollection(result.getErrorCollection());
                this.fields = new IssueFields(getXsrfToken(), ErrorCollection.of(result.getErrorCollection()));
                setErrorReturnCode(getLoggedInUser());
                return JSON;
            }

            userIssueHistoryManager.addIssueToHistory(getLoggedInUser(), issue);

            populateIssueFields(issue, false, ErrorCollection.of(result.getErrorCollection()));

            return JSON;
        }
        catch (Throwable e)
        {
            addErrorMessage(getText("admin.errors.issues.exception.occured", e));
            log.error("Exception occurred editing issue: " + e, e);
            this.fields = new IssueFields(getXsrfToken(), ErrorCollection.of(getText("admin.errors.issues.exception.occured", e)));
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
            return JSON;
        }
    }

    public String getJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(fields);
    }

    public boolean isSingleFieldEdit()
    {
        return singleFieldEdit;
    }

    public void setSingleFieldEdit(boolean singleFieldEdit)
    {
        this.singleFieldEdit = singleFieldEdit;
    }

    @Analytics("kickass.issueRenderTime")
    public static class IssueRenderTime
    {
        public final double fieldsTimeMs;
        public final double issueTimeMs;
        public final double panelsTimeMs;

        public IssueRenderTime(long fieldsTimeNano, long issueTimeNano, long panelsTimeNano)
        {
            this.fieldsTimeMs = fieldsTimeNano / (1000.0 * 1000.0);
            this.issueTimeMs = issueTimeNano / (1000.0 * 1000.0);
            this.panelsTimeMs = panelsTimeNano / (1000.0 * 1000.0);
        }

        public double getFieldsTimeMs()
        {
            return fieldsTimeMs;
        }

        public double getIssueTimeMs()
        {
            return issueTimeMs;
        }

        public double getPanelsTimeMs()
        {
            return panelsTimeMs;
        }
    }
}
