package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorActivityHelper;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.ErrorLog;
import com.atlassian.jira.collector.plugin.components.fieldchecker.MissingFieldsChecker;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.seraph.util.RedirectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists all the collectors currently defined
 *
 * @since v1.0
 */
public class ViewCollectors extends AbstractProjectAdminAction
{
	private final CollectorService collectorService;
	private final ConstantsManager constantsManager;
	private final UserFormats userFormats;
	private final CollectorActivityHelper collectorActivityHelper;
	private final ErrorLog errorLog;
	private final MissingFieldsChecker missingFieldsChecker;
	private List<Collector> collectors;
	private Map<String, List<Integer>> collectorActivty = new HashMap<String, List<Integer>>();
	private List<Collector> collectorsWithMissingFields;

	public ViewCollectors(final CollectorService collectorService,
			final ConstantsManager constantsManager, final UserFormats userFormats,
			final CollectorActivityHelper collectorActivityHelper, final ErrorLog errorLog, final MissingFieldsChecker missingFieldsChecker)
	{
		this.collectorService = collectorService;
		this.constantsManager = constantsManager;
		this.userFormats = userFormats;
		this.collectorActivityHelper = collectorActivityHelper;
		this.errorLog = errorLog;
		this.missingFieldsChecker = missingFieldsChecker;
	}

	@Override
	public String doDefault() throws Exception
	{
		initRequest();

		if (getLoggedInUser() == null) {
			final HttpServletRequest request = ExecutingHttpRequest.get();
			return forceRedirect(RedirectUtils.getLoginUrl(request));
		}

		if(getProject() == null)
		{
			return ERROR;
		}

		ServiceOutcome<List<Collector>> result = collectorService.getCollectors(getLoggedInUser(), getProject());
		if (!result.isValid())
		{
			return ERROR;
		}
		collectors = result.getReturnedValue();

		collectorsWithMissingFields = missingFieldsChecker.getMisconfiguredCollectors(collectors, getProject());

		return SUCCESS;
	}

	public List<Collector> getCollectors()
	{
		return collectors;
	}

	public IssueType getIssueType(Long issueTypeId)
	{
		return constantsManager.getIssueTypeObject(issueTypeId.toString());
	}

	public String getFormattedUser(String username, String className)
	{
		return userFormats.forType("profileLinkActionHeader").format(username, className);
	}

	public List<Integer> getActivityForCollector(final Collector collector)
	{
		if (!collectorActivty.containsKey(collector.getId()))
		{
			collectorActivty.put(collector.getId(), collectorActivityHelper.getIssuesCreatedPerDay(getLoggedInUser(), collector, 30));
		}
		return collectorActivty.get(collector.getId());
	}

	public int getTotalNumberOfIssuesForCollector(final Collector collector)
	{
		final List<Integer> activityForCollector = getActivityForCollector(collector);
		int sum = 0;
		for (Integer issuesCreated : activityForCollector)
		{
			sum += issuesCreated;
		}
		return sum;
	}

	public String getActivityUrl(final Collector collector)
	{
		return collectorActivityHelper.getIssueNavigatorUrl(getLoggedInUser(), collector);
	}

	public List<String> getCollectorErrors()
	{
		return errorLog.getFormattedErrors(getProject(), getLoggedInUser());
	}

	public List<Collector> getCollectorsWithMissingFields()
	{
		return collectorsWithMissingFields;
	}
}
