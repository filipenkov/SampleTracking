package com.atlassian.jira.bc.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.map.EasyMap;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.easymock.classextension.MockClassControl;
import org.easymock.internal.AlwaysMatcher;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the issue service.
 *
 * @since v4.1
 */
public class TestDefaultIssueService extends ListeningTestCase
{

    @Test
    public void testGetIssueById() throws Exception
    {
        final MockControl mockIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockIssue = (MutableIssue) mockIssueControl.getMock();
        mockIssueControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(12L);
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManagerControl.replay();

        final AtomicBoolean getIssueCalled = new AtomicBoolean(false);
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {
            @Override
            MutableIssue getIssue(final User user, final MutableIssue issue, final I18nHelper i18n, final ErrorCollection errorCollection)
            {
                getIssueCalled.set(true);
                return mockIssue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final IssueService.IssueResult issueResult = defaultIssueService.getIssue(null, new Long(12));

        assertTrue(issueResult.isValid());
        assertTrue(getIssueCalled.get());
        mockIssueControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testGetIssueByKey() throws Exception
    {
        final MockControl mockIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockIssue = (MutableIssue) mockIssueControl.getMock();
        mockIssueControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("TST-1");
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManagerControl.replay();

        final AtomicBoolean getIssueCalled = new AtomicBoolean(false);
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {
            @Override
            MutableIssue getIssue(final User user, final MutableIssue issue, final I18nHelper i18n, final ErrorCollection errorCollection)
            {
                getIssueCalled.set(true);
                return mockIssue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final IssueService.IssueResult issueResult = defaultIssueService.getIssue(null, "TST-1");

        assertTrue(issueResult.isValid());
        assertTrue(getIssueCalled.get());
        mockIssueControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testGetIssueNullIssue() throws Exception
    {
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        assertNull(defaultIssueService.getIssue(null, null, new MockI18nBean(), errorCollection));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The issue no longer exists.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetIssueNoPermission() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final MockControl mockPermissionManagerControl = MockControl.createStrictControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (User) null);
        mockPermissionManagerControl.setReturnValue(false);
        mockPermissionManagerControl.replay();

        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, mockPermissionManager, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        assertNull(defaultIssueService.getIssue(null, issue, new MockI18nBean(), errorCollection));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have the permission to see the specified issue", errorCollection.getErrorMessages().iterator().next());
        mockPermissionManagerControl.verify();
    }

    @Test
    public void testGetIssueHappy() throws Exception
    {
        final MockIssue mockIssue = new MockIssue();

        final MockControl mockPermissionManagerControl = MockControl.createStrictControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.BROWSE, mockIssue, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManagerControl.replay();

        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, mockPermissionManager, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        final Issue issue = defaultIssueService.getIssue(null, mockIssue, new MockI18nBean(), null);

        assertEquals(mockIssue, issue);
        assertFalse(errorCollection.hasAnyErrors());
        mockPermissionManagerControl.verify();
    }

    @Test
    public void testValidateCreateDefaultProvidedFields()
    {
        final MockControl mockMutableIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockMutableIssue = (MutableIssue) mockMutableIssueControl.getMock();
        mockMutableIssueControl.replay();

        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                assertNull(issueInputParameters.getProvidedFields());
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), new HashMap<String, Object>());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mockMutableIssue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueService.validateCreate(null, issueInputParameters);

        assertTrue(validateCreateCalled.get());
        mockMutableIssueControl.verify();
    }

    @Test
    public void testValidateSubTaskCreate()
    {
        final MockControl mockMutableIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockMutableIssue = (MutableIssue) mockMutableIssueControl.getMock();
        mockMutableIssue.setParentId(111L);
        mockMutableIssueControl.replay();

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), issueInputParameters.getFieldValuesHolder());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mockMutableIssue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        issueService.validateSubTaskCreate(null, 111L, issueInputParameters);

        assertTrue(validateCreateCalled.get());
        mockMutableIssueControl.verify();
    }

    @Test
    public void testValidateSubTaskCreateWithProvidedFields()
    {
        final MockControl mockMutableIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockMutableIssue = (MutableIssue) mockMutableIssueControl.getMock();
        mockMutableIssue.setParentId(111L);
        mockMutableIssueControl.replay();

        final Collection<String> providedFields = Collections.singleton("test");

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setProvidedFields(providedFields);

        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                assertEquals(providedFields, issueInputParameters.getProvidedFields());
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), issueInputParameters.getFieldValuesHolder());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mockMutableIssue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        issueService.validateSubTaskCreate(null, new Long(111), issueInputParameters);

