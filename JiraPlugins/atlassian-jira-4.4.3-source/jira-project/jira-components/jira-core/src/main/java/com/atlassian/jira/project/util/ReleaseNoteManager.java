/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.order.SortOrder;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ReleaseNoteManager
{
    private static final Logger log = Logger.getLogger(ReleaseNoteManager.class);

    public static final String RELEASE_NOTE_NAME = "jira.releasenotes.templatenames";
    public static final String RELEASE_NOTE_DEFAULT = "jira.releasenotes.default";
    public static final String RELEASE_NOTE_TEMPLATE = "jira.releasenotes.templates";
    public static final String TEMPLATES_DIR = "templates/jira/project/releasenotes/";

    private Map styles;

    private final ApplicationProperties applicationProperties;
    private final VelocityManager velocityManager;
    private final ConstantsManager constantsManager;
    private final SearchProvider searchProvider;
    private final CustomFieldManager customFieldManager;

    public ReleaseNoteManager(final ApplicationProperties applicationProperties, final VelocityManager velocityManager,
            final ConstantsManager constantsManager, final SearchProvider searchProvider,
            final CustomFieldManager customFieldManager)
    {
        this.applicationProperties = applicationProperties;
        this.velocityManager = velocityManager;
        this.constantsManager = constantsManager;
        this.searchProvider = searchProvider;
        this.customFieldManager = customFieldManager;
    }

    public Map getStyles()
    {
        if (styles == null)
        {
            loadReleaseNoteTemplates();
        }
        return styles;
    }

    private List splitString(String strings)
    {
        if (strings == null)
        {
            return Collections.EMPTY_LIST;
        }

        List stringList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(strings, ",");
        while (tokenizer.hasMoreTokens())
        {
            stringList.add(tokenizer.nextToken().trim());
        }
        return stringList;
    }

    private void loadReleaseNoteTemplates()
    {
        List allReleaseNoteNames = splitString(applicationProperties.getDefaultBackedString(RELEASE_NOTE_NAME));
        List allReleaseNoteTemplates = splitString(applicationProperties.getDefaultBackedString(RELEASE_NOTE_TEMPLATE));
        if (allReleaseNoteTemplates.size() != allReleaseNoteNames.size())
        {
            throw new RuntimeException("Error loading release notes; differing numbers of names and templates specified in properties file.");
        }

        styles = new HashMap(allReleaseNoteTemplates.size());

        for (int i = 0; i < allReleaseNoteNames.size(); i++)
        {
            styles.put(allReleaseNoteNames.get(i), allReleaseNoteTemplates.get(i));
        }
    }

    /**
     * Return a releasenote for this version, using the specified releaseNoteStyleName.  The issues returned will be
     * the issues that the user has permission to see.
     *
     * @throws IllegalArgumentException if there is no matching template for this releaseNoteStyleName
     */
    public String getReleaseNote(Action action, String releaseNoteStyleName, Version version, User user, GenericValue project) throws IllegalArgumentException
    {
        try
        {
            String templateName = (String) getStyles().get(releaseNoteStyleName);

            // use Default
            if (templateName == null)
            {
                final String defaultType = applicationProperties.getDefaultBackedString(RELEASE_NOTE_DEFAULT);
                if (StringUtils.isNotBlank(defaultType))
                {
                    templateName = (String) getStyles().get(defaultType);
                }
            }
            
            // use first
            if (templateName == null)
            {
                templateName = getFirstStyle();
            }

            if (templateName == null)
            {
                log.error("No styles available for release notes");
                throw new IllegalArgumentException("No styles available for release notes");
            }

            Map templateVariables = getTemplateVariables(action, version, user, project);

            return velocityManager.getBody(TEMPLATES_DIR, templateName, templateVariables);
        }
        catch (VelocityException e)
        {
            log.error("Exception occurred while attempting to get velocity body for release note template.", e);
            return null;
        }
    }

    private String getFirstStyle()
    {
        final Collection values = getStyles().values();
        if (values != null)
        {
            final Iterator iterator = values.iterator();
            if (iterator.hasNext())
            {
                return (String) iterator.next();
            }
        }
        return null;
    }

    private Map getTemplateVariables(Action action, Version version, User user, GenericValue project)
    {
        List issueTypes = new ArrayList();
        for (IssueType  issueType : constantsManager.getAllIssueTypeObjects())
        {
            issueTypes.add(new IssuesByType(issueType, user, version.getLong("id")));
        }
        TextUtils textUtils = new TextUtils();
        Map templateVarMap = EasyMap.build("action", action,
                "req", ActionContext.getRequest(),
                "issueTypes", issueTypes,
                "appProps", applicationProperties,
                "version", version.getName(),
                "versionObj", version,
                "project", project.getString("name"),
                "textUtils", textUtils,
                "requestContext", new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext());
        templateVarMap.put("constantsManager", constantsManager);
        templateVarMap.put("customFieldManager", customFieldManager);
        return templateVarMap;
    }

    public class IssuesByType
    {
        private final IssueType issueType;
        private final User user;
        private final Long fixForVersion;
        private Collection issues;

        private IssuesByType(IssueType issueType, User user, @NotNull Long fixForVersion)
        {
            this.issueType = issueType;
            this.user = user;
            this.fixForVersion = notNull("fixForVersion", fixForVersion);
        }

        public String getName()
        {
            return issueType.getNameTranslation();
        }

        public Collection getIssues()
        {
            if (issues == null)
            {
                final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
                queryBuilder.where().fixVersion(fixForVersion).and().issueType(issueType.getId());
                queryBuilder.orderBy().issueKey(SortOrder.ASC);
                try
                {
                    issues = searchProvider.search(queryBuilder.buildQuery(), user, PagerFilter.getUnlimitedFilter()).getIssues();
                }
                catch (SearchException e)
                {
                    log.error("Error searching for issues", e);
                }
            }
            return issues;
        }
    }
}
