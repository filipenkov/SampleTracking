package com.atlassian.streams.jira;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.spi.EntityIdentifier;
import com.atlassian.streams.spi.StreamsEntityAssociationProvider;

import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.project;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ISSUE_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class JiraEntityAssociationProvider implements StreamsEntityAssociationProvider
{
    /**
     * Regex to parse URIs.
     * 
     * Group 1: project key, e.g. STRM
     * Group 2: issue key suffix (makes complete issue key when appended to project key), e.g. -123
     * Group 3: request parameters and hash
     */
    private static final Pattern PATTERN = Pattern.compile("([^-\\?#]+)(-?[^\\?#]*)(.*)");
    private static final String BROWSE = "/browse/";
    
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;

    public JiraEntityAssociationProvider(ApplicationProperties applicationProperties, 
            PermissionManager permissionManager, 
            ProjectManager projectManager, 
            IssueManager issueManager,
            JiraAuthenticationContext authenticationContext)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.permissionManager = checkNotNull(permissionManager, "permissionManager");
        this.projectManager = checkNotNull(projectManager, "projectManager");
        this.issueManager = checkNotNull(issueManager, "issueManager");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
    }
    
    @Override
    public Iterable<EntityIdentifier> getEntityIdentifiers(URI target)
    {
        String targetStr = target.toString();
        if (target.isAbsolute())
        {
            //the target URI must reference an entity on this server
            if (!targetStr.startsWith(applicationProperties.getBaseUrl() + BROWSE))
            {
                return ImmutableList.of();
            }
            
            String suffix = targetStr.substring((applicationProperties.getBaseUrl()).length() + BROWSE.length());
            return matchEntities(suffix);
        }
        else
        {
            return matchEntities(targetStr);
        }
    }

    @Override
    public Option<URI> getEntityURI(EntityIdentifier identifier)
    {
        if (identifier.getType().equals(project().iri())
            || identifier.getType().equals(issue().iri()))
        {
            return some(URI.create(applicationProperties.getBaseUrl() + BROWSE + identifier.getValue()));
        }
        return none();
    }

    @Override
    public Option<String> getFilterKey(EntityIdentifier identifier)
    {
        if (identifier.getType().equals(project().iri()))
        {
            return some(PROJECT_KEY);
        }
        else if (identifier.getType().equals(issue().iri()))
        {
            return some(ISSUE_KEY.getKey());
        }
        return none();
    }
    
    @Override
    public Option<Boolean> getCurrentUserViewPermission(EntityIdentifier identifier)
    {
        return getCurrentUserPermission(identifier, Permissions.BROWSE);
    }

    @Override
    public Option<Boolean> getCurrentUserEditPermission(EntityIdentifier identifier)
    {
        if (identifier.getType().equals(issue().iri()))
        {
            // JIRA only defines an Edit permission for issues, not for projects
            return getCurrentUserPermission(identifier, Permissions.EDIT_ISSUE);
        }
        return getCurrentUserViewPermission(identifier);
    }
    
    private Option<Boolean> getCurrentUserPermission(EntityIdentifier identifier, int permission)
    {
        User user = authenticationContext.getLoggedInUser();
        if (user != null)
        {
            if (identifier.getType().equals(issue().iri()))
            {
                Issue issue = issueManager.getIssueObject(identifier.getValue());
                if (issue != null)
                {
                    return some(Boolean.valueOf(permissionManager.hasPermission(permission, issue, user)));
                }
            }
            else if (identifier.getType().equals(project().iri()))
            {
                Project project = projectManager.getProjectObjByKeyIgnoreCase(identifier.getValue());
                if (project != null)
                {
                    return some(Boolean.valueOf(permissionManager.hasPermission(permission, project, user)));
                }
            }
        }
        return none();
    }
    
    private Iterable<EntityIdentifier> matchEntities(String input)
    {
        Matcher matcher = PATTERN.matcher(input);
        
        //the target URI is of an unknown format
        if (!matcher.matches())
        {
            return ImmutableList.of();
        }
        
        String projectKey = matcher.group(1);
        String issueKeySuffix = matcher.group(2);
        ImmutableList.Builder<EntityIdentifier> identifiers = ImmutableList.builder();

        //if there is an issue identifier, make it the first item in the returned list, to
        //indicate that it is the preferred target        
        if (isNotEmpty(issueKeySuffix))
        {
            //add the issue identifier if visible to the current user
            String issueKey = projectKey + issueKeySuffix;
            Issue issue = issueManager.getIssueObject(issueKey);
            if (issue != null)
            {
                URI canonicalUri = URI.create(applicationProperties.getBaseUrl() + BROWSE + issueKey);
                identifiers.add(new EntityIdentifier(issue().iri(),
                                                     issueKey,
                                                     canonicalUri));
            }
        }

        //add the project identifier if visible to the current user
        Project project = projectManager.getProjectObjByKeyIgnoreCase(projectKey);
        if (project != null)
        {
            URI canonicalUri = URI.create(applicationProperties.getBaseUrl() + BROWSE + projectKey);
            identifiers.add(new EntityIdentifier(project().iri(),
                                                 projectKey,
                                                 canonicalUri));
        }
        
        return identifiers.build();
    }
}
