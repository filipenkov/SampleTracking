package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.velocity.VelocityManager;
import webwork.action.ActionContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

/**
 * Displays activity stream fragment for a project.
 *
 * @since v4.0
 */
public class ActivityStreamFragment extends AbstractFragment
{
    protected static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";
    private static final String ACTIVTY_STREAM_GADGET_MODULE_KEY = "com.atlassian.streams.streams-jira-plugin:activitystream-gadget";
    private static final String PREF_IS_CONFIGURED = "isConfigured";
    private static final String PREF_KEYS = "keys";
    private static final String GADGET_URI = "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin/gadgets/activitystream-gadget.xml";
    private static final String PREF_NUMOFENTRIES = "numofentries";
    private static final String NUMOFENTRIES = "10";
    private static final String PREFS_IS_CONFIGURABLE = "isConfigurable";
    private static final String PREF_TITLE_REQUIRED = "titleRequired";

    private final PluginAccessor pluginAccessor;

    public ActivityStreamFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites,
            final JiraAuthenticationContext jiraAuthenticationContext, final PluginAccessor pluginAccessor)
    {
        super(velocityManager, applicationProperites, jiraAuthenticationContext);
        this.pluginAccessor = pluginAccessor;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> params = super.createVelocityParams(ctx);

        final GadgetViewFactory viewFactory = ComponentManager.getOSGiComponentInstanceOfType(GadgetViewFactory.class);

        final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();
        prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
        prefsBuilder.add(PREFS_IS_CONFIGURABLE, Boolean.FALSE.toString());
        prefsBuilder.add(PREF_KEYS, ctx.getProject().getKey());
        prefsBuilder.add(PREF_NUMOFENTRIES, NUMOFENTRIES);
        prefsBuilder.add(PREF_TITLE_REQUIRED, Boolean.FALSE.toString());

        final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf("1")).specUri(URI.create(GADGET_URI)).userPrefs(prefsBuilder.toMap()).build();
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Writer gadgetWriter = new OutputStreamWriter(baos);
            //TODO Dashboard: Revisit if we should use the ActionContext here...
            final GadgetRequestContext requestContext = ComponentManager.getOSGiComponentInstanceOfType(GadgetRequestContextFactory.class).get(ActionContext.getRequest());
            final View settings = new View.Builder().viewType(ViewType.DEFAULT).writable(false).build();
            viewFactory.createGadgetView(gadget, ModuleId.valueOf(1L), settings, requestContext).writeTo(gadgetWriter);
            gadgetWriter.flush();

            params.put("gadgetHtml", baos.toString());
        }
        catch (IOException e)
        {
            log.error("Error rendering activity stream gadget.", e);
        }
        catch (RuntimeException e)
        {
            log.error("Runtime error rendering activity stream gadget.", e);
        }

        return params;
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "activitystream";
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        //need to both have the gadget plugin as well as the activity stream gadget available!
        final GadgetViewFactory viewFactory = ComponentManager.getOSGiComponentInstanceOfType(GadgetViewFactory.class);
        return viewFactory != null && pluginAccessor.isPluginModuleEnabled(ACTIVTY_STREAM_GADGET_MODULE_KEY);
    }
}
