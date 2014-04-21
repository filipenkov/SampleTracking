package com.atlassian.jira.issue.context.persistence;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.collect.MapBuilder.build;

public class FieldConfigContextPersisterImpl implements FieldConfigContextPersister
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String ENTITY_TABLE_NAME = "ConfigurationContext";

    public static final String ENTITY_PROJECT_CATEGORY = JiraContextNode.FIELD_PROJECT_CATEGORY;
    public static final String ENTITY_PROJECT = JiraContextNode.FIELD_PROJECT;

    public static final String ENTITY_KEY = "key";
    public static final String ENTITY_CONFIG = "fieldconfigscheme";

    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final OfBizDelegator delegator;
    private final ProjectManager projectManager;
    private final JiraContextTreeManager treeManager;

    private static final Logger log = Logger.getLogger(FieldConfigContextPersisterImpl.class);

    // ---------------------------------------------------------------------------------------------------- Constructors

    public FieldConfigContextPersisterImpl(final OfBizDelegator delegator, final ProjectManager projectManager, final JiraContextTreeManager treeManager)
    {
        this.delegator = delegator;
        this.projectManager = projectManager;
        this.treeManager = treeManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public List<JiraContextNode> getAllContextsForCustomField(final String key)
    {
        return CollectionUtil.transform(delegator.findByAnd(ENTITY_TABLE_NAME, build(ENTITY_KEY, key)), new Function<GenericValue, JiraContextNode>()
        {
            public JiraContextNode get(final GenericValue input)
            {
                return transformToDomainObject(input);
            }
        });
    }

    public List<JiraContextNode> getAllContextsForConfigScheme(final FieldConfigScheme fieldConfigScheme)
    {
        return CollectionUtil.transform(delegator.findByAnd(ENTITY_TABLE_NAME, build(ENTITY_CONFIG, fieldConfigScheme.getId())),
            new Function<GenericValue, JiraContextNode>()
            {
                public JiraContextNode get(final GenericValue input)
                {
                    return transformToDomainObject(input);
                }
            });
    }

    public void removeContextsForConfigScheme(final Long fieldConfigSchemeId)
    {
        final int result = delegator.removeByAnd(ENTITY_TABLE_NAME, build(ENTITY_CONFIG, fieldConfigSchemeId));
        if (log.isDebugEnabled())
        {
            log.debug(result + " contexts deleted for field config scheme with id '" + fieldConfigSchemeId + "'");
        }
    }

    public void removeContextsForProject(final GenericValue project)
    {
        final int result = delegator.removeByAnd(ENTITY_TABLE_NAME, build(ENTITY_PROJECT, project.getLong("id")));
        if (log.isDebugEnabled())
        {
            log.debug(result + " contexts deleted for " + project);
        }
    }

    @Override
    public void removeContextsForProject(Project project)
    {
        final int result = delegator.removeByAnd(ENTITY_TABLE_NAME, build(ENTITY_PROJECT, project.getId()));
        if (log.isDebugEnabled())
        {
            log.debug(result + " contexts deleted for " + project);
        }
    }

    public void removeContextsForProjectCategory(final ProjectCategory projectCategory)
    {
        final int result = delegator.removeByAnd(ENTITY_TABLE_NAME, build(ENTITY_PROJECT_CATEGORY, projectCategory.getId()));
        if (log.isDebugEnabled())
        {
            log.debug(result + " contexts deleted for " + projectCategory);
        }
    }

    /**
     * Returns a Long object representing the id of the FieldConfigScheme
     * @param context the bandana context
     * @param key the database key
     */
    public Object retrieve(final BandanaContext context, final String key)
    {
        if (context != null)
        {
            final List<GenericValue> result = delegator.findByAnd(ENTITY_TABLE_NAME, transformToFieldsMap((JiraContextNode) context).add(ENTITY_KEY,
                key).toMap());
            if ((result != null) && !result.isEmpty())
            {
                final Long schemeId = result.iterator().next().getLong(ENTITY_CONFIG);
                if (result.size() > 1)
                {
                    log.warn("More than one FieldConfigScheme returned for a given context. Database may be corrupted." + "Returning first Long: " + schemeId + ". Context: " + context + " with key: " + key + " returned " + result + ".");
                }
                return schemeId;
            }
        }
        return null;
    }

    public void store(final BandanaContext context, final String key, final Object value)
    {
        final JiraContextNode contextNode = (JiraContextNode) context;
        final FieldConfigScheme config = (FieldConfigScheme) value;
        if (retrieve(contextNode, key) != null)
        {
            remove(contextNode, key);
        }

        // if config==null just remove it
        if (config != null)
        {
            final MapBuilder<String, Object> props = transformToFieldsMap(contextNode);
            props.add(ENTITY_KEY, key);
            props.add(ENTITY_CONFIG, config.getId());
            EntityUtils.createValue(ENTITY_TABLE_NAME, props.toMap());
        }
    }

    public void flushCaches()
    {
        throw new IllegalArgumentException();
    }

    public void remove(final BandanaContext context)
    {
        if (context != null)
        {
            final JiraContextNode contextNode = (JiraContextNode) context;
            delegator.removeByAnd(ENTITY_TABLE_NAME, transformToFieldsMap(contextNode).toMap());
        }
        else
        {
            log.warn("Context was null. Nothing was removed");
        }
    }

    public void remove(final BandanaContext context, final String key)
    {
        if ((context != null) && (key != null))
        {
            delegator.removeByAnd(ENTITY_TABLE_NAME, transformToFieldsMap((JiraContextNode) context).add(ENTITY_KEY, key).toMap());
        }
        else
        {
            log.warn("Context or key was null. Nothing was removed");
        }
    }

    // ------------------------------------------------------------------------------------------ Transformation Methods
    private MapBuilder<String, Object> transformToFieldsMap(final JiraContextNode contextNode)
    {
        return MapBuilder.newBuilder(contextNode.appendToParamsMap(Collections.<String, Object> emptyMap()));
    }

    private JiraContextNode transformToDomainObject(final GenericValue gv)
    {
        return new ProjectContext(projectManager.getProject(gv.getLong(ENTITY_PROJECT)), treeManager);
    }
}
