package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorActivityHelper;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.ScriptletRenderer;
import com.atlassian.jira.collector.plugin.components.Trigger;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.seraph.util.RedirectUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists all the collectors currently defined
 *
 * @since v1.0
 */
public class ViewCollector extends AbstractProjectAdminAction
{
    private static final int DAYS_PAST = 30;

    public static final String GADGET_URL = "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:filter-results-gadget/gadgets/filter-results-gadget.xml";
    private static final String PREF_FILTER_ID = "filterId";
    private static final String PREF_IS_POPUP = "isPopup";
    private static final String PREF_IS_CONFIGURED = "isConfigured";
    private static final String JQL_PREFIX = "jql-";
    private static final String PREF_NUM = "num";
    private static final String PREF_COL_NAMES = "columnNames";
    private static final String PREF_REFRESH = "refresh";
	public static final int ISSUES_IN_TABLE = 7;

	private final CollectorService collectorService;
    private final ConstantsManager constantsManager;
    private final UserFormats userFormats;
    private final CollectorActivityHelper collectorActivityHelper;
    private final GadgetViewFactory gadgetViewFactory;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final ScriptletRenderer scriptletRenderer;

    private String collectorId;
    private Map<String, List<Integer>> collectorActivty = new HashMap<String, List<Integer>>();
    private Collector collector;
    private String canonicalBaseUrl;
	private List<Issue> recentIssues;

    public ViewCollector(final CollectorService collectorService,
            final ConstantsManager constantsManager,
            final CollectorActivityHelper collectorActivityHelper, final GadgetViewFactory gadgetViewFactory,
            final GadgetRequestContextFactory gadgetRequestContextFactory, final ScriptletRenderer scriptletRenderer,
            final VelocityRequestContextFactory velocityRequestContextFactory, final UserFormats userFormats)
    {
        this.collectorService = collectorService;
        this.constantsManager = constantsManager;
        this.collectorActivityHelper = collectorActivityHelper;
        this.gadgetViewFactory = gadgetViewFactory;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.scriptletRenderer = scriptletRenderer;
        this.userFormats = userFormats;
        this.canonicalBaseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }

	@Override
    public String doDefault() throws Exception
    {
        initRequest();

		if (getLoggedInUser() == null) {
			final HttpServletRequest request = ExecutingHttpRequest.get();
			return forceRedirect(RedirectUtils.getLoginUrl(request));
		}

		final ServiceOutcome<Collector> result = collectorService.getCollector(collectorId);
        if (!result.isValid() || result.getReturnedValue() == null)
        {
            return ERROR;
        }

        collector = result.getReturnedValue();
        if (!isProjectAdmin(getProjectManager().getProjectObj(collector.getProjectId())))
        {
            return ERROR;
        }

		recentIssues = collectorActivityHelper.getCollectorIssues(getLoggedInUser(), collector, ISSUES_IN_TABLE);
        return SUCCESS;
    }

    public Collector getCollector()
    {
        return collector;
    }

    public IssueType getIssueType(Long issueTypeId)
    {
        return constantsManager.getIssueTypeObject(issueTypeId.toString());
    }

    public String getFormattedUser(String username)
    {
        return userFormats.forType("profileLinkActionHeader").format(username, "collector-reporter");
    }

    public List<String> getActiviyDates(final Collector collector)
    {
        final DateTime dateTime = new DateTime();
        final List<String> ret = new ArrayList<String>();
        final DateTimeFormatter format = DateTimeFormat.forPattern("d MMM");
        for (int i = DAYS_PAST - 1; i >= 0; i--)
        {
            final DateTime pastDateTime = dateTime.dayOfYear().addToCopy(-i);
            ret.add(format.print(pastDateTime));
        }
        return ret;
    }

    public List<Integer> getActivityForCollector(final Collector collector)
    {
        if (!collectorActivty.containsKey(collector.getId()))
        {
            collectorActivty.put(collector.getId(), collectorActivityHelper.getIssuesCreatedPerDay(getLoggedInUser(), collector, DAYS_PAST));
        }
        return collectorActivty.get(collector.getId());
    }

    public String getActivityUrl()
    {
        return collectorActivityHelper.getIssueNavigatorUrl(getLoggedInUser(), collector);
    }

    public String getCollectorId()
    {
        return collectorId;
    }

    public void setCollectorId(final String collectorId)
    {
        this.collectorId = collectorId;
    }

    public String getScriptSource()
    {
        return scriptletRenderer.render(collector);
    }

    public String getScriptSourceJavascript()
    {
        return scriptletRenderer.renderJavascript(collector);
    }

    public boolean isCustomTrigger()
    {
        return collector.getTrigger().getPosition().equals(Trigger.Position.CUSTOM);
    }

    public String getFilterGadgetHtml()
    {
        final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();

        prefsBuilder.add(PREF_IS_POPUP, Boolean.FALSE.toString());
        prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
        prefsBuilder.add(PREF_NUM, "7");
        prefsBuilder.add(PREF_COL_NAMES, "--Default--");
        prefsBuilder.add(PREF_REFRESH, "false");
        prefsBuilder.add(PREF_FILTER_ID, JQL_PREFIX + collectorActivityHelper.getJql(getLoggedInUser(), collector));

        final Map<String, String> prefs = prefsBuilder.toMutableMap();

        final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf("1")).specUri(URI.create(GADGET_URL)).userPrefs(prefs).build();
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Writer gadgetWriter = new OutputStreamWriter(baos);
            final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);
            final View readOnlyDefaultView = new View.Builder().viewType(ViewType.DEFAULT).addViewParam("enableReload", "false").writable(false).build();
            gadgetViewFactory.createGadgetView(gadget, ModuleId.valueOf(1L), readOnlyDefaultView, requestContext).writeTo(gadgetWriter);
            gadgetWriter.flush();

            return baos.toString();
        }
        catch (IOException e)
        {
            log.error("Error rendering gadget '" + GADGET_URL + "'", e);
        }

        return "";
    }

    public String getCanonicalBaseUrl() {
        return canonicalBaseUrl;
    }

	public List<Issue> getRecentIssues() {
		return recentIssues;
	}

	public int getIssuesCount() {
		return collectorActivityHelper.getAllCollectorIssuesCount(getLoggedInUser(), collector);
	}
}
