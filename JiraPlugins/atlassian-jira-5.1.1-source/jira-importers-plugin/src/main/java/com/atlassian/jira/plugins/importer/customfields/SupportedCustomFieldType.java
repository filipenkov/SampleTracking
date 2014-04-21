package com.atlassian.jira.plugins.importer.customfields;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Set;

public class SupportedCustomFieldType implements Predicate<CustomFieldType> {
    private final Set<String> supportedCustomfields;

    {
        supportedCustomfields = Sets.newHashSet(CustomFieldConstants.DATE_FIELD_TYPE, CustomFieldConstants.DATE_PICKER_FIELD_TYPE,
                CustomFieldConstants.DATETIME_FIELD_TYPE, CustomFieldConstants.FREE_TEXT_FIELD_TYPE,
                CustomFieldConstants.GH_RANKING_FIELD_TYPE, CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE,
                CustomFieldConstants.MULTISELECT_FIELD_TYPE, CustomFieldConstants.NUMBER_FIELD_TYPE,
                CustomFieldConstants.RADIO_FIELD_TYPE, CustomFieldConstants.SELECT_FIELD_TYPE,
                CustomFieldConstants.SINGLE_VERSION_PICKER_TYPE, CustomFieldConstants.TEXT_FIELD_TYPE,
                CustomFieldConstants.URL_FIELD_TYPE, CustomFieldConstants.USER_PICKER_FIELD_TYPE,
                CustomFieldConstants.VERSION_PICKER_TYPE, CustomFieldConstants.LABELS_TYPE,
                CustomFieldConstants.MULTIUSER_PICKER_FIELD_TYPE, CustomFieldConstants.MULTIGROUP_PICKER_FIELD_TYPE,
                CustomFieldConstants.GROUP_PICKER_FIELD_TYPE);
    }


    @Override
    public boolean apply(@Nullable CustomFieldType customFieldType) {
        return customFieldType != null && supportedCustomfields.contains(customFieldType.getKey());
    }
}
