package com.atlassian.jira.issue.fields.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collection;

public class TestVersionHelperBean extends MockControllerTestCase
{
    @Test
    public void testValidateVersionIdNullIds()
    {
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        mockController.replay();

        versionHelperBean.validateVersionIds(null, errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateVersionIdUnkownAndOther()
    {
        Collection versionIds = CollectionBuilder.newBuilder(1L, -1L, 2L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        i18n.getText("issue.field.versions.noneselectedwithother");
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateVersionIdUnkownByItsSelf()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-1L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        mockController.replay();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateVersionIdReallyNegative()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-2L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        i18n.getText("issue.field.versions.releasedunreleasedselected");
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateVersionIdReallyReallyNegative()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-99L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        i18n.getText("issue.field.versions.releasedunreleasedselected");
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }


    @Test
    public void testValidateForProjectNullProject()
    {
        Collection versionIds = CollectionBuilder.newBuilder(-99L).asList();
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, null, errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();

    }

    @Test
    public void testValidateForProjectNullIds()
    {
        VersionHelperBean versionHelperBean = new VersionHelperBean(null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        mockController.replay();

        versionHelperBean.validateVersionForProject(null, new MockProject(11L, "HSP"), errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();

    }

    @Test
    public void testValidateForProjectInvalidVersion()
    {
        Collection versionIds = CollectionBuilder.newBuilder(11L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);
        versionManager.getVersion(11L);
        mockController.setReturnValue(null);

        i18n.getText("issue.field.versions.invalid.version.id", 11L);
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, new MockProject(11L, "HSP"), errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateForProjectInvalidVersionAmoungstGood()
    {
        MockProject project = new MockProject(99L, "Eleven");
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);

        final Version version1 = mockController.getMock(Version.class);

        versionManager.getVersion(11L);
        mockController.setReturnValue(version1);

        version1.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(12L);
        mockController.setReturnValue(null);

        i18n.getText("issue.field.versions.invalid.version.id", 12L);
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateForProjectInvalidVersionAmoungstOtherProjects()
    {
        MockProject project = new MockProject(99L, "Good");
        MockProject badProject = new MockProject(666L, "Bad", "Bad Name");
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);

        final Version version1 = mockController.getMock(Version.class);
        final Version version2 = mockController.getMock(Version.class);

        versionManager.getVersion(11L);
        mockController.setReturnValue(version1);
        version1.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(12L);
        mockController.setReturnValue(version2);
        version2.getProjectObject();
        mockController.setReturnValue(badProject);
        version2.getName();
        mockController.setReturnValue("Bad Version");
        version2.getId();
        mockController.setReturnValue(12L);

        versionManager.getVersion(13L);
        mockController.setReturnValue(null);

        i18n.getText("issue.field.versions.invalid.version.id", 13L);
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateForProjectBadProject()
    {
        MockProject project = new MockProject(99L, "Good", "Good Name");
        MockProject badProject = new MockProject(666L, "Bad", "Bad Name");
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);

        final Version version1 = mockController.getMock(Version.class);
        final Version version2 = mockController.getMock(Version.class);
        final Version version3 = mockController.getMock(Version.class);

        versionManager.getVersion(11L);
        mockController.setReturnValue(version1);
        version1.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(12L);
        mockController.setReturnValue(version2);
        version2.getProjectObject();
        mockController.setReturnValue(badProject);
        version2.getName();
        mockController.setReturnValue("Bad Version");
        version2.getId();
        mockController.setReturnValue(12L);

        versionManager.getVersion(13L);
        mockController.setReturnValue(version3);
        version3.getProjectObject();
        mockController.setReturnValue(project);

        i18n.getText("issue.field.versions.versions.not.valid.for.project", "Bad Version(12)", "Good Name");
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateForProjectBadProjects()
    {
        MockProject project = new MockProject(99L, "Good", "Good Name");
        MockProject badProject = new MockProject(666L, "Bad", "Bad Name");
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);

        final Version version1 = mockController.getMock(Version.class);
        final Version version2 = mockController.getMock(Version.class);
        final Version version3 = mockController.getMock(Version.class);
        final Version version4 = mockController.getMock(Version.class);

        versionManager.getVersion(11L);
        mockController.setReturnValue(version1);
        version1.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(12L);
        mockController.setReturnValue(version2);
        version2.getProjectObject();
        mockController.setReturnValue(badProject);
        version2.getName();
        mockController.setReturnValue("Bad Version");
        version2.getId();
        mockController.setReturnValue(12L);

        versionManager.getVersion(13L);
        mockController.setReturnValue(version3);
        version3.getProjectObject();
        mockController.setReturnValue(badProject);
        version3.getName();
        mockController.setReturnValue("Bad Version 2");
        version3.getId();
        mockController.setReturnValue(13L);

        versionManager.getVersion(14L);
        mockController.setReturnValue(version4);
        version4.getProjectObject();
        mockController.setReturnValue(project);

        i18n.getText("issue.field.versions.versions.not.valid.for.project", "Bad Version(12), Bad Version 2(13)", "Good Name");
        mockController.setReturnValue("Expected String");

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Expected String", errorCollection.getErrors().get("field"));

        mockController.verify();
    }

    @Test
    public void testValidateForProjectAllGood()
    {
        MockProject project = new MockProject(99L, "Good", "Good Name");
        Collection versionIds = CollectionBuilder.newBuilder(11L, 12L, 13L, 14L).asList();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager);

        final Version version1 = mockController.getMock(Version.class);
        final Version version2 = mockController.getMock(Version.class);
        final Version version3 = mockController.getMock(Version.class);
        final Version version4 = mockController.getMock(Version.class);

        versionManager.getVersion(11L);
        mockController.setReturnValue(version1);
        version1.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(12L);
        mockController.setReturnValue(version2);
        version2.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(13L);
        mockController.setReturnValue(version3);
        version3.getProjectObject();
        mockController.setReturnValue(project);

        versionManager.getVersion(14L);
        mockController.setReturnValue(version4);
        version4.getProjectObject();
        mockController.setReturnValue(project);

        mockController.replay();

        versionHelperBean.validateVersionForProject(versionIds, project, errorCollection, i18n, "field");

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }
}
