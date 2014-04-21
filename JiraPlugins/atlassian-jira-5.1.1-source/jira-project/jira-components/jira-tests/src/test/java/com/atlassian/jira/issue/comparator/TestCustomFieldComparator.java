/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericValue;

public class TestCustomFieldComparator extends LegacyJiraMockTestCase
{
    private CustomFieldComparator customFieldComparator;
    private GenericValue project;
    private GenericValue issueType;
    private GenericValue customField2;
    private GenericValue customField1;
    private GenericValue customField3;

    public TestCustomFieldComparator(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));
        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "type"));
        customFieldComparator = new CustomFieldComparator();

        customField1 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "ABC"));
        customField2 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("project", project.getLong("id"), "name", "BBC"));
        customField3 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("issuetype", issueType.getString("id"), "name", "CBC"));
    }

    public void testCompareGlobalCustomFieldAndProjectCustomField()
    {
        int i = customFieldComparator.compare(customField1, customField2);
        assertTrue(i < 0);
        i = customFieldComparator.compare(customField2, customField1);
        assertTrue(i > 0);
    }

    public void testCompareGlobalCustomFieldAndIssueTypeCustomField()
    {
        int i = customFieldComparator.compare(customField1, customField3);
        assertTrue(i < 0);
        i = customFieldComparator.compare(customField3, customField1);
        assertTrue(i > 0);
    }

    public void testCompareProjectCustomFieldAndIssueTypeCustomField()
    {
        int i = customFieldComparator.compare(customField2, customField3);
        assertTrue(i < 0);
        i = customFieldComparator.compare(customField3, customField2);
        assertTrue(i > 0);
    }
    /*
    public void testCompareWithACustomFieldWithInvalidIssueType()
    {
        GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Valid IssueType", "sequence", new Long(1)));
        GenericValue customField1 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "Test Custom Field With Value Issue Type", "issuetype", issueType.getString("id"), "fieldtype", new Long(CustomFieldType.TEXT)));
        GenericValue customField2 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "Test Custom Field With Value Invalue Issue Type", "issuetype", "2", "fieldtype", new Long(CustomFieldType.TEXT)));

        CustomFieldComparator customFieldComparator = new CustomFieldComparator();
        customFieldComparator.compare(customField2, customField1);
    }

    public void testCompareWithACustomFieldWithInvalidProject()
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "ABC"));
        GenericValue customField1 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "Test Custom Field With Value Issue Type", "project", project.getLong("id"), "fieldtype", new Long(CustomFieldType.TEXT)));
        GenericValue customField2 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "Test Custom Field With Value Invalue Issue Type", "project", new Long(2), "fieldtype", new Long(CustomFieldType.TEXT)));

        CustomFieldComparator customFieldComparator = new CustomFieldComparator();
        customFieldComparator.compare(customField2, customField1);
    }
    */
}
