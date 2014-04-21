package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * @since v4.0
 */
public class TestAbstractActualValueOperatorQueryFactory extends MockControllerTestCase
{
    private IndexValueConverter indexValueConverter;

    @Before
    public void setUp() throws Exception
    {
        indexValueConverter = mockController.getMock(IndexValueConverter.class);
    }

    @Test
    public void testGetIndexValues() throws Exception
    {
        final QueryLiteral literal1 = createLiteral(10L);
        final QueryLiteral literal2 = createLiteral("10");
        final QueryLiteral literal3 = new QueryLiteral();

        EasyMock.expect(indexValueConverter.convertToIndexValue(literal1)).andReturn("Something");
        EasyMock.expect(indexValueConverter.convertToIndexValue(literal2)).andReturn(null);

        mockController.replay();

        final AbstractActualValueOperatorQueryFactory factory = new AbstractActualValueOperatorQueryFactory(indexValueConverter)
        {
        };

        final List<QueryLiteral> inputList = CollectionBuilder.newBuilder(literal1, literal2, literal3).asList();
        final List<String> result = factory.getIndexValues(inputList);

        assertEquals(2, result.size());
        assertTrue(result.contains("Something"));
        assertTrue(result.contains(null));
        
        mockController.verify();
    }
}
