package com.atlassian.crowd.cql.parser;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.PropertyTypeService;
import com.atlassian.crowd.search.query.entity.PropertyTypeServiceImpl;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for CqlQueryParser.
 *
 * @since 2.2
 */
public class CqlQueryParserImplTest
{
    /**
     * The format used for times in the REST plugin. Conforms to ISO 8601. Format is also used in JIRA.
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    private CqlQueryParser parser;
    private PropertyTypeService service;
    private Property<String> stringProperty;
    private Property<Date> dateProperty;
    private Property<Integer> intProperty;
    private Property<Float> floatProperty;
    private Property<Boolean> boolProperty;

    @Before
    public void setup()
    {
        stringProperty = new PropertyImpl<String>("strprop", String.class);
        dateProperty = new PropertyImpl<Date>("dateprop", Date.class);
        intProperty = new PropertyImpl<Integer>("intprop", Integer.class);
        floatProperty = new PropertyImpl<Float>("floatprop", Float.class);
        boolProperty = new PropertyImpl<Boolean>("boolprop", Boolean.class);

        parser = new CqlQueryParserImpl();
        Map<String, Class> typeMap = Maps.newHashMap();
        typeMap.put("strprop", String.class);
        typeMap.put("boolprop", Boolean.class);
        typeMap.put("intprop", Integer.class);
        typeMap.put("floatprop", Float.class);
        typeMap.put("dateprop", Date.class);
        service = new PropertyTypeServiceImpl(typeMap);
    }

    @After
    public void tearDown()
    {
        parser = null;
    }

    /**
     * Tests parsing the unquoted string types
     */
    @Test
    public void testParseQuery_UnquotedStringProperty() throws Exception
    {
        final PropertyRestriction<String> expectedPR1 = Restriction.on(stringProperty).exactlyMatching("mystringval");
        final PropertyRestriction<String> expectedPR2 = Restriction.on(stringProperty).exactlyMatching("mystringval");
        final PropertyRestriction<String> expectedPR3 = Restriction.on(stringProperty).exactlyMatching("123mystringval");
        final PropertyRestriction<String> expectedPR4 = Restriction.on(stringProperty).startingWith("my");
        final PropertyRestriction<String> expectedPR5 = Restriction.on(stringProperty).containing("my");

        final SearchRestriction pr1 = parser.parseQuery("strprop = mystringval", service);
        final SearchRestriction pr2 = parser.parseQuery("strprop=mystringval", service);
        final SearchRestriction pr3 = parser.parseQuery("strprop=123mystringval", service);
        final SearchRestriction pr4 = parser.parseQuery("strprop=my*", service);
        final SearchRestriction pr5 = parser.parseQuery("strprop=*my*", service);

        assertEquals(expectedPR1, pr1);
        assertEquals(expectedPR2, pr2);
        assertEquals(expectedPR3, pr3);
        assertEquals(expectedPR4, pr4);
        assertEquals(expectedPR5, pr5);
    }

    /**
     * Tests parsing the unquoted string types
     */
    @Test
    public void testParseQuery_QuotedStringProperty() throws Exception
    {
        final PropertyRestriction<String> expectedPR1 = Restriction.on(stringProperty).exactlyMatching("mystringval");
        final PropertyRestriction<String> expectedPR2 = Restriction.on(stringProperty).exactlyMatching("mystringval");
        final PropertyRestriction<String> expectedPR3 = Restriction.on(stringProperty).exactlyMatching("123mystringval");
        final PropertyRestriction<String> expectedPR4 = Restriction.on(stringProperty).startingWith("my");
        final PropertyRestriction<String> expectedPR5 = Restriction.on(stringProperty).containing("my");

        final SearchRestriction pr1 = parser.parseQuery("strprop = \"mystringval\"", service);
        final SearchRestriction pr2 = parser.parseQuery("strprop=\"mystringval\"", service);
        final SearchRestriction pr3 = parser.parseQuery("strprop=\"123mystringval\"", service);
        final SearchRestriction pr4 = parser.parseQuery("strprop=\"my*\"", service);
        final SearchRestriction pr5 = parser.parseQuery("strprop=\"*my*\"", service);

        assertEquals(expectedPR1, pr1);
        assertEquals(expectedPR2, pr2);
        assertEquals(expectedPR3, pr3);
        assertEquals(expectedPR4, pr4);
        assertEquals(expectedPR5, pr5);
    }

