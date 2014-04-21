/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Add a default notification scheme to all projects
 */
public class UpgradeTask_Build35 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build35.class);
    private static final String SYSTEM_EVENT_TYPE_CONFIG_FILE = "upgrade-system-event-types.xml";

    public UpgradeTask_Build35()
    {
        super(false);
    }

    public String getBuildNumber()
    {
        return "35";
    }

    /**
     * Add a default notification scheme to all projects
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws Exception
    {
        log.debug("UpgradeTask_Build35 - add a default notification scheme to all projects.");

        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();

        if (nsm.getSchemes() == null || nsm.getSchemes().size() == 0)
        {
            GenericValue scheme = nsm.createDefaultScheme();

            //read in the event type ids from an xml file in the class path
            InputStream is = ClassLoaderUtils.getResourceAsStream(SYSTEM_EVENT_TYPE_CONFIG_FILE, UpgradeTask_Build35.class);
            try
            {
                Document doc = new Document(is);
                Element root = doc.getRoot();
                Elements actions = root.getElements("eventtype");

                while (actions.hasMoreElements())
                {
                    Element action = (Element) actions.nextElement();
                    Long eventTypeId = new Long(action.getAttributeValue("id"));

                    nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", null, eventTypeId));
                    nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Reporter", null, eventTypeId));
                    nsm.createSchemeEntity(scheme, new SchemeEntity("All_Watchers", null, eventTypeId));
                }
            }
            catch (ParseException e)
            {
                log.error("Error parsing "+ SYSTEM_EVENT_TYPE_CONFIG_FILE + ": " + e, e);
            }

            for (Iterator iterator = ManagerFactory.getProjectManager().getProjects().iterator(); iterator.hasNext();)
            {
                GenericValue project = (GenericValue) iterator.next();
                nsm.addSchemeToProject(project, scheme);
            }
        }
        else
        {
            log.debug("There is already a notification scheme - not adding default scheme.");
        }
    }
}
