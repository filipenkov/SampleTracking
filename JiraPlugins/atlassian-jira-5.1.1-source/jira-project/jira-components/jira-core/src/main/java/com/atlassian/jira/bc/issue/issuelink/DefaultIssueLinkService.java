package com.atlassian.jira.bc.issue.issuelink;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuelink.Direction;
import com.atlassian.jira.issue.issuelink.IssueLink;
import com.atlassian.jira.issue.issuelink.IssueLinkType;
import com.atlassian.jira.issue.issuelink.IssueLinks;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.ErrorCollection.Reason;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of Issue Linking Service
 *
 * @since v4.4
 */
public class DefaultIssueLinkService implements IssueLinkService
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final UserHistoryManager userHistoryManager;


    public DefaultIssueLinkService(final IssueLinkTypeManager issueLinkTypeManager, final IssueManager issueManager, final PermissionManager permissionManager, final I18nHelper.BeanFactory beanFactory, final IssueLinkManager issueLinkManager, UserHistoryManager userHistoryManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.beanFactory = beanFactory;
        this.issueLinkManager = issueLinkManager;
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public Collection<IssueLinkType> getIssueLinkTypes()
    {
        return transformIssueLinkType(issueLinkTypeManager.getIssueLinkTypes());

    }

    @Override
    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId)
    {
        final com.atlassian.jira.issue.link.IssueLink rawIssueLink = issueLinkManager.getIssueLink(sourceId, destinationId, issueLinkTypeId);

        if (rawIssueLink == null)
        {
            return null;
        }

        return new IssueLink()
        {
            @Override
            public Issue getSourceIssue()
            {
                return rawIssueLink.getSourceObject();
            }

            @Override
            public Issue getDestinationIssue()
            {
                return rawIssueLink.getDestinationObject();
            }

            @Override
            public IssueLinkType getIssueLinkType()
            {
                return transformIssueLinkType(rawIssueLink.getIssueLinkType());
            }
        };
    }


    @Override
    public IssueLinkResult getIssueLinks(User user, Issue issue)
    {
        return this.getIssueLinks(user, issue, true);
    }

    @Override
    public IssueLinkResult getIssueLinks(User user, Issue issue, boolean excludeSystemLinks)
    {
        ErrorCollection errorCollection = validateIssuePermission(user, issue, Permissions.BROWSE);
        if (!errorCollection.hasAnyErrors())
        {

            final LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, user, excludeSystemLinks);
            Set<com.atlassian.jira.issue.link.IssueLinkType> linkTypes = linkCollection.getLinkTypes();

            // build outwards link map
            Map<String, List<IssueLink>> outwardIssueLinksByName = Maps.newHashMap();
            for (com.atlassian.jira.issue.link.IssueLinkType linkType : linkTypes)
            {
                String input = linkType.getName();
                final List<Issue> list = linkCollection.getOutwardIssues(input);
                if (list != null)
                {
                    outwardIssueLinksByName.put(input, transformListToIssueLink(issue, list, linkType));
                }
            }

            // build inwards link map
            Map<String, List<IssueLink>> inwardsIssueLinksByName = Maps.newHashMap();
            for (com.atlassian.jira.issue.link.IssueLinkType linkType : linkTypes)
            {
                String input = linkType.getName();
                final List<Issue> list = linkCollection.getInwardIssues(input);
                if (list != null)
                {
                    inwardsIssueLinksByName.put(input, transformListToIssueLink(issue, list, linkType));
                }
            }

            final List<IssueLink> allIssueLinks = Lists.newArrayList();
            for (List<IssueLink> issueLinks : Iterables.concat(outwardIssueLinksByName.values(), inwardsIssueLinksByName.values()))
            {
                allIssueLinks.addAll(issueLinks);
            }
            return makeIssueLinkResult(errorCollection, issue, linkTypes, allIssueLinks, outwardIssueLinksByName, inwardsIssueLinksByName);
        }
        else
        {
            return makeIssueLinkResult(errorCollection, issue, Collections.<com.atlassian.jira.issue.link.IssueLinkType>emptyList(), Collections.<IssueLink>emptyList(), Collections.<String, List<IssueLink>>emptyMap(), Collections.<String, List<IssueLink>>emptyMap());
        }
    }

    @Override
    public AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, String linkName, Collection<String> linkKeys)
    {
        IssueLinkType linkType = matchToIssueLinkType(linkName);
        if (null == linkType)
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.name", linkName));
            return new AddIssueLinkValidationResult(user, errors, null, null, null, null);
        }
        Direction direction = linkName.equals(linkType.getInward()) ? Direction.IN : Direction.OUT;
        return validateAddIssueLinks(user, issue, linkType, direction, linkKeys, true);
    }

    @Override
    public AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, Long issueLinkTypeId, Direction direction, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        IssueLinkType linkType = resolveIssueLinkTypeById(issueLinkTypeId);
        if (null == linkType)
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.id", issueLinkTypeId));
            return new AddIssueLinkValidationResult(user, errors, null, null, null, null);
        }
        return validateAddIssueLinks(user, issue, linkType, direction, linkKeys, excludeSystemLinks);
    }

    private AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, IssueLinkType linkType, Direction direction, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        Assertions.notNull("issue", issue);
        Assertions.notNull("linkKeys", linkKeys);

        ErrorCollection errorCollection = validateIssuePermission(user, issue, Permissions.EDIT_ISSUE);
        if (!errorCollection.hasAnyErrors())
        {
            validateLinkInput(user, errorCollection, issue, linkType, linkKeys, excludeSystemLinks);
        }
        return new AddIssueLinkValidationResult(user, errorCollection, issue, linkType,  direction, linkKeys);
    }


    @Override
    public IssueLinkResult addIssueLinks(User user, AddIssueLinkValidationResult result)
    {
        notNull("result", result);

        if (!result.isValid())
        {
            throw new IllegalStateException("Cannot add issue links with invalid validation result!");
        }

        IssueLinkType linkType = result.getLinkType();
        if (linkType == null)
        {
            throw new IllegalArgumentException("Cant find issue link type '" + result.getLinkType().getName() + "'");
        }

        final Issue sourceIssue = result.getIssue();

        for (String targetIssueKey : result.getLinkKeys())
        {
            MutableIssue destinationIssue = issueManager.getIssueObject(targetIssueKey);
            if (destinationIssue == null)
            {
                throw new IllegalArgumentException("Issue with key '" + targetIssueKey + "' no longer exists!");
            }

            try
            {
                // This reflects what the old code used to do.  It matches the chosen direction and then depending on whether
                // it's outwards our inwards, it flips the issue value around to match.
                if (result.getDirection() == Direction.OUT)
                {
                    issueLinkManager.createIssueLink(sourceIssue.getId(), destinationIssue.getId(), linkType.getId(), null, result.getUser());
                }
                else
                {
                    issueLinkManager.createIssueLink(destinationIssue.getId(), sourceIssue.getId(), linkType.getId(), null, result.getUser());
                }
                userHistoryManager.addItemToHistory(UserHistoryItem.ISSUELINKTYPE, result.getUser(), String.valueOf(linkType.getId()), result.getLinkName());
            }
            catch (CreateException createE)
            {
                throw new RuntimeException(createE);
            }

        }

        return getIssueLinks(user, result.getIssue());
    }

    @Override
    public DeleteIssueLinkValidationResult validateDelete(User user, Issue issue, IssueLink issueLink)
    {
        final ErrorCollection errors = validateIssuePermission(user, issue, Permissions.LINK_ISSUE, "admin.errors.issues.no.permission.to.delete.links");

        validateLinkingEnabled(user, errors);

        if (issueLink == null)
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("admin.errors.issues.cannot.find.link"), Reason.NOT_FOUND);
        }

        return new IssueLinkService.DeleteIssueLinkValidationResult(errors, errors.hasAnyErrors() ? null : issueLink, user);
    }

    @Override
    public void delete(DeleteIssueLinkValidationResult validationResult)
    {
        notNull("validationResult", validationResult);

        if (!validationResult.isValid())
        {
            throw new IllegalStateException("You cannot delete an issue link with an invalid validation result.");
        }

        final IssueLink issueLink = validationResult.getIssueLink();
        final Long sourceId = issueLink.getSourceIssue().getId();
        final Long destinationId = issueLink.getDestinationIssue().getId();
        final Long issueLinkTypeId = issueLink.getIssueLinkType().getId();

        try
        {
            issueLinkManager.removeIssueLink(issueLinkManager.getIssueLink(sourceId, destinationId, issueLinkTypeId),
                                             validationResult.getUser());
        }
        catch (RemoveException e)
        {
            throw new RuntimeException("Issue link deletion failed" , e);
        }
    }

    private ErrorCollection validateIssuePermission(final User user, final Issue issue, final int permissionsId)
    {
        return validateIssuePermission(user, issue, permissionsId, "issuelinking.service.error.issue.no.permission");
    }

    private ErrorCollection validateIssuePermission(final User user, final Issue issue, final int permissionsId, final String errorMsgKey)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        if (issue == null)
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.issue.doesnt.exist"));
            return errors;
        }
        if (!permissionManager.hasPermission(permissionsId, issue, user))
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText(errorMsgKey, issue.getKey()));
        }
        return errors;
    }

    private void validateLinkingEnabled(final User user, final ErrorCollection errors)
    {
        final I18nHelper i18n = beanFactory.getInstance(user);
        if (!issueLinkManager.isLinkingEnabled())
        {
            errors.addErrorMessage(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), Reason.FORBIDDEN);
        }
    }
    
    private void validateLinkInput(final User user, ErrorCollection errors, Issue issue, IssueLinkType linkType, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        if (excludeSystemLinks && linkType.isSystemLinkType())
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.type", linkType.getName()));
        }
        if (linkKeys == null || linkKeys.isEmpty())
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.must.provide.issue.links"));
        }
        else
        {
            for (String linkKey : linkKeys)
            {
                if (linkKey.equalsIgnoreCase(issue.getKey()))
                {
                    errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.self.reference", linkKey));
                }
                MutableIssue linkedIssue = issueManager.getIssueObject(linkKey);
                if (linkedIssue == null)
                {
                    errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.linked.issue.doesnt.exist", linkKey));
                }
                // NOTE we don't validate that the link type requested does not already link the two issues because the
                // IssueLinkManager ignores this on creation if it already exists.
            }
        }
    }

    private IssueLinkType resolveIssueLinkTypeById(Long issueLinkTypeId)
    {
        final com.atlassian.jira.issue.link.IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(issueLinkTypeId);
        if (issueLinkType == null)
        {
            return null;
        }
        else
        {
            return transformIssueLinkType(issueLinkType);
        }
    }

    private IssueLinkResult makeIssueLinkResult(final ErrorCollection errorCollection, final Issue issue, final Collection<com.atlassian.jira.issue.link.IssueLinkType> issueLinkTypes, final Collection<IssueLink> allIssues, final Map<String, List<IssueLink>> outwardIssues, final Map<String, List<IssueLink>> inwardIssues)
    {

        final Set<IssueLinkType> transformedTypes = new HashSet<IssueLinkType>(transformIssueLinkType(issueLinkTypes));
        return new IssueLinkResult(errorCollection, transformedTypes, new IssueLinks()
        {
            @Override
            public Issue getIssue()
            {
                return issue;
            }

            @Override
            public Set<IssueLinkType> getLinkTypes()
            {
                return transformedTypes;
            }

            @Override
            public Collection<IssueLink> getOutwardIssues(String linkName)
            {
                return outwardIssues.get(linkName);
            }

            @Override
            public Collection<IssueLink> getInwardIssues(String linkName)
            {
                return inwardIssues.get(linkName);
            }

            @Override
            public Collection<IssueLink> getAllIssues()
            {
                return allIssues;
            }
        });
    }


    private IssueLinkType matchToIssueLinkType(String linkName)
    {
        Collection<com.atlassian.jira.issue.link.IssueLinkType> linkTypes = issueLinkTypeManager.getIssueLinkTypes();
        for (com.atlassian.jira.issue.link.IssueLinkType linkType : linkTypes)
        {
            if (linkName.equals(linkType.getOutward()) || linkName.equals(linkType.getInward()))
            {
                return transformIssueLinkType(linkType);
            }
        }
        return null;
    }

    private List<IssueLink> transformListToIssueLink(final Issue sourceIssue, List<Issue> destinationIssues, final com.atlassian.jira.issue.link.IssueLinkType linkType)
    {
        List<IssueLink> issueLinks = Lists.newArrayList();
        for (final Issue destinationIssue : destinationIssues)
        {
            issueLinks.add(new IssueLink()
            {
                @Override
                public Issue getSourceIssue()
                {
                    return sourceIssue;
                }

                @Override
                public Issue getDestinationIssue()
                {
                    return destinationIssue;
                }

                @Override
                public IssueLinkType getIssueLinkType()
                {
                    return transformIssueLinkType(linkType);
                }
            });
        }
        return issueLinks;
    }


    private Collection<IssueLinkType> transformIssueLinkType(Collection<com.atlassian.jira.issue.link.IssueLinkType> issueLinkTypes)
    {
        List<IssueLinkType> transformedList = new ArrayList<IssueLinkType>(issueLinkTypes.size());
        for (final com.atlassian.jira.issue.link.IssueLinkType rawIssueLinkType : issueLinkTypes)
        {
            transformedList.add(transformIssueLinkType(rawIssueLinkType));
        }
        return Collections.unmodifiableCollection(transformedList);
    }

    private IssueLinkType transformIssueLinkType(final com.atlassian.jira.issue.link.IssueLinkType rawIssueLinkType)
    {
        return new IssueLinkType()
        {
            @Override
            public Long getId()
            {
                return rawIssueLinkType.getId();
            }

            @Override
            public String getName()
            {
                return rawIssueLinkType.getName();
            }

            @Override
            public String getOutward()
            {
                return rawIssueLinkType.getOutward();
            }

            @Override
            public String getInward()
            {
                return rawIssueLinkType.getInward();
            }

            @Override
            public String getStyle()
            {
                return rawIssueLinkType.getStyle();
            }

            @Override
            public boolean isSubTaskLinkType()
            {
                return rawIssueLinkType.isSubTaskLinkType();
            }

            @Override
            public boolean isSystemLinkType()
            {
                return rawIssueLinkType.isSystemLinkType();
            }
        };
    }
}
