package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorFieldValidator;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.Template;
import com.atlassian.jira.collector.plugin.components.TemplateStore;
import com.atlassian.jira.collector.plugin.components.Trigger;
import com.atlassian.jira.collector.plugin.components.fieldchecker.MissingFieldsChecker;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.seraph.util.RedirectUtils;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddCollector extends AbstractProjectAdminAction implements OperationContext
{
	private final CollectorService collectorService;
	private final UserPickerSearchService searchService;
	private final TemplateStore templateStore;
	private final PermissionManager permissionManager;
	private final IssueFactory issueFactory;
	private final WebResourceManager webResourceManager;
	private final MissingFieldsChecker missingFieldsChecker;
    private final CollectorFieldValidator collectorFieldValidator;

	private final String CUSTOM_FUNC_EXAMPLE = "function(onclickHandler) {\n"
			+ "\t//Requries that jQuery is available! \n"
			+ "\tjQuery(\"#myCustomTrigger\").click(function(e) {\n"
			+ "\t\te.preventDefault();\n"
			+ "\t\tonclickHandler();\n"
			+ "\t});\n"
			+ "}";

	private final static String NO_ADD_PERMISSION = "noaddpermission";

	private Long issuetype;
	private String name;
	private String reporter;
	private String description;
	private String templateId;
	private boolean recordWebInfo;
	private boolean useCredentials;
	private String triggerText;
	private String triggerPosition;
	private String customFunction;
	private String customMessage;
	private String customTemplateFields;
	private String customTemplateTitle;
	private String customTemplateLabels;

	public AddCollector(final CollectorService collectorService,
                        final UserPickerSearchService searchService, final TemplateStore templateStore,
                        final PermissionManager permissionManager, final IssueFactory issueFactory, final WebResourceManager webResourceManager,
                        final MissingFieldsChecker missingFieldsChecker, final CollectorFieldValidator collectorFieldValidator)
	{
		this.collectorService = collectorService;
		this.searchService = searchService;
		this.templateStore = templateStore;
		this.permissionManager = permissionManager;
		this.issueFactory = issueFactory;
		this.webResourceManager = webResourceManager;
		this.missingFieldsChecker = missingFieldsChecker;
        this.collectorFieldValidator = collectorFieldValidator;
    }

	@Override
	public String doDefault() throws Exception
	{
		initRequest();

		if (getLoggedInUser() == null) {
			final HttpServletRequest request = ExecutingHttpRequest.get();
			return forceRedirect(RedirectUtils.getLoginUrl(request));
		}

		if (getPid() == null)
		{
			return ERROR;
		}

		final ServiceOutcome<Boolean> outcome = collectorService.validateAddCollectorPremission(getProject(),getLoggedInUser());
		if (!outcome.getReturnedValue()) {
			this.addErrorCollection(outcome.getErrorCollection());
			return NO_ADD_PERMISSION;
		}

		//some sensible defaults
		webResourceManager.requireResource("com.atlassian.jira.jira-issue-nav-plugin:common");
		this.triggerPosition = Trigger.Position.TOP.toString();
		this.triggerText = getText("collector.plugin.trigger.text.default");
		this.recordWebInfo = true;
		this.useCredentials = false;
		this.templateId = templateStore.getTemplates().get(0).getId();
		this.customFunction = CUSTOM_FUNC_EXAMPLE;
		this.customTemplateTitle = "";
		this.customMessage = getText("collector.plugin.admin.custom.message");

		return INPUT;
	}


	@Override
	protected void doValidation()
	{
		initRequest();
		updateCheckboxes();
		final Trigger trigger = new Trigger(triggerText, Trigger.Position.valueOf(triggerPosition), customFunction);
		final ServiceOutcome<Collector> outcome =
				collectorService.validateCreateCollector(getLoggedInUser(), name, getPid(), issuetype, reporter,
						description, templateId, recordWebInfo, useCredentials, trigger, customMessage, getCustomTemplateFieldList(), customTemplateTitle, customTemplateLabels);
		if (!outcome.isValid())
		{
			this.addErrorCollection(outcome.getErrorCollection());
		}
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception
	{
		initRequest();
		updateCheckboxes();
		final Trigger trigger = new Trigger(triggerText, Trigger.Position.valueOf(triggerPosition), customFunction);
		final ServiceOutcome<Collector> outcome =
				collectorService.validateCreateCollector(getLoggedInUser(), name, getPid(), issuetype, reporter,
						description, templateId, recordWebInfo, useCredentials, trigger, customMessage, getCustomTemplateFieldList(), customTemplateTitle, customTemplateLabels);
		if (outcome.isValid())
		{
			final ServiceOutcome<Collector> collector = collectorService.createCollector(getLoggedInUser(), outcome);
			return getRedirect("/secure/InsertCollectorHelp!default.jspa?projectKey=" + TextUtils.htmlEncode(getProjectKey()) + "&collectorId=" + collector.getReturnedValue().getId());
		}
		return ERROR;
	}

	private List<String> getCustomTemplateFieldList()
	{
		final List<String> customFields = new ArrayList<String>();
		if (StringUtils.isNotBlank(customTemplateFields))
		{
			final Set<String> internal = new LinkedHashSet<String>();
			try
			{
				final JSONArray array = new JSONArray(customTemplateFields);
				for (int i = 0; i < array.length(); i++)
				{
					internal.add(array.getString(i));
				}
			}
			catch (JSONException e)
			{
				addErrorMessage("Invalid JSON array '" + customTemplateFields + "'");
			}
			customFields.addAll(internal);
		}
		return customFields;
	}

	public String getIssueTypeHtml()
	{
		return getFieldHtml(IssueFieldConstants.ISSUE_TYPE);
	}

	public Long getIssuetype()
	{
		return issuetype;
	}

	public void setIssuetype(final Long issuetype)
	{
		this.issuetype = issuetype;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getReporter()
	{
		return reporter;
	}

	public void setReporter(final String reporter)
	{
		this.reporter = reporter;
	}

    public User getReporterUser()
    {
        return UserUtils.getUser(reporter);
    }

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public boolean isRecordWebInfo()
	{
		return recordWebInfo;
	}

	public void setRecordWebInfo(final boolean recordWebInfo)
	{
		this.recordWebInfo = recordWebInfo;
	}

	public boolean isUseCredentials()
	{
		return useCredentials;
	}

	public void setUseCredentials(final boolean useCredentials)
	{
		this.useCredentials = useCredentials;
	}

	public String getTriggerText()
	{
		return triggerText;
	}

	public void setTriggerText(final String triggerText)
	{
		this.triggerText = triggerText;
	}

	public String getTriggerPosition()
	{
		return triggerPosition;
	}

	public void setTriggerPosition(final String triggerPosition)
	{
		this.triggerPosition = triggerPosition;
	}

	public String getCustomFunction()
	{
		return customFunction;
	}

	public void setCustomFunction(final String customFunction)
	{
		this.customFunction = customFunction;
	}

	public String getTemplateId()
	{
		return templateId;
	}

	public void setTemplateId(final String templateId)
	{
		this.templateId = templateId;
	}

	public boolean canPerformAjaxSearch()
	{
		return searchService.canPerformAjaxSearch(getJiraServiceContext());
	}

	public List<Template> getCollectorTemplates()
	{
		return templateStore.getTemplates();
	}

	public boolean isPublicProject()
	{
		return permissionManager.hasPermission(Permissions.BROWSE, getProject(), (User) null);
	}

	public String getCustomMessage()
	{
		return customMessage;
	}

	public void setCustomMessage(final String customMessage)
	{
		this.customMessage = customMessage;
	}

	public String getCustomTemplateFields()
	{
		return customTemplateFields;
	}

	public void setCustomTemplateFields(final String customTemplateFields)
	{
		this.customTemplateFields = customTemplateFields;
	}

	@Override
	public Map getFieldValuesHolder()
	{
		return new HashMap();
	}

	@Override
	public IssueOperation getIssueOperation()
	{
		return IssueOperations.CREATE_ISSUE_OPERATION;
	}

	public Long getPid()
	{
		final Project project = getProject();
		return project == null ? null : project.getId();
	}

	private void updateCheckboxes()
	{
		if (!ActionContext.getParameters().containsKey("recordWebInfo"))
		{
			recordWebInfo = false;
		}
		if (!ActionContext.getParameters().containsKey("useCredentials"))
		{
			useCredentials = false;
		}
	}

	private String getFieldHtml(final String fieldId)
	{
		final Map<String, Object> displayParams = new HashMap<String, Object>();
		displayParams.put("theme", "aui");
		final MutableIssue issue = issueFactory.getIssue();
		issue.setProjectId(getPid());

		return ((OrderableField) getField(fieldId)).getCreateHtml(null, this, this, issue, displayParams);
	}

	public String getCustomTemplateTitle()
	{
		return customTemplateTitle;
	}

	public void setCustomTemplateTitle(final String customTemplateTitle)
	{
		this.customTemplateTitle = customTemplateTitle;
	}

	public String getCustomTemplateLabels()
	{
		return customTemplateLabels;
	}

	public void setCustomTemplateLabels(final String customTemplateLabels)
	{
		this.customTemplateLabels = customTemplateLabels;
	}

	public String getMissingFieldPerIssueType()
	{
		try {
			return new ObjectMapper().writeValueAsString(missingFieldsChecker.getIssueTypeToMissingFieldsMapping(getProject()));
		} catch (IOException e) {
			return "";
		}
	}

    public String getRequiredInvalidFields()
    {
        try {
            return new ObjectMapper().writeValueAsString(collectorFieldValidator.getRequiredInvalidFieldsForProject(getLoggedInUser(), getProject()));
        } catch (IOException e) {
            return "";
        }
    }
}
