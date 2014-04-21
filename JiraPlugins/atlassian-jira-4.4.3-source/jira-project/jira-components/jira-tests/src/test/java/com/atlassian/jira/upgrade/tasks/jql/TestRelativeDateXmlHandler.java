package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestRelativeDateXmlHandler extends MockControllerTestCase
{
    @Test
    public void testGetClauseFromXmlNoDates() throws Exception
    {
        Document document = new Document("<created name='created:relative'></created>");

        final RelativeDateXmlHandler dateXmlHandler = new RelativeDateXmlHandler(Collections.singletonList("created"));
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(document.getRoot());
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'created' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlOnlyFrom() throws Exception
    {
        Document document = new Document("<created name='created:relative'><previousOffset>259200000</previousOffset></created>");

        @SuppressWarnings ({ "unchecked" }) Function<Long, String> support = mockController.getMock(Function.class);
        support.get(259200000L);
        mockController.setReturnValue("-3d");

        mockController.replay();

        final RelativeDateXmlHandler dateXmlHandler = new RelativeDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, "-3d");
        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlOnlyTo() throws Exception
    {
        Document document = new Document("<created name='created'><nextOffset>55</nextOffset><a/><somerandom>aaaa</somerandom></created>");

        @SuppressWarnings ({ "unchecked" }) Function<Long, String> support = mockController.getMock(Function.class);
        support.get(55L);
        mockController.setReturnValue("44w");

        mockController.replay();

        final RelativeDateXmlHandler dateXmlHandler = new RelativeDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "44w");
        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXml() throws Exception
    {
        Document document = new Document("<created name='created'><nextOffset>55</nextOffset><a/><somerandom>aaaa</somerandom><previousOffset>9</previousOffset></created>");

        @SuppressWarnings ({ "unchecked" }) Function<Long, String> support = mockController.getMock(Function.class);
        support.get(9L);
        mockController.setReturnValue("3d 2h");

        support.get(55L);
        mockController.setReturnValue("3w 2d");

        mockController.replay();

        final RelativeDateXmlHandler dateXmlHandler = new RelativeDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new AndClause(
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, "3d 2h"),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "3w 2d"));

        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlBadParameterDate() throws Exception
    {
        Document document = new Document("<created name='created'><nextOffset>55</nextOffset><a/><somerandom>aaaa</somerandom><previousOffset>yure</previousOffset></created>");

        @SuppressWarnings ({ "unchecked" }) Function<Long, String> support = mockController.getMock(Function.class);
        support.get(55L);
        mockController.setReturnValue("nextOffset");

        mockController.replay();

        final RelativeDateXmlHandler dateXmlHandler = new RelativeDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "nextOffset");

        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }
}
