package com.atlassian.jira.bc.workflow;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.JiraDraftWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import junit.framework.Assert;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestDefaultWorkflowService extends ListeningTestCase
{
    I18nHelper mockI18nBean;

    @Before
    public void setUp() throws Exception
    {
        mockI18nBean = new I18nHelper()
        {
            public String getText(final String key)
            {
                return key;
            }

            public String getUnescapedText(final String key)
            {
                return key;
            }

            public Locale getLocale()
            {
                return null;
            }

            public String getText(final String key, final String value1)
            {
                return key;
            }

            public String getText(final String key, final String value1, final String value2)
            {
                return null;
            }

            public String getText(final String key, final String value1, final String value2, final String value3) //(added by Shaun during i18n)
            {
                return null;
            }

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
            {
                return null;
            }

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
            {
                return null;
            }

            public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
            {
                return null;
            }

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
            {
                return null;
            }

            public ResourceBundle getDefaultResourceBundle()
            {
                return null;
            }

            public String getText(final String key, final Object parameters)
            {
                return null;
            }

            public Set<String> getKeysForPrefix(final String prefix)
            {
                return null;
            }

            public String getText(String key, Object value1, Object value2, Object value3)
            {
                return getText(key, EasyList.build(value1, value2, value3));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8));
            }            
        };

    }

    @Test
    public void testGetDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());
        mockWorkflowManager.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("testworkflow") }, null);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.getDraftWorkflow(jiraServiceContext, "testworkflow");
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testGetDraftWorkflowWithNoAdminPermission()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };

        workflowService.getDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.admin.permission",
            jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
    }

    @Test
    public void testGetDraftWorkflowWithNoParent()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, null);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.getDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.retrieve.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();

        jiraServiceContext.getErrorCollection().setErrorMessages(new ArrayList());
        workflowService.getDraftWorkflow(jiraServiceContext, null);
        assertEquals("admin.workflows.service.error.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());

        jiraServiceContext.getErrorCollection().setErrorMessages(new ArrayList());
        workflowService.getDraftWorkflow(jiraServiceContext, "");
        assertEquals("admin.workflows.service.error.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testCreateDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());
        mockWorkflowManager.expectAndReturn("isActive", P.ANY_ARGS, Boolean.TRUE);
        mockWorkflowManager.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq("testuser"), P.eq("testworkflow") }, null);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCreateDraftWorkflowNoAdminPermission()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.admin.permission",
            jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCreateDraftWorkflowWithNullUser()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());
        mockWorkflowManager.expectAndReturn("isActive", P.ANY_ARGS, Boolean.TRUE);
        mockWorkflowManager.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq(""), P.eq("testworkflow") }, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCreateDraftWorkflowWithNoParentWorkflow()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, null);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();

        jiraServiceContext.getErrorCollection().setErrorMessages(new ArrayList());
        workflowService.createDraftWorkflow(jiraServiceContext, null);
        assertEquals("admin.workflows.service.error.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());

        jiraServiceContext.getErrorCollection().setErrorMessages(new ArrayList());
        workflowService.createDraftWorkflow(jiraServiceContext, "");
        assertEquals("admin.workflows.service.error.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());

    }

    @Test
    public void testCreateDraftWorkflowWithInActiveParentWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());
        mockWorkflowManager.expectAndReturn("isActive", P.ANY_ARGS, Boolean.FALSE);

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {

            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.parent.not.active", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCreateDraftWorkflowIllegalState()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());
        mockWorkflowManager.expectAndReturn("isActive", P.ANY_ARGS, Boolean.TRUE);
        //this may happen if two users try to create 2 drafts at the same time.
        mockWorkflowManager.expectAndThrow("createDraftWorkflow", new Constraint[] { P.eq("testuser"), P.eq("testworkflow") },
            new IllegalStateException("Draft already exists"));

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.createDraftWorkflow(jiraServiceContext, "testworkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorMessage("admin.workflows.service.error.draft.exists.or.workflow.not.active", jiraServiceContext.getErrorCollection());
        mockWorkflowManager.verify();
    }

    @Test
    public void testDeleteDraftWorkflow()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("testWorkflow") }, Boolean.TRUE);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        workflowService.deleteDraftWorkflow(null, "testWorkflow");

        mockWorkflowManager.verify();
    }

    @Test
    public void testDeleteDraftWorkflowWithNoAdminPermission()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.deleteDraftWorkflow(jiraServiceContext, "testWorkflow");
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.admin.permission",
            jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
    }

    @Test
    public void testDeleteDraftWorkflowWithNullParent()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {

            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.deleteDraftWorkflow(jiraServiceContext, null);

        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("admin.workflows.service.error.delete.no.parent", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
    }

    @Test
    public void testUpdateDraftWorkflow()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(new DescriptorFactory().createWorkflowDescriptor());
        mockJiraDraftWorkflow.isEditable();
        mockJiraDraftWorkflowControl.setDefaultReturnValue(true);
        mockJiraDraftWorkflowControl.replay();

        mockWorkflowManager.expectVoid("updateWorkflow", new Constraint[] { P.eq("testuser"), P.eq(mockJiraDraftWorkflow) });

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, mockJiraDraftWorkflow);

        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
        mockJiraDraftWorkflowControl.verify();
    }

    @Test
    public void testUpdateWorkflowNullWorkflow()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, null);

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.update.no.workflow", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testUpateWorkflowNullDescriptor()
    {
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(null);
        mockJiraDraftWorkflowControl.replay();

        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, mockJiraDraftWorkflow);

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.update.no.workflow", errorCollection.getErrorMessages().iterator().next());

        mockJiraDraftWorkflowControl.verify();
    }

    @Test
    public void testUpdateWorkflowNoAdminPermission()
    {
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflowControl.replay();

        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };

        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, mockJiraDraftWorkflow);

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.admin.permission", errorCollection.getErrorMessages().iterator().next());
        mockJiraDraftWorkflowControl.verify();

    }

    @Test
    public void testUpdateWorkflowNullUsername()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(new DescriptorFactory().createWorkflowDescriptor());
        mockJiraDraftWorkflow.isEditable();
        mockJiraDraftWorkflowControl.setDefaultReturnValue(true);
        mockJiraDraftWorkflowControl.replay();

        mockWorkflowManager.expectVoid("updateWorkflow", new Constraint[] { P.eq(""), P.eq(mockJiraDraftWorkflow) });

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, mockJiraDraftWorkflow);

        mockWorkflowManager.verify();
        mockJiraDraftWorkflowControl.verify();
    }

    @Test
    public void testUpdateWorkflowNotEditable()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(new DescriptorFactory().createWorkflowDescriptor());
        mockJiraDraftWorkflow.isEditable();
        mockJiraDraftWorkflowControl.setDefaultReturnValue(false);
        mockJiraDraftWorkflowControl.replay();

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.updateWorkflow(jiraServiceContext, mockJiraDraftWorkflow);

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.not.editable", errorCollection.getErrorMessages().iterator().next());
        mockWorkflowManager.verify();
        mockJiraDraftWorkflowControl.verify();
    }

    @Test
    public void testOverwriteWorkflowNullName()
    {
        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(null, null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, null);

        //ensure we have the right error.
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.overwrite.no.parent", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testOverwriteWorkflowNoAdminPermission()
    {
        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(null, null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");

        //ensure we have the right error.
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("admin.workflows.service.error.no.admin.permission", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testOverwriteWorkflow()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);
        mockWorkflowManager.expectVoid("overwriteActiveWorkflow", new Constraint[] { P.eq("testuser"), P.eq("jiraworkflow") });

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            @Override
            public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
            {
            // Don't do any validation for this test.
            }
        };
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");

        //ensure we have the right error.
        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testOverwriteWorkflowNullUser()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);
        mockWorkflowManager.expectVoid("overwriteActiveWorkflow", new Constraint[] { P.eq(""), P.eq("jiraworkflow") });

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            @Override
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            @Override
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            @Override
            public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
            {
            // Don't do any validation for this test.
            }
        };
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");

        //ensure we have the right error.
        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testValidateOverwriteWorkflowNoPermission()
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.IS_ANYTHING, P.IS_ANYTHING }, Boolean.FALSE);
        final PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();
        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(null, null, permissionManager)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // Null workflow name is invalid
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, null);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateOverwriteWorkflowInvalidWorkflowName()
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        // Mock out the PermissionManager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.IS_ANYTHING, P.IS_ANYTHING }, Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();
        // Mock out the WorkflowManager
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("getWorkflow", new Constraint[] { P.eq("Some Rubbish") }, null);
        final WorkflowManager workFlowManager = (WorkflowManager) mockWorkflowManager.proxy();

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(workFlowManager, null, permissionManager)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // Null workflow name is invalid
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, null);
        assertErrorMessage("admin.workflows.service.error.overwrite.no.parent", jiraServiceContext.getErrorCollection());

        // Empty workflow name is invalid
        resetErrorCollection(errorCollection);
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "");
        assertErrorMessage("admin.workflows.service.error.overwrite.no.parent", jiraServiceContext.getErrorCollection());

        // Workflow name is unknown
        resetErrorCollection(errorCollection);
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "Some Rubbish");
        assertErrorMessage("admin.workflows.service.error.overwrite.no.parent", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateOverwriteWorkflowNoDraftWorkflow() throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        // Mock out the PermissionManager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.IS_ANYTHING, P.IS_ANYTHING }, Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();
        // Mock out the WorkflowManager
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        final WorkflowManager workFlowManager = (WorkflowManager) mockWorkflowManager.proxy();
        mockWorkflowManager.setStrict(true);
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isActive", Boolean.TRUE);

        // the live workflow exists
        mockWorkflowManager.expectAndReturn("getWorkflow", new Constraint[] { P.eq("My Workflow") }, mockJiraWorkflow.proxy());
        // but there is no draft
        mockWorkflowManager.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("My Workflow") }, null);

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(workFlowManager, null, permissionManager)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // Workflow name is unknown
        resetErrorCollection(errorCollection);
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("admin.workflows.service.error.overwrite.no.draft", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateOverwriteWorkflowInactiveParent() throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        // Mock out the PermissionManager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.IS_ANYTHING, P.IS_ANYTHING }, Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();
        // Mock out the WorkflowManager
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        final WorkflowManager workFlowManager = (WorkflowManager) mockWorkflowManager.proxy();
        mockWorkflowManager.setStrict(true);
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isActive", Boolean.FALSE);

        // the live workflow exists
        mockWorkflowManager.expectAndReturn("getWorkflow", new Constraint[] { P.eq("My Workflow") }, mockJiraWorkflow.proxy());
        // but there is no draft

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(workFlowManager, null, permissionManager)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // Workflow name is unknown
        resetErrorCollection(errorCollection);
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("admin.workflows.service.error.overwrite.inactive.parent", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateOverwriteWorkflow() throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        // Mock out the PermissionManager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.IS_ANYTHING, P.IS_ANYTHING }, Boolean.TRUE);
        final PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();

        // Mock out the WorkflowManager
        final Mock workflowManagerMock = new Mock(WorkflowManager.class);
        workflowManagerMock.setStrict(true);
        final WorkflowManager workflowManager = (WorkflowManager) workflowManagerMock.proxy();
        // create the live JiraWorkflow
        final WorkflowDescriptor oldWorkflowDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        final MockJiraWorkflow oldJiraWorkflow = new MockJiraWorkflow();
        workflowManagerMock.expectAndReturn("getWorkflow", new Constraint[] { P.eq("My Workflow") }, oldJiraWorkflow);
        // create the draft JiraWorkflow
        final MockJiraWorkflow newJiraWorkflow = new MockJiraWorkflow();
        workflowManagerMock.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("My Workflow") }, newJiraWorkflow);
        final AtomicBoolean validateAddWorkflowTransitionCalled = new AtomicBoolean(false);

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(workflowManager, null, permissionManager)
        {
            I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }

            public void validateAddWorkflowTransitionToDraft(final JiraServiceContext jiraServiceContext, final JiraWorkflow workflow, final int stepId)
            {
                validateAddWorkflowTransitionCalled.set(true);
                super.validateAddWorkflowTransitionToDraft(jiraServiceContext, workflow, stepId);
            }
        };

        // Set up a minimal workflow
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(1, "Closed");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("The draft workflow does not contain required status 'Open'.", errorCollection);

        // Now make the workflow have the required status but with wrong step ID
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(2, "Open");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("You cannot change the association between step '1' and status 'Open'.", errorCollection);

        // OK - now finally make a trivial valid change
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(2, "Closed");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertFalse(errorCollection.hasAnyErrors());

        // Bigger set of steps
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        oldJiraWorkflow.addStep(2, "Assigned");
        oldJiraWorkflow.addStep(3, "Resolved");
        oldJiraWorkflow.addStep(4, "Closed");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(4, "Closed");
        newJiraWorkflow.addStep(2, "Assigned");
        newJiraWorkflow.addStep(3, "Resolved");
        newJiraWorkflow.addStep(5, "Re-Opened");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertFalse(errorCollection.hasAnyErrors());

        // OK - now finally make a trivial valid change
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        oldJiraWorkflow.addStep(2, "Assigned");
        oldJiraWorkflow.addStep(3, "Resolved");
        oldJiraWorkflow.addStep(4, "Closed");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(2, "Assigned");
        newJiraWorkflow.addStep(4, "Closed");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("The draft workflow does not contain required status 'Resolved'.", errorCollection);

        // OK - now finally make a trivial valid change
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        oldJiraWorkflow.addStep(2, "Assigned");
        oldJiraWorkflow.addStep(3, "Resolved");
        oldJiraWorkflow.addStep(4, "Closed");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(2, "Assigned");
        newJiraWorkflow.addStep(3, "Resolved");
        newJiraWorkflow.addStep(4, "Rubbish");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("The draft workflow does not contain required status 'Closed'.", errorCollection);

        // OK - now finally make a trivial valid change
        errorCollection.setErrorMessages(new ArrayList());
        oldJiraWorkflow.clear();
        oldJiraWorkflow.addStep(1, "Open");
        oldJiraWorkflow.addStep(2, "Assigned");
        oldJiraWorkflow.addStep(3, "Resolved");
        oldJiraWorkflow.addStep(4, "Closed");
        newJiraWorkflow.clear();
        newJiraWorkflow.addStep(1, "Open");
        newJiraWorkflow.addStep(2, "Assigned");
        newJiraWorkflow.addStep(3, "Resolved");
        newJiraWorkflow.addStep(6, "Closed");
        defaultWorkflowService.validateOverwriteWorkflow(jiraServiceContext, "My Workflow");
        assertErrorMessage("You cannot change the association between step '4' and status 'Closed'.", errorCollection);
        assertTrue(validateAddWorkflowTransitionCalled.get());
    }

    @Test
    public void testGetWorkflowNullName()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.getWorkflow(jiraServiceContext, null);
        assertErrorMessage("admin.workflows.service.error.null.name", errorCollection);

        resetErrorCollection(errorCollection);
        workflowService.getWorkflow(jiraServiceContext, "");
        assertErrorMessage("admin.workflows.service.error.null.name", errorCollection);
    }

    @Test
    public void testValidateCopyWorkflowNoAdminRights()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.validateCopyWorkflow(jiraServiceContext, null);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", errorCollection);
    }

    @Test
    public void testValidateCopyWorkflow()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("workflowExists", new Constraint[] { P.eq("Copy of Workflow") }, Boolean.TRUE);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // null workflow name.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        workflowService.validateCopyWorkflow(jiraServiceContext, null);
        assertEquals("admin.errors.you.must.specify.a.workflow.name", errorCollection.getErrors().get("newWorkflowName"));
        resetErrorCollection(errorCollection);

        //non-ascii chars
        workflowService.validateCopyWorkflow(jiraServiceContext, "The\u0192\u00e7WORKFLOW");
        assertEquals("admin.common.errors.use.only.ascii", errorCollection.getErrors().get("newWorkflowName"));
        resetErrorCollection(errorCollection);

        //already exists.
        workflowService.validateCopyWorkflow(jiraServiceContext, "Copy of Workflow");
        assertEquals("admin.errors.a.workflow.with.this.name.already.exists", errorCollection.getErrors().get("newWorkflowName"));
        mockWorkflowManager.verify();
    }

    @Test
    public void testValidateCopyWorkflowSuccess()
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("workflowExists", new Constraint[] { P.eq("Copy of Workflow") }, Boolean.FALSE);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // null workflow name.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        workflowService.validateCopyWorkflow(jiraServiceContext, "Copy of Workflow");
        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCopyWorkflowSuccess()
    {
        final MockJiraWorkflow workflow = new MockJiraWorkflow();
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("copyWorkflow",
            new Constraint[] { P.eq("testuser"), P.eq("Copy of Workflow"), P.IS_NULL, P.eq(workflow) }, workflow);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // null workflow name.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        workflowService.copyWorkflow(jiraServiceContext, "Copy of Workflow", null, workflow);
        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCopyWorkflowSuccessWithNullUser()
    {
        final MockJiraWorkflow workflow = new MockJiraWorkflow();
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("copyWorkflow", new Constraint[] { P.eq(""), P.eq("Copy of Workflow"), P.IS_NULL, P.eq(workflow) },
            workflow);
        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // null workflow name.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        workflowService.copyWorkflow(jiraServiceContext, "Copy of Workflow", null, workflow);
        assertFalse(errorCollection.hasAnyErrors());
        mockWorkflowManager.verify();
    }

    @Test
    public void testCopyWorkflowNoAdminPermission()
    {
        final MockJiraWorkflow workflow = new MockJiraWorkflow();
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        // null workflow name.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        workflowService.copyWorkflow(jiraServiceContext, "Copy of Workflow", null, workflow);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", errorCollection);
    }

    @Test
    public void testConcurrentEditAndOverwrite() throws ExecutionException, InterruptedException
    {
        final AtomicLong updateWorkflowTime = new AtomicLong();
        final AtomicLong overwriteWorkflowTime = new AtomicLong();
        final Object workflowManagerDelegate = new Object()
        {
            public void overwriteActiveWorkflow(final String username, final String workflowName)
            {
                Assert.assertEquals("testuser", username);
                Assert.assertEquals("jiraworkflow", workflowName);
                overwriteWorkflowTime.set(System.currentTimeMillis());
            }

            public void updateWorkflow(final String username, final JiraWorkflow workflow)
            {
                updateWorkflowTime.set(System.currentTimeMillis());
            }
        };

        final WorkflowManager mockWorkflowManager = (WorkflowManager) DuckTypeProxy.getProxy(WorkflowManager.class, workflowManagerDelegate);
        final CountDownLatch validateOverwriteLatch = new CountDownLatch(1);

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(mockWorkflowManager, null, null)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }

            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
            {
                //countdown the latch, to get the update going.
                validateOverwriteLatch.countDown();
                try
                {
                    //make this thread sleep for a little while to give the update thread some time
                    //to try to run.
                    Thread.sleep(200);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        final List tasks = new ArrayList();
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("getDescriptor", P.ANY_ARGS, new DescriptorFactory().createWorkflowDescriptor());
        mockJiraWorkflow.expectAndReturn("isEditable", P.ANY_ARGS, Boolean.TRUE);
        final JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        tasks.add(new Callable()
        {
            public Object call() throws Exception
            {
                //wait until validate was called.
                validateOverwriteLatch.await();
                defaultWorkflowService.updateWorkflow(jiraServiceContext, workflow);
                return null;
            }
        });
        tasks.add(new Callable()
        {

            public Object call() throws Exception
            {
                defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");
                return null;
            }
        });

        runMultiThreadedTest(tasks, 2);

        //check that the update of the workflow always occurs after the overwrite.
        assertTrue(updateWorkflowTime.get() >= overwriteWorkflowTime.get());
    }

    private void runMultiThreadedTest(final List tasks, final int threads) throws InterruptedException, ExecutionException
    {
        final ExecutorService pool = Executors.newFixedThreadPool(threads);

        List /*<Future>*/futures;
        try
        {
            futures = pool.invokeAll(tasks);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        //wait until all tasks have finished executing.
        for (final Iterator it = futures.iterator(); it.hasNext();)
        {
            final Future future = (Future) it.next();
            future.get();
        }
    }

    private void resetErrorCollection(final ErrorCollection errorCollection)
    {
        errorCollection.setErrorMessages(new ArrayList());
        errorCollection.getErrors().clear();
    }

    /**
     * Asserts that the given ErrorCollection contains only the given message.
     *
     * @param message         Expected message
     * @param errorCollection Actual ErrorCollection
     */
    private void assertErrorMessage(final String message, final ErrorCollection errorCollection)
    {
        final int numMessages = errorCollection.getErrorMessages().size();
        if (numMessages != 1)
        {
            fail("The given error Collection was expected to contain exactly one message, but instead it contains " + numMessages + " error messages.");
        }
        // We only expect "error messages", not "errors"
        if (errorCollection.getErrors().isEmpty())
        {
            assertEquals(message, errorCollection.getErrorMessages().iterator().next());
        }
        else
        {
            fail("ErrorCollection was only expected to contain an error of type 'ErrorMessage', but it contains type" + " 'Error' as well.");
        }
    }

    @Test
    public void testValidateUpdateWorkflowNoAdminPermission()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, null, null);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateUpdateWorkflowThatIsNotModifiableIsNotAllowed() throws WorkflowException
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(), null);
        assertErrorMessage("admin.errors.workflow.cannot.be.edited.as.it.is.not.editable", jiraServiceContext.getErrorCollection());
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateUpdateWorkflowWithNullNewName() throws WorkflowException
    {

        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(), null);
        assertTrue(jiraServiceContext.getErrorCollection().getErrors().get("newWorkflowName").equals("admin.errors.you.must.specify.a.workflow.name"));
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateUpdateWorkflowWithEmptyNewName() throws WorkflowException
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(), "");
        assertTrue(jiraServiceContext.getErrorCollection().getErrors().get("newWorkflowName").equals("admin.errors.you.must.specify.a.workflow.name"));
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateUpdateWorkflowWithInvalidNewName() throws WorkflowException
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(),
            "InvalidNewName\u0192\u00e7");
        assertTrue(jiraServiceContext.getErrorCollection().getErrors().get("newWorkflowName").equals("admin.errors.please.use.only.ascii.characters"));
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateUpdateWorkflowWithNewWorkflowExists() throws WorkflowException
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("workflowExists", new Constraint[] { P.eq("newWorkflow") }, Boolean.TRUE);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflowName");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(), "newWorkflow");
        assertTrue(jiraServiceContext.getErrorCollection().getErrors().get("newWorkflowName").equals(
            "admin.errors.a.workflow.with.this.name.already.exists"));
        mockWorkflowManager.verify();
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateUpdateWorkflow() throws WorkflowException
    {
        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectAndReturn("workflowExists", new Constraint[] { P.eq("newWorkflow") }, Boolean.FALSE);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflowName");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockCurrentWorkflow.expectAndReturn("isEditable", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());
        workflowService.validateUpdateWorkflowNameAndDescription(jiraServiceContext, (JiraWorkflow) mockCurrentWorkflow.proxy(), "newWorkflow");
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockWorkflowManager.verify();
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testUpdateWorkflowNameNoAdminPermission()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.updateWorkflowNameAndDescription(jiraServiceContext, null, null, null);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testUpdateWorkflowName()
    {
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        final JiraWorkflow currentWorkflow = (JiraWorkflow) mockCurrentWorkflow.proxy();

        final Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectVoid("updateWorkflowNameAndDescription",
            new Constraint[] { P.eq("testuser"), P.eq(currentWorkflow), P.eq("newWorkflowName"), P.eq("newDescription") });

        final DefaultWorkflowService workflowService = new DefaultWorkflowService((WorkflowManager) mockWorkflowManager.proxy(), null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        workflowService.updateWorkflowNameAndDescription(jiraServiceContext, currentWorkflow, "newWorkflowName", "newDescription");
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockCurrentWorkflow.verify();
    }

    @Test
    public void testValidateAddWorkflowTransitionToDraftNoAdminPermission()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        workflowService.validateAddWorkflowTransitionToDraft(jiraServiceContext, null, 1);
        assertErrorMessage("admin.workflows.service.error.no.admin.permission", jiraServiceContext.getErrorCollection());
    }

    @Test
    public void testValidateAddWorkflowTransitionToDraftActiveWorkflow()
    {
        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };

        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        workflowService.validateAddWorkflowTransitionToDraft(jiraServiceContext, (JiraWorkflow) mockJiraWorkflow.proxy(), 1);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockJiraWorkflow.verify();
    }

    @Test
    public void testValidateAddWorkflowTransitionToDraftWorkflow()
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        _testValidateAddWorkflowTransitionToDraftWorkflow(jiraServiceContext, EasyList.build("Some dude", "blah"));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateAddWorkflowTransitionToDraftWorkflowError()
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User testUser = new User("testuser", new MockProviderAccessor(), new MockCrowdService());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        _testValidateAddWorkflowTransitionToDraftWorkflow(jiraServiceContext, Collections.EMPTY_LIST);
        assertErrorMessage("admin.workflowtransitions.error.add.transition.draft.step.without.transition", jiraServiceContext.getErrorCollection());
    }

    public void _testValidateAddWorkflowTransitionToDraftWorkflow(final JiraServiceContext jiraServiceContext, final List actions)
    {
        final MockControl mockStepDescriptorControl = MockClassControl.createControl(StepDescriptor.class);
        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final Mock mockParentWorkflow = new Mock(JiraWorkflow.class);

        final DefaultWorkflowService workflowService = new DefaultWorkflowService(null, null, null)
        {
            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            public JiraWorkflow getWorkflow(final JiraServiceContext jiraServiceContext, final String name)
            {
                assertEquals("Hamlet", name);

                final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
                final StepDescriptor mockStepDescriptor = (StepDescriptor) mockStepDescriptorControl.getMock();

                mockStepDescriptor.getActions();
                mockStepDescriptorControl.setReturnValue(actions);
                mockStepDescriptorControl.replay();

                mockWorkflowDescriptor.getStep(120);
                mockWorkflowDescriptorControl.setDefaultReturnValue(mockStepDescriptor);
                mockWorkflowDescriptorControl.replay();

                mockParentWorkflow.expectAndReturn("getDescriptor", mockWorkflowDescriptor);

                return (JiraWorkflow) mockParentWorkflow.proxy();
            }

            I18nHelper getI18nBean()
            {
                return mockI18nBean;
            }
        };

        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
        mockJiraWorkflow.expectAndReturn("getName", "Hamlet");

        if (actions.isEmpty())
        {
            final MockControl mockNewStepDescriptorControl = MockClassControl.createControl(StepDescriptor.class);
            final StepDescriptor mockNewStepDescriptor = (StepDescriptor) mockNewStepDescriptorControl.getMock();
            mockNewStepDescriptor.getActions();
            mockNewStepDescriptorControl.setReturnValue(EasyList.build("Some", "Actions"));
            mockNewStepDescriptor.getName();
            mockNewStepDescriptorControl.setReturnValue("Gretel");
            mockNewStepDescriptorControl.replay();

            final MockControl mockNewWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
            final WorkflowDescriptor mockNewWorkflowDescriptor = (WorkflowDescriptor) mockNewWorkflowDescriptorControl.getMock();
            mockNewWorkflowDescriptor.getStep(120);
            mockNewWorkflowDescriptorControl.setReturnValue(mockNewStepDescriptor);
            mockNewWorkflowDescriptorControl.replay();
            mockJiraWorkflow.expectAndReturn("getDescriptor", mockNewWorkflowDescriptor);
        }

        workflowService.validateAddWorkflowTransitionToDraft(jiraServiceContext, (JiraWorkflow) mockJiraWorkflow.proxy(), 120);
        mockJiraWorkflow.verify();
        mockParentWorkflow.verify();
        mockWorkflowDescriptorControl.verify();
        mockStepDescriptorControl.verify();
    }
}
