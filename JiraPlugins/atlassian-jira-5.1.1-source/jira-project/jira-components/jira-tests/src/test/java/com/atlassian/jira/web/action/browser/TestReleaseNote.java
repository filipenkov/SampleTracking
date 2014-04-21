/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.browser;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.util.ReleaseNoteManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.project.version.VersionManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestReleaseNote extends LegacyJiraMockTestCase
{
    public TestReleaseNote(String s)
    {
        super(s);
    }

    public void testGetVersionsCallsProjectManagerCorrectly() throws Exception
    {
        Mock versionManager = new Mock(VersionManager.class);

        final long projectId = 79;
        Version version = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001))));

        final Collection unreleasedVersions = EasyList.build(version);
        final Collection expectedVersions = EasyList.build(new VersionProxy(-2, "Unreleased Versions"), new VersionProxy(version));
        Mock projectManagerMock = new Mock(ProjectManager.class);
        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ABC Project 1", "id", projectId));
        MockProject project = new MockProject(projectGV);
        projectManagerMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(projectId))), project);
        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(projectId), P.IS_FALSE), unreleasedVersions);
        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(projectId), P.IS_FALSE), Collections.EMPTY_LIST);

        ReleaseNote releaseNote = new ReleaseNote((ProjectManager) projectManagerMock.proxy(), null, null, (VersionManager) versionManager.proxy());
        releaseNote.setProjectId(projectId);
        releaseNote.doDefault();

        assertEquals(expectedVersions, releaseNote.getVersions());
        projectManagerMock.verify();
    }

    public void testGetStyleNames()
    {
        ApplicationProperties applicationProperties = new ApplicationPropertiesImpl(null)
        {
            public String getDefaultBackedString(String name)
            {
                if (ReleaseNoteManager.RELEASE_NOTE_NAME.equals(name))
                    return "text, html";
                else if (ReleaseNoteManager.RELEASE_NOTE_TEMPLATE.equals(name))
                    return "text-template, html-template";
                else
                    return null;
            }
        };

        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null)
        {
            public Map getStyles()
            {
                return EasyMap.build("text", "text-template", "html", "html-template");
            }
        };

        ReleaseNote releaseNote = new ReleaseNote(null, releaseNoteManager, null, null);
        final Collection styleNames = releaseNote.getStyleNames();
        assertEquals(2, styleNames.size());
        assertTrue(styleNames.contains("text"));
        assertTrue(styleNames.contains("html"));
    }

    public void testInputResultForInvalidInput() throws Exception
    {
        ReleaseNote releaseNote = new ReleaseNote(null, null, null, null);
        assertEquals(ActionSupport.INPUT, releaseNote.execute());

        releaseNote = new ReleaseNote(null, null, null, null);
        releaseNote.setVersion(null);
        releaseNote.setStyleName(null);
        assertEquals(ActionSupport.INPUT, releaseNote.execute());
        assertNotNull(releaseNote.getErrors().get("version"));

        releaseNote = new ReleaseNote(null, null, null, null);
        releaseNote.setVersion("-1");
        releaseNote.setStyleName("");
        assertEquals(ActionSupport.INPUT, releaseNote.execute());
        assertNotNull(releaseNote.getErrors().get("version"));

        releaseNote = new ReleaseNote(null, null, null, null);
        releaseNote.setVersion("-2");
        releaseNote.setStyleName("");
        assertEquals(ActionSupport.INPUT, releaseNote.execute());
        assertNotNull(releaseNote.getErrors().get("version"));

        releaseNote = new ReleaseNote(null, null, null, null);
        releaseNote.setVersion("-3");
        releaseNote.setStyleName("");
        assertEquals(ActionSupport.INPUT, releaseNote.execute());
        assertNotNull(releaseNote.getErrors().get("version"));
    }

    public void testGetterSetters()
    {
        long projectId = 1L;
        String styleName = "test-style";
        String version = "test-version";

        Mock projectManagerMock = new Mock(ProjectManager.class);
        ReleaseNote releaseNote = new ReleaseNote((ProjectManager) projectManagerMock.proxy(), null, null, null);

        releaseNote.setProjectId(projectId);
        assertEquals(projectId, releaseNote.getProjectId());

        releaseNote.setStyleName(styleName);
        assertEquals(styleName, releaseNote.getStyleName());

        releaseNote.setVersion(version);
        assertEquals(version, releaseNote.getVersion());
    }

    private static class MyVersion extends VersionImpl
    {
        String name = "Version 1";
        long sequence = 1;
        boolean archived = false;
        boolean released = false;

        public MyVersion()
        {
            super(null, null);
        }

//        public GenericValue getProject() throws GenericEntityException
//        {
//            return project1;
//        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Long getSequence()
        {
            return new Long(sequence);
        }

        public void setSequence(Long sequence)
        {
            this.sequence = sequence.longValue();
        }

        public boolean isArchived()
        {
            return archived;
        }

        public void setArchived(boolean archived)
        {
            this.archived = archived;
        }

        public boolean isReleased()
        {
            return released;
        }

        public void setReleased(boolean released)
        {
            this.released = released;
        }
    }

}
