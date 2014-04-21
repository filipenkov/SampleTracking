package com.atlassian.streams.jira.upgrade;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTask;
import com.atlassian.jira.upgrade.util.SimpleLegacyPortletUpgradeTask;

import com.opensymphony.module.propertyset.PropertySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityStreamPortletUpgradeTask
{
    private static final Logger log = LoggerFactory.getLogger(ActivityStreamPortletUpgradeTask.class);

    private static final String ORIGINAL_PORTLET_PLUGIN_KEY = "com.atlassian.studio.jira-streams";
    private static final String RENAMED_PORTLET_KEY = "com.atlassian.streams.streams-jira-plugin:activityfeed";

    private static final URI GADGET_URI = URI.create("rest/gadgets/1.0/g/"
            + "com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml");

    private static final String PROJECT_ID_KEY = "projectid";
    private static final String PROJECT_KEY_KEY = "keys";

    /*
     * The standard LegacyPortletUpgradeTask interface doesn't really accommodate multiple portlet keys migrating to
     * the same gadget very well. So this class doesn't implement that, but rather wraps up an internal instance. 
     */
    private final LegacyPortletUpgradeTask internalPortletUpgradeTask;

    public ActivityStreamPortletUpgradeTask(final ProjectManager projectManager)
    {
        internalPortletUpgradeTask = new SimpleLegacyPortletUpgradeTask(null, GADGET_URI)
        {
            @Override
            public Map<String, String> convertUserPrefs(final PropertySet propertySet)
            {
                final Map<String, String> userPrefs = super.convertUserPrefs(propertySet);
                // Project ids need to be converted to keys
                if (userPrefs.containsKey(PROJECT_ID_KEY))
                {
                    final String[] projectids = userPrefs.get(PROJECT_ID_KEY).split("\\" + MULTIVALUE_SEPARATOR);
                    final List<String> keys = new ArrayList<String>();
                    for (final String idString : projectids)
                    {
                        if (idString.trim().length() > 0)
                        {
                            try
                            {
                                final long projectId = Long.parseLong(idString);
                                final Project project = projectManager.getProjectObj(projectId);
                                if (project != null)
                                {
                                    keys.add(project.getKey());
                                }
                            }
                            catch (final NumberFormatException nfe)
                            {
                                log.warn("Bad project ids value found: " + idString +
                                        ". This portlet configuration may not be migrated properly.");
                            }
                        }
                    }
                    if (keys.size() > 0)
                    {
                        final StringBuffer sb = new StringBuffer();
                        String sep = "";
                        for (String key : keys)
                        {
                            sb.append(sep);
                            sb.append(key);
                            sep = MULTIVALUE_SEPARATOR;
                        }
                        userPrefs.put(PROJECT_KEY_KEY, sb.toString());
                    }
                }
                return userPrefs;
            }
        };
    }

    public boolean canUpgrade(String portletKey)
    {
        return portletKey != null &&
                (portletKey.startsWith(ORIGINAL_PORTLET_PLUGIN_KEY) || portletKey.startsWith(RENAMED_PORTLET_KEY));
    }

    public Map<String, String> convertUserPrefs(final PropertySet propertySet)
    {
        return internalPortletUpgradeTask.convertUserPrefs(propertySet);
    }

    public URI getGadgetUri()
    {
        return GADGET_URI;
    }
}
