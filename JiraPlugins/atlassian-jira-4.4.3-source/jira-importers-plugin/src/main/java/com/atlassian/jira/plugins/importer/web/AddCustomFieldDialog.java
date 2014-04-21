/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

// does not add anything, just renders the dialog contents & validates
public class AddCustomFieldDialog extends JiraWebActionSupport {
	public static final String CUSTOM_FIELD_TYPE = "customFieldType";
	public static final String CUSTOM_FIELD_NAME = "customFieldName";
	private String customFieldName;
	private String customFieldType;
	private final CustomFieldManager customFieldManager;

	public AddCustomFieldDialog(CustomFieldManager customFieldManager) {
		this.customFieldManager = customFieldManager;
	}

	@Override
	protected void doValidation() {
		// copy-paste from CustomFieldValidatorImpl. Too bad somebody decided to hide it from the world in ContainerRegistrar
		if (StringUtils.isBlank(customFieldType)) {
			addError(CUSTOM_FIELD_TYPE, getText("admin.errors.customfields.no.field.type.specified"));
        } else {
            try {
				if (customFieldManager.getCustomFieldType(customFieldType) == null) {
                    addError(CUSTOM_FIELD_TYPE, getText("admin.errors.customfields.invalid.field.type"));
                }
            } catch (IllegalArgumentException e) {
                // IllegalArgumentException for invalid fieldType plugin key
                addError(CUSTOM_FIELD_TYPE, getText("admin.errors.customfields.invalid.field.type"));
            }
        }

        if (StringUtils.isBlank(customFieldName)) {
            addError(CUSTOM_FIELD_NAME, getText("admin.errors.customfields.no.name"));
        }
	}

	@Override
	protected String doExecute() throws Exception {
		return returnComplete();
	}

	public Map<String, String> getCustomFieldTypes() {
		@SuppressWarnings({"unchecked"})
		final List<CustomFieldType> customFieldTypes = customFieldManager.getCustomFieldTypes();
		final Map<String, String> customFieldMap = Maps.newLinkedHashMap();
		for (CustomFieldType fieldType : customFieldTypes) {
			customFieldMap.put(fieldType.getKey(), fieldType.getName());
		}
		return customFieldMap;
	}

	public String getCustomFieldName() {
		return customFieldName;
	}

	public void setCustomFieldName(String customFieldName) {
		this.customFieldName = customFieldName;
	}

	public String getCustomFieldType() {
		return customFieldType;
	}

	public void setCustomFieldType(String customFieldType) {
		this.customFieldType = customFieldType;
	}
}
