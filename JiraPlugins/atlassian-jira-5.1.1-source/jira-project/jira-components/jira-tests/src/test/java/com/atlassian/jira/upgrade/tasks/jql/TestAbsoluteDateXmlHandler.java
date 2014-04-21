package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestAbsoluteDateXmlHandler extends MockControllerTestCase
{
    @Test
    public void testGetClauseFromXmlNoDates() throws Exception
    {
        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        Document document = new Document("<created name='created:absolute'></created>");

        mockController.replay();
        final AbsoluteDateXmlHandler dateXmlHandler = new AbsoluteDateXmlHandler(Collections.singletonList("created"), support);
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(document.getRoot());
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'created' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlOnlyFrom() throws Exception
    {
        Document document = new Document("<created name='created:absolute'><fromDate>347115600000</fromDate></created>");

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.getDateString(new Date(347115600000L));
        mockController.setReturnValue("1981-01-12");

        mockController.replay();

        final AbsoluteDateXmlHandler dateXmlHandler = new AbsoluteDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, "1981-01-12");
        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlOnlyTo() throws Exception
    {
        Document document = new Document("<created name='created'><toDate>55</toDate><a/><somerandom>aaaa</somerandom></created>");

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.getDateString(new Date(55));
        mockController.setReturnValue("52667263763873");

        mockController.replay();

        final AbsoluteDateXmlHandler dateXmlHandler = new AbsoluteDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "52667263763873");
        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXml() throws Exception
    {
        Document document = new Document("<created name='created'><toDate>55</toDate><a/><somerandom>aaaa</somerandom><fromDate>9</fromDate></created>");

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.getDateString(new Date(9));
        mockController.setReturnValue("3/4/0001");

        support.getDateString(new Date(55));
        mockController.setReturnValue("15/6/2007");

        mockController.replay();

        final AbsoluteDateXmlHandler dateXmlHandler = new AbsoluteDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new AndClause(
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, "3/4/0001"),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "15/6/2007"));

        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlBadParameterDate() throws Exception
    {
        Document document = new Document("<created name='created'><toDate>55</toDate><a/><somerandom>aaaa</somerandom><fromDate>yure</fromDate></created>");

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.getDateString(new Date(55));
        mockController.setReturnValue("toDate");

        mockController.replay();

        final AbsoluteDateXmlHandler dateXmlHandler = new AbsoluteDateXmlHandler(Collections.singletonList("created"), support);
        final Clause actualClause = dateXmlHandler.convertXmlToClause(document.getRoot()).getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, "toDate");

        assertEquals(expectedClause, actualClause);

        mockController.verify();
    }
}
