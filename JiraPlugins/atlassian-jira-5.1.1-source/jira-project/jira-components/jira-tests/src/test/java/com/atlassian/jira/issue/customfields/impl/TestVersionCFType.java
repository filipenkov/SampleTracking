package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.customfields.ProjectImportLabelFieldParser;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.project.version.MockVersionManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestVersionCFType extends ListeningTestCase
{
    @Test
    public void testValidate()
    {
        MockVersionManager mockVersionManager = new MockVersionManager();
        mockVersionManager.add(new MockVersion(1, "1.0"));
        mockVersionManager.add(new MockVersion(2, "2.0"));

        ErrorCollection errors = checkValidateFromParams(mockVersionManager, null);
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Collections.<String>emptyList());
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Arrays.asList(new String[]{"1", "2"}));
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Arrays.asList(new String[]{"1", "blah blah blah"}));
        assertEquals(1, errors.getErrors().size());
        assertEquals("issue.field.versions.invalid.version.id blah blah blah", errors.getErrors().get("customfield_1"));
    }

    private ErrorCollection checkValidateFromParams(MockVersionManager mockVersionManager, Collection<String> params)
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        if (null != params)
        {
            customFieldParams.put(null, params);
        }
        ErrorCollection errors = new SimpleErrorCollection();

        FieldConfig fieldConfig = new FieldConfigImpl(1L, "Some Versions", null, null, "customfield_1");

        newVersionCFType(mockVersionManager).validateFromParams(customFieldParams, errors, fieldConfig);
        return errors;
    }

    private VersionCFType newVersionCFType(VersionManager versionManager)
    {
        MockAuthenticationContext mockAuthenticationContext = new MockAuthenticationContext(null, null, new MockI18nHelper());
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);
        return new VersionCFType(null, mockAuthenticationContext, null, null, null, versionHelperBean, null);
    }
}
