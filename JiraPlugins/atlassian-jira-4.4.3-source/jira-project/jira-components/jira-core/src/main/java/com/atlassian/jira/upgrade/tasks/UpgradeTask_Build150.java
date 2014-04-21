/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UpgradeTask_Build150 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build150.class);

    public final static String systemEventTypeConfigFile = "upgrade-system-event-types.xml";

    public static final String ID_STRING = "id";
    public static final String NAME_STRING = "name";
    public static final String DESC_STRING = "description";
    public static final String TYPE_STRING = "type";
    public static final String NOTIFICATION_NAME = "notificationName";
    public static final String EVENT_NAME = "eventName";

    public static final String EVENT_COL_NAME = "event";
    public static final String EVENT_TYPE_ID = "eventTypeId";
    public static final String EVENT_TYPE = "eventType";

    public static final String NOTIFICATION_INSTANCE_ENTITY_NAME = "NotificationInstance";

    private Map nameIdMap = new HashMap();

    private final OfBizDelegator delegator;

    public UpgradeTask_Build150(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public String getBuildNumber()
    {
        return "150";
    }

    public String getShortDescription()
    {
        return "Initialise the JIRA Event Type table with system event types and update the workflows and Notification and NotificationInstance tables with Event Type Ids.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Maintain order
        initSystemEventTypes();
        updateNotificationTable();
        updateWorkflows();
        updateNotificationInstanceTable();
    }

    /**
     * Add the system event types to the EventType table
     */
    private void initSystemEventTypes() throws Exception
    {
        //read in the event types from an xml file in the class path
        InputStream is = null;
        try
        {
            is = ClassLoaderUtils.getResourceAsStream(systemEventTypeConfigFile, getClass());
            Document doc = new Document(is);
            Element root = doc.getRoot();
            Elements actions = root.getElements("eventtype");

            while (actions.hasMoreElements())
            {
                Element action = (Element) actions.nextElement();
                parseAction(action);
            }
        }
        catch (ParseException e)
        {
            log.error("Error parsing " + systemEventTypeConfigFile + ": " + e.getMessage(), e);
            throw new ParseException("Error parsing " + systemEventTypeConfigFile + ": " + e.getMessage());
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (IOException e)
            {
                log.error("Could not close event types inputStream.", e);
                throw new ParseException(("Could not close event types inputStream: " + e.getMessage()));
            }
        }

    }

    void parseAction(final Element action)
    {
        String id = action.getAttributeValue("id");
        Element nameKeyElement = action.getElement("i18n-name-key");
        Element descKeyElement = action.getElement("i18n-description-key");
        String name = action.getElement("name").getTextString();
        String desc = action.getElement("description").getTextString();
        String oldName = action.getElement(EVENT_NAME).getTextString();

        name = nameKeyElement == null ? name : getI18nTextWithDefault(nameKeyElement.getTextString(), name);
        desc = descKeyElement == null ? desc : getI18nTextWithDefault(descKeyElement.getTextString(), desc);

        nameIdMap.put(oldName, id);
        createNewEventIfNotExistAlready(id, name, desc);
    }

    private String getI18nTextWithDefault(String key, String defaultResult)
    {
        String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }

    /**
     * Create an event in the {@link EventType#EVENT_TYPE} table if one does not already exist
     */
    private void createNewEventIfNotExistAlready(String id, String name, String desc)
    {
        //if this upgrade task runs more than once - we want to make sure we don't create events twice.  Upgrade tasks should be idempotent!
        boolean eventAlreadyExists = (delegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build(ID_STRING, new Long(id))) != null);
        if (!eventAlreadyExists)
        {
            Map eventTypeParamasMap = EasyMap.build(ID_STRING, new Long(id), NAME_STRING, name, DESC_STRING, desc, TYPE_STRING, EventType.JIRA_SYSTEM_EVENT_TYPE);
            delegator.createValue(EventType.EVENT_TYPE, eventTypeParamasMap);
        }
    }

    /**
     * Add eventTypeIds to Notification table
     */
    private void updateNotificationTable() throws Exception
    {
        //read in the event types from an xml file in the class path
        InputStream is = ClassLoaderUtils.getResourceAsStream(systemEventTypeConfigFile, getClass());
        try
        {
            Document doc = new Document(is);
            Element root = doc.getRoot();
            Elements actions = root.getElements("eventtype");

            // Update the notification table
            while (actions.hasMoreElements())
            {
                Element action = (Element) actions.nextElement();
                String originalEventName = action.getElement(NOTIFICATION_NAME).getTextString();
                Long id = new Long(action.getAttributeValue("id"));

                // update the values
                delegator.bulkUpdateByAnd("Notification", EasyMap.build(EVENT_TYPE_ID, id), EasyMap.build(EVENT_COL_NAME, originalEventName));
            }
        }
        catch (ParseException e)
        {
            log.error("Error parsing " + systemEventTypeConfigFile + ": " + e.getMessage(), e);
            throw new ParseException("Error parsing " + systemEventTypeConfigFile + ": " + e.getMessage());
        }

        try
        {
            is.close();
        }
        catch (IOException e)
        {
            log.error("Could not close event types inputStream.", e);
            throw new ParseException("Could not close event types inputStream: " + e.getMessage());
        }
    }

    /**
     * Update workflows with event type ids
     * <p/>
     * Output warning if encounted workflow saved to disk - JIRA 3.6 Upgrade Guide details modifying disk workflows.
     */
    private void updateWorkflows() throws Exception
    {
        Collection<String> changes = new ArrayList<String>();

        if (nameIdMap != null && !nameIdMap.isEmpty())
        {
            WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();

            Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();

            for (final JiraWorkflow workflow : workflows)
            {
                // JIRA workflow already modified
                if (!workflow.getName().equals("jira"))
                {
                    log.info("Inspecting workflow '" + workflow.getName() + "'.");

                    // Retrieve map of: actions -> post functions
                    Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = workflowManager.getPostFunctionsForWorkflow(workflow);

                    Collection<ActionDescriptor> keys = transitionPostFunctionMap.keySet();

                    for (final ActionDescriptor actionDescriptor : keys)
                    {
                        Collection<FunctionDescriptor> postFunctions = transitionPostFunctionMap.get(actionDescriptor);

                        for (final FunctionDescriptor functionDescriptor : postFunctions)
                        {
                            if (functionDescriptor.getArgs().containsKey(EVENT_TYPE))
                            {
                                String oldName = (String) functionDescriptor.getArgs().get(EVENT_TYPE);

                                // This null check is needed to ensure that if the upgrade task is run twice it does not break workflows
                                if (oldName != null)
                                {
                                    // Log manual update for system workflows
                                    if (workflow.isSystemWorkflow())
                                    {
                                        changes.add(workflow.getName() + ": The <arg name=\"eventType\">" + oldName + "</arg> element needs to be replaced with <arg name=\"eventTypeId\">" + nameIdMap.get(oldName) + "</arg>.");
                                    }
                                    else
                                    {
                                        functionDescriptor.getArgs().remove(EVENT_TYPE);
                                        functionDescriptor.getArgs().put(EVENT_TYPE_ID, nameIdMap.get(oldName));
                                    }
                                }
                            }
                        }
                    }

                    if (workflow.isSystemWorkflow())
                    {
                        if (changes != null && !changes.isEmpty())
                        {
                            log.warn("The workflow: " + workflow.getName() + " needs to be updated manually. Please refer to the following upgrade guide for further information: " + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.update.event.workflows"));
                            log.warn("The following manual updates are required:");

                            for (final String change : changes)
                            {
                                log.warn(change);
                            }
                        }
                    }
                    else
                    {
                        try
                        {
                            workflowManager.saveWorkflowWithoutAudit(workflow);
                        }
                        catch (WorkflowException e)
                        {
                            log.error("Unable to modify the workflow:" + workflow.getName() + ". If this workflow is saved externally to JIRA, it will need to be" +
                                    "manually updated as detailed at:" + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.update.event.workflows"));
                            throw new WorkflowException("Unable to modify the workflow:" + workflow.getName() + ".");
                        }
                    }
                }
            }
        }
        else
        {
            log.error("Unable to update workflows with event type ids");
            throw new Exception("Unable to update workflows with event type ids");
        }
    }

    /**
     * Update the notification instance table 'TYPE' column to include event type ids
     *
     * @throws Exception
     */
    public void updateNotificationInstanceTable() throws Exception
    {
        InputStream is = null;
        try
        {
            is = ClassLoaderUtils.getResourceAsStream(UpgradeTask_Build150.systemEventTypeConfigFile, getClass());
            if (is != null)
            {
                Document doc = new Document(is);
                Element root = doc.getRoot();
                Elements actions = root.getElements("eventtype");

                long count = delegator.getCount(NOTIFICATION_INSTANCE_ENTITY_NAME);

                if (count > 0)
                {
                    String message = "Updating " + count + " records in the '" + NOTIFICATION_INSTANCE_ENTITY_NAME + "' table.";
                    String message2 = "This might take a long time. Please do NOT stop JIRA.";
                    int repeatCount = Math.max(message.length(), message2.length());
                    log.info(StringUtils.repeat("*", repeatCount));
                    log.info(message);
                    log.info(message2);
                    log.info(StringUtils.repeat("*", repeatCount));

                    // Update the notification table
                    while (actions.hasMoreElements())
                    {
                        Element action = (Element) actions.nextElement();
                        String originalEventName = MailThreadManager.NOTIFICATION_KEY + action.getElement(UpgradeTask_Build150.NOTIFICATION_NAME).getTextString();

                        Long id = new Long(action.getAttributeValue("id"));

                        log.info("Updating records of type '" + originalEventName + "'.");

                        // update the values
                        delegator.bulkUpdateByAnd(NOTIFICATION_INSTANCE_ENTITY_NAME, EasyMap.build("type", MailThreadManager.NOTIFICATION_KEY + id), EasyMap.build("type", originalEventName));
                    }

                    log.info("Update of '" + NOTIFICATION_INSTANCE_ENTITY_NAME + "' records finished.");
                }
                else
                {
                    log.info("No records in '" + NOTIFICATION_INSTANCE_ENTITY_NAME + "' table to update.");
                }
            }
            else
            {
                log.error("Could not find file '" + UpgradeTask_Build150.systemEventTypeConfigFile + "'.");
                log.error("Records in NotificationInstance table will not be updated, and notification e-mails for existing issues will not be threaded.");
            }
        }
        catch (ParseException e)
        {
            log.error("Error parsing " + UpgradeTask_Build150.systemEventTypeConfigFile + ": " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            log.error("Error occurred : " + e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (IOException e)
            {
                log.error("Could not close event types inputStream.", e);
            }
        }
    }
}
