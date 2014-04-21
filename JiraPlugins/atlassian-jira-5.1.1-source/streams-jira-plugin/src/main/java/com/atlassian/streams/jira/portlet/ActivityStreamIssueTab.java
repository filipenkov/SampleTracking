package com.atlassian.streams.jira.portlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.webresource.WebResourceManager;

import com.google.common.collect.ImmutableList;
import com.atlassian.crowd.embedded.api.User;

import org.json.JSONObject;

import webwork.action.ActionContext;

/**
 * An issue tab panel that displays an activity stream restricted to items related to the current issue (because they
 * link to it, or it links to them).
 */
@SuppressWarnings("deprecation")
public class ActivityStreamIssueTab extends AbstractIssueTabPanel
{
    public static final String USER_PROFILE_STREAM_TITLE = "user.profile.stream.panel.title";
    private static final String PREF_IS_CONFIGURED = "isConfigured";
    private static final String GADGET_URI = "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin/gadgets/activitystream-gadget.xml";
    private static final String PREF_NUMOFENTRIES = "numofentries";
    private static final String NUMOFENTRIES = "20";
    private static final String PREF_IS_CONFIGURABLE = "isConfigurable";
    private static final String RULES_PREF = "rules";

    private final WebResourceManager webResourceManager;
    private final GadgetViewFactory gadgetViewFactory;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private static final String PREF_TITLE_REQUIRED = "titleRequired";
    private static final String PREF_IS_CUSTOMIZED = "isCustomized";
    private static final String PREF_IS_REALLY_CONFIGURED = "isReallyConfigured";
    private static final String PREF_CONTEXT = "renderingContext";
    private static final String CONTEXT = "view-issue";

    private static final String ISSUE_TAB_VIEW_NAME = "issueTab";

    static
    {
        try
        {
            // We want to make sure a view with this name exists,
            // so we create it if necessary. If it already exists,
            // we should be fine
            ViewType.createViewType(ISSUE_TAB_VIEW_NAME);
        }
        catch (IllegalArgumentException e) { }
    }

    public ActivityStreamIssueTab(WebResourceManager webResourceManager, final GadgetViewFactory gadgetViewFactory,
                                  final GadgetRequestContextFactory gadgetRequestContextFactory)
    {
        this.webResourceManager = webResourceManager;
        this.gadgetViewFactory = gadgetViewFactory;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
    }

    public List<IssueAction> getActions(Issue issue, User user)
    {
        // this is a bit of a perversion of the way that these IssueTabs are designed to work, but
        // by faking it, we can just a our existing framework for displaying updates
        List<IssueAction> l = new ArrayList<IssueAction>(1);
        l.add(createIssueAction(issue));
        return l;
    }

    private IssueAction createIssueAction(final Issue issue)
    {
        return new AbstractIssueAction(this.descriptor)
        {
            @Override
            public Date getTimePerformed()
            {
                return new Date();
            }

            @Override
            public boolean isDisplayActionAllTab()
            {
                return false;
            }

            @Override
            @SuppressWarnings ({ "unchecked", "rawtypes" })
            protected void populateVelocityParams(Map map)
            {
                final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();
                prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
                prefsBuilder.add(PREF_IS_CONFIGURABLE, Boolean.FALSE.toString());
                prefsBuilder.add(PREF_IS_REALLY_CONFIGURED, Boolean.TRUE.toString());
                prefsBuilder.add(PREF_NUMOFENTRIES, NUMOFENTRIES);
                prefsBuilder.add(PREF_TITLE_REQUIRED, Boolean.FALSE.toString());
                prefsBuilder.add(PREF_IS_CUSTOMIZED, Boolean.TRUE.toString());
                prefsBuilder.add(PREF_CONTEXT, CONTEXT);

                try
                {
                    JSONObject issueKeyRule = new JSONObject()
                        .put("provider", "streams")
                        .put("rule", "issue-key")
                        .put("operator", "is")
                        .put("value", issue.getKey())
                        .put("type", "string");
                    JSONObject projectRule = new JSONObject()
                        .put("provider", "streams")
                        .put("rule", "key")
                        .put("operator", "is")
                        .put("value", ImmutableList.of(issue.getProjectObject().getKey()))
                        .put("type","select");
                    JSONObject globalProvider = new JSONObject()
                        .put("provider", "streams")
                        .put("rules", ImmutableList.of(issueKeyRule, projectRule));
                    final JSONObject rules = new JSONObject().put("providers", ImmutableList.of(globalProvider));
                    prefsBuilder.add(RULES_PREF, rules.toString());
                }
                catch (Exception ex)
                {
                    // This should never happen--- the JSON is statically defined except for the issue key
                    throw new RuntimeException("Error adding JSON pref", ex);
                }

                final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf("stream")).specUri(URI.create(GADGET_URI)).userPrefs(prefsBuilder.toMap()).build();
                try
                {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final Writer gadgetWriter = new OutputStreamWriter(baos);
                    final View settings = new View.Builder().viewType(ViewType.valueOf(ISSUE_TAB_VIEW_NAME)).writable(false).build();
                    gadgetViewFactory.createGadgetView(gadget, ModuleId.valueOf(0), settings, gadgetRequestContextFactory.get(ActionContext.getRequest())).writeTo(gadgetWriter);
                    gadgetWriter.flush();

                    prefsBuilder.add("gadgetHtml", baos.toString());
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Error rendering activity stream gadget.", e);
                }
                map.putAll(prefsBuilder.toMap());
            }
        };
    }

    public boolean showPanel(Issue issue, User user)
    {
        return true;
    }
}
