package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DefaultIssueTypeScreenSchemeManager implements IssueTypeScreenSchemeManager, Startable
{
    private final OfBizDelegator ofBizDelegator;
    private final ConstantsManager constantsManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final AssociationManager associationManager;
    private final EventPublisher eventPublisher;

    // Caches project to scheme id relationships
    private final ConcurrentMap<Long, Long> projectAssociationCache = new ConcurrentHashMap<Long, Long>();
    // Caches the actual scheme object using scheme id as a key
    private final ConcurrentMap<Long, IssueTypeScreenScheme> schemeCache = new ConcurrentHashMap<Long, IssueTypeScreenScheme>();

    public DefaultIssueTypeScreenSchemeManager(final OfBizDelegator ofBizDelegator, final ConstantsManager constantsManager,
            final FieldScreenSchemeManager fieldScreenSchemeManager, final AssociationManager associationManager,
            final EventPublisher eventPublisher)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.constantsManager = constantsManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.associationManager = associationManager;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes()
    {
        return buildIssueTypeScreenSchemes(ofBizDelegator.findAll(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, EasyList.build("name")));
    }

    private Collection<IssueTypeScreenScheme> buildIssueTypeScreenSchemes(final List<GenericValue> issueTypeScreenSchemeGVs)
    {
        final List<IssueTypeScreenScheme> issueTypeScreenSchemes = new LinkedList<IssueTypeScreenScheme>();
        for (final Object element : issueTypeScreenSchemeGVs)
        {
            issueTypeScreenSchemes.add(buildIssueTypeScreenScheme((GenericValue) element));
        }
        return issueTypeScreenSchemes;
    }

    protected IssueTypeScreenScheme buildIssueTypeScreenScheme(final GenericValue genericValue)
    {
        return new IssueTypeScreenSchemeImpl(this, genericValue);
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Long id)
    {
        IssueTypeScreenScheme issueTypeScreenScheme = schemeCache.get(id);

        if (issueTypeScreenScheme == null)
        {
            final GenericValue issueTypeScreenSchemeGV = getOfBizDelegator().findByPrimaryKey(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME,
                EasyMap.build("id", id));
            issueTypeScreenScheme = buildIssueTypeScreenScheme(issueTypeScreenSchemeGV);
            final IssueTypeScreenScheme result = schemeCache.putIfAbsent(issueTypeScreenScheme.getId(), issueTypeScreenScheme);
            return (result == null) ? issueTypeScreenScheme : result;
        }
        else
        {
            return issueTypeScreenScheme;
        }
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme(final GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        final Long projectId = project.getLong("id");
        final Long schemeId = projectAssociationCache.get(projectId);
        if (schemeId != null)
        {
            return getIssueTypeScreenScheme(schemeId);
        }

        IssueTypeScreenScheme issueTypeScreenScheme = null;

        try
        {
            final GenericValue issueTypeScreenSchemeGV = EntityUtil.getOnly(getAssociationManager().getSinkFromSource(project,
                ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, SchemeManager.PROJECT_ASSOCIATION, false));
            if (issueTypeScreenSchemeGV != null)
            {
                issueTypeScreenScheme = buildIssueTypeScreenScheme(issueTypeScreenSchemeGV);
                // Cache the scheme's id for the project
                projectAssociationCache.putIfAbsent(projectId, issueTypeScreenScheme.getId());
            }

            return issueTypeScreenScheme;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving a iisue type screen scheme.", e);
        }
    }

    public FieldScreenScheme getFieldScreenScheme(final Issue issue)
    {
        final Project project = issue.getProjectObject();
        if (project == null)
        {
            throw new RuntimeException("Issue '" + issue + "' has no project");
        }
        final IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme(project.getGenericValue());
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issue.getIssueType().getString("id"));
        if (issueTypeScreenSchemeEntity == null)
        {
            // Try default entry
            issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null);
            if (issueTypeScreenSchemeEntity == null)
            {
                throw new IllegalStateException("No default entity for issue type screen scheme with id '" + issueTypeScreenScheme.getId() + "'.");
            }
        }

        return issueTypeScreenSchemeEntity.getFieldScreenScheme();
    }

    public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        List issueTypeScreenSchemeEntities = new LinkedList();
        List issueTypeScreenSchemeEntitymGVs = getOfBizDelegator().findByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, EasyMap.build("scheme", issueTypeScreenScheme.getId()));
        for (Iterator iterator = issueTypeScreenSchemeEntitymGVs.iterator(); iterator.hasNext();)
        {
            GenericValue issueTypeScreenSchemeEntityGV = (GenericValue) iterator.next();
            IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = buildIssueTypeScreenSchemeEntity(issueTypeScreenSchemeEntityGV);
            issueTypeScreenSchemeEntity.setIssueTypeScreenScheme(issueTypeScreenScheme);
            issueTypeScreenSchemeEntities.add(issueTypeScreenSchemeEntity);
        }

        return issueTypeScreenSchemeEntities;
    }

    protected IssueTypeScreenSchemeEntity buildIssueTypeScreenSchemeEntity(final GenericValue genericValue)
    {
        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(this, genericValue,
            fieldScreenSchemeManager, constantsManager);
        issueTypeScreenSchemeEntity.setIssueTypeId(genericValue.getString("issuetype"));
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(genericValue.getLong("fieldscreenscheme")));
        return issueTypeScreenSchemeEntity;
    }

    public void createIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        // Used by upgarde tasks - so needs to stay here
        final Map params = EasyMap.build("name", issueTypeScreenScheme.getName(), "description", issueTypeScreenScheme.getDescription());
        if (issueTypeScreenScheme.getId() != null)
        {
            params.put("id", issueTypeScreenScheme.getId());
        }

        final GenericValue fieldScreenSchemeGV = ofBizDelegator.createValue(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, params);
        issueTypeScreenScheme.setGenericValue(fieldScreenSchemeGV);
    }

    public void updateIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        // Used by upgarde tasks - so needs to stay here
        ofBizDelegator.store(issueTypeScreenScheme.getGenericValue());
        schemeCache.remove(issueTypeScreenScheme.getId());
    }

    public void removeIssueTypeSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        getOfBizDelegator().removeByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, EasyMap.build("scheme", issueTypeScreenScheme.getId()));
    }

    public void removeIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        getOfBizDelegator().removeValue(issueTypeScreenScheme.getGenericValue());
        schemeCache.remove(issueTypeScreenScheme.getId());
    }

    public void createIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        final String issueTypeId = issueTypeScreenSchemeEntity.getIssueTypeId();
        final GenericValue issueTypeScreenSchemeEntityGV = ofBizDelegator.createValue(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME, EasyMap.build(
            "issuetype", issueTypeId, "fieldscreenscheme", issueTypeScreenSchemeEntity.getFieldScreenScheme().getId(), "scheme",
            issueTypeScreenSchemeEntity.getIssueTypeScreenScheme().getId()));
        issueTypeScreenSchemeEntity.setGenericValue(issueTypeScreenSchemeEntityGV);
        schemeCache.remove(issueTypeScreenSchemeEntity.getFieldScreenScheme().getId());
    }

    public void updateIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        ofBizDelegator.store(issueTypeScreenSchemeEntity.getGenericValue());
        schemeCache.remove(issueTypeScreenSchemeEntity.getFieldScreenScheme().getId());
    }

    public void removeIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        ofBizDelegator.removeValue(issueTypeScreenSchemeEntity.getGenericValue());
        schemeCache.remove(issueTypeScreenSchemeEntity.getIssueTypeScreenScheme().getId());
    }

    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes(final FieldScreenScheme fieldScreenScheme)
    {
        final List<IssueTypeScreenScheme> issueTypeScreenSchemes = new LinkedList<IssueTypeScreenScheme>();
        final Set<Long> issueTypeScreenSchemeIds = new HashSet<Long>();
        final List<GenericValue> issueTypeScreenSchemeEntityGVs = ofBizDelegator.findByAnd(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME,
            EasyMap.build("fieldscreenscheme", fieldScreenScheme.getId()));
        for (final GenericValue issueTypeScreenSchemeEntityGV : issueTypeScreenSchemeEntityGVs)
        {
            issueTypeScreenSchemeIds.add(issueTypeScreenSchemeEntityGV.getLong("scheme"));
        }

        for (final Long element : issueTypeScreenSchemeIds)
        {
            issueTypeScreenSchemes.add(getIssueTypeScreenScheme(element));
        }

        return issueTypeScreenSchemes;
    }

    public void addSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        try
        {
            // Get old association
            final IssueTypeScreenScheme oldIssueTypeScreenScheme = getIssueTypeScreenScheme(project);
            if (oldIssueTypeScreenScheme != null)
            {
                // Only do anything if the schemes are different
                if (!oldIssueTypeScreenScheme.equals(issueTypeScreenScheme))
                {
                    // Remove old association
                    removeSchemeAssociation(project, oldIssueTypeScreenScheme);
                    if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
                    {
                        associationManager.createAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
                    }
                }
            }
            else
            {
                if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
                {
                    associationManager.createAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while associating project and issue type screen scheme.", e);
        }
    }

    public void removeSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        try
        {
            if ((issueTypeScreenScheme != null) && (issueTypeScreenScheme.getGenericValue() != null))
            {
                getAssociationManager().removeAssociation(project, issueTypeScreenScheme.getGenericValue(), SchemeManager.PROJECT_ASSOCIATION);
                projectAssociationCache.remove(project.getLong("id"));
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while removing association between project and issue type screen scheme.", e);
        }
    }

    public Collection getProjects(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        try
        {
            final List<GenericValue> projects = associationManager.getSourceFromSink(issueTypeScreenScheme.getGenericValue(), "Project",
                SchemeManager.PROJECT_ASSOCIATION, false);
            Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
            return projects;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void associateWithDefaultScheme(final GenericValue project)
    {
        addSchemeAssociation(project, getDefaultScheme());
    }

    public IssueTypeScreenScheme getDefaultScheme()
    {
        return getIssueTypeScreenScheme(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
    }

    public void addSchemeAssociation(final GenericValue project, final FieldScreenScheme fieldScreenScheme)
    {
        // Check if we have an issue type field screen scheme for this field screen scheme
        IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme(fieldScreenScheme);
        if (issueTypeScreenScheme == null)
        {
            // Create a issue type screen scheme to wrap this field screen scheme
            issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(this, null);
            issueTypeScreenScheme.setName("Scheme for " + fieldScreenScheme.getName());
            issueTypeScreenScheme.setDescription("Scheme created for " + fieldScreenScheme.getName() + " screen scheme.");
            issueTypeScreenScheme.store();
            final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(this, (GenericValue) null,
                fieldScreenSchemeManager, constantsManager);
            issueTypeScreenSchemeEntity.setIssueTypeId(null);
            issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
            issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);
        }

        // Associate the project with the issue type screen scheme
        addSchemeAssociation(project, issueTypeScreenScheme);
    }

    public Collection getProjects(final FieldScreenScheme fieldScreenScheme)
    {
        final IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme(fieldScreenScheme);
        if (issueTypeScreenScheme == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return getProjects(issueTypeScreenScheme);
        }
    }

    protected IssueTypeScreenScheme getIssueTypeScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        final Collection issueTypeScreenSchemes = getIssueTypeScreenSchemes(fieldScreenScheme);
        if ((issueTypeScreenSchemes == null) || issueTypeScreenSchemes.isEmpty())
        {
            return null;
        }
        else if (issueTypeScreenSchemes.size() > 1)
        {
            throw new IllegalStateException(
                "A field screen scheme is associated with multiple issue type screen schemes. This should not occur in this edition of JIRA.");
        }
        else
        {
            return (IssueTypeScreenScheme) issueTypeScreenSchemes.iterator().next();
        }
    }

    public void refresh()
    {
        schemeCache.clear();
        projectAssociationCache.clear();
    }

    protected OfBizDelegator getOfBizDelegator()
    {
        return ofBizDelegator;
    }

    protected AssociationManager getAssociationManager()
    {
        return associationManager;
    }
}
