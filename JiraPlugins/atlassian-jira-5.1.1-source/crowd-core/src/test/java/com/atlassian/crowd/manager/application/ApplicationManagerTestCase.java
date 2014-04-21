package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.application.ApplicationDirectoryRemovedEvent;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.util.I18nHelper;
import com.atlassian.event.api.EventPublisher;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.matcher.InvokeOnceMatcher;

import java.util.Date;

public class ApplicationManagerTestCase extends MockObjectTestCase
{
    protected ApplicationManagerGeneric applicationManager = null;
    protected ApplicationImpl application;
    protected DirectoryMapping directoryMapping1;
    protected DirectoryMapping directoryMapping2;
    protected Group group1;
    protected Mock mockRemoteDirectory1;
    protected Mock mockRemoteDirectory2;
    protected DirectoryImpl directory1;
    protected DirectoryImpl directory2;
    protected Mock mockApplicationDAO;
    protected Mock mockEventPublisher;

    protected final String applicationName = "My Test App";

    protected void setUp() throws Exception
    {
        mockApplicationDAO = new Mock(ApplicationDAO.class);
        mockEventPublisher = new Mock(EventPublisher.class);

        applicationManager = new ApplicationManagerGeneric((ApplicationDAO) mockApplicationDAO.proxy(), null, (EventPublisher) mockEventPublisher.proxy());

        // Objects used by all tests
        application = new ApplicationImpl(new InternalEntityTemplate(1L, applicationName, true, new Date(), new Date()));
        application.setCredential(PasswordCredential.unencrypted("secret"));

        mockRemoteDirectory1 = new Mock(RemoteDirectory.class);
        directory1 = new DirectoryImpl()
        {
            public RemoteDirectory getImplementation() throws DirectoryInstantiationException
            {
                return (RemoteDirectory) mockRemoteDirectory1.proxy();
            }

            @Override
            public Long getId()
            {
                return 1L;
            }
        };
        directory1.setName("Test Directory 1");
        directoryMapping1 = new DirectoryMapping(application, directory1, Boolean.FALSE);

        mockRemoteDirectory2 = new Mock(RemoteDirectory.class);
        directory2 = new DirectoryImpl()
        {
            public RemoteDirectory getImplementation() throws DirectoryInstantiationException
            {
                return (RemoteDirectory) mockRemoteDirectory2.proxy();
            }

            @Override
            public Long getId()
            {
                return 2L;
            }
        };
        directory2.setName("Test Directory 2");
        directoryMapping2 = new DirectoryMapping(application, directory2, Boolean.FALSE);

        application.addDirectoryMapping(directoryMapping1.getDirectory(), directoryMapping1.isAllowAllToAuthenticate(), directoryMapping1.getAllowedOperations().toArray(new OperationType[directoryMapping1.getAllowedOperations().size()]));
        application.addDirectoryMapping(directoryMapping2.getDirectory(), directoryMapping2.isAllowAllToAuthenticate(), directoryMapping2.getAllowedOperations().toArray(new OperationType[directoryMapping2.getAllowedOperations().size()]));


        group1 = new GroupTemplate("Remote Group", directory1.getId(), GroupType.GROUP);

//        groupMapping1 = new OldGroupMapping(true, directory1, group1.getName());

//        Mock mockDirectoryDao = new Mock(DirectoryDAO.class);
//        mockDirectoryDao.expects(new InvokeAtMostOnceMatcher()).method("findByID").withAnyArguments().will(new ReturnStub(directory1));
//        applicationManager.setDirectoryDAO((DirectoryDAO) mockDirectoryDao.proxy());

//        application.setGroupMappings(EasyList.build(new OldGroupMapping(true, directory1, "test-group")));
        application.addDirectoryMapping(directory1, Boolean.TRUE);
    }

    public void testRemoveCrowdApplicationFails()
    {
        Application app = ApplicationImpl.newInstance("test", ApplicationType.CROWD);

        try
        {
            applicationManager.remove(app);
            fail("ApplicationManagerException expected");
        }
        catch (ApplicationManagerException e)
        {
            // expected
        }
    }

    public void testRenamePermanantApplicationFails() throws Exception
    {
        ApplicationImpl app = ApplicationImpl.newInstance("new", ApplicationType.CROWD);
        Application oldApp = ApplicationImpl.newInstance("old", ApplicationType.CROWD);

        mockApplicationDAO.expects(once()).method("findById").will(returnValue(oldApp));

        try
        {
            applicationManager.update(app);
            fail("ApplicationManagerException expected");
        }
        catch (ApplicationManagerException e)
        {
            // expected
        }
    }

    public void testDeactivateCrowdApplicationFails() throws Exception
    {
        ApplicationImpl app = ApplicationImpl.newInstance("new", ApplicationType.CROWD);
        app.setActive(false);

        try
        {
            applicationManager.update(app);
            fail("ApplicationManagerException expected");
        }
        catch (ApplicationManagerException e)
        {
            // expected
        }
    }


    public void testRemoveDirectoryFromApplicationWithInvalidArguments() throws ApplicationManagerException
    {
        try
        {
            applicationManager.removeDirectoryFromApplication(null, application);
            fail("We should not allow a null Directory");
        }
        catch (NullPointerException e)
        {

        }

        try
        {
            applicationManager.removeDirectoryFromApplication(directory1, null);
            fail("We should not allow a null Application");
        }
        catch (NullPointerException e)
        {

        }
    }

    public void testRemoveDirectoryFromApplicationThatAllMappingsAreRemoved()
    {
        mockApplicationDAO.expects(new InvokeOnceMatcher()).method("removeDirectoryMapping").with(eq(application.getId()), eq(directory1.getId()));
        mockEventPublisher.expects(new InvokeOnceMatcher()).method("publish").with(isA(ApplicationDirectoryRemovedEvent.class));

        try
        {
            applicationManager.removeDirectoryFromApplication(directory1, application);
        }
        catch (ApplicationManagerException e)
        {
            fail("No exceptions should be thrown");
        }
    }

    public void testRemoveDirectoryFromApplicationThatNotAllMappingsAreRemoved()
    {
        mockApplicationDAO.expects(new InvokeOnceMatcher()).method("removeDirectoryMapping").with(eq(application.getId()), eq(directory1.getId()));
        mockEventPublisher.expects(new InvokeOnceMatcher()).method("publish").with(isA(ApplicationDirectoryRemovedEvent.class));

        // Add two extra mappings
        DirectoryMapping directoryMapping = new DirectoryMapping(application, directory2, Boolean.TRUE);
        application.getDirectoryMappings().add(directoryMapping);

        try
        {
            applicationManager.removeDirectoryFromApplication(directory1, application);
        }
        catch (ApplicationManagerException e)
        {
            fail("No exceptions should be thrown");
        }

        assertFalse(application.getDirectoryMappings().isEmpty());
        assertTrue(application.getDirectoryMappings().contains(directoryMapping));
    }

    protected static class MockI18nHelper implements I18nHelper
    {

        public String getText(String key)
        {
            return "";
        }

        public String getText(String key, String value1)
        {
            return "";
        }

        public String getText(String key, String value1, String value2)
        {
            return "";
        }

        public String getText(String key, Object parameters)
        {
            return "";
        }

        public String getUnescapedText(String key)
        {
            return "";
        }
    }

}
