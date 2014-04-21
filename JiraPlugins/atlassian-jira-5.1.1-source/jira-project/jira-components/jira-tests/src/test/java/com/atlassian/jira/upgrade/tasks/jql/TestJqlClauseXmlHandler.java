package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.query.Query;
import electric.xml.Document;
import electric.xml.Element;
import org.easymock.classextension.EasyMock;

/**
 * @since v4.0
 */
public class TestJqlClauseXmlHandler extends MockControllerTestCase
{
    private JqlQueryParser jqlQueryParser;

    @Before
    public void setUp() throws Exception
    {
        jqlQueryParser = mockController.getMock(JqlQueryParser.class);
    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        final JqlClauseXmlHandler handler = new JqlClauseXmlHandler(jqlQueryParser);
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testConvertNonQuery() throws Exception
    {
        final Document doc = new Document("<project><value>1</value></project>");
        final Element element = doc.getElement("project");

        final MockI18nHelper i18nHelper = new MockI18nHelper();

        replay();

        final JqlClauseXmlHandler handler = new JqlClauseXmlHandler(jqlQueryParser);
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(element);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("jira.jql.upgrade.error.converting.to.jql test project", result.getMessage(i18nHelper, "test"));
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testConvertNullQuery() throws Exception
    {
        final Document doc = new Document("<query></query>");
        final Element element = doc.getElement("query");

        final MockI18nHelper i18nHelper = new MockI18nHelper();

        EasyMock.expect(jqlQueryParser.parseQuery(""))
                .andThrow(new JqlParseException(JqlParseErrorMessages.genericParseError()));

        replay();

        final JqlClauseXmlHandler handler = new JqlClauseXmlHandler(jqlQueryParser);
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(element);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("jira.jql.upgrade.error.converting.to.jql.no.values test query", result.getMessage(i18nHelper, "test"));
    }

    @Test
    public void testConvertHappyPath() throws Exception
    {
        final Document doc = new Document("<query>type = Bug</query>");
        final Element element = doc.getElement("query");

        final MockI18nHelper i18nHelper = new MockI18nHelper();

        final Query query = JqlQueryBuilder.newBuilder().where().issueType("Bug").buildQuery();
        EasyMock.expect(jqlQueryParser.parseQuery("type = Bug"))
                .andReturn(query);

        replay();

        final JqlClauseXmlHandler handler = new JqlClauseXmlHandler(jqlQueryParser);
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(element);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertEquals(query.getWhereClause(), result.getClause());
        assertNull(result.getMessage(i18nHelper, "test"));
    }
}
