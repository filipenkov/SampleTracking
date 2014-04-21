package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.opensymphony.user.User;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestVersionSearchInputTransformer extends MockControllerTestCase
{
    private NameResolver<Version> nameResolver;
    private ClauseNames clauseNames;
    private String urlParameterName = "vp";
    private IndexInfoResolver<Version> indexInfoResolver;
    private JqlOperandResolver operandResolver;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private SearchContextVisibilityChecker searchContextVisibilityChecker;
    private VersionSearchInputTransformer transformer;
    private VersionManager versionManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        nameResolver = getMock(NameResolver.class);
        clauseNames = new ClauseNames("version");
        indexInfoResolver = getMock(IndexInfoResolver.class);
        operandResolver = getMock(JqlOperandResolver.class);
        fieldFlagOperandRegistry = getMock(FieldFlagOperandRegistry.class);
        searchContextVisibilityChecker = getMock(SearchContextVisibilityChecker.class);
        versionManager = getMock(VersionManager.class);
    }

    @Test
    public void testGetSearchClauseNoNavigatorValues() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl(MapBuilder.emptyMap());
        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager);
        replay();

        final Clause result = transformer.getSearchClause(theUser, values);

        assertNull(result);
    }

    @Test
    public void testGetSearchClauseNoProjects() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl(MapBuilder.singletonMap(urlParameterName, Collections.singletonList("123")));
        final AtomicBoolean specialCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the default indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    @Test
    public void testGetSearchClauseTwoProjects() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, Collections.singletonList("123"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), CollectionBuilder.list("1", "2"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the default indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }
        };        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectIsFlag() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, Collections.singletonList("123"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("-1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        final IndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }


    @Test
    public void testGetSearchClauseOneProjectOneVersionMatch() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, Collections.singletonList("123"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean helperCalled = new AtomicBoolean(false);

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                helperCalled.set(true);
                return helper;
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Simple searches should not call through to the special indexed input helper");
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(helperCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectTwoVersionsOneDoesntMatch() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, CollectionBuilder.list("123", "456"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean defaultCalled = new AtomicBoolean(false);

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                defaultCalled.set(true);
                return helper;
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Simple searches should not call through to the special indexed input helper");
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(defaultCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectTwoVersionsOneDoesntResolveOneNotANumber() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, CollectionBuilder.list("ABC", "456"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean helperCalled = new AtomicBoolean(false);

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                helperCalled.set(true);
                return helper;
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Simple searches should not call through to the special indexed input helper");
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(helperCalled.get());
    }

    @Test
    public void testQueryContainsArchivedVersions() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("version", 12345L);
        final Query query = JqlQueryBuilder.newBuilder().where().addClause(clause).buildQuery();

        operandResolver.getValues((User)null, clause.getOperand(), clause);
        mockController.setReturnValue(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), 12345L)));

        indexInfoResolver.getIndexedValues(12345L);
        mockController.setReturnValue(CollectionBuilder.list("12345"));

        versionManager.getVersion(12345L);
        final MockVersion mockVersion = new MockVersion(12345L, "My Version");
        mockVersion.setArchived(true);
        mockController.setReturnValue(mockVersion);

        mockController.replay();
        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager);

        assertTrue(transformer.queryContainsArchivedVersions(null, query));
    }

    @Test
    public void testQueryContainsArchivedVersionsMultiValueOperand() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("version", 12345L, 54321L);
        final Query query = JqlQueryBuilder.newBuilder().where().addClause(clause).buildQuery();

        operandResolver.getValues((User)null, clause.getOperand(), clause);
        mockController.setReturnValue(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), 12345L), new QueryLiteral(clause.getOperand(), 54321L)));

        indexInfoResolver.getIndexedValues(12345L);
        mockController.setReturnValue(CollectionBuilder.list("12345"));
        indexInfoResolver.getIndexedValues(54321L);
        mockController.setReturnValue(CollectionBuilder.list("54321"));

        versionManager.getVersion(12345L);
        final MockVersion mockVersion = new MockVersion(12345L, "My Version");
        mockVersion.setArchived(false);
        mockController.setReturnValue(mockVersion);
        versionManager.getVersion(54321L);
        final MockVersion mockVersion1 = new MockVersion(54321L, "My Version");
        mockVersion1.setArchived(true);
        mockController.setReturnValue(mockVersion1);

        mockController.replay();
        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager);

        assertTrue(transformer.queryContainsArchivedVersions(null, query));
    }

    @Test
    public void testQueryContainsArchivedVersionsMultiValueOperandNoArchived() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("version", 12345L, 54321L);
        final Query query = JqlQueryBuilder.newBuilder().where().addClause(clause).buildQuery();

        operandResolver.getValues((User)null, clause.getOperand(), clause);
        mockController.setReturnValue(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), 12345L), new QueryLiteral(clause.getOperand(), 54321L)));

        indexInfoResolver.getIndexedValues(12345L);
        mockController.setReturnValue(CollectionBuilder.list("12345"));
        indexInfoResolver.getIndexedValues(54321L);
        mockController.setReturnValue(CollectionBuilder.list("54321"));

        versionManager.getVersion(12345L);
        final MockVersion mockVersion = new MockVersion(12345L, "My Version");
        mockVersion.setArchived(false);
        mockController.setReturnValue(mockVersion);
        versionManager.getVersion(54321L);
        final MockVersion mockVersion1 = new MockVersion(54321L, "My Version");
        mockVersion1.setArchived(false);
        mockController.setReturnValue(mockVersion1);

        mockController.replay();
        transformer = new VersionSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver, versionManager);

        assertFalse(transformer.queryContainsArchivedVersions(null, query));
    }

}
