package com.atlassian.jira.issue.link;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.CollectionReorderer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.atlassian.jira.issue.link.IssueLink.DESTINATION_ID_LINK_NAME;
import static com.atlassian.jira.issue.link.IssueLink.LINK_TYPE_ID_FIELD_NAME;
import static com.atlassian.jira.issue.link.IssueLink.SEQUENCE_FIELD_NAME;
import static com.atlassian.jira.issue.link.IssueLink.SOURCE_ID_FIELD_NAME;

public class DefaultIssueLinkManager implements IssueLinkManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultIssueLinkManager.class);

    private final OfBizDelegator delegator;
    private final IssueLinkCreator issueLinkCreator;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final CollectionReorderer collectionReorderer;
    private final IssueUpdater issueUpdater;
    private final IssueIndexManager issueIndexManager;
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private Map<Map, List<GenericValue>> cache;

    public DefaultIssueLinkManager(OfBizDelegator genericDelegator, IssueLinkCreator issueLinkCreator,
            IssueLinkTypeManager issueLinkTypeManager, CollectionReorderer collectionReorderer,
            IssueUpdater issueUpdater, IssueIndexManager issueIndexManager,
            ApplicationProperties applicationProperties, final EventPublisher eventPublisher)
    {
        this.delegator = genericDelegator;
        this.issueLinkCreator = issueLinkCreator;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.collectionReorderer = collectionReorderer;
        this.issueUpdater = issueUpdater;
        this.issueIndexManager = issueIndexManager;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.cache = Collections.synchronizedMap(new LRUMap(1000));
    }


    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    public void createIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, Long sequence, User remoteUser)
            throws CreateException
    {
        //if the link is already created, then don't do anything
        if (getIssueLink(sourceId, destinationId, issueLinkTypeId) != null)
        {
            return;
        }

        IssueLink issueLink = null;
        try
        {
            issueLink = storeIssueLink(sourceId, destinationId, issueLinkTypeId, sequence);

            final IssueLinkType issueLinkType = issueLink.getIssueLinkType();
            // Create change record only if the issue link is not of a system issue link type
            if (!issueLinkType.isSystemLinkType())
            {
                // Manually do our changelogs and issue updates as we have two issues
                createCreateIssueLinkChangeItems(issueLink, issueLinkType, remoteUser);
            }
        }
        finally
        {
            // Clear the cache before we reindex - Plugin developers may add link info to the index,
            // and we don't want to serve them stale cache data. See JRA-16199.
            clearCache();
            // We always need to reindex linked Issues - the updated date of both issues is updated. see JRA-7156
            if (issueLink != null)
            {
                reindexLinkedIssues(issueLink);
            }
        }
    }

    public void createIssueLink(final Long sourceIssueId, final Long destinationIssueId, final Long issueLinkTypeId, final Long sequence, final com.opensymphony.user.User remoteUser)
            throws CreateException
    {
        createIssueLink(sourceIssueId, destinationIssueId, issueLinkTypeId, sequence, (User) remoteUser);
    }

    protected void reindexLinkedIssues(IssueLink issueLink)
    {
        try
        {
            issueIndexManager.reIndex(issueLink.getSourceObject());
            issueIndexManager.reIndex(issueLink.getDestinationObject());
        }
        catch (IndexException e)
        {
            throw new NestableRuntimeException(e);
        }
    }

    private void createCreateIssueLinkChangeItems(IssueLink issueLink, IssueLinkType issueLinkType, User remoteUser)
            throws CreateException
    {
        final Issue source = issueLink.getSourceObject();
        final Issue destination = issueLink.getDestinationObject();

        try
        {
            // Create change item for source issue
            ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, destination.getKey(),
                    "This issue " + issueLinkType.getOutward() + " " + destination.getKey());
            createChangeItem(source.getGenericValue(), cib, remoteUser);

            // Create change item for destination issue
            cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, source.getKey(),
                    "This issue " + issueLinkType.getInward() + " " + source.getKey());
            createChangeItem(destination.getGenericValue(), cib, remoteUser);
        }
        catch (Exception e)
        {
            throw new CreateException("Error occurred while creating change item for creating issue link between entity with id '" +
                    issueLink.getSourceId() + "' and entity with id '" + issueLink.getDestinationId() + "'.", e);
        }
    }

    private void createChangeItem(GenericValue issue, ChangeItemBean changeItemBean, User remoteUser)
            throws JiraException
    {
        // Note the event will not be dispatched. The issue is updated however so we pass ISSUE_UPDATED constant
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue, issue, EventType.ISSUE_UPDATED_ID, remoteUser);
        issueUpdateBean.setDispatchEvent(false);
        issueUpdateBean.setChangeItems(EasyList.build(changeItemBean));
        issueUpdater.doUpdate(issueUpdateBean, true);
    }

    public void removeIssueLink(IssueLink issueLink, User remoteUser) throws RemoveException
    {
        if (issueLink == null)
        {
            throw new IllegalArgumentException("Link cannot be null");
        }

        try
        {
            // Delete the link type from the database
            delegator.removeByAnd(OfBizDelegator.ISSUE_LINK, EasyMap.build("id", issueLink.getId()));

            if (log.isDebugEnabled())
            {
                log.debug("Deleted link with id '" + issueLink.getId() + "'.");
            }

            // Create change record only if the issue link is not of a system issue link type
            final IssueLinkType issueLinkType = issueLink.getIssueLinkType();
            if (!issueLinkType.isSystemLinkType())
            {
                createRemoveIssueLinkChangeItems(issueLink, issueLinkType, remoteUser);
            }
        }
        finally
        {
            // Clear the cache before we reindex - Plugin developers may add link info to the index,
            // and we don't want to serve them stale cache data. See JRA-16199.
            clearCache();
            // We always need to reindex after removing a link - the updated date of both issues is updated.
            // See JRA-7156, and JRA-14877
            reindexLinkedIssues(issueLink);
        }
    }

    public void removeIssueLink(final IssueLink issueLink, final com.opensymphony.user.User remoteUser)
            throws RemoveException
    {
        removeIssueLink(issueLink, (User) remoteUser);
    }

    private void createRemoveIssueLinkChangeItems(IssueLink issueLink, IssueLinkType issueLinkType, User remoteUser)
            throws RemoveException
    {
        try
        {
            final Issue source = issueLink.getSourceObject();
            final Issue destination = issueLink.getDestinationObject();

            // Create change item for source issue
            ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", destination.getKey(),
                    "This issue " + issueLinkType.getOutward() + " " + destination.getKey(), null, null);
            createChangeItem(source.getGenericValue(), cib, remoteUser);

            // Create change item for destination issue
            cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", source.getKey(),
                    "This issue " + issueLinkType.getInward() + " " + source.getKey(), null, null);
            createChangeItem(destination.getGenericValue(), cib, remoteUser);
        }
        catch (Exception e)
        {
            throw new RemoveException("Error occurred while creating change item for removing issue issue link with id '" + issueLink.getId() + "'.", e);
        }
    }

    public int removeIssueLinks(GenericValue issue, User remoteUser) throws RemoveException
    {
        if (issue == null)
        {
            return 0;
        }

        List<IssueLink> outwardLinks = getOutwardLinks(issue.getLong("id"));
        deleteIssueLinksFromIssue(outwardLinks, remoteUser);
        int totalLinksDeleted = outwardLinks.size();
        if (log.isDebugEnabled())
        {
            log.debug("Deleted " + outwardLinks.size() + " outward links from issue " + issue.getString("key"));
        }

        List<IssueLink> inwardLinks = getInwardLinks(issue.getLong("id"));
        deleteIssueLinksFromIssue(inwardLinks, remoteUser);
        totalLinksDeleted += inwardLinks.size();
        if (log.isDebugEnabled())
        {
            log.debug("Deleted " + inwardLinks.size() + " inward links from issue " + issue.getString("key"));
        }

        if (totalLinksDeleted > 0)
        {
            clearCache();
        }

        return totalLinksDeleted;
    }

    public int removeIssueLinks(final GenericValue issue, final com.opensymphony.user.User remoteUser)
            throws RemoveException
    {
        return removeIssueLinks(issue, (User) remoteUser);
    }

    private void deleteIssueLinksFromIssue(List<IssueLink> issueLinks, User remoteUser) throws RemoveException
    {
        if (issueLinks != null)
        {
            for (final IssueLink issueLink : issueLinks)
            {
                removeIssueLink(issueLink, remoteUser);
            }
        }
    }

    public LinkCollection getLinkCollection(GenericValue issue, com.opensymphony.user.User remoteUser)
    {
        Set<IssueLinkType> linkTypes = new TreeSet<IssueLinkType>();
        Map<String, List<Issue>> outwardLinkMap = new HashMap<String, List<Issue>>();
        Long issueId = issue.getLong("id");
        Collection<IssueLink> outwardLinks = getOutwardLinks(issueId);

        if (outwardLinks != null)
        {
            for (final IssueLink issueLink : outwardLinks)
            {
                IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(issueLink.getLinkTypeId());

                if (!issueLinkType.isSystemLinkType())
                {
                    linkTypes.add(issueLinkType);

                    Issue linkedIssue = issueLink.getDestinationObject();
                    storeInLinkMap(outwardLinkMap, issueLinkType.getName(), linkedIssue);
                }
            }
        }

        Collection<IssueLink> inwardLinks = getInwardLinks(issueId);
        Map<String, List<Issue>> inwardLinkMap = new HashMap<String, List<Issue>>();

        if (inwardLinks != null)
        {
            for (final IssueLink issueLink : inwardLinks)
            {
                IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(issueLink.getLinkTypeId());

                if (!issueLinkType.isSystemLinkType())
                {
                    linkTypes.add(issueLinkType);

                    Issue linkedIssue = issueLink.getSourceObject();
                    storeInLinkMap(inwardLinkMap, issueLinkType.getName(), linkedIssue);
                }
            }
        }

        return new LinkCollection(issueId, linkTypes, outwardLinkMap, inwardLinkMap, remoteUser, applicationProperties);
    }

    public LinkCollection getLinkCollection(final Issue issue, final User remoteUser)
    {
        return _getLinkCollection(issue, remoteUser, false, true);
    }

    @Override
    public LinkCollection getLinkCollection(Issue issue, User remoteUser, boolean excludeSystemLinks)
    {
        return _getLinkCollection(issue, remoteUser, false, excludeSystemLinks);
    }

    public LinkCollection getLinkCollection(final Issue issue, final com.opensymphony.user.User remoteUser)
    {
        return getLinkCollection(issue, (User) remoteUser);
    }

    public LinkCollection getLinkCollectionOverrideSecurity(final Issue issue)
    {
        return _getLinkCollection(issue, null, true, true);
    }

    private LinkCollection _getLinkCollection(final Issue issue, final User remoteUser, final boolean overrideSecurity, boolean excludeSystemLinks)
    {
        Set<IssueLinkType> linkTypes = new TreeSet<IssueLinkType>();
        Map<String, List<Issue>> outwardLinkMap = new HashMap<String, List<Issue>>();
        Collection<IssueLink> outwardLinks = getOutwardLinks(issue.getId());

        if (outwardLinks != null)
        {
            for (final IssueLink issueLink : outwardLinks)
            {
                IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(issueLink.getLinkTypeId(), excludeSystemLinks);

                if (!excludeSystemLinks || !issueLinkType.isSystemLinkType())
                {
                    linkTypes.add(issueLinkType);

                    Issue linkedIssue = issueLink.getDestinationObject();
                    storeInLinkMap(outwardLinkMap, issueLinkType.getName(), linkedIssue);
                }
            }
        }

        Collection<IssueLink> inwardLinks = getInwardLinks(issue.getId());
        Map<String, List<Issue>> inwardLinkMap = new HashMap<String, List<Issue>>();

        if (inwardLinks != null)
        {
            for (final IssueLink issueLink : inwardLinks)
            {
                IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(issueLink.getLinkTypeId(), excludeSystemLinks);

                if (!excludeSystemLinks || !issueLinkType.isSystemLinkType())
                {
                    linkTypes.add(issueLinkType);

                    Issue linkedIssue = issueLink.getSourceObject();
                    storeInLinkMap(inwardLinkMap, issueLinkType.getName(), linkedIssue);
                }
            }
        }

        return new LinkCollection(issue.getId(), linkTypes, outwardLinkMap, inwardLinkMap, remoteUser, overrideSecurity, applicationProperties);
    }

    public List<IssueLink> getOutwardLinks(Long sourceId)
    {
        final List<GenericValue> issueLinkGVs = getLinks(EasyMap.build("source", sourceId));
        return buildIssueLinks(issueLinkGVs);
    }

    public List<IssueLink> getInwardLinks(Long id)
    {
        final List<GenericValue> issueLinkGVs = getLinks(EasyMap.build("destination", id));
        return buildIssueLinks(issueLinkGVs);
    }

    public void moveIssueLink(List<IssueLink> issueLinks, Long currentSequence, Long sequence)
    {
        if (currentSequence == null)
        {
            throw new IllegalArgumentException("Current sequence cannot be null.");
        }

        if (sequence == null)
        {
            throw new IllegalArgumentException("Sequence cannot be null.");
        }

        final int currentIndex = currentSequence.intValue();
        final int index = sequence.intValue();

        collectionReorderer.moveToPosition(issueLinks, currentIndex, index);

        resetSequences(issueLinks);
    }

    public void resetSequences(List<IssueLink> issueLinks)
    {
        // Recalculate sequences
        recalculateSequences(issueLinks);

        // Store the list of sub-task issue types (will clear caches)
        storeIssueLinks(issueLinks);
    }

    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId)
    {
        final GenericValue linkGV = EntityUtil.getFirst(getLinks(EasyMap.build(SOURCE_ID_FIELD_NAME, sourceId, DESTINATION_ID_LINK_NAME, destinationId, LINK_TYPE_ID_FIELD_NAME, issueLinkTypeId)));
        if (linkGV == null)
        {
            return null;
        }
        return buildIssueLink(linkGV);
    }

    public Collection<IssueLink> getIssueLinks(Long issueLinkTypeId)
    {
        return buildIssueLinks(getLinks(EasyMap.build(LINK_TYPE_ID_FIELD_NAME, issueLinkTypeId)));
    }

    public void changeIssueLinkType(IssueLink issueLink, IssueLinkType swapLinkType, User remoteUser)
            throws RemoveException
    {
        final IssueLinkType oldIssueLinkType = issueLink.getIssueLinkType();
        if (!oldIssueLinkType.isSystemLinkType() && swapLinkType.isSystemLinkType())
        {
            log.warn("Changing non-system link type to a system link type.");
        }
        else if (oldIssueLinkType.isSystemLinkType() && !swapLinkType.isSystemLinkType())
        {
            log.warn("Changing system link type to a non-system link type.");
        }

        issueLink.setIssueLinkType(swapLinkType);

        // If the link we are swaping from is not a system link type, it means its creation should have
        // been recorded in 'change history', so we should update change history here.
        if (!oldIssueLinkType.isSystemLinkType())
        {
            try
            {
                createRemoveIssueLinkChangeItems(issueLink, oldIssueLinkType, remoteUser);
                createCreateIssueLinkChangeItems(issueLink, swapLinkType, remoteUser);
            }
            catch (Exception e)
            {
                throw new RemoveException("Error occurred while removing issue changing issue link type for issue link with id '" + issueLink.getLong("id") + "'.");
            }
        }

        issueLink.store();
        clearCache();
    }

    public void changeIssueLinkType(final IssueLink issueLink, final IssueLinkType swapLinkType, final com.opensymphony.user.User remoteUser)
            throws RemoveException
    {
        changeIssueLinkType(issueLink, swapLinkType, (User) remoteUser);
    }

    public boolean isLinkingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
    }

    private IssueLink storeIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, Long sequence)
    {
        // create the outward link from issue -> destination
        try
        {
            Map fields = EasyMap.build(SOURCE_ID_FIELD_NAME, sourceId, DESTINATION_ID_LINK_NAME, destinationId, LINK_TYPE_ID_FIELD_NAME, issueLinkTypeId, SEQUENCE_FIELD_NAME, sequence);
            return buildIssueLink(delegator.createValue(OfBizDelegator.ISSUE_LINK, fields));
        }
        finally
        {
            clearCache();
        }
    }

    private void storeIssueLinks(Collection<IssueLink> issueLinks)
    {
        try
        {
            for (final IssueLink issueLink : issueLinks)
            {
                issueLink.store();
            }
        }
        finally
        {
            clearCache();
        }
    }

    private void recalculateSequences(List<IssueLink> issueLinks)
    {
        long i = 0;
        for (final IssueLink issueLink : issueLinks)
        {
            issueLink.setSequence(i);
            i++;
        }
    }

    private List<IssueLink> buildIssueLinks(final Collection<GenericValue> issueLinkGVs)
    {
        List<IssueLink> issueLinks = new ArrayList<IssueLink>();
        for (final GenericValue issueLinkGV : issueLinkGVs)
        {
            issueLinks.add(buildIssueLink(issueLinkGV));
        }
        return issueLinks;
    }


    private IssueLink buildIssueLink(GenericValue issueLinkGV)
    {
        return issueLinkCreator.createIssueLink(issueLinkGV);
    }

    private void storeInLinkMap(Map<String, List<Issue>> linkMap, String linkTypeName, Issue linkedIssue)
    {
        List<Issue> matchingLinks = linkMap.get(linkTypeName);

        if (matchingLinks == null)
        {
            matchingLinks = new ArrayList<Issue>();
            linkMap.put(linkTypeName, matchingLinks);
        }

        matchingLinks.add(linkedIssue);
    }

    private List<GenericValue> getLinks(Map criteria)
    {
        List<GenericValue> result = cache.get(criteria);

        if (result == null)
        {
            result = delegator.findByAnd(OfBizDelegator.ISSUE_LINK, criteria);

            if (result == null)
            {
                result = Collections.emptyList();
            }
            if (log.isDebugEnabled())
            {
                log.debug("Caching " + result.size() + " links for query criteria: " + criteria);
            }

            cache.put(criteria, result);
        }

        return result;
    }

    public void clearCache()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Clearing issue link cache, had " + cache.size() + " entries");
        }
        cache.clear();
    }
}