        assertTrue(validateCreateCalled.get());
        mockMutableIssueControl.verify();
    }

    @Test
    public void testCreate() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        MockGenericValue mockProject = new MockGenericValue("Project", EasyMap.build("key", "TST"));
        mockIssue.setProject(mockProject);

        final MockGenericValue issueGV = new MockGenericValue("Issue");

        Map<String, Object> fields = MapBuilder.<String, Object>newBuilder().add("issue", mockIssue).add(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue).add("pkey", "TST").toMap();

        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactory.getIssue(issueGV);
        mockIssueFactoryControl.setReturnValue(mockIssue);
        mockIssueFactoryControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(123L);
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManager.createIssue((User)null, fields);
        mockIssueManagerControl.setReturnValue(issueGV);
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(mockIssueFactory, null, null, mockIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()));

        mockIssueFactoryControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateWithAuxSubmit() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        MockGenericValue mockProject = new MockGenericValue("Project", EasyMap.build("key", "TST"));
        mockIssue.setProject(mockProject);

        final MockGenericValue issueGV = new MockGenericValue("Issue");

        Map fields = EasyMap.build("issue", mockIssue, WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue, "pkey", "TST", "submitbutton", "AuxSubmitName");

        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactory.getIssue(issueGV);
        mockIssueFactoryControl.setReturnValue(mockIssue);
        mockIssueFactoryControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(new Long(123));
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManager.createIssue((User)null, fields);
        mockIssueManagerControl.setReturnValue(issueGV);
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(mockIssueFactory, null, null, mockIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");

        mockIssueFactoryControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateWithNullResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.create(null, null, "AuxSubmitName");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCreateWithNullIssueInResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.create(null, new IssueService.CreateValidationResult(null, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCreateWithInvalidResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage("blah");
            issueService.create(null, new IssueService.CreateValidationResult(new MockIssue(), errorCollection, new HashMap<String, Object>()), "AuxSubmitName");
            fail("Should throw IAE");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testCreateWithAuxSubmitCreateExceptionThrown() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        MockGenericValue mockProject = new MockGenericValue("Project", EasyMap.build("key", "TST"));
        mockIssue.setProject(mockProject);

        Map<String, Object> fields = MapBuilder.<String, Object>newBuilder().add("issue", mockIssue).add(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue).add("pkey", "TST").add("submitbutton", "AuxSubmitName").toMap();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(123L);
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManager.createIssue((User)null, fields);
        mockIssueManagerControl.setThrowable(new CreateException("Problem with create"));
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final IssueService.IssueResult issueResult = issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");
        assertFalse(issueResult.isValid());
        assertNull(issueResult.getIssue());
        assertEquals("Error creating issue: Problem with create", issueResult.getErrorCollection().getErrorMessages().iterator().next());

        mockIssueManagerControl.verify();
    }

    @Test
    public void testValidateUpdate()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        final AtomicBoolean getRendererCalled = new AtomicBoolean(false);
        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(mockIssue);

        EasyMock.replay(defaultIssueManager);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, boolean updateComment)
            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            FieldScreenRenderer getUpdateFieldScreenRenderer(final User user, final Issue issue)
            {
                getRendererCalled.set(true);
                return null;
            }
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertEquals(mockIssue, issueValidationResult.getIssue());

        assertTrue(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertTrue(copyCalled.get());
        assertTrue(valAndUpCalled.get());
        assertTrue(getRendererCalled.get());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateUpdateErrorInUpdateValidate()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        final AtomicBoolean getRendererCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(mockIssue);

        EasyMock.replay(defaultIssueManager);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                errorCollection.addErrorMessage("I am an error");
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            FieldScreenRenderer getUpdateFieldScreenRenderer(final User user, final Issue issue)
            {
                getRendererCalled.set(true);
                return null;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertNull(issueValidationResult.getIssue());

        assertFalse(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertTrue(copyCalled.get());
        assertTrue(valAndUpCalled.get());
        assertTrue(getRendererCalled.get());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateUpdateNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(mockIssue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                errors.addErrorMessage("Perm error");
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertNull("No perm to update should return null", issueValidationResult.getIssue());

        assertFalse(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
    }

    @Test
    public void testValidateUpdateNullIssueInputParameters()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        try
        {
            issueService.validateUpdate(null, 123L, null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
    }

    @Test
    public void testValidateUpdateNullIssueId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, boolean updateComment)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, null, issueInputParameters);

        assertFalse(issueValidationResult.isValid());
        assertEquals("You can not update a null issue.", issueValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
    }

    @Test
    public void testValidateUpdateIssueIdDoesNotResolve()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(null);

        EasyMock.replay(defaultIssueManager);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);

        assertFalse(issueValidationResult.isValid());
        assertEquals("You can not update a null issue.", issueValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testUpdate() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.updateIssue((User) null, mockIssue, eventDispatchOption, true);
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final IssueService.IssueResult result = issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), eventDispatchOption, true);
        assertTrue(result.isValid());
        assertEquals(mockIssue, result.getIssue());
        mockIssueManagerControl.verify();
    }

    @Test
    public void testUpdateNullEventDispatch() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

    }

    @Test
    public void testUpdateNullValidationResult() throws UpdateException
    {
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        try
        {
            issueService.update(null, null, eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

    }

    @Test
    public void testUpdateInvalidValidationResult() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            final ErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage("blah");
            issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, errorCollection, new HashMap<String, Object>()), eventDispatchOption, true);
            fail("Should have thrown ISE");
        }
        catch (IllegalStateException e)
        {
            //expected
        }
    }

    @Test
    public void testUpdateNullIssue() throws UpdateException
    {
        MockIssue mockIssue = null;
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals("You can not update a null issue.", e.getMessage());
        }
    }

    @Test
    public void testUpdateWithDefaultEventDispatchOption() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            public IssueResult update(final User user, final UpdateValidationResult issueValidationResult, final EventDispatchOption eventDispatchOption, final boolean sendMail)
            {
                assertEquals(EventDispatchOption.ISSUE_UPDATED, eventDispatchOption);
                return null;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        issueService.update(null, null);
    }

    @Test
    public void testValidateDelete()
    {
        final MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(mockIssue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateDelete(null, 123L);
        assertTrue(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateDeleteNoPermission()
    {
        final MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(mockIssue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        issueService.validateDelete(null, new Long(123));
        assertTrue(hasPermCalled.get());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateDeleteNullIssueId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final IssueService.DeleteValidationResult validationResult = issueService.validateDelete(null, null);
        assertFalse(validationResult.isValid());
        assertEquals("You can not delete a null issue.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
    }

    @Test
    public void testValidateDeleteNoIssueForId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(null);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final IssueService.DeleteValidationResult validationResult = issueService.validateDelete(null, 123L);
        assertFalse(validationResult.isValid());
        assertEquals("You can not delete a null issue.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        EasyMock.verify(defaultIssueManager);
    }
    
    @Test
    public void testDelete() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.deleteIssue((User) null, mockIssue, eventDispatchOption, true);
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);

        assertFalse(errorCollection.hasAnyErrors());
        mockIssueManagerControl.verify();
    }

    @Test
    public void testDeleteWithRemoveException() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        mockIssue.setKey("TST-1");
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.deleteIssue((User) null, mockIssue, eventDispatchOption, true);
        mockIssueManagerControl.setThrowable(new RemoveException("I can't remove the issue"));
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ErrorCollection results = issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);
        assertTrue(results.hasAnyErrors());
        assertEquals("There was a system error trying to delete the issue 'TST-1'.", results.getErrorMessages().iterator().next());

        mockIssueManagerControl.verify();
    }

    @Test
    public void testDeleteNullEventDispatch() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, new SimpleErrorCollection()), eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

    }

    @Test
    public void testDeleteNullIssue() throws RemoveException
    {
        MockIssue mockIssue = null;
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        try
        {
            issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, new SimpleErrorCollection()), eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals("You can not delete a null issue.", e.getMessage());
        }
    }

    @Test
    public void testDeleteNullValidationResult() throws RemoveException
    {
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        try
        {
            issueService.delete(null, null, eventDispatchOption, true);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testDeleteInvalidValidationResult() throws RemoveException
    {
        MockIssue mockIssue = null;
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage("blah");
            issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);
            fail("Should have thrown ISE");
        }
        catch (IllegalStateException e)
        {
            //expected
        }
    }

    @Test
    public void testDeleteWithDefaultEventDispatchOption() throws RemoveException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            public ErrorCollection delete(final User user, final DeleteValidationResult issueValidationResult, final EventDispatchOption eventDispatchOption, final boolean sendMail)
            {
                assertEquals(EventDispatchOption.ISSUE_DELETED, eventDispatchOption);
                return null;
            }
        };

        issueService.delete(null, null);
    }

    @Test
    public void testValidateAndUpdateIssueFromFieldsBadParams()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        I18nHelper i18n = new MockI18nBean();

        final AtomicBoolean valAndPopCalled = new AtomicBoolean(false);
        final AtomicBoolean updateCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected void validateAndPopulateParams(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final OperationContext operationContext, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer)
            {
                errorCollection.addErrorMessage("Bad params");
                valAndPopCalled.set(true);
            }

            @Override
            protected void updateIssueFromFields(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder, final boolean updateComment)
            {
                updateCalled.set(true);
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        assertNull(issueService.validateAndUpdateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18n, null, false));

        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(valAndPopCalled.get());
        assertFalse(updateCalled.get());
    }

    @Test
    public void testValidateAndUpdateIssueFromFields()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);
        I18nHelper i18n = new MockI18nBean();

        final AtomicBoolean valAndPopCalled = new AtomicBoolean(false);
        final AtomicBoolean checkAttachCalled = new AtomicBoolean(false);
        final AtomicBoolean updateCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected void validateAndPopulateParams(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final OperationContext operationContext, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer)

            {
                valAndPopCalled.set(true);
            }
        
            @Override
            protected void updateIssueFromFields(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder, final boolean updateComment)
            {
                updateCalled.set(true);
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        assertEquals(mockIssue, issueService.validateAndUpdateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18n, null, false));

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(0, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(valAndPopCalled.get());
        assertTrue(updateCalled.get());
        assertFalse(checkAttachCalled.get());
    }

    @Test
    public void testValidateRealCreateNullIssueInputParameters()
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.validateCreate(null, null);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testValidateRealCreateNullIssue()
    {
        MockIssue mockIssue = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        try
        {
            issueService.validateCreate(null, mockIssue, new IssueInputParametersImpl());
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testValidateRealCreateLicenseDoesNotAllowCreate()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                errorCollection.addErrorMessage("License error");
                return true;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(1, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertFalse(validateProjectCalled.get());
        assertFalse(validateIssueTypeCalled.get());
        assertFalse(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateProjectIssueTypeValidationFails()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                errorCollection.addErrorMessage("Project error");
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                errorCollection.addErrorMessage("Issue Type error");
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(2, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertFalse(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateNoPermission()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                errors.addErrorMessage("no permission");
                return false;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(1, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertTrue(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateHappyPath()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertEquals(mockIssue, result.getIssue());

        assertTrue(result.isValid());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertTrue(hasPermCalled.get());
        assertTrue(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsDefaultFields()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRendererControl.replay();

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        mockIssueCreationHelperBean.getProvidedFieldNames((User) null, mockIssue);
        mockIssueCreationHelperBeanControl.setReturnValue(Collections.EMPTY_LIST);

        mockIssueCreationHelperBean.validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, mockFieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), i18nBean);

        mockIssueCreationHelperBean.updateIssueFromFieldValuesHolder(mockFieldScreenRenderer, (User) null, mockIssue, issueInputParameters.getFieldValuesHolder());

        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return mockFieldScreenRenderer;
            }
        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertFalse(errorCollection.hasAnyErrors());
        mockIssueCreationHelperBeanControl.verify();
        mockFieldScreenRendererControl.verify();
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsWithProvidedFields()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final List<String> providedFields = CollectionBuilder.list("test", "test2");
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        issueInputParameters.setProvidedFields(providedFields);

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRendererControl.replay();

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        mockIssueCreationHelperBean.validateCreateIssueFields(jiraServiceContext, providedFields, mockIssue, mockFieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), i18nBean);

        mockIssueCreationHelperBean.updateIssueFromFieldValuesHolder(mockFieldScreenRenderer, (User) null, mockIssue, issueInputParameters.getFieldValuesHolder());

        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return mockFieldScreenRenderer;
            }

        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertFalse(errorCollection.hasAnyErrors());
        mockIssueCreationHelperBeanControl.verify();
        mockFieldScreenRendererControl.verify();
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsValidationError()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRendererControl.replay();

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError(IssueFieldConstants.PROJECT, " I am an error");

        final MockI18nBean i18nBean = new MockI18nBean();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, errorCollection);

        mockIssueCreationHelperBean.getProvidedFieldNames((User) null, mockIssue);
        mockIssueCreationHelperBeanControl.setReturnValue(Collections.EMPTY_LIST);

        mockIssueCreationHelperBean.validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, mockFieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), i18nBean);

        mockIssueCreationHelperBeanControl.replay();


        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return mockFieldScreenRenderer;
            }

        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertTrue(errorCollection.hasAnyErrors());
        mockIssueCreationHelperBeanControl.verify();
        mockFieldScreenRendererControl.verify();
    }

    @Test
    public void testValidateAndSetIssueType()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("123");

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        mockIssueCreationHelperBean.validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null);

        issueService.validateAndSetIssueType(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertEquals("123", mockIssue.getIssueTypeId());
        assertFalse(errorCollection.hasAnyErrors());
        mockIssueCreationHelperBeanControl.verify();

    }

    @Test
    public void testValidateAndSetIssueTypeErrorWithIssueType()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("123");

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError(IssueFieldConstants.ISSUE_TYPE, "I am an error");
        final MockI18nBean i18nBean = new MockI18nBean();
        mockIssueCreationHelperBean.validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null);

        issueService.validateAndSetIssueType(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertNull("The issue type should not have been set on the issue", mockIssue.getIssueTypeId());
        assertTrue(errorCollection.hasAnyErrors());
        // See that the error was transfered to the messages
        assertEquals(1, errorCollection.getErrorMessages().size());
        mockIssueCreationHelperBeanControl.verify();

    }

    @Test
    public void testValidateAndSetProject() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(123L);
        issueInputParameters.setIssueTypeId("789");

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        mockIssueCreationHelperBean.validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null);

        issueService.validateAndSetProject(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertEquals(new Long(123), mockIssue.getProjectId());
        assertFalse(errorCollection.hasAnyErrors());
        mockIssueCreationHelperBeanControl.verify();
    }

    @Test
    public void testValidateAndSetProjectErrorWithProject() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(123L);
        issueInputParameters.setIssueTypeId("789");

        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("pid", "I am an error");
        final MockI18nBean i18nBean = new MockI18nBean();
        mockIssueCreationHelperBean.validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null);

        issueService.validateAndSetProject(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertNull("The project id should not have been set.", mockIssue.getProjectId());
        assertTrue(errorCollection.hasAnyErrors());
        // See that the error was transfered to the messages
        assertEquals(1, errorCollection.getErrorMessages().size());
        mockIssueCreationHelperBeanControl.verify();
    }

    @Test
    public void testHasPermissionToEditNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("isEditable", P.args(P.eq(mockIssue), P.IS_NULL), Boolean.FALSE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, (IssueManager) mockIssueManager.proxy(), null, null, null, null);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToEdit(null, mockIssue, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to edit issues in this project.", errorCollection.getErrorMessages().iterator().next());

        mockIssueManager.verify();
    }

    @Test
    public void testHasPermissionToEdit()
    {
        MockIssue mockIssue = new MockIssue(123L);

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("isEditable", P.args(P.eq(mockIssue), P.IS_NULL), Boolean.TRUE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, (IssueManager) mockIssueManager.proxy(), null, null, null, null);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, new SimpleErrorCollection());
        assertTrue(issueService.hasPermissionToEdit(null, mockIssue, new MockI18nBean(), null));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        mockIssueManager.verify();
    }

    @Test
    public void testHasPermissionToCreateNoPerm()
    {
        MockProject mockProject = new MockProject(123, "TST", "Test Project");

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.CREATE_ISSUE), P.eq(mockProject), P.IS_NULL), Boolean.FALSE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, ((PermissionManager)mockPermissionManager.proxy()), null, null, null);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToCreate(null, mockProject, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to create issues in this project.", errorCollection.getErrorMessages().iterator().next());

        mockPermissionManager.verify();
    }

    @Test
    public void testHasPermissionToCreate()
    {
        MockProject mockProject = new MockProject(123, "TST", "Test Project");

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.CREATE_ISSUE), P.eq(mockProject), P.IS_NULL), Boolean.TRUE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, ((PermissionManager)mockPermissionManager.proxy()), null, null, null);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertTrue(issueService.hasPermissionToCreate(null, mockProject, new MockI18nBean(), errorCollection));
        assertFalse(errorCollection.hasAnyErrors());

        mockPermissionManager.verify();
    }

    @Test
    public void testHasPermissionToDeleteNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.DELETE_ISSUE), P.eq(mockIssue), P.IS_NULL), Boolean.FALSE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, ((PermissionManager)mockPermissionManager.proxy()), null, null, null);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToDelete(null, mockIssue, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to delete issues in this project.", errorCollection.getErrorMessages().iterator().next());

        mockPermissionManager.verify();
    }

    @Test
    public void testHasPermissionToDelete()
    {
        MockIssue mockIssue = new MockIssue(123L);

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.DELETE_ISSUE), P.eq(mockIssue), P.IS_NULL), Boolean.TRUE);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, ((PermissionManager)mockPermissionManager.proxy()), null, null, null);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, new SimpleErrorCollection());
        assertTrue(issueService.hasPermissionToDelete(null, mockIssue, new MockI18nBean(), null));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        mockPermissionManager.verify();
    }

    @Test
    public void testLicenseInvalidForIssueCreationNotValid() throws Exception
    {
        final AtomicBoolean validateLicenseCalled = new AtomicBoolean(false);

        IssueCreationHelperBean issueCreationHelperBean = new IssueCreationHelperBean()
        {
            public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer, final OperationContext operationContext, final Map<String, String[]> parameters, final I18nHelper i18n)
            {
            }

            public void validateLicense(final ErrorCollection errors, final I18nHelper i18n)
            {
                validateLicenseCalled.set(true);
                errors.addErrorMessage("I am a test error");
            }

            @Override
            public void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, com.opensymphony.user.User remoteUser, MutableIssue issueObject, Map fieldValuesHolder)
            {
            }

            public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final User remoteUser, final MutableIssue issueObject, final Map customFieldValuesHolder)
            {
            }

            @Override
            public FieldScreenRenderer createFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issueObject)
            {
                return null;
            }

            public FieldScreenRenderer createFieldScreenRenderer(final User remoteUser, final Issue issueObject)
            {
                return null;
            }

            @Override
            public List<String> getProvidedFieldNames(com.opensymphony.user.User remoteUser, Issue issueObject)
            {
                return null;
            }

            public List<String> getProvidedFieldNames(final User remoteUser, final Issue issueObject)
            {
                return null;
            }

            @Override
            public List<OrderableField> getFieldsForCreate(User user, Issue issueObject)
            {
                return null;
            }

            public void validateProject(final Issue issue, final OperationContext operationContext, final Map actionParams, final ErrorCollection errors, final I18nHelper i18n)
            {
            }

            public void validateIssueType(final Issue issue, final OperationContext operationContext, final Map actionParams, final ErrorCollection errors, final I18nHelper i18n)
            {
            }

            public void updateFieldValuesHolderWithDefaults(final Issue issueObject, final GenericValue project, final String issuetype, final Map actionParams, final Map<String, Object> fieldValuesHolder, final User remoteUser, final FieldScreenRenderer fieldScreenRenderer)
            {
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = new MockI18nBean();

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null);

        assertTrue(issueService.licenseInvalidForIssueCreation(errorCollection, i18nBean));
        assertTrue(validateLicenseCalled.get());
    }

    @Test
    public void testLicenseInvalidForIssueCreation() throws Exception
    {
        final MockControl mockIssueCreationHelperBeanControl = MockControl.createStrictControl(IssueCreationHelperBean.class);
        final IssueCreationHelperBean mockIssueCreationHelperBean = (IssueCreationHelperBean) mockIssueCreationHelperBeanControl.getMock();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        mockIssueCreationHelperBean.validateLicense(new SimpleErrorCollection(), i18nBean);
        mockIssueCreationHelperBeanControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, mockIssueCreationHelperBean, null, null, null, null, null, null);

        assertFalse(issueService.licenseInvalidForIssueCreation(errorCollection, i18nBean));

        mockIssueCreationHelperBeanControl.verify();
    }

    @Test
    public void testConstructNewIssue() throws Exception
    {
        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactory.getIssue();
        final MockIssue issue = new MockIssue();
        mockIssueFactoryControl.setReturnValue(issue);
        mockIssueFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(mockIssueFactory, null, null, null, null, null, null, null);
        assertEquals(issue, issueService.constructNewIssue());
        mockIssueFactoryControl.verify();
    }

    @Test
    public void testCopyIssue() throws Exception
    {
        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue");

        mockIssueFactory.getIssue(mockGenericValue);
        final MockIssue issue = new MockIssue();
        issue.setGenericValue(mockGenericValue);
        mockIssueFactoryControl.setReturnValue(issue);

        mockIssueFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(mockIssueFactory, null, null, null, null, null, null, null);
        assertEquals(issue, issueService.copyIssue(issue));
        mockIssueFactoryControl.verify();
    }

    @Test
    public void testGetCreateFieldScreenRenderer() throws Exception
    {
        final MockControl mockFieldScreenRendererFactoryControl = MockControl.createStrictControl(FieldScreenRendererFactory.class);
        final FieldScreenRendererFactory mockFieldScreenRendererFactory = (FieldScreenRendererFactory) mockFieldScreenRendererFactoryControl.getMock();
        mockFieldScreenRendererFactory.getFieldScreenRenderer((User) null, null, IssueOperations.CREATE_ISSUE_OPERATION, false);
        mockFieldScreenRendererFactoryControl.setReturnValue(null);
        mockFieldScreenRendererFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, mockFieldScreenRendererFactory, null, null);
        issueService.getCreateFieldScreenRenderer(null, null);

        mockFieldScreenRendererFactoryControl.verify();
    }

    @Test
    public void testGetUpdateFieldScreenRenderer() throws Exception
    {
        final MockControl mockFieldScreenRendererFactoryControl = MockControl.createStrictControl(FieldScreenRendererFactory.class);
        final FieldScreenRendererFactory mockFieldScreenRendererFactory = (FieldScreenRendererFactory) mockFieldScreenRendererFactoryControl.getMock();
        mockFieldScreenRendererFactory.getFieldScreenRenderer((User) null, null, IssueOperations.EDIT_ISSUE_OPERATION, false);
        mockFieldScreenRendererFactoryControl.setReturnValue(null);
        mockFieldScreenRendererFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, mockFieldScreenRendererFactory, null, null);
        issueService.getUpdateFieldScreenRenderer(null, null);

        mockFieldScreenRendererFactoryControl.verify();
    }

    @Test
    public void testValidateAndPopulateParamsRetainIssueValues()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        final MockControl mockFieldScreenRenderLayoutItemControl = MockControl.createStrictControl(FieldScreenRenderLayoutItem.class);
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItemControl.getMock();

        final MockControl mockOrderableFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockOrderableField = (OrderableField) mockOrderableFieldControl.getMock();
        mockOrderableField.getId();
        mockOrderableFieldControl.setReturnValue("10000");
        mockOrderableField.populateFromIssue(issueInputParameters.getFieldValuesHolder(), mockIssue);
        mockOrderableField.validateParams(operationContext, errorCollection, i18n, mockIssue, mockFieldScreenRenderLayoutItem);
        mockOrderableFieldControl.replay();

        mockFieldScreenRenderLayoutItem.isShow(mockIssue);
        mockFieldScreenRenderLayoutItemControl.setReturnValue(true);
        mockFieldScreenRenderLayoutItem.getOrderableField();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(mockOrderableField);
        mockFieldScreenRenderLayoutItemControl.replay();

        final MockControl mockFieldScreenRenderTabControl = MockControl.createStrictControl(FieldScreenRenderTab.class);
        final FieldScreenRenderTab mockFieldScreenRenderTab = (FieldScreenRenderTab) mockFieldScreenRenderTabControl.getMock();
        mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing();
        mockFieldScreenRenderTabControl.setReturnValue(EasyList.build(mockFieldScreenRenderLayoutItem));
        mockFieldScreenRenderTabControl.replay();

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRenderer.getFieldScreenRenderTabs();
        mockFieldScreenRendererControl.setReturnValue(EasyList.build(mockFieldScreenRenderTab));
        mockFieldScreenRendererControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, mockFieldScreenRenderer);

        mockFieldScreenRenderLayoutItemControl.verify();
        mockFieldScreenRenderTabControl.verify();
        mockFieldScreenRendererControl.verify();
        mockOrderableFieldControl.verify();
    }

    @Test
    public void testValidateAndPopulateParams()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        final MockControl mockFieldScreenRenderLayoutItemControl = MockControl.createStrictControl(FieldScreenRenderLayoutItem.class);
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItemControl.getMock();

        final MockControl mockOrderableFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockOrderableField = (OrderableField) mockOrderableFieldControl.getMock();
        mockOrderableField.populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        mockOrderableField.validateParams(operationContext, errorCollection, i18n, mockIssue, mockFieldScreenRenderLayoutItem);
        mockOrderableFieldControl.replay();

        mockFieldScreenRenderLayoutItem.isShow(mockIssue);
        mockFieldScreenRenderLayoutItemControl.setReturnValue(true);
        mockFieldScreenRenderLayoutItem.getOrderableField();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(mockOrderableField);
        mockFieldScreenRenderLayoutItemControl.replay();

        final MockControl mockFieldScreenRenderTabControl = MockControl.createStrictControl(FieldScreenRenderTab.class);
        final FieldScreenRenderTab mockFieldScreenRenderTab = (FieldScreenRenderTab) mockFieldScreenRenderTabControl.getMock();
        mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing();
        mockFieldScreenRenderTabControl.setReturnValue(EasyList.build(mockFieldScreenRenderLayoutItem));
        mockFieldScreenRenderTabControl.replay();

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRenderer.getFieldScreenRenderTabs();
        mockFieldScreenRendererControl.setReturnValue(EasyList.build(mockFieldScreenRenderTab));
        mockFieldScreenRendererControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, mockFieldScreenRenderer);

        mockFieldScreenRenderLayoutItemControl.verify();
        mockFieldScreenRenderTabControl.verify();
        mockFieldScreenRendererControl.verify();
        mockOrderableFieldControl.verify();
    }

    @Test
    public void testValidateAndPopulateParamsWithComment()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("Comment");


        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        final MockControl mockFieldScreenRenderLayoutItemControl = MockControl.createStrictControl(FieldScreenRenderLayoutItem.class);
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItemControl.getMock();

        final MockControl mockOrderableFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockOrderableField = (OrderableField) mockOrderableFieldControl.getMock();
        mockOrderableField.populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        mockOrderableField.validateParams(operationContext, errorCollection, i18n, mockIssue, mockFieldScreenRenderLayoutItem);
        mockOrderableFieldControl.replay();

        mockFieldScreenRenderLayoutItem.isShow(mockIssue);
        mockFieldScreenRenderLayoutItemControl.setReturnValue(true);
        mockFieldScreenRenderLayoutItem.getOrderableField();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(mockOrderableField);
        mockFieldScreenRenderLayoutItemControl.replay();

        final MockControl mockFieldScreenRenderTabControl = MockControl.createStrictControl(FieldScreenRenderTab.class);
        final FieldScreenRenderTab mockFieldScreenRenderTab = (FieldScreenRenderTab) mockFieldScreenRenderTabControl.getMock();
        mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing();
        mockFieldScreenRenderTabControl.setReturnValue(EasyList.build(mockFieldScreenRenderLayoutItem));
        mockFieldScreenRenderTabControl.replay();

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRenderer.getFieldScreenRenderTabs();
        mockFieldScreenRendererControl.setReturnValue(EasyList.build(mockFieldScreenRenderTab));
        mockFieldScreenRendererControl.replay();


        final MockControl mockCommentFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockCommentField = (OrderableField) mockCommentFieldControl.getMock();
        mockCommentField.populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        mockCommentField.validateParams(operationContext, errorCollection, i18n, mockIssue, null);
        mockCommentFieldControl.replay();

        final MockControl mockFieldManagerControl = MockControl.createStrictControl(FieldManager.class);
        final FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getField(IssueFieldConstants.COMMENT);
        mockFieldManagerControl.setReturnValue(mockCommentField);
        mockFieldManagerControl.replay();

        final AtomicBoolean getLayoutForFieldCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, mockFieldManager, null, null, null, null, null)
        {
            @Override
            FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(final User user, final Issue issue, final OrderableField field)
            {
                getLayoutForFieldCalled.set(true);
                return null;
            }
        };

        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, mockFieldScreenRenderer);

        assertTrue(getLayoutForFieldCalled.get());
        mockFieldScreenRenderLayoutItemControl.verify();
        mockFieldScreenRenderTabControl.verify();
        mockFieldScreenRendererControl.verify();
        mockOrderableFieldControl.verify();
        mockFieldManagerControl.verify();
        mockCommentFieldControl.verify();
    }

    @Test
    public void testUpdateIssueFromFieldsNoCommentUpdate()
    {
        assertUpdateIssueFromFields(false);
    }

    @Test
    public void testUpdateIssueFromFieldsFalseCommentUpdate()
    {
        assertUpdateIssueFromFields(true);
    }

    private void assertUpdateIssueFromFields(final boolean updateComment)
    {
        MockIssue mockIssue = new MockIssue(123L);
        Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();

        final MockControl mockFieldLayoutItemControl = MockControl.createStrictControl(FieldLayoutItem.class);
        final FieldLayoutItem mockFieldLayoutItem = (FieldLayoutItem) mockFieldLayoutItemControl.getMock();
        mockFieldLayoutItemControl.replay();

        final MockControl mockOrderableFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockOrderableField = (OrderableField) mockOrderableFieldControl.getMock();
        mockOrderableField.updateIssue(mockFieldLayoutItem, mockIssue, fieldValuesHolder);
        mockOrderableFieldControl.replay();

        final MockControl mockFieldScreenRenderLayoutItemControl = MockControl.createStrictControl(FieldScreenRenderLayoutItem.class);
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItemControl.getMock();
        mockFieldScreenRenderLayoutItem.isShow(mockIssue);
        mockFieldScreenRenderLayoutItemControl.setReturnValue(true);
        mockFieldScreenRenderLayoutItem.getOrderableField();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(mockOrderableField);
        mockFieldScreenRenderLayoutItem.getFieldLayoutItem();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(mockFieldLayoutItem);
        mockFieldScreenRenderLayoutItemControl.replay();

        final MockControl mockFieldScreenRenderTabControl = MockControl.createStrictControl(FieldScreenRenderTab.class);
        final FieldScreenRenderTab mockFieldScreenRenderTab = (FieldScreenRenderTab) mockFieldScreenRenderTabControl.getMock();
        mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing();
        mockFieldScreenRenderTabControl.setReturnValue(EasyList.build(mockFieldScreenRenderLayoutItem));
        mockFieldScreenRenderTabControl.replay();

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRenderer.getFieldScreenRenderTabs();
        mockFieldScreenRendererControl.setReturnValue(EasyList.build(mockFieldScreenRenderTab));
        mockFieldScreenRendererControl.replay();

        final AtomicBoolean updateIssueWithCommentCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {
            void updateIssueWithComment(final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder)
            {
                // No-op
                updateIssueWithCommentCalled.set(true);
            }
        };

        issueService.updateIssueFromFields(mockFieldScreenRenderer, mockIssue, null, fieldValuesHolder, updateComment);

        assertTrue(updateIssueWithCommentCalled.get() == updateComment);
        mockFieldScreenRenderLayoutItemControl.verify();
        mockFieldScreenRenderTabControl.verify();
        mockFieldScreenRendererControl.verify();
        mockOrderableFieldControl.verify();
        mockFieldLayoutItemControl.verify();
    }

    @Test
    public void testUpdateIssueWithComment() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();

        final MockControl mockCommentFieldControl = MockControl.createStrictControl(OrderableField.class);
        final OrderableField mockCommentField = (OrderableField) mockCommentFieldControl.getMock();
        mockCommentField.updateIssue(null, mockIssue, fieldValuesHolder);
        mockCommentFieldControl.replay();

        final MockControl mockFieldManagerControl = MockControl.createStrictControl(FieldManager.class);
        final FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getField(IssueFieldConstants.COMMENT);
        mockFieldManagerControl.setReturnValue(mockCommentField);
        mockFieldManagerControl.replay();

        final MockControl mockFieldScreenRenderLayoutItemControl = MockControl.createStrictControl(FieldScreenRenderLayoutItem.class);
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItemControl.getMock();
        mockFieldScreenRenderLayoutItem.getFieldLayoutItem();
        mockFieldScreenRenderLayoutItemControl.setReturnValue(null);
        mockFieldScreenRenderLayoutItemControl.replay();

        final AtomicBoolean getLayoutForFieldCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, mockFieldManager, null, null, null, null, null)
        {
            FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(final User user, final Issue issue, final OrderableField field)
            {
                getLayoutForFieldCalled.set(true);
                return mockFieldScreenRenderLayoutItem;
            }
        };

        issueService.updateIssueWithComment(mockIssue, null, fieldValuesHolder);

        assertTrue(getLayoutForFieldCalled.get());
        mockFieldManagerControl.verify();
        mockCommentFieldControl.verify();
        mockFieldScreenRenderLayoutItemControl.verify();
    }

    @Test
    public void testGetFieldScreenRendererLayoutItemForField() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);

        final MockControl mockFieldScreenRendererControl = MockControl.createStrictControl(FieldScreenRenderer.class);
        final FieldScreenRenderer mockFieldScreenRenderer = (FieldScreenRenderer) mockFieldScreenRendererControl.getMock();
        mockFieldScreenRenderer.getFieldScreenRenderLayoutItem(null);
        mockFieldScreenRendererControl.setReturnValue(null);
        mockFieldScreenRendererControl.replay();

        final MockControl mockFieldScreenRendererFactoryControl = MockControl.createStrictControl(FieldScreenRendererFactory.class);
        final FieldScreenRendererFactory mockFieldScreenRendererFactory = (FieldScreenRendererFactory) mockFieldScreenRendererFactoryControl.getMock();
        mockFieldScreenRendererFactory.getFieldScreenRenderer((User) null, mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, false);
        mockFieldScreenRendererFactoryControl.setReturnValue(mockFieldScreenRenderer);
        mockFieldScreenRendererFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, mockFieldScreenRendererFactory, null, null);

        issueService.getFieldScreenRendererLayoutItemForField(null, mockIssue, null);

        mockFieldScreenRendererFactoryControl.verify();
        mockFieldScreenRendererControl.verify();
    }

    @Test
    public void testTransitionNullTransitionResult() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        try
        {
            issueService.transition(user, null);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testTransitionNullIssue() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        try
        {
            issueService.transition(user, new IssueService.TransitionValidationResult(null, new SimpleErrorCollection(), null, Collections.EMPTY_MAP, 1));
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
            assertEquals("You can not transition a null issue.", e.getMessage());
        }
    }

    @Test
    public void testTransitionInvalidValidationResult() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage("blah");
            issueService.transition(user, new IssueService.TransitionValidationResult(null, errorCollection, null, Collections.EMPTY_MAP, 1));
            fail("Should throw ISE");
        }
        catch (IllegalStateException e)
        {
            //Expected
        }
    }

    @Test
    public void testTransitionHappyPath() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");
        final MockIssue issue = new MockIssue();
        issue.setId(12L);

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.doWorkflowAction(null);
        mockWorkflowManagerControl.setMatcher(new AlwaysMatcher());
        mockWorkflowManagerControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(issue.getId());
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, mockIssueManager, null, null, mockWorkflowManager, null);
        IssueService.TransitionValidationResult transitionResult = new IssueService.TransitionValidationResult(issue, new SimpleErrorCollection(), Collections.<String, Object>emptyMap(), Collections.EMPTY_MAP, 1);

        issueService.transition(user, transitionResult);
        mockWorkflowManagerControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testGetActionDescriptorNullWorkflow() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.getWorkflow(issue);
        mockWorkflowManagerControl.setReturnValue(null);
        mockWorkflowManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, mockWorkflowManager, null);

        assertNull(issueService.getActionDescriptor(issue, 2));

        mockWorkflowManagerControl.verify();
    }

    @Test
    public void testGetActionDescriptorNullDescriptor() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final MockControl mockJiraWorkflowControl = MockControl.createStrictControl(JiraWorkflow.class);
        final JiraWorkflow mockJiraWorkflow = (JiraWorkflow) mockJiraWorkflowControl.getMock();
        mockJiraWorkflow.getDescriptor();
        mockJiraWorkflowControl.setReturnValue(null);
        mockJiraWorkflowControl.replay();

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.getWorkflow(issue);
        mockWorkflowManagerControl.setReturnValue(mockJiraWorkflow);
        mockWorkflowManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, mockWorkflowManager, null);

        assertNull(issueService.getActionDescriptor(issue, 2));

        mockJiraWorkflowControl.verify();
        mockWorkflowManagerControl.verify();
    }

    @Test
    public void testGetActionDescriptorHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptorControl.replay();

        final MockControl mockWorkflowDescriptorControl = MockClassControl.createStrictControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
        mockWorkflowDescriptor.getAction(2);
        mockWorkflowDescriptorControl.setReturnValue(mockActionDescriptor);
        mockWorkflowDescriptorControl.replay();

        final MockControl mockJiraWorkflowControl = MockControl.createStrictControl(JiraWorkflow.class);
        final JiraWorkflow mockJiraWorkflow = (JiraWorkflow) mockJiraWorkflowControl.getMock();
        mockJiraWorkflow.getDescriptor();
        mockJiraWorkflowControl.setReturnValue(mockWorkflowDescriptor);
        mockJiraWorkflowControl.replay();

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.getWorkflow(issue);
        mockWorkflowManagerControl.setReturnValue(mockJiraWorkflow);
        mockWorkflowManagerControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, mockWorkflowManager, null);

        assertEquals(mockActionDescriptor, issueService.getActionDescriptor(issue, 2));

        mockWorkflowDescriptorControl.verify();
        mockJiraWorkflowControl.verify();
        mockWorkflowManagerControl.verify();
        mockActionDescriptorControl.verify();
    }

    @Test
    public void testGetTransitionFieldScreenRenderer() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptorControl.replay();

        final MockControl mockFieldScreenRendererFactoryControl = MockControl.createStrictControl(FieldScreenRendererFactory.class);
        final FieldScreenRendererFactory mockFieldScreenRendererFactory = (FieldScreenRendererFactory) mockFieldScreenRendererFactoryControl.getMock();
        mockFieldScreenRendererFactory.getFieldScreenRenderer((User) null, issue, mockActionDescriptor);
        mockFieldScreenRendererFactoryControl.setReturnValue(null);
        mockFieldScreenRendererFactoryControl.replay();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, mockFieldScreenRendererFactory, null, null);

        issueService.getTransitionFieldScreenRenderer(null, issue, mockActionDescriptor);

        mockFieldScreenRendererFactoryControl.verify();
    }

    @Test
    public void testValidateTransitionNullIssueInputParameters() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null);

        try
        {
            issueService.validateTransition(user, 123L, 2, null);
            fail("Should have thrown IAE");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    @Test
    public void testValidateTransitionNullIssueId() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user, null, 2, issueInputParameters);
        assertFalse(transitionValidationResult.isValid());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not transition a null issue.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateTransitionNullIssueFromId() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");
        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(null);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertFalse(transitionValidationResult.isValid());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not transition a null issue.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionNoSuchAction() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        final MockIssue issue = new MockIssue();
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, null)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return null;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("The workflow operation with action id '2' does not exist in the workflow.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionInvalidAction() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        final MockIssue issue = new MockIssue();
        issue.setKey("TST-1");

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptor.getName();
        mockActionDescriptorControl.setReturnValue("Test");
        mockActionDescriptorControl.replay();

        final MockControl mockIssueUtilsBeanControl = MockClassControl.createControl(IssueUtilsBean.class);
        final IssueUtilsBean mockIssueUtilsBean = (IssueUtilsBean) mockIssueUtilsBeanControl.getMock();
        mockIssueUtilsBean.isValidAction(issue, 2);
        mockIssueUtilsBeanControl.setReturnValue(false);
        mockIssueUtilsBeanControl.replay();

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, mockIssueUtilsBean)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return mockActionDescriptor;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("It seems that you have tried to perform a workflow operation (Test) that is not valid for the current state of this issue (TST-1). The likely cause is that somebody has changed the issue recently, please look at the issue history for details.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());

        mockActionDescriptorControl.verify();
        mockIssueUtilsBeanControl.verify();
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionUpdateIssueUpdateHasError() throws Exception
    {
        final MockIssue issue = new MockIssue();
        issue.setKey("TST-1");

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptor.getView();
        mockActionDescriptorControl.setReturnValue("BlahView");
        mockActionDescriptorControl.replay();

        final MockControl mockIssueUtilsBeanControl = MockClassControl.createControl(IssueUtilsBean.class);
        final IssueUtilsBean mockIssueUtilsBean = (IssueUtilsBean) mockIssueUtilsBeanControl.getMock();
        mockIssueUtilsBean.isValidAction(issue, 2);
        mockIssueUtilsBeanControl.setReturnValue(true);
        mockIssueUtilsBeanControl.replay();

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, mockIssueUtilsBean)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return mockActionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                // Set a bs error
                errorCollection.addErrorMessage("I am a bs error");
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        // We should still get a transition result but the issue should be null and we should see the errors
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(null, 123L, 2, issueInputParameters);
        assertNull(transitionResult.getIssue());
        assertEquals(1, transitionResult.getErrorCollection().getErrorMessages().size());
        assertEquals("I am a bs error", transitionResult.getErrorCollection().getErrorMessages().iterator().next());

        assertTrue(getFieldRendererCalled.get());
        mockActionDescriptorControl.verify();
        mockIssueUtilsBeanControl.verify();
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionUpdateIssueUpdateHasErrorWithComment() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        final MockIssue issue = new MockIssue();
        issue.setKey("TST-1");

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptor.getView();
        mockActionDescriptorControl.setReturnValue("BlahView");
        mockActionDescriptorControl.replay();

        final MockControl mockIssueUtilsBeanControl = MockClassControl.createControl(IssueUtilsBean.class);
        final IssueUtilsBean mockIssueUtilsBean = (IssueUtilsBean) mockIssueUtilsBeanControl.getMock();
        mockIssueUtilsBean.isValidAction(issue, 2);
        mockIssueUtilsBeanControl.setReturnValue(true);
        mockIssueUtilsBeanControl.replay();

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, mockIssueUtilsBean)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return mockActionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                // Set a bs error
                errorCollection.addErrorMessage("I am a bs error");
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Map<String, Object> createAdditionalParameters(final User user, final Map<String, Object> fieldValuesHolder)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("I am a bs error", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());

        assertTrue(getFieldRendererCalled.get());
        mockActionDescriptorControl.verify();
        mockIssueUtilsBeanControl.verify();
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionUpdateIssueHappyPath() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");

        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        final MockIssue issue = new MockIssue();
        issue.setKey("TST-1");

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptor.getView();
        mockActionDescriptorControl.setReturnValue("BlahView");
        mockActionDescriptorControl.replay();

        final MockControl mockIssueUtilsBeanControl = MockClassControl.createControl(IssueUtilsBean.class);
        final IssueUtilsBean mockIssueUtilsBean = (IssueUtilsBean) mockIssueUtilsBeanControl.getMock();
        mockIssueUtilsBean.isValidAction(issue, 2);
        mockIssueUtilsBeanControl.setReturnValue(true);
        mockIssueUtilsBeanControl.replay();

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, mockIssueUtilsBean)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return mockActionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                // Set a bs error
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Map<String, Object> createAdditionalParameters(final User user, final Map<String, Object> fieldValuesHolder)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertEquals(issue, transitionResult.getIssue());
        assertEquals(addParams, transitionResult.getAdditionInputs());

        assertTrue(getFieldRendererCalled.get());
        mockActionDescriptorControl.verify();
        mockIssueUtilsBeanControl.verify();
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testValidateTransitionHappyPath() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");

        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        final MockIssue issue = new MockIssue();
        issue.setKey("TST-1");

        final MockControl mockActionDescriptorControl = MockClassControl.createStrictControl(ActionDescriptor.class);
        final ActionDescriptor mockActionDescriptor = (ActionDescriptor) mockActionDescriptorControl.getMock();
        mockActionDescriptor.getView();
        mockActionDescriptorControl.setReturnValue(null);
        mockActionDescriptorControl.replay();

        final MockControl mockIssueUtilsBeanControl = MockClassControl.createControl(IssueUtilsBean.class);
        final IssueUtilsBean mockIssueUtilsBean = (IssueUtilsBean) mockIssueUtilsBeanControl.getMock();
        mockIssueUtilsBean.isValidAction(issue, 2);
        mockIssueUtilsBeanControl.setReturnValue(true);
        mockIssueUtilsBeanControl.replay();

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        final IssueManager defaultIssueManager = EasyMock.createMock(IssueManager.class);
        EasyMock.expect(defaultIssueManager.getIssueObject(123L)).andReturn(issue);

        EasyMock.replay(defaultIssueManager);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, defaultIssueManager, null, null, null, mockIssueUtilsBean)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return mockActionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment)
            {
                // Set a bs error
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }

            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            Map<String, Object> createAdditionalParameters(final User user, final Map<String, Object> fieldValuesHolder)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(user, 123L, 2, issueInputParameters);
        assertEquals(issue, transitionResult.getIssue());
        assertSame(addParams, transitionResult.getAdditionInputs());

        assertFalse(getFieldRendererCalled.get());
        mockActionDescriptorControl.verify();
        mockIssueUtilsBeanControl.verify();
        EasyMock.verify(defaultIssueManager);
    }

    @Test
    public void testCreateAdditionalParametersNullUser() throws Exception
    {
        Map<String, Object> fvh = MapBuilder.singletonMap(IssueFieldConstants.COMMENT, new Object());
        Map<String, Object> outputMap = MapBuilder.singletonMap("username", null);

        final IMocksControl control = EasyMock.createControl();

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        final CommentSystemField csf = control.createMock(CommentSystemField.class);

        EasyMock.expect(fieldManager.getOrderableField(IssueFieldConstants.COMMENT)).andReturn(csf);

        csf.populateAdditionalInputs(fvh, outputMap);

        control.replay();

        final DefaultIssueService service = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null);
        final Map<String, Object> actualMap = service.createAdditionalParameters(null, fvh);
        assertEquals(outputMap, actualMap);

        control.verify();
    }

    @Test
    public void testCreateAdditionalParametersWithUser() throws Exception
    {
        User user = new MockUser("fred", "Fred Flinstone", "fred@example.com");

        Map<String, Object> fvh = MapBuilder.singletonMap(IssueFieldConstants.COMMENT, new Object());
        Map<String, Object> outputMap = MapBuilder.<String, Object>singletonMap("username", "fred");

        final IMocksControl control = EasyMock.createControl();

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        final CommentSystemField csf = control.createMock(CommentSystemField.class);

        EasyMock.expect(fieldManager.getOrderableField(IssueFieldConstants.COMMENT)).andReturn(csf);

        csf.populateAdditionalInputs(fvh, outputMap);

        control.replay();

        final DefaultIssueService service = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null);
        final Map<String, Object> actualMap = service.createAdditionalParameters(user, fvh);
        assertEquals(outputMap, actualMap);

        control.verify();
    }

}
