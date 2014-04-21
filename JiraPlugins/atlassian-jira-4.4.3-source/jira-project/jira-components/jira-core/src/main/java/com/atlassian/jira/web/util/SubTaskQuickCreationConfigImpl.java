/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldManager;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Used to provide the {@link SubTaskQuickCreationWebComponent} with configuration parameters.
 * For example, what fields should be displayed
 */
public class SubTaskQuickCreationConfigImpl implements SubTaskQuickCreationConfig
{
    private static final Logger log = Logger.getLogger(SubTaskQuickCreationConfigImpl.class);
    private static final Collection<String> DEFAULT_LIST = ImmutableList.of(
            IssueFieldConstants.SUMMARY,
            IssueFieldConstants.ISSUE_TYPE,
            IssueFieldConstants.ASSIGNEE,
            IssueFieldConstants.TIMETRACKING  // original time estimate
    );

    private final ApplicationProperties applicationProperties;
    private final FieldManager fieldManager;

    public SubTaskQuickCreationConfigImpl(ApplicationProperties applicationProperties, FieldManager fieldManager)
    {
        this.applicationProperties = applicationProperties;
        this.fieldManager = fieldManager;
    }

    public Collection getDisplayFieldIds()
    {
        return parseFieldIds(new FieldParser()
        {
            public Object parse(String fieldId)
            {
                if (TextUtils.stringSet(fieldId))
                {
                    String[] fieldVal = StringUtils.split(fieldId, '=');
                    if (fieldVal.length != 1)
                    {
                        return null; // eliminates presets and empties
                    }

                    final String fieldValue = fieldVal[0].trim();
                    if (fieldManager.getOrderableField(fieldValue) != null)
                    {
                        return fieldValue;
                    }
                    else
                    {
                        log.error("Orderable field with id '" + fieldId + "' does not exist.");
                    }
                }
                return null;
            }
        });
    }

    public Collection getFieldIds()
    {
        return parseFieldIds(new FieldParser()
        {
            public Object parse(String fieldId)
            {
                String[] fieldVal = StringUtils.split(fieldId, '=');
                if (fieldVal.length > 0 && fieldManager.getOrderableField(fieldVal[0].trim()) != null)
                {
                    return fieldVal[0].trim();
                }
                else
                {
                    return null;
                }
            }
        });
    }

    public Collection getPresetFieldIds()
    {
        return parseFieldIds(new FieldParser()
        {
            public Object parse(String fieldId)
            {
                String[] fieldVal = StringUtils.split(fieldId, '=');
                if (fieldVal.length != 2)
                {
                    return null; // only allow presets
                }
                if (fieldManager.getOrderableField(fieldVal[0].trim()) != null)
                {
                    return fieldVal[0].trim();
                }
                else
                {
                    log.error("Orderable field with id '" + fieldId + "' does not exist.");
                }
                return null;
            }
        });
    }

    public String getFieldI18nLabelKey(String fieldId)
    {
        return parseFieldLabelKey(fieldId);
    }

    public String getPreset(String fieldId)
    {
        Collection presets = parseFieldIds(new FieldParser()
        {

            public Object parse(String fieldId)
            {
                if (TextUtils.stringSet(fieldId))
                {
                    String[] fieldVal = StringUtils.split(fieldId, '=');
                    if (fieldVal.length == 2)
                    {
                        fieldVal[0] = fieldVal[0].trim();
                        fieldVal[1] = fieldVal[1].trim();
                        return fieldVal;
                    }
                }
                return null;
            }
        });
        for (Iterator iter = presets.iterator(); iter.hasNext();)
        {
            String[] val = (String[]) iter.next();
            if (val[0].equals(fieldId))
            {
                return val[1];
            }
        }
        return null;
    }

    public String getVelocityTemplate()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_SUBTASK_QUICKCREATE_TEMPLATE);
    }

    private Collection parseFieldIds(FieldParser fieldParser)
    {
        String fieldIdsProperty = applicationProperties.getDefaultBackedString(APKeys.JIRA_SUBTASK_QUICKCREATE_FIELDS);
        if (TextUtils.stringSet(fieldIdsProperty))
        {
            String[] fieldIdLabels = StringUtils.splitPreserveAllTokens(fieldIdsProperty, ",");
            if (fieldIdLabels != null)
            {
                Collection ids = new ArrayList();
                for (int i = 0; i < fieldIdLabels.length; i++)
                {
                    String fieldIdLabel = fieldIdLabels[i];

                    String[] fieldEntities = StringUtils.splitPreserveAllTokens(fieldIdLabel, ":");
                    String fieldId = fieldEntities.length == 0 ? "" : fieldEntities[0];
                    Object parsed = fieldParser.parse(fieldId);
                    if (parsed != null)
                    {
                        ids.add(parsed);
                    }
                }
                return ids;
            }
            else
            {
                log.error("The value '" + fieldIdsProperty + "' for property '" + APKeys.JIRA_SUBTASK_QUICKCREATE_FIELDS + "' is invalid . Using default.");
                return DEFAULT_LIST;
            }
        }
        else
        {
            log.error("Could not find entry for '" + APKeys.JIRA_SUBTASK_QUICKCREATE_FIELDS + "' in . Using default.");
            return DEFAULT_LIST;
        }
    }

    private String parseFieldLabelKey(String fieldId)
    {
        String fieldIdsProperty = applicationProperties.getDefaultBackedString(APKeys.JIRA_SUBTASK_QUICKCREATE_FIELDS);
        if (TextUtils.stringSet(fieldIdsProperty))
        {
            String[] fieldIdLabels = StringUtils.splitPreserveAllTokens(fieldIdsProperty, ",");
            if (fieldIdLabels != null)
            {
                for (int i = 0; i < fieldIdLabels.length; i++)
                {
                    String fieldIdLabel = fieldIdLabels[i];
                    String[] fieldEntries = StringUtils.splitPreserveAllTokens(fieldIdLabel, ":");
                    if (fieldEntries.length > 0 && fieldEntries[0].equals(fieldId))
                    {
                        return fieldEntries.length > 1 ? fieldEntries[1] : null;
                    }
                }
            }
        }
        return null;
    }
}

interface FieldParser
{
    Object parse(String fieldId);
}
