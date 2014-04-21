package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.classextension.EasyMock;

import java.util.Date;
import java.util.List;

/**
 * @since v4.0
 */
public class TestAbstractDateOperatorQueryFactory extends MockControllerTestCase
{
    private JqlDateSupport jqlDateSupport;

    @Before
    public void setUp() throws Exception
    {
        jqlDateSupport = mockController.getMock(JqlDateSupport.class);
    }

    @Test
    public void testGetDateValues() throws Exception
    {
        final Date longDate = new Date(123456789L);
        final Date stringDate = new Date(987654321L);
        EasyMock.expect(jqlDateSupport.convertToDate(10L)).andReturn(longDate);
        EasyMock.expect(jqlDateSupport.convertToDate("10")).andReturn(stringDate);

        mockController.replay();

        final AbstractDateOperatorQueryFactory factory = new AbstractDateOperatorQueryFactory(jqlDateSupport)
        {
        };

        final List<Date> dateValues = factory.getDateValues(CollectionBuilder.newBuilder(createLiteral(10L), createLiteral("10"), new QueryLiteral()).asList());
        assertEquals(3, dateValues.size());
        assertTrue(dateValues.contains(longDate));
        assertTrue(dateValues.contains(stringDate));
        assertTrue(dateValues.contains(null));
        
        mockController.verify();
    }
}
