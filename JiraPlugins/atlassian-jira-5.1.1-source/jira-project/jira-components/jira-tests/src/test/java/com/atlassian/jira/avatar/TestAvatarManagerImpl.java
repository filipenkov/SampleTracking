package com.atlassian.jira.avatar;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AbstractJiraHome;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.TempDirectoryUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for {@link com.atlassian.jira.avatar.AvatarManagerImpl}.
 *
 * @since v4.0
 */
public class TestAvatarManagerImpl extends MockControllerTestCase
{
    final User fred = new MockUser("fred", "Fred Flintstone", "fred@example.com");
    final User admin = new MockUser("admin", "Fred Flintstone", "fred@example.com");
    private AvatarStore mockAvatarStore;

    @Before
    public void setUp() throws Exception
    {

        mockAvatarStore = getMock(AvatarStore.class);
    }



    @Test
    public void testGetById()
    {
        Avatar a = new AvatarImpl(null, "foo", "mime/type", PROJECT, "otto", false);

        expect(mockAvatarStore.getById(1001L)).andReturn(a);
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        assertEquals(a, avatarManager.getById(1001L));
    }

    @Test
    public void testCreate()
    {
        Avatar a = new AvatarImpl(null, "foo", "mime/type", PROJECT, "otto", false);
        Avatar a2 = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        expect(mockAvatarStore.create(a)).andReturn(a2);
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        assertEquals(a2, avatarManager.create(a));
    }

    @Test
    public void testCreateSad()
    {
        replay();
        Avatar system = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, null, null, null);
        try
        {
            avatarManager.create(system, null, null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException yay)
        {
            assertTrue(yay.getMessage().toLowerCase().contains("system avatars"));
        }
        catch (IOException e)
        {

        }
    }

    @Test
    public void testUpdate()
    {
        Avatar a = new AvatarImpl(99L, "foo", "mime/type", PROJECT, "otto", false);
        mockAvatarStore.update(a); expectLastCall();
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        avatarManager.update(a);
    }

    @Test
    public void testDelete()
    {
        expect(mockAvatarStore.getById(6543L)).andReturn(new AvatarImpl(6543L, "foo", "mime/type", PROJECT, "otto", false));
        expect(mockAvatarStore.delete(6543L)).andReturn(true);
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        assertTrue(avatarManager.delete(6543L, false));
   }

    @Test
    public void testGetAllSystemAvatars()
    {
        List<Avatar> systemAvatars = new ArrayList<Avatar>();
        systemAvatars.add(AvatarImpl.createSystemAvatar("foo", "mime/type", PROJECT));
        systemAvatars.add(AvatarImpl.createSystemAvatar("foo2", "mime/type", PROJECT));

        expect(mockAvatarStore.getAllSystemAvatars(PROJECT)).andReturn(systemAvatars);
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        assertEquals(systemAvatars, avatarManager.getAllSystemAvatars(PROJECT));
    }

