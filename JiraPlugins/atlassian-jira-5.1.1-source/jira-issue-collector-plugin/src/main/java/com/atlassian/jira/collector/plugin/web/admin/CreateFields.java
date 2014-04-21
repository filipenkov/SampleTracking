package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.jira.collector.plugin.components.CollectorFieldValidator;
import com.atlassian.jira.collector.plugin.rest.model.Field;
import com.atlassian.jira.collector.plugin.rest.model.Fields;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugins.rest.common.json.DefaultJaxbJsonMarshaller;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateFields extends AbstractProjectAdminAction implements OperationContext
{
    private static final Map<String, Object> DISPLAY_PARAMS =
            MapBuilder.<String, Object>newBuilder("noHeader", "true", "theme", "aui",
                    "isFirstField", true, "isLastField", true).toMutableMap();

	private static final Logger log = Logger.getLogger(CreateFields.class);

    public static final int TRIMED_NAME_LENGTH = 19;

    private Fields fields;
    private ErrorCollection errors;
    private String issueType;
    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private final ConstantsManager constantsManager;
    private final IssueFactory issueFactory;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final ApplicationProperties applicationProperties;
    private final CollectorFieldValidator collectorFieldValidator;

    public CreateFields(final ConstantsManager constantsManager, final IssueFactory issueFactory,
                        final FieldScreenRendererFactory fieldScreenRendererFactory,
                        final ApplicationProperties applicationProperties, final CollectorFieldValidator collectorFieldValidator)
    {
        this.constantsManager = constantsManager;
        this.issueFactory = issueFactory;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.applicationProperties = applicationProperties;
        this.collectorFieldValidator = collectorFieldValidator;
    }

    @Override
    public String doDefault() throws Exception
    {
        initRequest();

		ActionContext.getResponse().setContentType("application/json");
        this.fields = new Fields();

        final Project project = getProject();
        final IssueType issueTypeObject = constantsManager.getIssueTypeObject(issueType);
        if (project == null || issueTypeObject == null)
        {
            this.errors = ErrorCollection.of("No valid project or issue type provided");
			log.error(String.format("No valid project or issue type provided; projectKey='%s', issueType='%s'", getProjectKey(), issueType));
            return ERROR;
        }

        final MutableIssue issue = issueFactory.getIssue();
        issue.setProjectId(project.getId());
        issue.setIssueTypeId(issueType);

        final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getLoggedInUser(), issue, IssueOperations.CREATE_ISSUE_OPERATION, false);
        final List<FieldScreenRenderTab> fieldScreenRenderTabs = fieldScreenRenderer.getFieldScreenRenderTabs();
		final Set<String> allowedCustomFieldIds = collectorFieldValidator.getAllowedCustomFieldIds(project, issueType);

        for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderTabs)
        {
            for (final FieldScreenRenderLayoutItem fsrli : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                final OrderableField orderableField = fsrli.getOrderableField();
                if(collectorFieldValidator.isFieldAllowedInCustomCollector(orderableField.getId()) || allowedCustomFieldIds.contains(orderableField.getId()))
                {
					orderableField.populateDefaults(getFieldValuesHolder(), issue);
                    final String createHtml = fsrli.getCreateHtml(this, this, issue, DISPLAY_PARAMS);
                    fields.addField(new Field(orderableField.getId(), getText(truncateFieldName(orderableField.getName())), isRequired(fsrli, issue), StringUtils.trim(createHtml)));
                }
            }
        }
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS))
        {
            fields.addField(createSpecialAttachmentsField());
        }

        return SUCCESS;
    }

    private boolean isRequired(final FieldScreenRenderLayoutItem fsrli, final Issue issue)
    {
        boolean hasDefaultValue = true;
        final Object defaultValue = fsrli.getOrderableField().getDefaultValue(issue);
        if(defaultValue == null || (defaultValue instanceof Collection && ((Collection) defaultValue).size() == 0))
        {
            hasDefaultValue = false;
        }
        //assignee field will be set to AUTOMATIC meaning it will select a default assignee anyways that's valid.
        if(fsrli.getOrderableField().getId().equals(IssueFieldConstants.ASSIGNEE))
        {
            return false;
        }
        //fields are only required if they don't have a default value.
        return fsrli.isRequired() && !hasDefaultValue;
    }

    private Field createSpecialAttachmentsField() {
        final String editHtml = String.format(
                      "<fieldset class=\"group\">\n" +
                      "     <legend><span>%s</span></legend>\n" +
                      "     <div id=\"screenshot-group\" class=\"field-group\">\n" +
                      "         <input type=\"file\" name=\"screenshot\" class=\"file\" id=\"screenshot\">\n" +
                      "     </div>\n" +
                      "</fieldset>\n",
                      getText("collector.plugin.template.add.file"));

        return new Field("screenshots", getText("collector.plugin.template.add.file"), false,editHtml);
    }

    private String truncateFieldName(final String name) {
        if (name.length() > TRIMED_NAME_LENGTH) {
            final StringBuilder sb = new StringBuilder(30);
            sb.append(name.substring(0, TRIMED_NAME_LENGTH)).append("...");
            return sb.toString();
        } else {
            return name;
        }
    }

    public String getJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(fields);
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(final String issueType)
    {
        this.issueType = issueType;
    }

    public String getErrorJson()
    {
        final JaxbJsonMarshaller marshaller = new DefaultJaxbJsonMarshaller();
        return marshaller.marshal(errors);
    }

    @Override
    public Map getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.CREATE_ISSUE_OPERATION;
    }
}
