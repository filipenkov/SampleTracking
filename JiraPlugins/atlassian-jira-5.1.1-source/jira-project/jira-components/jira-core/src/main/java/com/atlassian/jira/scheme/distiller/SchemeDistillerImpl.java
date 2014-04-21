package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.permission.PermissionImpl;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link SchemeDistiller}.
 */
public class SchemeDistillerImpl implements SchemeDistiller
{
    private SchemeManagerFactory schemeManagerFactory;
    private SchemePermissions schemePermissions;
    private EventTypeManager eventTypeManager;

    public SchemeDistillerImpl(SchemeManagerFactory schemeManagerFactory, SchemePermissions schemePermissions, EventTypeManager eventTypeManager)
    {
        this.schemeManagerFactory = schemeManagerFactory;
        this.schemePermissions = schemePermissions;
        this.eventTypeManager = eventTypeManager;
    }

    public Scheme persistNewSchemeMappings(DistilledSchemeResult distilledSchemeResult) throws DataAccessException
    {
        // Create the new scheme and associated entities
        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(distilledSchemeResult.getType());

        // Set the name of the scheme from the tempname in the distilled result
        distilledSchemeResult.getResultingScheme().setName(distilledSchemeResult.getResultingSchemeTempName());

        Scheme scheme = schemeManager.createSchemeAndEntities(distilledSchemeResult.getResultingScheme());
        distilledSchemeResult.setResultingScheme(scheme);
        modifyAllProjectAssociations(schemeManager, distilledSchemeResult);

        return scheme;
    }

    public SchemeRelationships getSchemeRelationships(DistilledSchemeResults distilledSchemeResults)
    {
        SchemeRelationships schemeRelationships = null;

        // Initialize the results with the correct schemeTypes, either Notification types (Issue Created, etc..) or
        // Permission Types (Browse Project, etc...).
        final String schemeType = distilledSchemeResults.getSchemeType();
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(schemeType))
        {
            Map schemeTypes = eventTypeManager.getEventTypesMap();

            schemeRelationships = new SchemeRelationships(distilledSchemeResults.getDistilledSchemeResults(),
                    distilledSchemeResults.getUnDistilledSchemes(), getNotificationTypes(schemeTypes));
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(schemeType))
        {
            Map schemeTypes = schemePermissions.getSchemePermissions();

            // Create the holder for our results
            schemeRelationships = new SchemeRelationships(distilledSchemeResults.getDistilledSchemeResults(),
                    distilledSchemeResults.getUnDistilledSchemes(), getPermissionTypes(schemeTypes));
        }

        return schemeRelationships;
    }

    private Collection getNotificationTypes(Map events)
    {
        Collection notificationTypes = new ArrayList();
        for (Iterator iterator = events.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            EventType eventType = (EventType) entry.getValue();
            notificationTypes.add(new SchemeEntityType(entry.getKey(), eventType.getNameKey()));
        }
        return notificationTypes;
    }

    private Collection getPermissionTypes(Map permissions)
    {
        Collection permissionTypes = new ArrayList();
        for (Iterator iterator = permissions.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer key = (Integer) entry.getKey();
            Long realKey = new Long(key.longValue());
            PermissionImpl permission = (PermissionImpl) entry.getValue();
            permissionTypes.add(new SchemeEntityType(realKey, permission.getNameKey()));
        }
        return permissionTypes;
    }

    private void modifyAllProjectAssociations(SchemeManager schemeManager, DistilledSchemeResult distilledSchemeResult)
    {
        // update the associations for all projects
        for (Iterator iterator = distilledSchemeResult.getAllAssociatedProjects().iterator(); iterator.hasNext();)
        {
            Project project = (Project) iterator.next();
            // Remove the current scheme
            schemeManager.removeSchemesFromProject(project);
            // Add the new scheme association
            schemeManager.addSchemeToProject(project, distilledSchemeResult.getResultingScheme());
        }
    }

    public DistilledSchemeResults distillSchemes(Collection schemes)
    {
        if (schemes == null)
        {
            return new DistilledSchemeResults(null);
        }
        else
        {
            // If we are creating a result set for a collection of schemes then we want to record the
            // type of the schemes we are a result container for.
            String type = null;
            if (!schemes.isEmpty())
            {
                type = ((Scheme) schemes.iterator().next()).getType();
            }

            MultiMap commonSchemeBucket = new MultiHashMap();

            // Iterate through all the schemes and divide them into categories based on the hashcode
            // of their child entities. This should put all the 'equal' schemes together. NOTE: this
            // does not separate based on type but it is VERY unlikely that a set of notification
            // schemeEntities and a set of permission schemeEntities will be exactly the same, so
            // we can safely ignore this.
            for (Iterator iterator = schemes.iterator(); iterator.hasNext();)
            {
                Scheme scheme = (Scheme) iterator.next();
                commonSchemeBucket.put(new HashSet(scheme.getEntities()), scheme);
            }

            DistilledSchemeResults distilledSchemeResults = new DistilledSchemeResults(type);
            // Ideally we would iterate over entrySet, but commonSchemeBucket is an instance of MultiMap and therefore
            // we must get the keySet first and lookup the Collection of values for it.
            for (Iterator iterator = commonSchemeBucket.keySet().iterator(); iterator.hasNext();)
            {
                List commonSchemes = new ArrayList((Collection) commonSchemeBucket.get(iterator.next()));

                // Check to see if the scheme matches any other schemes, this means we will smoosh, otherwise it is
                // just the original scheme :(
                if (commonSchemes.size() > 1)
                {
                    distilledSchemeResults.addDistilledSchemeResult(getDistilledSchemeResult(commonSchemes));
                }
                else
                {
                    distilledSchemeResults.addUndistilledScheme(((Scheme) commonSchemes.get(0)));
                }
            }
            return distilledSchemeResults;
        }
    }

    private DistilledSchemeResult getDistilledSchemeResult(List commonSchemes)
    {
        MultiMap projectsByScheme = new MultiHashMap();
        String type = null;
        for (Iterator iterator1 = commonSchemes.iterator(); iterator1.hasNext();)
        {
            Scheme scheme = (Scheme) iterator1.next();
            if (type == null)
            {
                type = scheme.getType();
            }
            for (Iterator iterator = getProjectsForScheme(scheme, type).iterator(); iterator.hasNext();)
            {
                Project project = (Project) iterator.next();
                projectsByScheme.put(scheme, project);
            }
        }
        return new DistilledSchemeResult(type, commonSchemes, projectsByScheme, ((Scheme) commonSchemes.get(0)).cloneScheme());
    }

    private Collection getProjectsForScheme(Scheme scheme, String type)
    {
        Collection projects = null;

        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(type);
        if (scheme.getId() != null)
        {
            projects = schemeManager.getProjects(scheme);
        }
        if (projects == null)
        {
            projects = Collections.EMPTY_LIST;
        }

        return projects;
    }

}
