package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestVersionResolver extends MockControllerTestCase
{
    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        versionManager.getVersionsByName("version1");

        final MockVersion mockVersion1 = new MockVersion(1L, "version1");
        final MockVersion mockVersion2 = new MockVersion(2L, "version1");
        mockController.setReturnValue(CollectionBuilder.newBuilder(mockVersion1, mockVersion2).asList());
        mockController.replay();

        final List<String> result = resolver.getIdsFromName("version1");
        assertEquals(2, result.size());
        assertTrue(result.contains(mockVersion1.getId().toString()));
        assertTrue(result.contains(mockVersion2.getId().toString()));
        mockController.verify();
    }

    @Test
    public void testGetIdsFromNameDoesntExist() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        versionManager.getVersionsByName("abc");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIdsFromName("abc");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testIdExistsNoVersionFlag() throws Exception
    {
        final Long NO_VERSION = new Long(VersionManager.NO_VERSIONS);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersion(NO_VERSION);
        mockController.setReturnValue(null);
        mockController.replay();

        VersionResolver resolver = new VersionResolver(versionManager);
        assertFalse(resolver.idExists(NO_VERSION));
        mockController.verify();
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        final MockVersion mockVersion = new MockVersion(2L, "version1");

        versionManager.getVersion(2L);
        mockController.setReturnValue(mockVersion);
        mockController.replay();

        final Version result = resolver.get(2L);
        assertEquals(mockVersion, result);
        mockController.verify();
    }

    @Test
    public void testGetIdDoesntExist() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        versionManager.getVersion(100L);
        mockController.setReturnValue(null);
        mockController.replay();

        final Version result = resolver.get(100L);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testNameExists() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        versionManager.getVersionsByName("name");
        mockController.setReturnValue(CollectionBuilder.newBuilder(new MockVersion(1000, "name")).asList());
        versionManager.getVersionsByName("noname");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        assertTrue(resolver.nameExists("name"));
        assertFalse(resolver.nameExists("noname"));
        mockController.verify();
    }

    @Test
    public void testIdExists() throws Exception
    {
        final VersionManager versionManager = mockController.getMock(VersionManager.class);

        VersionResolver resolver = new VersionResolver(versionManager);

        versionManager.getVersion(10L);
        mockController.setReturnValue(new MockVersion(1000, "name"));
        versionManager.getVersion(11L);
        mockController.setReturnValue(null);
        mockController.replay();

        assertTrue(resolver.idExists(10L));
        assertFalse(resolver.idExists(11L));
        mockController.verify();
    }
}