    /**
     * Tests parsing the unquoted string types
     */
    @Test
    public void testParseQuery_StringProperty_Failure() throws Exception
    {
        try
        {
            parser.parseQuery("strprop=mystringval\\", service);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            parser.parseQuery("strprop=\"123mystringval\\\"", service);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    /**
     * Tests parsing a Date property
     */
    @Test
    public void testParseQuery_DateProperty() throws Exception
    {
        final Date expectedDate1 = new DateTime(2010, 12, 8, 16, 11, 21, 181, DateTimeZone.forID("+11")).toDate();
        final Date expectedDate2 = new DateTime(2010, 12, 8, 16, 11, 21, 0).toDate();
        final Date expectedDate3 = new DateTime(2010, 12, 8, 16, 0, 0, 0).toDate();
        final Date expectedDate4 = new DateTime(2010, 12, 8, 0, 0, 0, 0).toDate();
        final Date expectedDate5 = new DateTime(2010, 12, 1, 0, 0, 0, 0).toDate();
        final Date expectedDate6 = new DateTime(2010, 1, 1, 0, 0, 0, 0).toDate();
        final PropertyRestriction<Date> expectedDatePR1 = Restriction.on(dateProperty).lessThan(expectedDate1);
        final PropertyRestriction<Date> expectedDatePR2 = Restriction.on(dateProperty).lessThan(expectedDate2);
        final PropertyRestriction<Date> expectedDatePR3 = Restriction.on(dateProperty).lessThan(expectedDate3);
        final PropertyRestriction<Date> expectedDatePR4 = Restriction.on(dateProperty).lessThan(expectedDate4);
        final PropertyRestriction<Date> expectedDatePR5 = Restriction.on(dateProperty).lessThan(expectedDate5);
        final PropertyRestriction<Date> expectedDatePR6 = Restriction.on(dateProperty).lessThan(expectedDate6);

        final SearchRestriction datePR1 = parser.parseQuery("dateprop < 2010-12-08T16:11:21.181+1100", service);
        final SearchRestriction datePR2 = parser.parseQuery("dateprop < 2010-12-08T16:11:21", service);
        final SearchRestriction datePR3 = parser.parseQuery("dateprop < 2010-12-08T16", service);
        final SearchRestriction datePR4 = parser.parseQuery("dateprop < 2010-12-08", service);
        final SearchRestriction datePR5 = parser.parseQuery("dateprop < 2010-12", service);
        final SearchRestriction datePR6 = parser.parseQuery("dateprop < 2010", service);

        assertEquals(expectedDatePR1, datePR1);
        assertEquals(expectedDatePR2, datePR2);
        assertEquals(expectedDatePR3, datePR3);
        assertEquals(expectedDatePR4, datePR4);
        assertEquals(expectedDatePR5, datePR5);
        assertEquals(expectedDatePR6, datePR6);
    }
    
    /**
     * Tests parsing a Boolean property
     */
    @Test
    public void testParseQuery_BooleanProperty() throws Exception
    {
        final Boolean expectedActive = Boolean.TRUE;
        final PropertyRestriction<Boolean> expectedActivePR = Restriction.on(boolProperty).exactlyMatching(expectedActive);

        final SearchRestriction activePR = parser.parseQuery("boolprop = true", service);

        assertEquals(expectedActivePR, activePR);
    }

    /**
     * Tests parsing an unknown property type - should throw IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQuery_UnknownPropertyType() throws Exception
    {
        parser.parseQuery("unknown = true", service);
    }

    /**
     * Tests parsing an <tt>AND</tt> search restriction.
     */
    @Test
    public void testParseQuery_AndOperation()
    {
        final SearchRestriction expectedSR1 = Combine.allOf(
                Restriction.on(stringProperty).exactlyMatching("mystringval"),
                Restriction.on(intProperty).exactlyMatching(321),
                Restriction.on(floatProperty).exactlyMatching(23.0f)
        );

        SearchRestriction sr1 = parser.parseQuery("strprop = \"mystringval\" aNd intprop = 321 AND floatprop = 23.0", service);
        assertEquals(expectedSR1, sr1);
    }

    /**
     * Tests parsing an <tt>OR</tt> search restriction.
     */
    @Test
    public void testParseQuery_OrOperation()
    {
        final SearchRestriction expectedSR1 = Combine.anyOf(
                Restriction.on(stringProperty).exactlyMatching("mystringval"),
                Restriction.on(intProperty).exactlyMatching(321),
                Restriction.on(floatProperty).exactlyMatching(23.0f)
        );

        SearchRestriction sr1 = parser.parseQuery("strprop = \"mystringval\" oR intprop = 321 OR floatprop = 23.0", service);
        assertEquals(expectedSR1, sr1);
    }

    @Test
    public void testParseQuery_Failure() throws Exception
    {
        try
        {
            parser.parseQuery("blah", service);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        
        try
        {
            parser.parseQuery("blah 34 12", service);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
}
