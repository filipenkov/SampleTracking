package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.favourites.OfBizFavouritesStore;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

/**
 * Make all filters favourites of the owner
 *
 * @since v3.13
 */
public class UpgradeTask_Build319 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build319.class);
    private final GenericDelegator delegator;
    private final OfBizFavouritesStore favouritesStore;

    public UpgradeTask_Build319(final GenericDelegator delegator, final OfBizFavouritesStore favouritesStore)
    {
        this.delegator = delegator;
        this.favouritesStore = favouritesStore;
    }

    public String getBuildNumber()
    {
        return "319";
    }

    public String getShortDescription()
    {
        return "Initialise favourite filters - make each filter a favourite of its owner.";
    }

    public void doUpgrade(boolean setupMode)
    {
        final List searchrequestList;
        try
        {
            searchrequestList = delegator.findByCondition("SearchRequest", null, EasyList.build("id", "author", "favCount"), null);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving filters.", e);
        }

        for (final Iterator iterator = searchrequestList.iterator(); iterator.hasNext();)
        {
            Long id = null;
            String authorName = null;
            try
            {
                final GenericValue searchRequestGV = (GenericValue) iterator.next();

                id = searchRequestGV.getLong("id");
                authorName = searchRequestGV.getString("author");
                if (authorName != null)
                {
                    final SharedEntity request = getFilter(id, authorName);
                    if (request != null)
                    {
                        addAsFavourite(authorName, request);
                        // Increment the fav count
                        Long favCount = searchRequestGV.getLong("favCount");
                        if (favCount == null)
                        {
                            favCount = 1L;
                        }
                        else
                        {
                            favCount++;
                        }
                        searchRequestGV.set("favCount", favCount);
                        try
                        {
                            searchRequestGV.store();
                        }
                        catch (GenericEntityException e)
                        {
                            throw new DataAccessException(e);
                        }
                    }
                }
                else
                {
                    log.warn("Filter with id '" + id + "' has no author.");
                }
            }
            catch (final DataAccessException dae)
            {
                // lets ignore this and move onto the next one.
                final String errMsg = "Error occurred while getting request or adding favourite for user '" + authorName + "' for filter '" + id + "'";
                if (log.isDebugEnabled())
                {
                    log.debug(errMsg, dae);
                }
                else
                {
                    log.warn(errMsg);
                }
            }
        }

    }

    SharedEntity getFilter(Long searchRequestId, String author)
    {
        return new SharedEntity.Identifier(searchRequestId, SearchRequest.ENTITY_TYPE, author);
    }

    boolean addAsFavourite(final String author, final SharedEntity request)
    {
        return favouritesStore.addFavourite(author, request);
    }

}