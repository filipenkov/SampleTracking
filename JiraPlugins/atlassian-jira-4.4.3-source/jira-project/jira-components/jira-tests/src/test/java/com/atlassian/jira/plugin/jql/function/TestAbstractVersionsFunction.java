package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.permission.LiteralSanitiser;
import com.atlassian.jira.jql.permission.MockLiteralSanitiser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestAbstractVersionsFunction extends MockControllerTestCase
{
    private static final String FUNC_NAME = "funcName";
    private TerminalClause terminalClause = null;
    private com.opensymphony.user.User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testDataType() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);
        mockController.replay();
        assertEquals(JiraDataTypes.VERSION, handler.getDataType());        
    }

    @Test
    public void testValidateZeroArgs() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);

        mockController.replay();

        final MessageSet messageSet = handler.validate(theUser, new FunctionOperand(FUNC_NAME), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateOneArgNotAProject() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);
        infoResolver.getIndexedValues("arg");
        mockController.setReturnValue(Collections.emptyList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);

        mockController.replay();

        final MessageSet messageSet = handler.validate(theUser, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("Could not resolve the project 'arg' provided to function 'funcName'."));

        mockController.verify();
    }

    @Test
    public void testValidateTwoArgsSecondNotAProject() throws Exception
    {
        final MockProject project = new MockProject(1L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("1"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.<String>emptyList());

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(1L)).andReturn(project);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (User) theUser)).andReturn(true);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);

        mockController.replay();

        final MessageSet messageSet = handler.validate(theUser, new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList()), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("Could not resolve the project 'arg2' provided to function 'funcName'."));

        mockController.verify();
    }

    @Test
    public void testValidateTwoArgsSecondNoPermission() throws Exception
    {
        final MockProject project1 = new MockProject(1L);
        final MockProject project2 = new MockProject(2L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("1"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.singletonList("2"));

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(1L)).andReturn(project1);
        EasyMock.expect(nameResolver.get(2L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) theUser)).andReturn(false);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);

        mockController.replay();

        final MessageSet messageSet = handler.validate(theUser, new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList()), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains("Could not resolve the project 'arg2' provided to function 'funcName'."));

        mockController.verify();
    }

    @Test
    public void testValidateOneArgTooManyProjects() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);
        infoResolver.getIndexedValues("arg");
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver);

        mockController.replay();

        try
        {
            handler.validate(theUser, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
            fail("Expected exception for too many projects returned");
        }
        catch(IllegalArgumentException expected) {}

        mockController.verify();
    }

    @Test
    public void testGetValuesZeroArgsOneViewableProjectOneNonViewable() throws Exception
    {
        final Project project1 = new MockProject(1L);
        final Project project2 = new MockProject(2L);
        final MockVersion version1 = new MockVersion(1L, "Version 1", project1);
        final MockVersion version2 = new MockVersion(2L, "Version 2", project2);

        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) theUser);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) theUser);
        mockController.setReturnValue(false);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, CollectionBuilder.<Version>newBuilder(version1, version2).asList(), null);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.<String>emptyList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertFalse(values.contains(new QueryLiteral(operand, 2L)));

        mockController.verify();
    }

    @Test
    public void testGetValuesZeroArgsOneViewableProjectOneNonViewableOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        final Project project1 = new MockProject(1L);
        final Project project2 = new MockProject(2L);
        final MockVersion version1 = new MockVersion(1L, "Version 1", project1);
        final MockVersion version2 = new MockVersion(2L, "Version 2", project2);

        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, CollectionBuilder.<Version>newBuilder(version1, version2).asList(), null);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.<String>emptyList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testGetValuesOneArgGood() throws Exception
    {
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");
        final MockProject project = new MockProject(100L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg")).andReturn(Collections.singletonList("100"));

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(100L)).andReturn(project);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, (User) theUser)).andReturn(true);

        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersionsReleased(100L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version1, version2).asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, versionManager);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.singletonList("arg"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testGetValuesOneArgNoResolvedProject() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);
        infoResolver.getIndexedValues("arg");
        mockController.setReturnValue(Collections.emptyList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, null);

        mockController.replay();

        final List<QueryLiteral> values = handler.getValues(queryCreationContext, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assertTrue(values.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetValuesOneArgMultipleResolvedProjects() throws Exception
    {
        final IndexInfoResolver infoResolver = mockController.getMock(IndexInfoResolver.class);
        infoResolver.getIndexedValues("arg");
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, null);

        mockController.replay();

        final List<QueryLiteral> values = handler.getValues(queryCreationContext, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assertTrue(values.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetValuesTwoArgsGood() throws Exception
    {
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);
        final MockProject project2 = new MockProject(200L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("100"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.singletonList("200"));

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(100L)).andReturn(project1);
        EasyMock.expect(nameResolver.get(200L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) theUser)).andReturn(true);

        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersionsReleased(100L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version2).asList());
        versionManager.getVersionsReleased(200L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version1).asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, versionManager);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneDoesntResolve() throws Exception
    {
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("100"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.<String>emptyList());

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(100L)).andReturn(project1);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) theUser)).andReturn(true);

        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersionsReleased(100L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version2).asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, versionManager);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneNoPermission() throws Exception
    {
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);
        final MockProject project2 = new MockProject(200L);

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("100"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.singletonList("200"));

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(nameResolver.get(100L)).andReturn(project1);
        EasyMock.expect(nameResolver.get(200L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) theUser)).andReturn(false);

        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersionsReleased(100L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version2).asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, versionManager);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneNoPermissionOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final IndexInfoResolver<Project> infoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(infoResolver.getIndexedValues("arg1")).andReturn(Collections.singletonList("100"));
        EasyMock.expect(infoResolver.getIndexedValues("arg2")).andReturn(Collections.singletonList("200"));

        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionManager.getVersionsReleased(100L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version1).asList());
        versionManager.getVersionsReleased(200L, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(version2).asList());

        AbstractVersionsFunction handler = createVersionFunction(infoResolver, null, versionManager);

        mockController.replay();

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, CollectionBuilder.newBuilder("arg1", "arg2").asList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());

        mockController.verify();
    }

    @Test
    public void testSanitiseEmptyArgs() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction(new MessageSetImpl());

        mockController.replay();

        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME);
        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser, inputOperand);
        assertSame(cleanOperand, inputOperand);

        mockController.verify();
    }

    @Test
    public void testSanitiseNotModified() throws Exception
    {
        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME, "arg1", "arg2");
        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(false, null), new QueryLiteral(inputOperand, "arg1"), new QueryLiteral(inputOperand, "arg2"));
        AbstractVersionsFunction handler = createVersionFunction(sanitiser);

        mockController.replay();

        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser, inputOperand);
        assertSame(cleanOperand, inputOperand);

        mockController.verify();
    }

    @Test
    public void testSanitiseModified() throws Exception
    {
        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME, "arg1", "arg2");
        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(true, Collections.singletonList(new QueryLiteral(inputOperand, "clean"))),
                new QueryLiteral(inputOperand, "arg1"),
                new QueryLiteral(inputOperand, "arg2"));
        AbstractVersionsFunction handler = createVersionFunction(sanitiser);

        mockController.replay();

        final FunctionOperand expectedOperand = new FunctionOperand(FUNC_NAME, "clean");
        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser, inputOperand);
        assertEquals(expectedOperand, cleanOperand);

        mockController.verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction((LiteralSanitiser) null);
        mockController.replay();

        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
        mockController.verify();
    }

    private AbstractVersionsFunction createVersionFunction(final IndexInfoResolver infoResolver)
    {
        final AbstractVersionsFunction function = new AbstractVersionsFunction((NameResolver<Project>) mockController.getMock(NameResolver.class), mockController.getMock(PermissionManager.class))
        {
            @Override
            protected IndexInfoResolver<Project> createIndexInfoResolver(final NameResolver<Project> projectResolver)
            {
                return infoResolver;
            }

            protected Collection<Version> getAllVersions()
            {
                throw new UnsupportedOperationException();
            }

            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        function.init(MockJqlFunctionModuleDescriptor.create(FUNC_NAME, true));
        return function;
    }

    private AbstractVersionsFunction createVersionFunction(final IndexInfoResolver infoResolver, final Collection<Version> allVersions, final VersionManager versionManager)
    {
        return new AbstractVersionsFunction((NameResolver<Project>) mockController.getMock(NameResolver.class), mockController.getMock(PermissionManager.class))
        {
            @Override
            protected IndexInfoResolver<Project> createIndexInfoResolver(final NameResolver<Project> projectResolver)
            {
                return infoResolver;
            }

            protected Collection<Version> getAllVersions()
            {
                if (allVersions == null)
                {
                    fail("Not expecting this call");
                }
                return allVersions;
            }

            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                if (versionManager == null)
                {
                    fail("Not expecting this call");
                    return null;
                }
                return versionManager.getVersionsReleased(projectId, true);
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }

    private AbstractVersionsFunction createVersionFunction(final MessageSet errors)
    {
        return new AbstractVersionsFunction((NameResolver<Project>) mockController.getMock(NameResolver.class), mockController.getMock(PermissionManager.class))
        {
            protected Collection<Version> getAllVersions()
            {
                throw new UnsupportedOperationException();
            }

            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            public MessageSet validate(final com.opensymphony.user.User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
            {
                return errors;
            }
        };
    }

    private AbstractVersionsFunction createVersionFunction(final LiteralSanitiser sanitiser)
    {
        return new AbstractVersionsFunction((NameResolver<Project>) mockController.getMock(NameResolver.class), mockController.getMock(PermissionManager.class))
        {
            protected Collection<Version> getAllVersions()
            {
                throw new UnsupportedOperationException();
            }

            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            public MessageSet validate(final com.opensymphony.user.User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
            {
                return new MessageSetImpl();
            }

            @Override
            LiteralSanitiser createLiteralSanitiser(final User user)
            {
                return sanitiser;
            }
        };
    }
}
