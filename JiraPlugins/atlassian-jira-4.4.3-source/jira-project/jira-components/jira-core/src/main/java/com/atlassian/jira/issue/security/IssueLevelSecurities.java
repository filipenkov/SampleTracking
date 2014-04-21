package com.atlassian.jira.issue.security;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class gets a list of all the security that can be part of a issue security scheme
 */
public class IssueLevelSecurities implements IssueSecurityLevelManager, Startable
{
    protected final Logger log = Logger.getLogger(IssueLevelSecurities.class);

    //Map to hold the Security Levels for a user
    private final ConcurrentMap<Long, Map<Object, GenericValue>> projectSecurityLevels = new ConcurrentHashMap<Long, Map<Object, GenericValue>>();

    private static final Object NULL_USER_KEY = new Object();
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    private SecurityTypeManager securityTypeManager;
    private ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final EventPublisher eventPublisher;

    public IssueLevelSecurities(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager securityTypeManager,
            ProjectManager projectManager, final PermissionManager permissionManager, final EventPublisher eventPublisher)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.securityTypeManager = securityTypeManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearUsersLevels();
    }

    public List<GenericValue> getSchemeIssueSecurityLevels(Long schemeId)
    {
        try
        {
            return CoreFactory.getGenericDelegator().findByAnd("SchemeIssueSecurityLevels",
                    EasyMap.build("scheme", schemeId),  // fields
                    EasyList.build("name"));            // order by
        }
        catch (GenericEntityException e)
        {
            log.error("Exception", e);
            return null;
        }
    }

    /**
     * Checks to see if the issue security exists
     *
     * @param id The security Id
     * @return True / False
     */
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
    public String getIssueSecurityName(Long id)
    {
        final GenericValue issueSecurity = getIssueSecurity(id);
        return issueSecurity == null ? null : issueSecurity.getString("name");
    }

    public String getIssueSecurityDescription(Long id)
    {
        final GenericValue issueSecurity = getIssueSecurity(id);
        return issueSecurity == null ? null : issueSecurity.getString("description");
    }

    public GenericValue getIssueSecurity(Long id)
    {
        try
        {
            // return the Security Level with the given ID.
            return EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("SchemeIssueSecurityLevels", EasyMap.build("id", id)));
        }
        catch (GenericEntityException e)
        {
            return null;
        }
    }
    public List<GenericValue> getUsersSecurityLevels(GenericValue entity, com.opensymphony.user.User user) throws GenericEntityException
    {
        return getUsersSecurityLevels(entity, (User) user);
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
    public List<GenericValue> getUsersSecurityLevels(GenericValue entity, User user) throws GenericEntityException
    {
        // Since we are using a concurrent hashmap to store our cache we need to treat the null user
        // as an object
        Object userKey = (user != null) ? user : NULL_USER_KEY;

        GenericValue project = getProject(entity);

        //if there is no project then there cant be a scheme so there are no security levels
        if (project != null)
        {
            // TODO: If the entity is an Issue, then it is not correct to use the cache value.
            //if the users levels for this project already exist then get them
            if (projectSecurityLevels.containsKey(project.getLong("id")))
            {
                //get the users levels for this project
                Map usersLevels = (Map) projectSecurityLevels.get(project.getLong("id"));

                //if the users levels exist and the user is logged in (ConcurrentHashMap does not accept null keys) then
                if (usersLevels != null && usersLevels.containsKey(userKey))
                {
                    List userSecurityLevels = (List) usersLevels.get(userKey);
                    if (userSecurityLevels != null)
                    {
                        return Collections.unmodifiableList(userSecurityLevels);
                    }
                    else
                    {
                        return null;
                    }
                }
            }

            final List levels = new ArrayList();

            //get the issue security scheme
            final GenericValue scheme = EntityUtil.getOnly(issueSecuritySchemeManager.getSchemes(project));
            //if there is no scheme then security cant be set
            if (scheme != null)
            {
                //get all the security level records for this scheme
                // This is the "denormalised" list of Security Levels with permission settings.
                // eg for Security Level (id = 10010) "Reporters and Developers", there may be two entries:
                //  * security=10010,type=reporter,parameter=null
                //  * security=10010,type=projectrole,parameter=10001
                List securities = issueSecuritySchemeManager.getEntities(scheme);
                // Map of Permission types eg "reporter"->CurrentReporter
                Map types = securityTypeManager.getTypes();
                for (Iterator iterator = securities.iterator(); iterator.hasNext();)
                {
                    GenericValue security = (GenericValue) iterator.next();

                    //if the level is already there then dont try and add it again
                    if (!levelExists(levels, security.getLong("security")))
                    {
                        //Get the type that matches the security record
                        SchemeType type = (SchemeType) types.get(security.getString("type"));
                        if (type != null)
                        {
                            //check if the user has permission for this security level and type
                            boolean hasPermission;
                            if (user == null)
                            {
                                // TODO: This is a Temporary workaround to JRA-14323. We always use project-level security settings, so that we at least have consistent behaviour.
                                // Fix this caching issue sometime.
//                                hasPermission = type.hasPermission(entity, security.getString("parameter"));
                                hasPermission = type.hasPermission(project, security.getString("parameter"));
                            }
                            else
                            {
                                // TODO: This is a Temporary workaround to JRA-14323. We always use project-level security settings, so that we at least have consistent behaviour.
                                // Fix this caching issue sometime.
//                                hasPermission = type.hasPermission(entity, security.getString("parameter"), user, false);
                                hasPermission = type.hasPermission(project, security.getString("parameter"), user, false);
                            }
                            if (hasPermission)
                            {
                                //get the details of the security level
                                GenericValue level = getIssueSecurityLevel(security.getLong("security"));
                                if (level != null)
                                {
                                    levels.add(level);
                                }
                            }
                        }
                    }
                }
            }

            // sort levels alphabetically
            Collections.sort(levels, new Comparator()
            {
                public int compare(final Object level1, final Object level2)
                {
                    final String value1 = ((GenericValue) level1).getString("name");
                    final String value2 = ((GenericValue) level2).getString("name");
                    return value1 == null ? value2 == null ? 0 : 1 : value2 == null ? -1 : value1.compareTo(value2);
                }
            });

            //get the map of user levels for this project
            Map usersLevels = (Map) projectSecurityLevels.get(project.getLong("id"));
            if (usersLevels == null)
            {
                usersLevels = new HashMap();
            }

            //add the levels for this user to the map and put it into the project levels map
            usersLevels.put(userKey, levels);
            // TODO: If the entity is an Issue, then it is not correct to cache this value like this.
            projectSecurityLevels.putIfAbsent(project.getLong("id"), new ConcurrentHashMap(usersLevels));

            return Collections.unmodifiableList(levels);
        }

        // TODO is null really a valid value to return? Should we rather have either an empty list or IllegalArgumentException?
        //if no project then return null
        return null;
    }
    public Collection<GenericValue> getAllUsersSecurityLevels(final com.opensymphony.user.User user) throws GenericEntityException
    {
        return getAllUsersSecurityLevels((User) user);
    }

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

    public Collection<GenericValue> getUsersSecurityLevelsByName(final User user, final String securityLevelName) throws GenericEntityException
    {
        return _getSecurityLevelsByName(securityLevelName, getAllUsersSecurityLevels(user));
    }

    public Collection<GenericValue> getUsersSecurityLevelsByName(final com.opensymphony.user.User user, final String securityLevelName) throws GenericEntityException
    {
        return getUsersSecurityLevelsByName((User) user, securityLevelName);
    }

    public Collection<GenericValue> getSecurityLevelsByName(final String securityLevelName) throws GenericEntityException
    {
        return _getSecurityLevelsByName(securityLevelName, getAllSecurityLevels());
    }

    private Collection<GenericValue> _getSecurityLevelsByName(final String securityLevelName, final Collection<GenericValue> securityLevels) throws GenericEntityException
    {
        final Predicate<GenericValue> namePredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return securityLevelName.equalsIgnoreCase(input.getString("name"));
            }
        };

        final Set<GenericValue> filteredSecurityLevels = new LinkedHashSet<GenericValue>();
        for (GenericValue levelGV : CollectionUtil.filter(securityLevels, namePredicate))
        {
            filteredSecurityLevels.add(levelGV);
        }
        return filteredSecurityLevels;
    }

    /**
     * Returns the project that the given entity belongs to.
     *
     * This method was ported from JiraEntityUtils.getProject(final GenericValue entity) in order to allow Dependancy Injection for unit tests.
     * @param entity Project or Issue GenericValue
     * @return the given Project, or the Project that the issue belongs to.
     */
    private GenericValue getProject(final GenericValue entity)
    {
        if (entity != null)
        {
            if (entity.getEntityName().equals("Project"))
            {
                return entity;
            }
            else if (entity.getEntityName().equals("Issue"))
            {
                return projectManager.getProject(entity);
            }
        }

        return null;
    }

    private boolean levelExists(List levels, Long id)
    {
        for (Iterator iterator = levels.iterator(); iterator.hasNext();)
        {
            GenericValue level = (GenericValue) iterator.next();
            if (level.getLong("id").equals(id))
            {
                return true;
            }
        }

        return false;
    }

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

    public GenericValue getIssueSecurityLevel(Long id) throws GenericEntityException
    {
        return CoreFactory.getGenericDelegator().findByPrimaryKey("SchemeIssueSecurityLevels", EasyMap.build("id", id));
    }

    public void deleteSecurityLevel(Long levelId) throws GenericEntityException
    {
        GenericValue level = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("SchemeIssueSecurityLevels", EasyMap.build("id", levelId)));

        if (level != null)
        {
            //remove all security records associated with this level and then remove the level
            level.removeRelated("ChildSchemeIssueSecurities");
            level.remove();
        }
    }

    /**
     * Clears the User security Level Map. This is done when security records are added or deleted
     */
    public void clearUsersLevels()
    {
        projectSecurityLevels.clear();
    }

    /**
     * Clears the User security Level Map. This is done when security records are added or deleted
     */
    public void clearProjectLevels(GenericValue project)
    {
        if (project != null)
        {
            projectSecurityLevels.remove(project.getLong("id"));
        }
    }
}
