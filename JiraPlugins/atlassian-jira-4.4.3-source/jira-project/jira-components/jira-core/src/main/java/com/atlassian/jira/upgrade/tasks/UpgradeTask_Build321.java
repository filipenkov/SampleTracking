package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.query.QueryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Make all filters favourites of the owner
 *
 * @since v3.13
 */
public class UpgradeTask_Build321 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build321.class);

    private static final class Column
    {
        private static final String ID = "id";
        private static final String AUTHOR = "author";
        private static final String USER = "user";
        private static final String GROUP = "group";
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String FAV_COUNT = "favCount";
    }

    private final OfBizDelegator delegator;
    private final ShareManager shareManager;

    public UpgradeTask_Build321(final OfBizDelegator delegator, final ShareManager shareManager)
    {
        this.delegator = delegator;
        this.shareManager = shareManager;
    }

    public String getBuildNumber()
    {
        return "321";
    }

    public String getShortDescription()
    {
        return "Initialise Share Permissions.  Move old shares to new shares.";
    }

    public void doUpgrade(boolean setupMode)
    {
        final List searchrequestList = delegator.findByCondition(SearchRequest.ENTITY_TYPE.getName(), null, EasyList.build(Column.ID, Column.AUTHOR,
            Column.USER, Column.GROUP), null);

        if (searchrequestList == null)
        {
            return;
        }

        for (final Iterator iterator = searchrequestList.iterator(); iterator.hasNext();)
        {
            final GenericValue searchRequestGV = (GenericValue) iterator.next();
            final Long id = searchRequestGV.getLong(Column.ID);

            // This will always be private on first run of upgrade task as permissions have not been converted into expected format.
            // On second run, it will return global or group if appropriate permissions were set.  If filter is private, we don't do anything anyway.
            final SharedEntity.SharePermissions sharePermissions;
            try
            {
                sharePermissions = shareManager.getSharePermissions(new SharedEntity.Identifier(searchRequestGV.getLong(Column.ID), SearchRequest.ENTITY_TYPE, searchRequestGV.getString(Column.AUTHOR)));
            }
            catch (Exception e)
            {
                UpgradeTask_Build321.log.warn("SearchReqeust '" + id + "' share permissions could not be retrieved.", e);
                continue;
            }

            if (sharePermissions != null && sharePermissions.isPrivate())
            {
                if (StringUtils.isEmpty(searchRequestGV.getString(Column.USER)))
                {
                    final Set <SharePermission>permissions = new HashSet<SharePermission>();

                    if (StringUtils.isNotEmpty(searchRequestGV.getString(Column.GROUP)))
                    {
                        // Shared with Group
                        permissions.add(new SharePermissionImpl(GroupShareType.TYPE, searchRequestGV.getString(Column.GROUP), null));
                    }
                    else
                    {
                        // GlobalShare
                        permissions.add(new SharePermissionImpl(GlobalShareType.TYPE, null, null));
                    }

                    try
                    {
                        final String userName = searchRequestGV.getString(Column.AUTHOR);
                        final String name = searchRequestGV.getString(Column.NAME);
                        final String description = searchRequestGV.getString(Column.DESCRIPTION);
                        final Long requestId = searchRequestGV.getLong(Column.ID);
                        final Long favCount = searchRequestGV.getLong(Column.FAV_COUNT);
                        final SearchRequest request = new SearchRequest(new QueryImpl(), userName, name, description, requestId, ((favCount == null) ? 0L : favCount));
                        request.setPermissions(new SharedEntity.SharePermissions(permissions));
                        shareManager.updateSharePermissions(request);
                    }
                    catch (final Exception e)
                    {
                        UpgradeTask_Build321.log.warn(
                            "SearchReqeust '" + searchRequestGV.getString(Column.NAME) + "' for user '" + searchRequestGV.getString(Column.USER) + "' could not be saved.", e);
                    }
                }
            }
            else
            {
                UpgradeTask_Build321.log.warn("SearchReqeust '" + id + "' already has permissions in new format.  Upgrade Task may have been run twice.  NO New perms for SearchRequest will be added.");
            }
        }
    }
}