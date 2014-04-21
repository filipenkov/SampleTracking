package com.atlassian.jira.plugins.importer.customfields;

import com.atlassian.jira.issue.fields.CustomField;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class SupportedCustomFieldPredicate implements Predicate<CustomField> {
    private final SupportedCustomFieldType predicate = new SupportedCustomFieldType();

    @Override
    public boolean apply(@Nullable CustomField customField) {
        return customField != null && predicate.apply(customField.getCustomFieldType());
    }
}