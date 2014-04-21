package com.atlassian.jira.user;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.NotNull;
import net.jcip.annotations.ThreadSafe;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * OfBiz implementation of {@link com.atlassian.jira.user.UserHistoryStore}
 *
 * @since v4.0
 */
@ThreadSafe
public class OfBizUserHistoryStore implements UserHistoryStore
{
    private static final Logger log = Logger.getLogger(OfBizUserHistoryStore.class);

    private static final String TABLE = "UserHistoryItem";

    private static final int DEFAULT_MAX_ITEMS = 50;

    private static final class Columns
    {
        public static final String ID = "id";
        public static final String USER = "username";   // Note: this column actually stores the user key
        public static final String TYPE = "type";
        public static final String ENTITY_ID = "entityId";
        public static final String LAST_VIEWED = "lastViewed";
        public static final String DATA = "data";
    }

    private final OfBizDelegator delegator;
    private final ApplicationProperties applicationProperties;

    public OfBizUserHistoryStore(OfBizDelegator delegator, ApplicationProperties applicationProperties)
    {
        this.delegator = delegator;
        this.applicationProperties = applicationProperties;
    }

    public void addHistoryItem(@NotNull ApplicationUser user, @NotNull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        final int numberRemoved = delegator.removeByAnd(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey(), Columns.ENTITY_ID, item.getEntityId()));
        delegator.createValue(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey(), Columns.ENTITY_ID, item.getEntityId(), Columns.LAST_VIEWED, item.getLastViewed(), Columns.DATA, item.getData()));

        // Only keep the ammount issues specified in jira-application.properties

        // Optimsation - if we removed one, the list can't be over limit
        if (numberRemoved == 0)
        {
            final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
            int maxItems = DEFAULT_MAX_ITEMS;
            try
            {
                maxItems = Integer.parseInt(maxItemsStr);
            }
            catch (NumberFormatException e)
            {
                log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
            }

            final List<GenericValue> historyItemGVs = delegator.findByAnd(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey()), EasyList.build(Columns.LAST_VIEWED + " DESC"));

            // only keep first 50 issues.
            for (int i = maxItems; i < historyItemGVs.size(); i++)
            {
                delegator.removeByAnd(TABLE, EasyMap.build(Columns.ID, historyItemGVs.get(i).getLong(Columns.ID)));
            }
        }

    }

    /**
     * Optimised method for adding a history item.  This will throw a duplicate row exception from the db if you try and
     * insert a history item that already exists.  Should only call if we are sure it doesn't exist.
     * <p/>
     * This does not expire old items or try and update existing items.  It is dumb.
     *
     * @param user The user to insert the record for
     * @param item Teh item to insert into the db
     */
    public void addHistoryItemNoChecks(@NotNull ApplicationUser user, @NotNull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        delegator.createValue(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey(), Columns.ENTITY_ID, item.getEntityId(), Columns.LAST_VIEWED, item.getLastViewed(), Columns.DATA, item.getData()));

    }

    /**
     * Optimised method for updating a record in the database.   If the record doesn't exist it will create it, otherwise just
     * update it.
     * <p/>
     * This does not expire old items or try and update existing items.  It is dumb.
     *
     * @param user The user to update the record for
     * @param item The item to update
     */
    public void updateHistoryItemNoChecks(@NotNull ApplicationUser user, @NotNull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        final List<GenericValue> list = delegator.findByAnd(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey(), Columns.ENTITY_ID, item.getEntityId()));

        if (list == null || list.size() == 0)
        {
            delegator.createValue(TABLE, EasyMap.build(Columns.TYPE, item.getType().getName(), Columns.USER, user.getKey(), Columns.ENTITY_ID, item.getEntityId(), Columns.LAST_VIEWED, item.getLastViewed(), Columns.DATA, item.getData()));
        }
        else if (list.size() == 1)
        {
            final GenericValue genericValue = list.get(0);
            genericValue.set(Columns.LAST_VIEWED, item.getLastViewed());
            try
            {
                genericValue.store();
            }
            catch (GenericEntityException e)
            {
                log.error("Exception thrown while updating user history item", e);
            }
        }
        else
        {
            // never get here;
            log.warn("Somehow there is more than one record for the following user/type/entity - " + item.toString());
        }
    }

    /**
     * Method for expiring old items.  You can actually delete any items but it is typically used to delete old records.
     *
     * @param user      The user to remove entries for
     * @param type      The type of record to remove
     * @param entityIds Teh list of entity ids to remove.
     */
    public void expireOldHistoryItems(@NotNull ApplicationUser user, @NotNull UserHistoryItem.Type type, Collection<String> entityIds)
    {
        // why oh why can't can't we bulk delete based on criteria..
        for (String entityId : entityIds)
        {
            delegator.removeByAnd(TABLE, EasyMap.build(Columns.USER, user.getKey(), Columns.TYPE, type.getName(), Columns.ENTITY_ID, entityId));
        }
    }

    @NotNull
    public List<UserHistoryItem> getHistory(@NotNull UserHistoryItem.Type type, @NotNull ApplicationUser user)
    {
        notNull("user", user);
        notNull("type", type);

        final List<GenericValue> historyItemGVs = delegator.findByAnd(TABLE, EasyMap.build(Columns.TYPE, type.getName(), Columns.USER, user.getKey()), EasyList.build(Columns.LAST_VIEWED + " DESC"));
        final List<UserHistoryItem> returnList = new ArrayList<UserHistoryItem>();

        for (GenericValue historyItemGV : historyItemGVs)
        {
            returnList.add(convertGV(historyItemGV));
        }

        return returnList;
    }


    public Set<UserHistoryItem.Type> removeHistoryForUser(@NotNull ApplicationUser user)
    {
        notNull("user", user);
        final List<GenericValue> historyItemGVs = delegator.findByAnd(TABLE, EasyMap.build(Columns.USER, user.getKey()));
        final Set<UserHistoryItem.Type> types = new HashSet<UserHistoryItem.Type>();

        if (historyItemGVs != null && !historyItemGVs.isEmpty())
        {
            for (GenericValue historyItemGV : historyItemGVs)
            {
                types.add(UserHistoryItem.Type.getInstance(historyItemGV.getString(Columns.TYPE)));
            }

            delegator.removeByAnd(TABLE, EasyMap.build(Columns.USER, user.getKey()));
        }

        return types;
    }

    private UserHistoryItem convertGV(GenericValue historyItemGV)
    {
        return new UserHistoryItem(UserHistoryItem.Type.getInstance(historyItemGV.getString(Columns.TYPE)), historyItemGV.getString(Columns.ENTITY_ID), historyItemGV.getLong(Columns.LAST_VIEWED), historyItemGV.getString(Columns.DATA));
    }


}
