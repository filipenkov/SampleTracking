package com.atlassian.jira.plugins.importer.views;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.issueview.IssueView;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.sample.SampleData;
import com.atlassian.jira.plugins.importer.sample.SampleDataImporterImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.codehaus.jackson.map.ObjectMapper;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Check {@link com.atlassian.jira.issue.views.IssueXMLView} to see how to get custom fields for an issue.
 */
public class IssueJsonView implements IssueView
{
    protected final FieldLayoutManager fieldLayoutManager;
    protected final CommentManager commentManager;
    protected final WatcherManager watcherManager;
    protected final VoteManager voteManager;
    protected final UserUtil userUtil;
    protected final PermissionManager permissionManager;
    protected final JiraAuthenticationContext authenticationContext;

    protected final HashSet<String> users;
    protected final ObjectMapper mapper;

    public IssueJsonView(FieldLayoutManager fieldLayoutManager, CommentManager commentManager,
                         WatcherManager watcherManager, VoteManager voteManager, UserUtil userUtil,
                         PermissionManager permissionManager, JiraAuthenticationContext authenticationContext)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.commentManager = commentManager;
        this.watcherManager = watcherManager;
        this.voteManager = voteManager;
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.mapper = SampleDataImporterImpl.getObjectMapper();
        this.users = Sets.newHashSet();
    }

    @Override
    public void init(IssueViewModuleDescriptor issueViewModuleDescriptor) {
        users.clear();
    }

    public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams)
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser())) {
            return authenticationContext.getI18nHelper().getText("jira-importer-plugin.must.be.admin");
        }

        final ExternalProject externalProject = convertProject(issue.getProjectObject());
        externalProject.setIssues(Lists.newArrayList(convertIssueToExternalIssue(issue)));

        final SampleData data = new SampleData(null, Sets.newHashSet(externalProject), convertUsers(users));

        try {
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Set<ExternalUser> convertUsers(Set<String> users) {
        return Sets.newHashSet(Collections2.transform(users, new Function<String, ExternalUser>() {
            @Override
            public ExternalUser apply(@Nullable String input) {
                final User user = userUtil.getUserObject(input);
                final ExternalUser eu = new ExternalUser(user.getName(), user.getDisplayName(), user.getEmailAddress());
                eu.setGroups(userUtil.getGroupNamesForUser(user.getName()));
                return eu;
            }
        }));
    }

    @Override
    public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams) {
        // nothing to do here
    }

    @Nullable
    protected String trackUser(@Nullable String user) {
        if (users != null) {
            users.add(user);
        }
        return user;
    }

    public ExternalProject convertProject(@Nonnull Project project) {
        final ExternalProject ep = new ExternalProject(project.getName(), project.getKey(), trackUser(project.getLeadUserName()));
        ep.setAssigneeType(project.getAssigneeType());
        if (project.getProjectComponents() != null) {
            ep.setComponents(Collections2.transform(project.getProjectComponents(), new Function<ProjectComponent, ExternalComponent>() {
                @Override
                public ExternalComponent apply(@Nullable ProjectComponent input) {
                    return new ExternalComponent(input.getName(), null, trackUser(input.getLead()), input.getDescription());
                }
            }));
        }
        ep.setDescription(project.getDescription());
        if (project.getProjectCategory() != null) {
            ep.setProjectCategoryName(project.getProjectCategory().getString("name"));
        }

        if (project.getVersions() != null) {
            ep.setVersions(Collections2.transform(project.getVersions(), new Function<Version, ExternalVersion>() {
                @Override
                public ExternalVersion apply(@Nullable Version input) {
                    final ExternalVersion ev = new ExternalVersion();
                    ev.setDescription(input.getDescription());
                    ev.setReleased(input.isReleased());
                    ev.setReleaseDate(input.getReleaseDate());
                    ev.setArchived(input.isArchived());
                    ev.setName(input.getName());
                    return ev;
                }
            }));
        }
        return ep;
    }

    public ExternalIssue convertIssueToExternalIssue(Issue issue)
    {
        final Function<Version, String> versionStringFunction = new Function<Version, String>() {
            @Override
            public String apply(@Nullable Version input) {
                return input.getName();
            }
        };

        final ExternalIssue ei = new ExternalIssue();
        if (issue.getStatusObject() != null) {
            ei.setStatus(issue.getStatusObject().getName());
        }
        if (issue.getAffectedVersions() != null) {
            ei.setAffectedVersions(Collections2.transform(issue.getAffectedVersions(), versionStringFunction));
        }
        if (issue.getFixVersions() != null) {
            ei.setFixedVersions(Collections2.transform(issue.getFixVersions(), versionStringFunction));
        }
        if (issue.getAssignee() != null) {
            ei.setAssignee(trackUser(issue.getAssignee().getName()));
        }
        ei.setCreated(issue.getCreated());
        ei.setUpdated(issue.getUpdated());
        ei.setDuedate(issue.getDueDate());
        ei.setResolutionDate(issue.getResolutionDate());
        ei.setDescription(issue.getDescription());
        ei.setEnvironment(issue.getEnvironment());
        ei.setTimeSpent(issue.getTimeSpent());
        ei.setComponents(Collections2.transform(issue.getComponents(), new Function<GenericValue, String>() {
            @Override
            public String apply(GenericValue input) {
                return input.getString("name");
            }
        }));
        ei.setEstimate(issue.getEstimate());
        ei.setOriginalEstimate(issue.getOriginalEstimate());
        if (issue.getIssueTypeObject() != null) {
            ei.setIssueType(issue.getIssueTypeObject().getName());
        }
        if (issue.getLabels() != null) {
            ei.setLabels(Collections2.transform(issue.getLabels(), new Function<Label, String>() {
                @Override
                public String apply(Label input) {
                    return input.getLabel();
                }
            }));
        }
        ei.setSummary(issue.getSummary());
        if (issue.getReporter() != null) {
            ei.setReporter(trackUser(issue.getReporter().getName()));
        }
        if (issue.getPriorityObject() != null) {
            ei.setPriority(issue.getPriorityObject().getName());
        }

        final List<Comment> comments = commentManager.getComments(issue);
        if (comments != null) {
            ei.setComments(Collections2.transform(comments, new Function<Comment, ExternalComment>() {
                @Override
                public ExternalComment apply(@Nullable Comment input) {
                    return new ExternalComment(input.getBody(), trackUser(input.getAuthor()), input.getCreated());
                }
            }));
        }

        final List<String> watchers = watcherManager.getCurrentWatcherUsernames(issue);
        if (watchers != null) {
            ei.setWatchers(watchers);
            users.addAll(watchers);
        }

        final Collection<String> voters = voteManager.getVoterUsernames(issue);
        if (voters != null) {
            ei.setVoters(voters);
            users.addAll(voters);
        }

        return ei;
    }

}