    @Test
    public void testGetCustomAvatarsForOwner()
    {
        List<Avatar> customAvatars = new ArrayList<Avatar>();
        customAvatars.add(AvatarImpl.createSystemAvatar("foo", "mime/type", PROJECT));
        customAvatars.add(AvatarImpl.createSystemAvatar("foo2", "mime/type", PROJECT));

        expect(mockAvatarStore.getCustomAvatarsForOwner(PROJECT, "skywalker")).andReturn(customAvatars);
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, null, null, null);
        assertEquals(customAvatars, avatarManager.getCustomAvatarsForOwner(PROJECT, "skywalker"));
    }

    @Test
    public void testCreateAvatarFile() throws IOException
    {
        JiraHome mockJiraHome = new SimpleJiraHome("func_tests_jira_home");
        
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, mockJiraHome, null, null);
        Avatar avatar = new AvatarImpl(123L, "filename", "image/png", PROJECT, "owner", false);
        final File avatarFile = avatarManager.createAvatarFile(avatar, "");
        assertFalse(avatarFile.isDirectory());
    }

    @Test
    public void testCreateWithStream() throws IOException
    {
        JiraHome mockJiraHome = new SimpleJiraHome("func_tests_jira_home");
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, mockJiraHome, null, null);
        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        try
        {
            avatarManager.create(null, imageData, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateSystemAvatarWithStream() throws IOException
    {
        JiraHome mockJiraHome = new SimpleJiraHome("func_tests_jira_home");
        
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, mockJiraHome, null, null);
        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        try
        {
            avatarManager.create(new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", true), imageData, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateAvatarWithNullStream() throws IOException
    {
        JiraHome mockJiraHome = new SimpleJiraHome("func_tests_jira_home");
        
        replay();
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(null, mockJiraHome, null, null);
        try
        {
            avatarManager.create(new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false), null, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateCustomAvatarWithStream() throws IOException
    {
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);

        expect(mockAvatarStore.create(avatar)).andReturn(avatar);

        replay();
        JiraHome mockJiraHome = new SimpleJiraHome("func_tests_jira_home");
        AvatarManagerImpl avatarManager = new AvatarManagerImpl(mockAvatarStore, mockJiraHome, null, null)
        {
            @Override
            File processImage(final Avatar created, final InputStream imageData, final Selection croppingSelection, final ImageSize size) throws IOException
            {
                final File tempFile = File.createTempFile("nothing", ".empty");
                tempFile.deleteOnExit();
                return tempFile;
            }
        };
        final ByteArrayInputStream imageData = new ByteArrayInputStream("foo".getBytes("UTF-8"));
        final Avatar avatarCreated = avatarManager.create(avatar, imageData, null);
        IOUtil.shutdownStream(imageData);
        assertTrue(avatar == avatarCreated); // we return the exact object in the mock, so we're ok asserting this
    }

    @Test
    public void testProcessAvatarDataNoImageGenerated() throws IOException
    {
        final AtomicBoolean processImageCalled = new AtomicBoolean(false);
        final File avData = File.createTempFile("TestAvatarManagerImpl.java", "testProcessAvatarData");
        new FileWriter(avData).append("imageData").close();
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);
        final AtomicBoolean getAvatarFileCalled = new AtomicBoolean(false);

        replay();
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null)
        {
            @Override
            public File getAvatarFile(Avatar av, String sizeFlag)
            {
                if(sizeFlag.equals("small_"))
                {
                    //a file that doesn't exist
                    return new File("blah");
                }
                else
                {
                    assertEquals(sizeFlag, "");
                    getAvatarFileCalled.set(true);
                    return avData;
                }
            }

            @Override
            File processImage(Avatar created, InputStream imageData, Selection croppingSelection, ImageSize size)
                    throws IOException
            {
                processImageCalled.set(true);
                final File tempFile = File.createTempFile("nothing", ".empty");
                tempFile.deleteOnExit();
                return tempFile;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@NotNull final InputStream in)
            {
                consumeCalled.set(true);
            }
        };
        am.processAvatarData(avatar, mockConsumer, AvatarManager.ImageSize.SMALL);
        assertTrue(consumeCalled.get());
        assertTrue(processImageCalled.get());
    }

    @Test
    public void testProcessAvatarData() throws IOException
    {
        final File avData = File.createTempFile("TestAvatarManagerImpl.java", "testProcessAvatarData");
        new FileWriter(avData).append("imageData").close();
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, "otto2", false);
        final AtomicBoolean getAvatarFileCalled = new AtomicBoolean(false);
        
        replay();
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null)
        {
            @Override
            public File getAvatarFile(Avatar av, String sizeFlag)
            {
                assertEquals(avatar, av);
                assertEquals("medium_", sizeFlag);
                getAvatarFileCalled.set(true);
                return avData;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@NotNull final InputStream in)
            {
                assertStreamEqualsString("imageData", in);
                consumeCalled.set(true);
            }
        };
        am.processAvatarData(avatar, mockConsumer, AvatarManager.ImageSize.MEDIUM);
        assertTrue(consumeCalled.get());
    }

    @Test
    public void testProcessAvatarDataSystem() throws IOException
    {
        final InputStream bogus = new ByteArrayInputStream("imageData".getBytes());
        replay();
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null)
        {
            @Override
            InputStream getClasspathStream(final String path)
            {
                return bogus;
            }
        };
        final AtomicBoolean consumeCalled = new AtomicBoolean(false);
        final Consumer<InputStream> mockConsumer = new Consumer<InputStream>()
        {
            public void consume(@NotNull final InputStream in)
            {
                assertStreamEqualsString("imageData", in);
                consumeCalled.set(true);
            }
        };
        am.processAvatarData(avatar, mockConsumer, AvatarManager.ImageSize.MEDIUM);
        assertTrue(consumeCalled.get());
    }

    @Test
    public void testProcessAvatarDataSystemNotFound() throws IOException
    {
        replay();
        final AvatarImpl avatar = new AvatarImpl(null, "foo2", "mime/type", PROJECT, null, true);
        AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, null)
        {
            @Override
            InputStream getClasspathStream(final String path)
            {
                return null;
            }
        };

        try
        {
            am.processAvatarData(avatar, null, AvatarManager.ImageSize.MEDIUM);
            fail("expected IOException because the classpath avatar data was null");
        }
        catch (IOException yay)
        {

        }
    }
    
    @Test
    public void testGetDefaultAvatarId()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        expect(mockApplicationProperties.getString("jira.avatar.default.id")).andReturn("3423");
        expect(mockApplicationProperties.getString("jira.avatar.user.default.id")).andReturn("4444");
        replay(mockApplicationProperties);
        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, mockApplicationProperties, null);
        final Long id = am.getDefaultAvatarId(Avatar.Type.PROJECT);
        assertEquals(new Long(3423), id);
        final Long userId = am.getDefaultAvatarId(Avatar.Type.USER);
        assertEquals(new Long(4444), userId);
        verify(mockApplicationProperties);
    }

    @Test
    public void testHasPermissionToViewProject()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final ProjectManager mockProjectManager = createMock(ProjectManager.class);

        expect(mockPermissionManager.hasPermission(0, (User) null)).andReturn(false);
        final Project mockProject = new MockProject(10012L);
        expect(mockProjectManager.getProjectObj(10012L)).andReturn(mockProject);
        expect(mockPermissionManager.hasPermission(23, mockProject, (User) null)).andReturn(false);
        expect(mockPermissionManager.hasPermission(10, mockProject, (User) null)).andReturn(true);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);
        final Project mockProject2 = new MockProject(10022L);
        expect(mockProjectManager.getProjectObj(10022L)).andReturn(mockProject2);
        expect(mockPermissionManager.hasPermission(23, mockProject2, fred)).andReturn(false);
        expect(mockPermissionManager.hasPermission(10, mockProject2, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, admin)).andReturn(true);
        expect(mockProjectManager.getProjectObj(10022L)).andReturn(mockProject2);
        expect(mockPermissionManager.hasPermission(23, mockProject2, admin)).andReturn(true);
        expect(mockPermissionManager.hasPermission(10, mockProject2, admin)).andReturn(false);


        replay(mockPermissionManager, mockProjectManager);

        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, mockPermissionManager)
        {
            @Override
            ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };
        assertTrue(am.hasPermissionToView(null, Avatar.Type.PROJECT, "10012"));
        assertFalse(am.hasPermissionToView(fred, Avatar.Type.PROJECT, "10022"));
        assertFalse(am.hasPermissionToView(fred, Avatar.Type.PROJECT, "INVALIDPROJECTID"));
        assertFalse(am.hasPermissionToView(fred, Avatar.Type.PROJECT, null));
        assertTrue(am.hasPermissionToView(admin, Avatar.Type.PROJECT, "10022"));


        verify(mockPermissionManager, mockProjectManager);
    }

    @Test
    public void testHasPermissionToViewUser()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);

        expect(mockPermissionManager.hasPermission(1, (User) null)).andReturn(false);
        expect(mockPermissionManager.hasPermission(1, fred)).andReturn(true);
        expect(mockPermissionManager.hasPermission(1, fred)).andReturn(true);
        expect(mockPermissionManager.hasPermission(1, fred)).andReturn(true);

        replay(mockPermissionManager);

        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, mockPermissionManager);

        assertFalse(am.hasPermissionToView(null, Avatar.Type.USER, "admin"));
        assertTrue(am.hasPermissionToView(fred, Avatar.Type.USER, null));
        assertTrue(am.hasPermissionToView(fred, Avatar.Type.USER, "fred"));
        assertTrue(am.hasPermissionToView(fred, Avatar.Type.USER, "admin"));

        verify(mockPermissionManager);
    }

    @Test
    public void testHasPermissionToEditUser()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);
        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, admin)).andReturn(true);


        replay(mockPermissionManager);

        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, mockPermissionManager);

        assertFalse(am.hasPermissionToEdit(null, Avatar.Type.USER, "admin"));
        assertFalse(am.hasPermissionToEdit(fred, Avatar.Type.USER, null));
        assertTrue(am.hasPermissionToEdit(fred, Avatar.Type.USER, "fred"));
        assertFalse(am.hasPermissionToEdit(fred, Avatar.Type.USER, "admin"));
        assertTrue(am.hasPermissionToEdit(admin, Avatar.Type.USER, "fred"));

        verify(mockPermissionManager);
    }

    @Test
    public void testHasPermissionToEditProject()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final ProjectManager mockProjectManager = createMock(ProjectManager.class);

        expect(mockPermissionManager.hasPermission(0, (User) null)).andReturn(false);
        final Project mockProject = new MockProject(10012L);
        expect(mockProjectManager.getProjectObj(10012L)).andReturn(mockProject);
        expect(mockPermissionManager.hasPermission(23, mockProject, (User) null)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);
        final Project mockProject2 = new MockProject(10022L);
        expect(mockProjectManager.getProjectObj(10022L)).andReturn(mockProject2);
        expect(mockPermissionManager.hasPermission(23, mockProject2, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);
        expect(mockPermissionManager.hasPermission(0, fred)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, admin)).andReturn(true);
        expect(mockProjectManager.getProjectObj(10022L)).andReturn(mockProject2);
        expect(mockPermissionManager.hasPermission(23, mockProject2, admin)).andReturn(false);

        expect(mockPermissionManager.hasPermission(0, admin)).andReturn(false);
        expect(mockProjectManager.getProjectObj(10022L)).andReturn(mockProject2);
        expect(mockPermissionManager.hasPermission(23, mockProject2, admin)).andReturn(true);


        replay(mockPermissionManager, mockProjectManager);

        final AvatarManagerImpl am = new AvatarManagerImpl(null, null, null, mockPermissionManager)
        {
            @Override
            ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };
        assertFalse(am.hasPermissionToEdit(null, Avatar.Type.PROJECT, "10012"));
        assertFalse(am.hasPermissionToEdit(fred, Avatar.Type.PROJECT, "10022"));
        assertFalse(am.hasPermissionToEdit(fred, Avatar.Type.PROJECT, "INVALIDPROJECTID"));
        assertFalse(am.hasPermissionToEdit(fred, Avatar.Type.PROJECT, null));
        assertTrue(am.hasPermissionToEdit(admin, Avatar.Type.PROJECT, "10022"));
        //just a project admin this time!
        assertTrue(am.hasPermissionToEdit(admin, Avatar.Type.PROJECT, "10022"));


        verify(mockPermissionManager, mockProjectManager);
    }


    private void assertStreamEqualsString(final String expectedContents, final InputStream in)
    {
        try
                {
                    assertEquals(expectedContents, IOUtil.toString(in));

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class SimpleJiraHome extends AbstractJiraHome
    {
        private final String name;
        private File tempFile;

        private SimpleJiraHome(final String name)
        {
            this.name = name;
        }

        @NotNull
        public File getHome()
        {
            if (tempFile != null)
            {
                return tempFile;
            }

            final File file = TempDirectoryUtil.createTempDirectory(name);
            if (!file.exists())
            {
                assertTrue(file.mkdir());
            }
            file.deleteOnExit();
            tempFile = file;
            return file;
        }
    }
}
