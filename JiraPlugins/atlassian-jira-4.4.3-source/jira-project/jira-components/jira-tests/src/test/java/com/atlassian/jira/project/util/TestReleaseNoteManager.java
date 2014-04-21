/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.util;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericValue;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestReleaseNoteManager extends AbstractUsersTestCase
{
    public TestReleaseNoteManager(String s)
    {
        super(s);
    }

    public void testGetReleaseNoteStyles()
    {
        final Map expectedStyles = EasyMap.build("text", "text-template", "html", "html-template");
        final String releaseNoteName = "text, html";
        final String releaseNoteTemplate = "text-template, html-template";
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties(releaseNoteName, releaseNoteTemplate);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        assertEquals(expectedStyles, releaseNoteManager.getStyles());
    }

    public void testGetReleaseNoteStylesHandlesNulls()
    {
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties(null, null);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        releaseNoteManager.getStyles(); // just check no exceptions
    }

    public void testGetReleaseNoteStylesWithCorruptedProperties()
    {
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", null);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        try
        {
            releaseNoteManager.getStyles();
        }
        catch (RuntimeException re)
        {
            assertNotNull(re.getMessage());
        }
    }

    public void testGetReleaseNoteWithInvalidStyleName()
    {
        User testUser = UtilsForTests.getTestUser("testuser");
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        Version version = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template", "text");
        VelocityManager velocityManager = new MyVelocityManager();
        ConstantsManager constantsManager = new MyConstantsManager();

        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, velocityManager, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "xml", version, testUser, project));
    }

    public void testGetReleaseNoteWithInvalidStyleNameAndInvalidDefault()
    {
        User testUser = UtilsForTests.getTestUser("testuser");
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        Version version = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template", "nicks");
        VelocityManager velocityManager = new MyVelocityManager();
        ConstantsManager constantsManager = new MyConstantsManager();

        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, velocityManager, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "xml", version, testUser, project));
    }

    public void testGetReleaseNote()
    {
        User testUser = UtilsForTests.getTestUser("testuser");
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        Version version = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template");
        VelocityManager velocityManager = new MyVelocityManager();
        ConstantsManager constantsManager = new MyConstantsManager();

        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, velocityManager, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "text", version, testUser, project));
    }

    private static class MyConstantsManager implements ConstantsManager
    {
        public Collection getAllIssueTypeObjects()
        {
            return Collections.EMPTY_LIST;
        }

        public Status getStatusByName(String name)
        {
            return null;
        }

        public void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName)
        {

        }

        public Collection getRegularIssueTypeObjects()
        {
            return null;
        }

        public Collection getSubTaskIssueTypeObjects()
        {
            return null;
        }

        public Collection getResolutionObjects()
        {
            return null;
        }

        public List getAllIssueTypeIds()
        {
            return null;
        }

        public IssueConstant getIssueConstant(GenericValue issueConstantGV)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public Collection getPriorities()
        {
            return null;
        }

        public Collection getPriorityObjects()
        {
            return null;
        }

        public GenericValue getPriority(String id)
        {
            return null;
        }

        public String getPriorityName(String id)
        {
            return null;
        }

        public Priority getPriorityObject(String id)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public GenericValue getDefaultPriority() {
            return null;
        }

        public Priority getDefaultPriorityObject() {
            return null;
        }

        public void refreshPriorities()
        {
        }

        public Collection getResolutions()
        {
            return null;
        }

        public GenericValue getResolution(String id)
        {
            return null;
        }

        public Resolution getResolutionObject(String id)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void refreshResolutions()
        {
        }

        public Collection getIssueTypes()
        {
            GenericValue issuetype1 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "100", "name", "testtype", "description", "test issue type"));
            GenericValue issuetype2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "200", "name", "another testtype", "description", "another test issue type"));
            return EasyList.build(issuetype1, issuetype2);
        }

        public GenericValue getIssueType(String id)
        {
            return null;
        }

        public IssueType getIssueTypeObject(String id)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void refreshIssueTypes()
        {
        }

        public GenericValue getStatus(String id)
        {
            return null;
        }

        public Status getStatusObject(String id)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public Collection getStatuses()
        {
            return null;
        }

        public Collection getStatusObjects()
        {
            return null;
        }

        public void refreshStatuses()
        {
        }

        public GenericValue getConstant(String constantType, String id)
        {
            return null;
        }

        public IssueConstant getConstantObject(String constantType, String id)
        {
            return null;
        }

        public Collection getConstantObjects(String constantType)
        {
            return null;
        }

        public List convertToConstantObjects(String constantType, Collection ids)
        {
            return null;
        }

        public boolean constantExists(String constantType, String name)
        {
            return false;
        }

        public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException
        {
            return null;
        }

        public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl)
        {
            throw new UnsupportedOperationException();
        }

        public void removeIssueType(String id) throws RemoveException
        {

        }

        public IssueConstant getConstantByNameIgnoreCase(final String constantType, final String name)
        {
            return null;
        }

        public GenericValue getConstantByName(String constantType, String name)
        {
            return null;
        }

        public IssueConstant getIssueConstantByName(String constantType, String name)
        {
            return null;
        }

        public Collection getSubTaskIssueTypes()
        {
            return null;
        }

        public List getEditableSubTaskIssueTypes()
        {
            return null;
        }

        public List getAllIssueTypes()
        {
            return null;
        }

        public void storeIssueTypes(List issueTypes)
        {
            throw new UnsupportedOperationException();
        }

        public void refresh()
        {
            throw new UnsupportedOperationException();
        }

        public List expandIssueTypeIds(Collection issueTypeIds)
        {
            throw new UnsupportedOperationException();
        }
    }

    // Mock Application Properties for tests
    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private final String changeLogName;
        private final String changeLogTemplate;
        private final String defaultTemplate;

        public MyApplicationProperties(String changeLogName, String changeLogTemplate)
        {
            super(null);
            this.changeLogName = changeLogName;
            this.changeLogTemplate = changeLogTemplate;
            defaultTemplate = null;
        }
        public MyApplicationProperties(String changeLogName, String changeLogTemplate, String defaultTemplate)
        {
            super(null);
            this.changeLogName = changeLogName;
            this.changeLogTemplate = changeLogTemplate;
            this.defaultTemplate = defaultTemplate;
        }

        @Override
        public String getDefaultBackedString(String name)
        {
            if (ReleaseNoteManager.RELEASE_NOTE_NAME.equals(name))
            {
                return changeLogName;
            }
            else if (ReleaseNoteManager.RELEASE_NOTE_TEMPLATE.equals(name))
            {
                return changeLogTemplate;
            }
            else if (ReleaseNoteManager.RELEASE_NOTE_DEFAULT.equals(name))
            {
                return defaultTemplate;
            }
            else
            {
                return null;
            }
        }

        @Override
        public String getString(String name)
        {
            if (APKeys.JIRA_BASEURL.equals(name))
            {
                return "jira";
            }
            else
            {
                return null;
            }
        }
    }

    // Mock Velocity class for tests
    private static class MyVelocityManager implements VelocityManager
    {
        public String getBody(String string, String string1, Map map) throws VelocityException
        {
            assertEquals(ReleaseNoteManager.TEMPLATES_DIR, string);
            assertEquals("text-template", string1);
            return "BODY";
        }

        public String getBody(String string, String string1, String string2, Map map) throws VelocityException
        {
            return null;
        }

        public String getEncodedBody(String string, String string1, String string2, Map map) throws VelocityException
        {
            return null;
        }

        public String getEncodedBody(String string, String string1, String string2, String string3, Map map) throws VelocityException
        {
            return null;
        }

        public String getEncodedBodyForContent(String string, String string1, Map map) throws VelocityException
        {
            return null;
        }

        public DateFormat getDateFormat()
        {
            return null;
        }

        public String getEncodedBody(String s, String s1, String s2, String s3, Context context) throws VelocityException
        {
            return null;
        }
    }

    private class MyVersion extends VersionImpl
    {
        String name;
        long id;
        long sequence;
        boolean archived;
        boolean released;

        public MyVersion(ProjectManager projectManager, GenericValue versionGV)
        {
            super(null, versionGV);
        }
    }
}
