package com.atlassian.jira.issue.security;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.IssueSecurityLevelEntity;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.entity.EntityUtils.internStringFieldValue;
import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * This class gets a list of all the security that can be part of a issue security scheme
 */
@EventComponent
public class IssueSecurityLevelManagerImpl implements IssueSecurityLevelManager
{
    protected final Logger log = Logger.getLogger(IssueSecurityLevelManagerImpl.class);

    //Map to hold the Security Levels for a user

    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SecurityTypeManager securityTypeManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final EntityEngine entityEngine;

    private final ConcurrentMap<CacheKey, List<GenericValue>> projectAndUserToSecurityLevelCache;

    // TODO: JRA-14323
    ///private final ConcurrentMap<CacheKey, List<GenericValue>> issueAndUserToSecurityLevelCache;

    public IssueSecurityLevelManagerImpl(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager securityTypeManager,
            ProjectManager projectManager, final PermissionManager permissionManager, EntityEngine entityEngine)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.securityTypeManager = securityTypeManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.entityEngine = entityEngine;

        this.projectAndUserToSecurityLevelCache = new MapMaker()
                .concurrencyLevel(16)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .makeMap();

        // TODO: JRA-14323
//        this.issueAndUserToSecurityLevelCache = new MapMaker()
//                .concurrencyLevel(16)
//                .expireAfterAccess(30, TimeUnit.MINUTES)
//                .makeMap();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearUsersLevels();
    }

    @Override
    public List<GenericValue> getSchemeIssueSecurityLevels(Long schemeId)
    {
        // Note: schemeId can be null
        return internSecurityLevelFields(
                ComponentAccessor.getOfBizDelegator().findByAnd("SchemeIssueSecurityLevels",
                        EasyMap.build("scheme", schemeId),     // fields
                        ImmutableList.of("name")));            // order by
    }

    @Override
    public List<IssueSecurityLevel> getIssueSecurityLevels(long schemeId)
    {
        return entityEngine.selectFrom(Entity.ISSUE_SECURITY_LEVEL)
                .whereEqual(IssueSecurityLevelEntity.SCHEME, schemeId)
                .orderBy(IssueSecurityLevelEntity.NAME);
    }

    @Override
    public GenericValue getIssueSecurity(Long id)
    {
        // return the Security Level with the given ID.
        return internSecurityLevelFields(ComponentAccessor.getOfBizDelegator().findById("SchemeIssueSecurityLevels", id));
    }

    @Override
    public IssueSecurityLevel getSecurityLevel(long id)
    {
        return entityEngine.selectFrom(Entity.ISSUE_SECURITY_LEVEL).findById(id);
    }

    /**
     * Checks to see if the issue security exists
     *
     * @param id The security Id
     * @return True / False
     */
    @Override
    public boolean schemeIssueSecurityExists(Long id)
    {
        return (getIssueSecurity(id) != null);
    }

    /**
     * Get the name of the issue security
     *
     * @param id The security Id
     * @return The name of the security
     */
    @Override
    public String getIssueSecurityName(Long id)
    {
        final GenericValue issueSecurity = getIssueSecurity(id);
        return issueSecurity == null ? null : issueSecurity.getString("name");
    }

    @Override
    public String getIssueSecurityDescription(Long id)
    {
        final GenericValue issueSecurity = getIssueSecurity(id);
        return issueSecurity == null ? null : issueSecurity.getString("description");
    }


    /**
     * Get the different levels of security that can be set for this issue.
     * TODO: JRA-14323 This method can return incorrect results because of a bug in caching.
     * <p>When editing an Issue, then you would pass in the GenericValue for the Issue.
     * When creating an Issue, the project is passed in.
     * </p>
     *
     * @param entity This is the issue or the project that the security is being checked for
     * @param user   The user used for the security check
     * @return an unmodifiable List containing the security levels
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getUsersSecurityLevels(GenericValue entity, User user) throws GenericEntityException
    {
        if (entity != null)
        {
            if (entity.getEntityName().equals("Project"))
            {
                return getUsersSecurityLevels(projectAndUserToSecurityLevelCache, entity, entity, user);
            }
            if (entity.getEntityName().equals("Issue"))
            {
                final GenericValue project = projectManager.getProject(entity);
                // TODO: JRA-14323 -- for now always returning the project's security levels for consistency
                //return getUsersSecurityLevels(issueAndUserToSecurityLevelCache, project, entity, user);
                return getUsersSecurityLevels(projectAndUserToSecurityLevelCache, project, project, user);
            }
        }

        // TODO is null really a valid value to return? Should we rather have either an empty list or IllegalArgumentException?
        //if no project then return null
        return null;
    }

    private List<GenericValue> getUsersSecurityLevels(ConcurrentMap<CacheKey,List<GenericValue>> cache,
            GenericValue project, GenericValue entity, User user) throws GenericEntityException
    {
        final Long entityId = entity.getLong("id");
        final CacheKey cacheKey = new CacheKey(entityId, user);

        List<GenericValue> securityLevels = cache.get(cacheKey);
        if (securityLevels == null)
        {
            securityLevels = getUsersSecurityLevelsUncached(project, entity, user);
            cache.put(cacheKey, securityLevels);
        }
        return securityLevels;
    }

    private List<GenericValue> getUsersSecurityLevelsUncached(GenericValue project, GenericValue entity, User user) throws GenericEntityException
    {
        //get the issue security scheme
        final GenericValue scheme = EntityUtil.getOnly(issueSecuritySchemeManager.getSchemes(project));

        //if there is no scheme then security cant be set
        if (scheme == null)
        {
            return ImmutableList.of();
        }

        //get all the security level records for this scheme
        // This is the "denormalised" list of Security Levels with permission settings.
        // eg for Security Level (id = 10010) "Reporters and Developers", there may be two entries:
        //  * security=10010,type=reporter,parameter=null
        //  * security=10010,type=projectrole,parameter=10001
        final List<GenericValue> securities = issueSecuritySchemeManager.getEntities(scheme);
        // Map of Permission types eg "reporter"->CurrentReporter
        final Map<String,SecurityType> types = securityTypeManager.getTypes();

        final List<GenericValue> levels = new ArrayList<GenericValue>();
        for (GenericValue security : securities)
        {
            if (!levelExists(levels, security.getLong("security")))
            {
                SchemeType type = types.get(security.getString("type"));
                if (type != null && hasPermission(entity, user, security, type))
                {
                    final GenericValue level = getIssueSecurityLevel(security.getLong("security"));
                    if (level != null)
                    {
                        levels.add(level);
                    }
                }
            }
        }

        Collections.sort(levels, SortByNameComparator.INSTANCE);
        return ImmutableList.copyOf(levels);
    }

    private boolean hasPermission(GenericValue entity, User user, GenericValue security, SchemeType type)
    {
        return (user != null)
                ? type.hasPermission(entity, security.getString("parameter"), user, false)
                : type.hasPermission(entity, security.getString("parameter") );
    }


    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Issue issue, User user)
    {
        try
        {
            List<GenericValue> genericValues = getUsersSecurityLevels(issue.getGenericValue(), user);
            return Entity.ISSUE_SECURITY_LEVEL.buildList(genericValues);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Project project, User user)
    {
        try
        {
            final List<GenericValue> genericValues = getUsersSecurityLevels(project.getGenericValue(), user);
            return Entity.ISSUE_SECURITY_LEVEL.buildList(genericValues);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Collection<GenericValue> getAllUsersSecurityLevels(final User user) throws GenericEntityException
    {
        Collection<GenericValue> projectGVs = permissionManager.getProjects(Permissions.BROWSE, user);
        Set<GenericValue> securityLevels = new HashSet<GenericValue>();
        for (GenericValue projectGV : projectGVs)
        {
            securityLevels.addAll(getUsersSecurityLevels(projectGV, user));
        }

        return securityLevels;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllSecurityLevelsForUser(final User user)
    {
        Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, user);
        Set<IssueSecurityLevel> securityLevels = new HashSet<IssueSecurityLevel>();
        for (Project project : projects)
        {
            securityLevels.addAll(getUsersSecurityLevels(project, user));
        }

        return securityLevels;
    }

    @Override
    public Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException
    {
        final List<GenericValue> schemes = this.issueSecuritySchemeManager.getSchemes();
        final Collection<GenericValue> allLevels = new LinkedHashSet<GenericValue>();
        for (GenericValue scheme : schemes)
        {
            allLevels.addAll(getSchemeIssueSecurityLevels(scheme.getLong("id")));
        }
        return allLevels;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllIssueSecurityLevels()
    {
        final List<GenericValue> schemes;
        try
        {
            schemes = issueSecuritySchemeManager.getSchemes();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        final Collection<IssueSecurityLevel> allLevels = new LinkedHashSet<IssueSecurityLevel>();
        for (GenericValue scheme : schemes)
        {
            allLevels.addAll(getIssueSecurityLevels(scheme.getLong("id")));
        }
        return allLevels;
    }

    @Override
    public Collection<GenericValue> getUsersSecurityLevelsByName(final User user, final String securityLevelName) throws GenericEntityException
    {
        return _getSecurityLevelsByName(securityLevelName, getAllUsersSecurityLevels(user));
    }

    @Override
    public Collection<IssueSecurityLevel> getSecurityLevelsForUserByName(final User user, final String securityLevelName)
    {
        return filterSecurityLevelsByName(securityLevelName, getAllSecurityLevelsForUser(user));
    }

    @Override
    public Collection<GenericValue> getSecurityLevelsByName(final String securityLevelName) throws GenericEntityException
    {
        return _getSecurityLevelsByName(securityLevelName, getAllSecurityLevels());
    }

    @Override
    public Collection<IssueSecurityLevel> getIssueSecurityLevelsByName(String securityLevelName)
    {
        return filterSecurityLevelsByName(securityLevelName, getAllIssueSecurityLevels());
    }

    private Collection<GenericValue> _getSecurityLevelsByName(final String securityLevelName, final Collection<GenericValue> securityLevels) throws GenericEntityException
    {
        final Predicate<GenericValue> namePredicate = new Predicate<GenericValue>()
        {
            @Override
            public boolean apply(final GenericValue input)
            {
                return securityLevelName.equalsIgnoreCase(input.getString("name"));
            }
        };

        final Set<GenericValue> filteredSecurityLevels = new LinkedHashSet<GenericValue>();
        for (GenericValue levelGV : Iterables.filter(securityLevels, namePredicate))
        {
            filteredSecurityLevels.add(levelGV);
        }
        return filteredSecurityLevels;
    }

    private List<IssueSecurityLevel> filterSecurityLevelsByName(String securityLevelName, Collection<IssueSecurityLevel> securityLevels)
    {
        List<IssueSecurityLevel> filteredSecurityLevels = new LinkedList<IssueSecurityLevel>();
        for (IssueSecurityLevel issueSecurityLevel : securityLevels)
        {
            if (issueSecurityLevel.getName().equals(securityLevelName))
            {
                filteredSecurityLevels.add(issueSecurityLevel);
            }
        }
        return filteredSecurityLevels;
    }

    private boolean levelExists(List<GenericValue> levels, Long id)
    {
        for (GenericValue level : levels)
        {
            if (level.getLong("id").equals(id))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Long getSchemeDefaultSecurityLevel(GenericValue project) throws GenericEntityException
    {
        if (project != null)
        {
            //get the issue security scheme
            GenericValue scheme = EntityUtil.getOnly(issueSecuritySchemeManager.getSchemes(project));
            return scheme == null ? null : scheme.getLong("defaultlevel");
        }
        else
        {
            return null;
        }
    }

    @Override
    public Long getDefaultSecurityLevel(Project project)
    {
        try
        {
            return getSchemeDefaultSecurityLevel(project.getGenericValue());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue getIssueSecurityLevel(Long id)
    {
        return ComponentAccessor.getOfBizDelegator().findById("SchemeIssueSecurityLevels", id);
    }

    @Override
    public void deleteSecurityLevel(Long levelId)
    {
        // Remove permission settings (children of the Issue Security Level)
        entityEngine.delete(Delete.from(Entity.Name.SCHEME_ISSUE_SECURITIES).whereEqual("security", levelId));
        // Remove the Issue Security Level
        entityEngine.delete(Delete.from(Entity.ISSUE_SECURITY_LEVEL).whereIdEquals(levelId));
    }

    /**
     * Clears the User security Level Map. This is done when security records are added or deleted
     */
    @Override
    public void clearUsersLevels()
    {
        projectAndUserToSecurityLevelCache.clear();
        // TODO: JRA-14323
        //issueAndUserToSecurityLevelCache.clear();
    }

    /**
     * Clears the User security Level Map. This is done when security records are added or deleted
     */
    @Override
    public void clearProjectLevels(GenericValue project)
    {
        if (project != null)
        {
            final Long projectId = project.getLong("id");
            if (projectId != null)
            {
                Iterables.removeIf(projectAndUserToSecurityLevelCache.keySet(), new MatchesEntityId(projectId));
                // TODO: JRA-14323
                //issueAndUserToSecurityLevelCache.clear();
            }
        }
    }

    private List<GenericValue> internSecurityLevelFields(List<GenericValue> list)
    {
        if (list != null)
        {
            for (GenericValue value : list)
            {
                internSecurityLevelFields(value);
            }
        }
        return list;
    }

    private GenericValue internSecurityLevelFields(GenericValue value)
    {
        internStringFieldValue(value, "name");
        internStringFieldValue(value, "description");
        return value;
    }

    static class SortByNameComparator implements Comparator<GenericValue>
    {
        static final SortByNameComparator INSTANCE = new SortByNameComparator();
        private SortByNameComparator() {}

        @Override
        public int compare(final GenericValue level1, final GenericValue level2)
        {
            final String value1 = level1.getString("name");
            final String value2 = level2.getString("name");
            return (value1 == null)
                    ? ((value2 == null) ? 0 : 1)
                    : ((value2 == null) ? -1 : value1.compareTo(value2));
        }
    }

    static class MatchesEntityId implements Predicate<CacheKey>
    {
        private final long entityId;
        MatchesEntityId(final long entityId)
        {
            this.entityId = entityId;
        }

        @Override
        public boolean apply(final CacheKey input)
        {
            return input.entityId == entityId;
        }
    }

    static class CacheKey
    {
        final Long entityId;
        final User user;

        CacheKey(final Long entityId, final User user)
        {
            this.entityId = notNull("entityId", entityId);
            this.user = user;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (! (o instanceof CacheKey))
            {
                return false;
            }
            final CacheKey other = (CacheKey)o;
            return entityId.longValue() == other.entityId.longValue() &&
                    ((user == null) ? (other.user == null) : user.equals(other.user));
        }

        @Override
        public int hashCode()
        {
            return entityId.hashCode() * 31 + ((user != null) ? user.hashCode() : 0);
        }

        @Override
        public String toString()
        {
            return "CacheKey[entityId=" + entityId + ", user=" + ((user != null) ? user.getName() : "(null)") + ']';
        }
    }
}

