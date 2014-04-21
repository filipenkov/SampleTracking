package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.issue.search.searchers.transformer.DateSearchInputTransformer}.
 *
 * @since v4.0
 */
@SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
public class TestDateSearchInputTransformer extends MockControllerTestCase
{
    private SearchContext searchContext;
    private DateTimeConverter dateTimeConverter;
    private User theUser = null;
    private String fieldName = "aa";
    private CustomFieldInputHelper customFieldInputHelper;
    private TimeZoneManager timeZoneManager;
    private DateTimeFormatter formatter;
    private DateTimeFormatterFactory formatterFactory;

    @Before
    public void setUp() throws Exception
    {
        searchContext = getMock(SearchContext.class);
        dateTimeConverter = getMock(DateTimeConverter.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        timeZoneManager = new TimeZoneManager()
        {
            @Override
            public TimeZone getLoggedInUserTimeZone()
            {
                return TimeZone.getDefault();
            }
            @Override
            public TimeZone getTimeZoneforUser(User user)
            {
                return TimeZone.getDefault();
            }
        };

        // set up DateTimeFormatter and its factory
        formatter = mock(DateTimeFormatter.class);
        formatterFactory = mock(DateTimeFormatterFactory.class);
        when(formatterFactory.formatter()).thenReturn(formatter);
        when(formatter.forLoggedInUser()).thenReturn(formatter);
        when(formatter.withStyle(Mockito.any(DateTimeStyle.class))).thenReturn(formatter);

        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Test
    public void testConstructor()
    {
        final DateConverter dateConverter = mockController.getMock(DateConverter.class);
        final JqlDateSupport dateSupport = mockController.getMock(JqlDateSupport.class);
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final DateSearcherConfig searcherConfig = new DateSearcherConfig("aa", new ClauseNames("aa"), fieldName);
        final DateTimeFormatterFactory dateTimeFormatterFactory = mockController.getMock(DateTimeFormatterFactory.class);

        mockController.replay();
        try
        {
            new DateSearchInputTransformer(false, null, dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
            fail("Expected and exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }


        try
        {
            new DateSearchInputTransformer(false, searcherConfig, null, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
            fail("Expected and exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearchInputTransformer(false, searcherConfig, dateConverter, dateTimeConverter, null, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
            fail("Expected and exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearchInputTransformer(false, searcherConfig, dateConverter, dateTimeConverter, operandResolver, null, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
            fail("Expected and exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        mockController.verify();
    }

    @Test
    public void testValidForNavigatorNullQuery() throws Exception
    {
        final String id = "fieldName";
        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        assertTrue(transformer.doRelevantClausesFitFilterForm(theUser, null, searchContext));
    }

    @Test
    public void testValidForNavigatorNoWhereClause() throws Exception
    {
        final String id = "fieldName";
        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        assertTrue(transformer.doRelevantClausesFitFilterForm(theUser, new QueryImpl(), searchContext));
    }

    @Test
    public void testValidForNavigatorFailed() throws Exception
    {
        final String id = "fieldName";
        TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");
        final DateSearcherInputHelper helper = mockController.getMock(DateSearcherInputHelper.class);
        helper.convertClause(clause, null, false);
        mockController.setReturnValue(new DateSearcherInputHelper.ConvertClauseResult(null, false));
        DateSearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(id, helper);
        mockController.replay();

        assertFalse(transformer.doRelevantClausesFitFilterForm(theUser, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testValidForNavigatorHappyPath() throws Exception
    {
        final String id = "fieldName";
        TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");
        final DateSearcherInputHelper helper = mockController.getMock(DateSearcherInputHelper.class);
        helper.convertClause(clause, null, false);
        mockController.setReturnValue(new DateSearcherInputHelper.ConvertClauseResult(new HashMap<String, String>(), true));
        DateSearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(id, helper);
        mockController.replay();

        assertTrue(transformer.doRelevantClausesFitFilterForm(theUser, new QueryImpl(clause), searchContext));
    }

    private DateSearchInputTransformer createTransformerForValidateForNavigatorTests(final String id, final DateSearcherInputHelper helper)
    {
        return new DateSearchInputTransformer(false, new DateSearcherConfig(id, new ClauseNames(id), fieldName), mockController.getMock(DateConverter.class), dateTimeConverter, mockController.getMock(JqlOperandResolver.class), mockController.getMock(JqlDateSupport.class), customFieldInputHelper, timeZoneManager, mockController.getMock(DateTimeFormatterFactory.class))
        {
            @Override
            DateSearcherInputHelper createDateSearcherInputHelper()
            {
                return helper;
            }
        };
    }

    @Test
    public void testPopulateFromParams()
    {
        final String id = "fieldName";
        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        final Map<String, String> map = EasyMap.build(id + ":before", "beforeValue1", id + ":after", "afterValue1", id + ":nonsense", "nonsenseValue1", id + ":next", "nextValue1");
        transformer.populateFromParams(theUser, fvh, new MockActionParams(map));

        // a few legit values
        assertEquals("beforeValue1", fvh.get(id + ":before"));
        assertEquals("afterValue1", fvh.get(id + ":after"));
        assertEquals("nextValue1", fvh.get(id + ":next"));

        assertFalse(fvh.containsKey(id + ":previous"));
        assertFalse(fvh.containsKey(id + ":nonsense"));
    }

    @Test
    public void testGetSearchClauseBlank() throws Exception
    {
        final DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause clause = transformer.getSearchClause(theUser, new FieldValuesHolderImpl());
        assertNull(clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteOnlyFrom() throws Exception
    {
        final String input = "FromValue";
        final String output = "OutputValue";
        final String id = "fieldName";
        final Timestamp time = new Timestamp(1000);

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(input);
        mockController.setReturnValue(time);

        JqlDateSupport jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(time);
        mockController.setReturnValue(output);


        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport , mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, input, null, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, output);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseAbsoluteOnlyTo() throws Exception
    {
        final String id = "fieldName";
        final String input = "ToInput";
        final String output = "ToInput";

        final Timestamp time = new Timestamp(2131738712L);

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(input);
        mockController.setReturnValue(time);

        JqlDateSupport jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(time);
        mockController.setReturnValue(output);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, input, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, output);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClausePrimaryNameAndFieldNameDifferent() throws Exception
    {
        final String id = "fieldName";
        final String input = "ToInput";
        final String output = "ToInput";

        final Timestamp time = new Timestamp(2131738712L);

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(input);
        mockController.setReturnValue(time);

        JqlDateSupport jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(time);
        mockController.setReturnValue(output);

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, id, fieldName))
                .andReturn(id);

        final DateSearchInputTransformer transformer = createTransformer(id, fieldName, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, input, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, output);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteFromTo() throws Exception
    {
        final String id = "fieldName";

        final String beforeInput = "ToValue";
        final Timestamp beforeTime = new Timestamp(2743613748223L);
        final String beforeOutput = "ToValue output";

        final String afterInput = "FromValue";
        final Timestamp afterTime = new Timestamp(472381473982L);
        final String afterOutput = "FromValue output";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterInput);
        mockController.setReturnValue(afterTime);

        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(beforeInput);
        mockController.setReturnValue(beforeTime);


        final JqlDateSupport jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(afterTime);
        mockController.setReturnValue(afterOutput);
        jqlDateSupport.getDateString(beforeTime);
        mockController.setReturnValue(beforeOutput);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterInput, beforeInput, null, null));
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeOutput);
        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput);
        final Clause expected = new AndClause(expectedAfter, expectedBefore);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseRelativeOnlyFrom() throws Exception
    {
        final String id = "why";
        final String input = "-4d";

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, null, input, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, input);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseRelativeOnlyTo() throws Exception
    {
        final String id = "why";
        final String input = "-78h";

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, null, null, input));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, input);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseRelativeFromTo() throws Exception
    {
        final String id = "testGetSearchClauseRelativeFromTo";

        final String beforeInput = "-3h";
        final String afterInput = "-3w";

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, null, afterInput, beforeInput));
        final Clause expectedPrevious = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterInput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput);
        final Clause expected = new AndClause(expectedPrevious, expectedNext);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseAbsoluteFromRelativeTo() throws Exception
    {
        final String id = "testGetSearchClauseRelativeFromTo";

        final String beforeInput = "-17d";
        
        final String afterInput = "FromValueTestGetSearchClauseRelativeFromTo";
        final Timestamp afterTime = new Timestamp(7676);
        final String afterOutput = "FromValueTestGetSearchClauseRelativeFromTo output";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterInput);
        mockController.setReturnValue(afterTime);


        final JqlDateSupport jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(afterTime);
        mockController.setReturnValue(afterOutput);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterInput, null, null, beforeInput));

        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput);
        final Clause expected = new AndClause(expectedNext, expectedAfter);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseAbsoluteFromToAndRelativeTo() throws Exception
    {
        final String id = "testGetSearchClauseAbsoluteFromToAndRelativeTo";

        final String beforeRelInput = "-6h";
        final String beforeAbsInput = "beforeAbsInput";
        final Timestamp beforeAbsTime = new Timestamp(1111);
        final String beforeAbsOutput = "beforeAbsOutput";

        final String afterAbsInput = "afterAbsInput";
        final Timestamp afterAbsTime = new Timestamp(2222);
        final String afterAbsOutput = "afterAbsOutput";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterAbsInput);
        mockController.setReturnValue(afterAbsTime);

        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(beforeAbsInput);
        mockController.setReturnValue(beforeAbsTime);

        final JqlDateSupport jqlDateSupport = mockController.getNiceMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(beforeAbsTime);
        mockController.setReturnValue(beforeAbsOutput);
        jqlDateSupport.getDateString(afterAbsTime);
        mockController.setReturnValue(afterAbsOutput);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterAbsInput, beforeAbsInput, null, beforeRelInput));

        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterAbsOutput);
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeAbsOutput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeRelInput);
        final Clause expectedAbsolute = new AndClause(expectedAfter, expectedBefore);
        final Clause expected = new AndClause(expectedNext, expectedAbsolute);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseAbsoluteAndRelative() throws Exception
    {
        final String id = "testGetSearchClauseAbsoluteAndRelative";

        final String beforeRelInput = "-2w";

        final String afterRelInput = "-5w";

        final String beforeAbsInput = "beforeAbsInput";
        final Timestamp beforeAbsTime = new Timestamp(111);
        final String beforeAbsOutput = "beforeAbsOutput";

        final String afterAbsInput = "afterAbsInput";
        final Timestamp afterAbsTime = new Timestamp(222);
        final String afterAbsOutput = "afterAbsOutput";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterAbsInput);
        mockController.setReturnValue(afterAbsTime);

        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(beforeAbsInput);
        mockController.setReturnValue(beforeAbsTime);

        final JqlDateSupport jqlDateSupport = mockController.getNiceMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(beforeAbsTime);
        mockController.setReturnValue(beforeAbsOutput);
        jqlDateSupport.getDateString(afterAbsTime);
        mockController.setReturnValue(afterAbsOutput);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));

        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterAbsInput, beforeAbsInput, afterRelInput, beforeRelInput));

        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterAbsOutput);
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeAbsOutput);
        final Clause expectedPrevious = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterRelInput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeRelInput);
        final Clause expectedAbsolute = new AndClause(expectedAfter, expectedBefore);
        final Clause expectedRelative = new AndClause(expectedPrevious, expectedNext);
        final Clause expected = new AndClause(expectedRelative, expectedAbsolute);
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseInvalidDate() throws Exception
    {
        final String id = "no...serously....why";
        final String input = "myinput";

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        final Clause result = transformer.getSearchClause(theUser, createFieldValuesHolder(id, null, null, null, input));
        TerminalClause expectedResult = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, input);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseInvalidDateWithValidPeriod() throws Exception
    {
        final String id = "try me please";

        final String afterInput = "invalidInput";
        final Timestamp afterTime = new Timestamp(30557);

        final String nextInput = "6d";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterInput);
        mockController.setReturnValue(afterTime);

        final JqlDateSupport jqlDateSupport = mockController.getNiceMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(afterTime);
        mockController.setReturnValue(null);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);

        Clause expectedClause = new AndClause(new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, nextInput), new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterInput));

        assertEquals(expectedClause, transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterInput, null, null, nextInput)));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseValidDateWithInvalidPeriod() throws Exception
    {
        final String id = "try me please";

        final String afterInput = "validInput";
        final Timestamp afterTime = new Timestamp(46321);
        final String afterOutput = "validOutput";

        final String beforeInput = "invalidInput";

        DateTimeFormatter formatter = mockController.getMock(DateTimeFormatter.class);
        DateTimeFormatterFactory formatterFactory = mockController.getMock(DateTimeFormatterFactory.class);
        formatterFactory.formatter();
        mockController.setReturnValue(formatter);
        formatter.withStyle(DateTimeStyle.DATE_PICKER);
        mockController.setReturnValue(formatter);
        formatter.forLoggedInUser();
        mockController.setReturnValue(formatter);
        formatter.parse(afterInput);
        mockController.setReturnValue(afterTime);

        final JqlDateSupport jqlDateSupport = mockController.getNiceMock(JqlDateSupport.class);
        jqlDateSupport.getDateString(afterTime);
        mockController.setReturnValue(afterOutput);

        final DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(),jqlDateSupport, mockController.getMock(DateConverter.class), formatterFactory);
        final Clause clause = transformer.getSearchClause(theUser, createFieldValuesHolder(id, afterInput, null, null, beforeInput));

        Clause expected = new AndClause(new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput), new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput));
        assertEquals(expected, clause);

        mockController.verify();
    }

    @Test
    public void testValidateParamsEmpty() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, new FieldValuesHolderImpl(), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteFromOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenReturn(new Date());

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", "After", null, null, null), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteFromOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenThrow(new IllegalArgumentException("Message!"));
        when(formatter.getFormatHint()).thenReturn("Format invalid");

        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder(id, "After", null, null, null), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.data.format Format invalid", errors.getErrors().get("fieldName:after"));

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteToOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("Before")).thenReturn(new Date());

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", null, "Before", null, null), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteToOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("Before")).thenThrow(new IllegalArgumentException("Message!"));
        when(formatter.getFormatHint()).thenReturn("Format invalid");

        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder(id, null, "Before", null, null), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.data.format Format invalid", errors.getErrors().get("fieldName:before"));

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeFromOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", null, null, "4d", null), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeFromOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder(id, null, null, "invalidDuration", null), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.date.period.format navigator.filter.constants.duedate.from", errors.getErrors().get("fieldName:previous"));

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeToOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();


        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", null, null, null, "4d"), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeToOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer(id, MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder(id, null, null, null, "invalidDuration"), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.date.period.format navigator.filter.constants.duedate.to", errors.getErrors().get("fieldName:next"));

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteFromToHappy() throws Exception
    {
        final Timestamp now = new Timestamp(new Date().getTime());
        final Timestamp aBitLater = new Timestamp(new Date().getTime() + 5000L);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenReturn(now);
        when(formatter.parse("Before")).thenReturn(aBitLater);

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", "After", "Before", null, null), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsAbsoluteFromToSad() throws Exception
    {
        final Timestamp now = new Timestamp(new Date().getTime());
        final Timestamp aBitLater = new Timestamp(new Date().getTime() + 5000L);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();


        when(formatter.parse("After")).thenReturn(aBitLater);
        when(formatter.parse("Before")).thenReturn(now);

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), formatterFactory);
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", "After", "Before", null, null), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.date.absolute.before.after", errors.getErrors().get("fieldName:after"));

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeFromToHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", null, null, "-4d", "4d"), i18n, errors);
        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsRelativeFromToSad()
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.validateParams(theUser, null, createFieldValuesHolder("fieldName", null, null, "4d", "-4d"), i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("fields.validation.date.period.from.to", errors.getErrors().get("fieldName:previous"));

        mockController.verify();
    }

    /**
     * Test for when there is no search in the query.
     */
    @Test
    public void testPopulateFromSearchRequestNoSearchQuery()
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        DateSearchInputTransformer transformer = createTransformer("testName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, holder, new QueryImpl(), searchContext);

        assertEquals(new FieldValuesHolderImpl(), holder);

        mockController.verify();
    }

    /**
     * Test for when there is no clause in the query.
     */
    @Test
    public void testPopulateFromSearchRequestNoClause()
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        DateSearchInputTransformer transformer = createTransformer("testName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, holder, new QueryImpl(), searchContext);

        assertEquals(new FieldValuesHolderImpl(), holder);

        mockController.verify();
    }

    /**
     * Test for when there is no date clauses.
     */
    @Test
    public void testPopulateFromSearchRequestNoDate()
    {
        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        DateSearchInputTransformer transformer = createTransformer("testName", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, new QueryImpl(new TerminalClauseImpl("notTestName", Operator.EQUALS, "something")), searchContext);

        assertEquals(expectedHolder, actualHolder);
        mockController.verify();
    }

    /**
     * Test what happens when the operand returns multiple dates. We don't support this.
     */
    @Test
    public void testPopulateFromSearchRequestTooManyDates()
    {
        final FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        final FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final DateSearchInputTransformer transformer = createTransformer("created2", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, new QueryImpl(new TerminalClauseImpl("created2", Operator.LESS_THAN_EQUALS, new MultiValueOperand("something", "more"))), searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= "-3w"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTo()
    {
        final String beforeDate = "-3w";

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:next", beforeDate);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, new QueryImpl(new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)), searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeFrom()
    {
        final String afterDate = "-2h";

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:previous", afterDate);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, new QueryImpl(new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, afterDate)), searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w" and created <= -2h' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeFromAndTo()
    {
        final String afterDate = "-2w";
        final String beforeDate = "-2h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, afterDate),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:previous", afterDate);
        expectedHolder.put("created:next", beforeDate);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
        mockController.verify();
    }

    /**
     * Check that the query 'created <= "-2w" and created <= -2h' is not converted.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTooManyTo()
    {
        final String beforeDate2 = "-2w";
        final String beforeDate = "-2h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate2),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
        mockController.verify();
    }

    /**
     * Check that the query 'created >= "-2w" and created >= -2h and created<=-5h' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTooManyFrom()
    {
        final String afterDate = "-2w";
        final String afterDate2 = "-2h";
        final String beforeDate = "-5h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, afterDate),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, afterDate2),
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= "10/10/2009"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteTo()
    {
        final String toInput = "10/10/2009";
        final Date toDate = createDate(2009, 11, 10, 0, 0, 0, 0);
        final String toOutput = "10/Nov/2009";

        final Query query = new QueryImpl(new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= 1014873287389' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteFrom()
    {
        final Long fromInput = 1014873287389L;
        final Date fromDate = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String fromOutput = "12/Jan/1981";

        final Query query = new QueryImpl(new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, fromInput));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:after", fromOutput);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= 1000 and created >= "25/12/2008"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteFromAndTo()
    {
        final Long toInput = 1000L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";

        final String fromInput = "25/12/2008";
        final Date fromDate = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String fromOutput = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, fromInput)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:after", fromOutput);
        expectedHolder.put("created:before", toOutput);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created <= 10001 and created <= 20002' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsolutTooManyTo()
    {
        final Long toInput = 10001L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";

        final Long toInput2 = 20002L;
        final Date toDate2 = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String toOutput2 = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput2)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(toInput);
        mockController.setReturnValue(toDate);
        support.convertToDate(toInput2);
        mockController.setReturnValue(toDate2);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(toDate);
        mockController.setReturnValue(toOutput);

        convert.getString(toDate2);
        mockController.setReturnValue(toOutput2);

        DateSearchInputTransformer transformer = createTransformer("created", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query 'created >= 10 and created >= 20 and created <= -3w' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsolutTooManyFrom()
    {
        final Long fromInput = 10L;
        final Date fromDate = createDate(1945, 7, 4, 0, 0, 0, 0);
        final String fromOutput = "4/July/1945";

        final Long fromInput2 = 20L;
        final Date fromDate2 = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String fromOutput2 = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("notCreated", Operator.GREATER_THAN_EQUALS, fromInput),
                new TerminalClauseImpl("notCreated", Operator.GREATER_THAN_EQUALS, fromInput2),
                new TerminalClauseImpl("notCreated", Operator.LESS_THAN_EQUALS, "-3w")
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(fromInput);
        mockController.setReturnValue(fromDate);
        support.convertToDate(fromInput2);
        mockController.setReturnValue(fromDate2);

        final DateConverter convert = mockController.getMock(DateConverter.class);
        convert.getString(fromDate);
        mockController.setReturnValue(fromOutput);

        convert.getString(fromDate2);
        mockController.setReturnValue(fromOutput2);

        DateSearchInputTransformer transformer = createTransformer("notCreated", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testPopulateFromSearchRequestBadLiteral()
    {
        final Operand operand = new SingleValueOperand("-67w");
        final TerminalClauseImpl clause = new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, operand);
        final Query query = new QueryImpl(clause);

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isEmptyOperand(operand);
        mockController.setDefaultReturnValue(false);
        jqlOperandResolver.isFunctionOperand(operand);
        mockController.setDefaultReturnValue(false);
        jqlOperandResolver.getValues((com.atlassian.crowd.embedded.api.User) theUser, operand, clause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral(new SingleValueOperand("blarg"), (String)null)));

        DateSearchInputTransformer transformer = createTransformer("field1", jqlOperandResolver, mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query with a bad date does not compute.
     */
    @Test
    public void testPopulateFromQueryInvalidDate()
    {
        final String badDate = "13/13/2008";
        final Query query = new QueryImpl(new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, badDate));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add("field1:before", badDate).toMap());

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(badDate);
        mockController.setReturnValue(null);

        DateSearchInputTransformer transformer = createTransformer("field1", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), mockController.getMock(DateConverter.class), mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    /**
     * Check that the query where the navigator cannot display the date without loss cannot be handled.
     */
    @Test
    public void testPopulateFromQueryNotLossy()
    {
        final String lossyInput = "2008/12/25 15:00";
        final Date lossyDate = createDate(2008, 12, 25, 15, 0, 0, 0);
        final String lossyDateOutput = "lossyDateOutput";
        final String okDateString = "6/10/2008";
        final Date okDate = createDate(2008, 10, 6, 0, 0, 0, 0);
        final String okDateOutput = "6/10/2008";
        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, lossyInput),
                new TerminalClauseImpl("field1", Operator.GREATER_THAN_EQUALS, okDateString))
        );

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(
                MapBuilder.newBuilder()
                        .add("field1:before", lossyDateOutput)
                        .add("field1:after", okDateOutput)
                        .toMap());

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final JqlDateSupport support = mockController.getMock(JqlDateSupport.class);
        support.convertToDate(lossyInput);
        mockController.setReturnValue(lossyDate);
        support.convertToDate(okDateString);
        mockController.setReturnValue(okDate);

        dateTimeConverter.getString(lossyDate);
        mockController.setReturnValue(lossyDateOutput);

        final DateConverter dateConverter = mockController.getMock(DateConverter.class);
        dateConverter.getString(okDate);
        mockController.setReturnValue(okDateOutput);

        DateSearchInputTransformer transformer = createTransformer("field1", MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(JqlDateSupport.class), dateConverter, mockController.getMock(DateTimeFormatterFactory.class));
        transformer.populateFromQuery(theUser, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);

        mockController.verify();
    }

    private FieldValuesHolder createFieldValuesHolder(String id, String after, String before, String previous, String next)
    {
        return new FieldValuesHolderImpl(EasyMap.build(id + ":before", before, id + ":after", after, id + ":previous", previous, id + ":next", next));
    }

    private DateSearchInputTransformer createTransformer(String id, final JqlOperandResolver operandResolver, final JqlDateSupport dateSupport, final DateConverter dateConverter, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        mockController.replay();
        return new DateSearchInputTransformer(false, new DateSearcherConfig(id, new ClauseNames(id), id), dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
    }

    private DateSearchInputTransformer createTransformer(String id, String fieldName, final JqlOperandResolver operandResolver, final JqlDateSupport dateSupport, final DateConverter dateConverter, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        mockController.replay();
        return new DateSearchInputTransformer(false, new DateSearcherConfig(id, new ClauseNames(id), fieldName), dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
    }

    private Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal.getTime();
    }

    private static class MockActionParams implements ActionParams
    {
        private Map<String, String> params = new HashMap<String, String>();

        private MockActionParams(final Map<String, String> params)
        {
            this.params = params;
        }

        public String[] getAllValues()
        {
            throw new UnsupportedOperationException();
        }

        public String[] getValuesForNullKey()
        {
            throw new UnsupportedOperationException();
        }

        public String[] getValuesForKey(final String key)
        {
            throw new UnsupportedOperationException();
        }

        public String getFirstValueForNullKey()
        {
            throw new UnsupportedOperationException();
        }

        public String getFirstValueForKey(final String key)
        {
            return params.get(key);
        }

        public void put(final String id, final String[] values)
        {
            throw new UnsupportedOperationException();
        }

        public Set getAllKeys()
        {
            throw new UnsupportedOperationException();
        }

        public Map getKeysAndValues()
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(final String key)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty()
        {
            throw new UnsupportedOperationException();
        }
    }
}
