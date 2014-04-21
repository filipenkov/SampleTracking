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

import java.util.Map;

/**
 * This upgrade task creates the notification event for the
 * {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENT_EDITED_ID} event.
 */
public class UpgradeTask_Build205 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build205.class);
    private final OfBizDelegator ofBizDelegator;
    private final EventTypeManager eventTypeManager;

    static final String ISSUE_COMMENT_EDITED_NAME_KEY = "event.type.issuecommentedited.name";
    static final String ISSUE_COMMENT_EDITED_DESC_KEY = "event.type.issuecommentedited.desc";
    static final String ISSUE_COMMENT_EDITED_NAME = "Issue Comment Edited";
    static final String ISSUE_COMMENT_EDITED_DESC = "This is the 'Issue Comment Edited' event type.";

    /**
     * Constructs a new instance with given {@link OfBizDelegator} and {@link EventTypeManager}.
     *
     * @param ofBizDelegator   OFBiz delegator
     * @param eventTypeManager event type manager
     */
    public UpgradeTask_Build205(OfBizDelegator ofBizDelegator, EventTypeManager eventTypeManager)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.eventTypeManager = eventTypeManager;
    }

    /**
     * Returns 205 as string
     *
     * @return 205 as string
     */
    public String getBuildNumber()
    {
        return "205";
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "Creates the notification event for the Issue Comment Edited event";
    }

    /**
     * Runs the core task which is to create the new Issue Comment Edited event type and update the eventTypeManager
     * with this change
     *
     * @throws DataAccessException if something goes wrong with database access
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws DataAccessException
    {
        try
        {
            // Create the notification event for the issue comment edited event
            Map eventTypeParamasMap = EasyMap.build(
                    UpgradeTask_Build150.ID_STRING, EventType.ISSUE_COMMENT_EDITED_ID,
                    UpgradeTask_Build150.NAME_STRING, getI18nTextWithDefault(ISSUE_COMMENT_EDITED_NAME_KEY, ISSUE_COMMENT_EDITED_NAME),
                    UpgradeTask_Build150.DESC_STRING, getI18nTextWithDefault(ISSUE_COMMENT_EDITED_DESC_KEY, ISSUE_COMMENT_EDITED_DESC),
                    UpgradeTask_Build150.TYPE_STRING, EventType.JIRA_SYSTEM_EVENT_TYPE);
            ofBizDelegator.createValue(EventType.EVENT_TYPE, eventTypeParamasMap);

            // Clear the cache of the manager
            eventTypeManager.clearCache();
        }
        catch (DataAccessException e)
        {
            log.error("JIRA was unable to create the new notification event type of 'Issue Comment Edited' with an id of 14.");
            throw e;
        }
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
