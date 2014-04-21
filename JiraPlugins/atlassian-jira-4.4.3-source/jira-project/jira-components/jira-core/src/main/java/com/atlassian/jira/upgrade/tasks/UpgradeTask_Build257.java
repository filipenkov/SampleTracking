package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * This upgrade task creates the notification event for the
 * {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_UPDATED_ID} and
 * {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_DELETED_ID} events.
 */
public class UpgradeTask_Build257 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build257.class);

    private OfBizDelegator ofBizDelegator;
    private EventTypeManager eventTypeManager;
    static final String ISSUE_WORKLOG_UPDATED_NAME_KEY = "event.type.issueworklogupdated.name";
    static final String ISSUE_WORKLOG_UPDATED_DESC_KEY = "event.type.issueworklogupdated.desc";
    static final String ISSUE_WORKLOG_DELETED_NAME_KEY = "event.type.issueworklogdeleted.name";
    static final String ISSUE_WORKLOG_DELETED_DESC_KEY = "event.type.issueworklogdeleted.desc";

    static final String ISSUE_WORKLOG_UPDATED_NAME = "Issue Worklog Updated";
    static final String ISSUE_WORKLOG_UPDATED_DESC = "This is the 'Issue Worklog Updated' event type.";
    static final String ISSUE_WORKLOG_DELETED_NAME = "Issue Worklog Deleted";
    static final String ISSUE_WORKLOG_DELETED_DESC = "This is the 'Issue Worklog Deleted' event type.";

    /**
     * Constructs a new instance with given {@link com.atlassian.jira.ofbiz.OfBizDelegator} and {@link com.atlassian.jira.event.type.EventTypeManager}.
     *
     * @param ofBizDelegator   OFBiz delegator
     * @param eventTypeManager event type manager
     */
    public UpgradeTask_Build257(OfBizDelegator ofBizDelegator, EventTypeManager eventTypeManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.eventTypeManager = eventTypeManager;
    }

    public String getShortDescription()
    {
        return "Creates the EventTypes for the ISSUE_WORKLOG_UPDATED and ISSUE_WORKLOG_DELETED events.";
    }

    public String getBuildNumber()
    {
        return "257";
    }

    /**
     * Runs the core task which is to create the new Issue Worklog Edited and Issue Worklog Deleted event type and
     * update the eventTypeManager with this change
     *
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if something goes wrong with database access
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws DataAccessException
    {
        try
        {
            GenericValue value = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build(UpgradeTask_Build150.ID_STRING, EventType.ISSUE_WORKLOG_UPDATED_ID));
            if (value == null)
            {
                // Create the notification event for the issue worklog updated event
                Map eventTypeParamasMap = EasyMap.build(
                        UpgradeTask_Build150.ID_STRING, EventType.ISSUE_WORKLOG_UPDATED_ID,
                        UpgradeTask_Build150.NAME_STRING, getI18nTextWithDefault(ISSUE_WORKLOG_UPDATED_NAME_KEY, ISSUE_WORKLOG_UPDATED_NAME),
                        UpgradeTask_Build150.DESC_STRING, getI18nTextWithDefault(ISSUE_WORKLOG_UPDATED_DESC_KEY, ISSUE_WORKLOG_UPDATED_DESC),
                        UpgradeTask_Build150.TYPE_STRING, EventType.JIRA_SYSTEM_EVENT_TYPE);
                ofBizDelegator.createValue(EventType.EVENT_TYPE, eventTypeParamasMap);
            }
            else
            {
                log.warn("Not creating 'Worklog Updated' event as it already exists.  This should only happen if this upgrade task is run twice.");
            }
        }
        catch (DataAccessException e)
        {
            log.error("JIRA was unable to create the new notification event type of 'Issue Worklog Updated' with an id of 15.");
            throw e;
        }

        try
        {
            GenericValue value = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build(UpgradeTask_Build150.ID_STRING, EventType.ISSUE_WORKLOG_DELETED_ID));
            if (value == null)
            {
                // Create the notification event for the issue worklog deleted event
                Map eventTypeParamasMap = EasyMap.build(
                        UpgradeTask_Build150.ID_STRING, EventType.ISSUE_WORKLOG_DELETED_ID,
                        UpgradeTask_Build150.NAME_STRING, getI18nTextWithDefault(ISSUE_WORKLOG_DELETED_NAME_KEY, ISSUE_WORKLOG_DELETED_NAME),
                        UpgradeTask_Build150.DESC_STRING, getI18nTextWithDefault(ISSUE_WORKLOG_DELETED_DESC_KEY, ISSUE_WORKLOG_DELETED_DESC),
                        UpgradeTask_Build150.TYPE_STRING, EventType.JIRA_SYSTEM_EVENT_TYPE);
                ofBizDelegator.createValue(EventType.EVENT_TYPE, eventTypeParamasMap);
            }
            else
            {
                log.warn("Not creating 'Worklog Deleted' event as it already exists.  This should only happen if this upgrade task is run twice.");
            }
        }
        catch (DataAccessException e)
        {
            log.error("JIRA was unable to create the new notification event type of 'Issue Worklog Deleted' with an id of 16.");
            throw e;
        }

        // Clear the cache of the manager
        eventTypeManager.clearCache();
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
}
