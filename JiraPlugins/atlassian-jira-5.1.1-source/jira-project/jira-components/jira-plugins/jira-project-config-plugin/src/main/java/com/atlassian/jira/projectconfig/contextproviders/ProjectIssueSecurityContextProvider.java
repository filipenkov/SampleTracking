package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.ProjectIssueSecuritySchemeHelper;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.AbstractSecurityType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the issue securities panel
 *
 * @since v4.4
 */
public class ProjectIssueSecurityContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_SHARED_PREOJECTS_KEY = "sharedProjects";
    private static final String CONTEXT_SCHEME_NAME = "schemeName";
    private static final String CONTEXT_SCHEME_ID = "schemeId";
    private static final String CONTEXT_SCHEME_DESCRIPTION = "schemeDescription";

    private static final String CONTEXT_ISSUESECURITYS = "issueSecurities";
    private static final String ENTITY_TYPE = "type";

    private static final String ENTITY_PARAMETER = "parameter";
    private static final String SCHEME_NAME = "name";
    private static final String SCHEME_ID = "id";

    private static final String SCHEME_DESCRIPTION = "description";
    private final SecurityTypeManager securityTypeManager;
    private final ProjectIssueSecuritySchemeHelper helper;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final ContextProviderUtils contextProviderUtils;

    public ProjectIssueSecurityContextProvider(final IssueSecuritySchemeManager issueSecuritySchemeManager,
            ContextProviderUtils contextProviderUtils, IssueSecurityLevelManager issueSecurityLevelManager,
            SecurityTypeManager securityTypeManager, final ProjectIssueSecuritySchemeHelper helper)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.contextProviderUtils = contextProviderUtils;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.securityTypeManager = securityTypeManager;
        this.helper = helper;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final List<String> errors = Lists.newArrayList();

        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);
        final I18nHelper i18nHelper = (I18nHelper) defaultContext.get(ContextProviderUtils.CONTEXT_I18N_KEY);

        final GenericValue scheme = getIssueSecurityScheme(project);

        if (scheme != null)
        {
            final Scheme schemeObject = issueSecuritySchemeManager.getSchemeObject(scheme.getLong("id"));
            final List<Project> sharedProjects = helper.getSharedProjects(schemeObject);

            contextMap.add(CONTEXT_SHARED_PREOJECTS_KEY, sharedProjects);
            contextMap.add(CONTEXT_SCHEME_NAME, scheme.getString(SCHEME_NAME));
            contextMap.add(CONTEXT_SCHEME_ID, scheme.getLong(SCHEME_ID));
            final String description = scheme.getString(SCHEME_DESCRIPTION);
            if (StringUtils.isNotBlank(description))
            {
                contextMap.add(CONTEXT_SCHEME_DESCRIPTION, description);
            }

            final List<IssueSecurity> issueSecurities = getIssueSecurities(scheme, i18nHelper);

            contextMap.add(CONTEXT_ISSUESECURITYS, issueSecurities);
        }
        else
        {
            contextMap.add(CONTEXT_SCHEME_NAME, i18nHelper.getText("admin.common.words.anyone"));
        }

        return contextMap.toMap();
    }

    private GenericValue getIssueSecurityScheme(final Project project)
    {
        try
        {
            List<GenericValue> schemes = issueSecuritySchemeManager.getSchemes(project.getGenericValue());
            if (schemes.size() > 0)
            {
                return schemes.get(0);
            }
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<IssueSecurity> getIssueSecurities(GenericValue scheme, I18nHelper i18nHelper)
    {
        Long defaultLevel = scheme.getLong("defaultlevel");
        List<GenericValue> securityLevels = issueSecurityLevelManager.getSchemeIssueSecurityLevels(scheme.getLong(SCHEME_ID));
        List<IssueSecurity> issueSecurityLevels =  new ArrayList<IssueSecurity>();
        for (GenericValue issueSecurityLevel : securityLevels)
        {
            Long id = issueSecurityLevel.getLong("id");
            boolean defaultSecurityLevel = id.equals(defaultLevel);
            final List<GenericValue> entities = getEntities(scheme, id);
            final List<String> entityDisplays = new ArrayList<String>(entities.size());
            for (GenericValue entity : entities)
            {
                final String typeStr = entity.getString(ENTITY_TYPE);
                
                final AbstractSecurityType type = (AbstractSecurityType) securityTypeManager.getSchemeType(typeStr);
                final String paramater = entity.getString(ENTITY_PARAMETER);
                final StringBuilder sb = new StringBuilder(type.getDisplayName());
                if (StringUtils.isNotBlank(paramater))
                {
                    sb.append(" (").append(type.getArgumentDisplay(paramater)).append(")");
                }
                else if (!(type.getType().equals("reporter") || type.getType().equals("lead") || type.getType().equals("assignee") ))
                {
                    sb.append(" (").append(i18nHelper.getText("admin.common.words.anyone")).append(")");
                }
                entityDisplays.add(sb.toString());
            }

            Collections.sort(entityDisplays);
            issueSecurityLevels.add(new IssueSecurity(issueSecurityLevel.getString("name"), issueSecurityLevel.getString("description"), entityDisplays, defaultSecurityLevel));
        }

        Collections.sort(issueSecurityLevels);
        return issueSecurityLevels;
    }

    private List<GenericValue> getEntities(GenericValue scheme, Long level)
    {
        try
        {
            return issueSecuritySchemeManager.getEntities(scheme, level);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }


    public static class IssueSecurity implements Comparable<IssueSecurity>
    {
        private final String name;
        private final String description;
        private final List<String> entities;
        private final boolean defaultSecurityLevel;

        public IssueSecurity(String name, String description, List<String> entities, boolean defaultSecurityLevel)
        {
            this.name = name;
            this.entities = entities;
            this.description = description;
            this.defaultSecurityLevel = defaultSecurityLevel;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public List<String> getEntities()
        {
            return entities;
        }

        public boolean isDefaultSecurityLevel()
        {
            return defaultSecurityLevel;
        }

        @Override
        public int compareTo(IssueSecurity o)
        {
            int defSort = defaultSecurityLevel ? 0 : 1;
            int otherDefSort = o.isDefaultSecurityLevel() ? 0 : 1;
            if (defSort == otherDefSort)
            {
                return name.compareTo(o.getName());
            }
            return (defSort < otherDefSort ? -1 : 1);
        }
    }
}
