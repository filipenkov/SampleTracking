package com.atlassian.jira.collector.plugin.components.fieldchecker;

import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class MissingFieldsChecker {
    private final FieldLayoutManager fieldLayoutManager;
    private final List<FieldConfiguration> fieldConfigurations;

    public MissingFieldsChecker(final FieldLayoutManager fieldLayoutManager, final List<FieldConfiguration> fieldConfigurations)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldConfigurations = fieldConfigurations;
    }

    public List<Collector> getMisconfiguredCollectors(List<Collector> collectors, final Project project)
    {
        return Lists.newArrayList(Iterables.filter(collectors, new Predicate<Collector>() {
            @Override
            public boolean apply(Collector collector) {
                return isUsedFieldMissing(collector, project);
            }
        }));
    }

    public Map<String, List<String>> getIssueTypeToMissingFieldsMapping(Project project)
    {
        Map<String, List<String>> missingFieldsMap = Maps.newHashMap();

        for (IssueType issueType : project.getIssueTypes())
        {
            List<String> missingFields = Lists.newArrayList();
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueType.getId());
            for (FieldConfiguration fieldToCheck : fieldConfigurations)
            {
                if (fieldLayout.isFieldHidden(fieldToCheck.getFieldName()))
                {
                    missingFields.add(fieldToCheck.getFieldName());
                }
            }
            missingFieldsMap.put(issueType.getId(), missingFields);
        }

        return missingFieldsMap;
    }

    private boolean isUsedFieldMissing(Collector collector, Project project)
    {
        final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, collector.getIssueTypeId()
                .toString());

        for (FieldConfiguration fieldConfiguration : fieldConfigurations)
        {
            final boolean isFieldHidden = fieldLayout.isFieldHidden(fieldConfiguration.getFieldName());
            if (isFieldHidden && fieldConfiguration.isUsed(collector))
            {
                return true;
            }
        }

        return false;
    }
}
