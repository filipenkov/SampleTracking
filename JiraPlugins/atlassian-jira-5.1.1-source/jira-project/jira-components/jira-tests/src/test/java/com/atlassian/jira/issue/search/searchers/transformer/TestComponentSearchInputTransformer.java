package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.clause.Clause;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestComponentSearchInputTransformer extends MockControllerTestCase
{
    private NameResolver<ProjectComponent> nameResolver;
    private ClauseNames clauseNames;
    private String urlParameterName = "vp";
    private IndexInfoResolver<ProjectComponent> indexInfoResolver;
    private JqlOperandResolver operandResolver;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private SearchContextVisibilityChecker searchContextVisibilityChecker;
    private ComponentSearchInputTransformer transformer;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        nameResolver = getMock(NameResolver.class);
        clauseNames = SystemSearchConstants.forComponent().getJqlClauseNames();
        indexInfoResolver = getMock(IndexInfoResolver.class);
        operandResolver = getMock(JqlOperandResolver.class);
        fieldFlagOperandRegistry = getMock(FieldFlagOperandRegistry.class);
        searchContextVisibilityChecker = getMock(SearchContextVisibilityChecker.class);
    }

    @Test
    public void testGetSearchClauseNoNavigatorValues() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl(MapBuilder.emptyMap());
        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver);
        replay();

        final Clause result = transformer.getSearchClause(theUser, values);

        assertNull(result);
    }

    @Test
    public void testGetSearchClauseNoProjects() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl(MapBuilder.singletonMap(urlParameterName, Collections.singletonList("123")));
        final AtomicBoolean specialCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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
    public void testGetSearchClauseTwoProjects() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, Collections.singletonList("123"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), CollectionBuilder.list("1", "2"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean defaultCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(defaultCalled.get());
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

        final IndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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
    public void testGetSearchClauseOneProjectOneProjectComponentMatch() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, Collections.singletonList("123"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        EasyMock.expect(nameResolver.get(123L))
                .andReturn(new MockProjectComponent(123L, "V1", 1L));

        final IndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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
    public void testGetSearchClauseOneProjectTwoProjectComponentsOneDoesntMatch() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, CollectionBuilder.list("123", "456"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean defaultCalled = new AtomicBoolean(false);

        EasyMock.expect(nameResolver.get(123L))
                .andReturn(new MockProjectComponent(123L, "V1", 1L));

        EasyMock.expect(nameResolver.get(456L))
                .andReturn(new MockProjectComponent(456L, "V2", 2L));

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(defaultCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectTwoProjectComponentsOneDoesntResolveOneNotANumber() throws Exception
    {
        final Map<String, List<String>> map = MapBuilder.<String, List<String>>newBuilder()
                .add(urlParameterName, CollectionBuilder.list("ABC", "456"))
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        EasyMock.expect(nameResolver.get(456L))
                .andReturn(null);

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<ProjectComponent>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new ComponentSearchInputTransformer(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, nameResolver)
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

}
