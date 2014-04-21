package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestAbstractOperatorQueryFactory extends MockControllerTestCase
{
    private IndexInfoResolver<?> indexInfoResolver;

    @Before
    public void setUp() throws Exception
    {
        indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
    }

    @Test
    public void testGetIndexValues() throws Exception
    {
        QueryLiteral literal1 = createLiteral("10");
        QueryLiteral literal2 = createLiteral(20L);
        QueryLiteral literal3 = createLiteral(30L);
        QueryLiteral literal4 = new QueryLiteral();

        EasyMock.expect(indexInfoResolver.getIndexedValues("10")).andReturn(CollectionBuilder.newBuilder("10", "20").asList());
        EasyMock.expect(indexInfoResolver.getIndexedValues(20L)).andReturn(Collections.<String>emptyList());
        EasyMock.expect(indexInfoResolver.getIndexedValues(30L)).andReturn(null);

        mockController.replay();

        final AbstractOperatorQueryFactory factory = new AbstractOperatorQueryFactory(indexInfoResolver)
        {
        };

        final List result1 = factory.getIndexValues(CollectionBuilder.newBuilder(literal1, literal2, literal3, literal4).asList());
        assertEquals(3, result1.size());
        assertTrue(result1.contains(null));
        assertTrue(result1.contains("10"));
        assertTrue(result1.contains("20"));

        assertTrue(factory.getIndexValues(CollectionBuilder.<QueryLiteral>newBuilder().asList()).isEmpty());
        assertTrue(factory.getIndexValues(null).isEmpty());

        mockController.verify();
    }
}
