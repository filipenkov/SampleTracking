package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.crowd.search.builder.*;
import com.atlassian.crowd.search.query.entity.restriction.*;
import com.atlassian.crowd.search.query.entity.restriction.constants.*;
import org.junit.*;

import static org.junit.Assert.assertNull;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link com.atlassian.crowd.integration.rest.util.SearchRestrictionEntityTranslator}.
 *
 * @since v2.1
 */
public class SearchRestrictionEntityTranslatorTest
{
    /**
     * Tests converting from a BooleanRestriction to a BooleanRestrictionEntity. Checks that the boolean logic are the
     * same. Checking the conversion of the actual collection of restrictions is done by tests for
     * {@link com.atlassian.crowd.plugin.rest.util.SearchRestrictionEntityTranslator#toSearchRestrictionEntity(com.atlassian.crowd.embedded.api.SearchRestriction)}.
     */
    @Test
    public void testToBooleanRestrictionEntity() throws Exception
    {
        // test OR
        BooleanRestriction booleanRestriction = Combine.anyOf(Restriction.on(UserTermKeys.DISPLAY_NAME).exactlyMatching("bob"));
        BooleanRestrictionEntity booleanRestrictionEntity = SearchRestrictionEntityTranslator.toBooleanRestrictionEntity(booleanRestriction);
        assertEquals(booleanRestriction.getBooleanLogic().name(), booleanRestrictionEntity.getBooleanLogic());

        // test AND
        booleanRestriction = Combine.allOf(Restriction.on(UserTermKeys.DISPLAY_NAME).exactlyMatching("bob"));
        booleanRestrictionEntity = SearchRestrictionEntityTranslator.toBooleanRestrictionEntity(booleanRestriction);
        assertEquals(booleanRestriction.getBooleanLogic().name(), booleanRestrictionEntity.getBooleanLogic());
    }

    /**
     * Tests converting from a BooleanRestrictionEntity to a BooleanRestriction. Checks that the boolean logic are the
     * same. Checking the conversion of the actual collection of restrictions is done by tests for
     * {@link com.atlassian.crowd.integration.rest.util.SearchRestrictionEntityTranslator#toSearchRestriction(com.atlassian.crowd.integration.rest.entity.SearchRestrictionEntity)}.
     */
    @Test
    public void testToBooleanRestriction() throws Exception
    {
        // test AND
        BooleanRestrictionEntity booleanRestrictionEntity = new BooleanRestrictionEntity(BooleanRestriction.BooleanLogic.AND.name(), Arrays.<SearchRestrictionEntity>asList(NullRestrictionEntity.INSTANCE));
        BooleanRestriction booleanRestriction = SearchRestrictionEntityTranslator.toBooleanRestriction(booleanRestrictionEntity);
        assertEquals(booleanRestrictionEntity.getBooleanLogic(), booleanRestriction.getBooleanLogic().name());

        // test OR
        booleanRestrictionEntity = new BooleanRestrictionEntity(BooleanRestriction.BooleanLogic.OR.name(), Arrays.<SearchRestrictionEntity>asList(NullRestrictionEntity.INSTANCE));
        booleanRestriction = SearchRestrictionEntityTranslator.toBooleanRestriction(booleanRestrictionEntity);
        assertEquals(booleanRestrictionEntity.getBooleanLogic(), booleanRestriction.getBooleanLogic().name());
    }

    /**
     * Tests that converting from a BooleanRestrictionEntity to a BooleanRestriction succeeds even when the case of the
     * boolean logic doesn't exactly match the BooleanLogic enum constant.
     */
    @Test
    public void testToBooleanRestriction_CaseInsensitive() throws Exception
    {
        BooleanRestrictionEntity booleanRestrictionEntity = new BooleanRestrictionEntity("aNd", Arrays.<SearchRestrictionEntity>asList(NullRestrictionEntity.INSTANCE));
        BooleanRestriction booleanRestriction = SearchRestrictionEntityTranslator.toBooleanRestriction(booleanRestrictionEntity);
        assertEquals(booleanRestrictionEntity.getBooleanLogic().toUpperCase(), booleanRestriction.getBooleanLogic().name());
    }

    /**
     * Tests converting from a BooleanRestrictionEntity to a BooleanRestriction with an unknown boolean logic throws an
     * IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanRestriction_UnknownBooleanLogic() throws Exception
    {
        BooleanRestrictionEntity booleanRestrictionEntity = new BooleanRestrictionEntity("UNKNOWN_LOGIC", Arrays.<SearchRestrictionEntity>asList(NullRestrictionEntity.INSTANCE));
        SearchRestrictionEntityTranslator.toBooleanRestriction(booleanRestrictionEntity);
    }

    /**
     * Tests converting from a PropertyRestriction to a PropertyRestrictionEntity.
     */
    @Test
    public void testToPropertyRestrictionEntity() throws Exception
    {
        // test MatchMode.EXACTLY_MATCHES
        PropertyRestriction propertyRestriction = Restriction.on(UserTermKeys.DISPLAY_NAME).exactlyMatching("Bob");
        PropertyRestrictionEntity propertyRestrictionEntity = SearchRestrictionEntityTranslator.toPropertyRestrictionEntity(propertyRestriction);
        assertEquals(propertyRestriction.getMatchMode().name(), propertyRestrictionEntity.getMatchMode());

        // test MatchMode.CONTAINS
        propertyRestriction = Restriction.on(UserTermKeys.DISPLAY_NAME).containing("Bob");
        propertyRestrictionEntity = SearchRestrictionEntityTranslator.toPropertyRestrictionEntity(propertyRestriction);
        assertEquals(propertyRestriction.getMatchMode().name(), propertyRestrictionEntity.getMatchMode());
    }

    /**
     * Tests converting from a PropertyRestrictionEntity to a PropertyRestriction.
     */
    @Test
    public void testToPropertyRestriction() throws Exception
    {
        PropertyEntity propertyEntity = new PropertyEntity("score", SearchRestrictionEntityTranslator.SupportedType.STRING.name());
        PropertyRestrictionEntity propertyRestrictionEntity = new PropertyRestrictionEntity(propertyEntity, MatchMode.CONTAINS.name(), "123");
        PropertyRestriction propertyRestriction = SearchRestrictionEntityTranslator.toPropertyRestriction(propertyRestrictionEntity);
        assertEquals(propertyRestrictionEntity.getMatchMode(), propertyRestriction.getMatchMode().name());
    }

    /**
     * Tests converting from a PropertyRestrictionEntity to a PropertyRestriction with an unknown match mode. An
     * IllegalArgumentException is expected to be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToPropertyRestriction_UnknownMatchMode() throws Exception
    {
        PropertyEntity propertyEntity = new PropertyEntity("score", SearchRestrictionEntityTranslator.SupportedType.STRING.name());
        PropertyRestrictionEntity propertyRestrictionEntity = new PropertyRestrictionEntity(propertyEntity, "UNKNOWN_MATCH_MODE", "123");
        SearchRestrictionEntityTranslator.toPropertyRestriction(propertyRestrictionEntity);
    }

    /**
     * Tests converting a Property to a PropertyEntity.
     */
    @Test
    public void testToPropertyEntity() throws Exception
    {
        Property property = new PropertyImpl("bob", String.class);
        PropertyEntity propertyEntity = SearchRestrictionEntityTranslator.toPropertyEntity(property);
        assertEquals(property.getPropertyName(), propertyEntity.getName());
        assertEquals(SearchRestrictionEntityTranslator.SupportedType.STRING.name(), propertyEntity.getType());
    }

    /**
     * Tests converting from PropertyEntity to Property.
     */
    @Test
    public void testToProperty() throws Exception
    {
        PropertyEntity propertyEntity = new PropertyEntity("bob", SearchRestrictionEntityTranslator.SupportedType.BOOLEAN.name());
        Property property = SearchRestrictionEntityTranslator.toProperty(propertyEntity);
        assertEquals(propertyEntity.getName(), property.getPropertyName());
        assertEquals(Boolean.class, property.getPropertyType());
    }

    /**
     * Tests converting from PropertyEntity to Property succeeds even when the case of property type doesn't exactly
     * match the SupportedType enum constant.
     */
    @Test
    public void testToProperty_CaseInsensitive() throws Exception
    {
        PropertyEntity propertyEntity = new PropertyEntity("bob", "StRiNg");
        Property property = SearchRestrictionEntityTranslator.toProperty(propertyEntity);
        assertEquals(propertyEntity.getName(), property.getPropertyName());
        assertEquals(String.class, property.getPropertyType());
    }

    /**
     * Tests that converting PropertyEntity with an unknown type throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToProperty_UnknownType() throws Exception
    {
        PropertyEntity propertyEntity = new PropertyEntity("bob", "UNKNOWN_TYPE");
        SearchRestrictionEntityTranslator.toProperty(propertyEntity);
    }

    /**
     * Tests that converting to and from a time string results in a Date returned that is equal to the original Date.
     */
    @Test
    public void testConvertingDate() throws Exception
    {
        final Date date = new Date();
        final String timeStr = SearchRestrictionEntityTranslator.asTimeString(date);
        final Date convertedDate = SearchRestrictionEntityTranslator.fromTimeString(timeStr);
        assertEquals(date, convertedDate);
    }

    /**
     * Tests that converting from a bad time string throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFromTimeString_BadString() throws Exception
    {
        SearchRestrictionEntityTranslator.fromTimeString("BadTimeString");
    }

    @Test
    public void nullRestrictionCanBeTranslated()
    {
        PropertyRestriction<String> sr = Restriction.on(UserTermKeys.FIRST_NAME).isNull();
        SearchRestrictionEntity entity = SearchRestrictionEntityTranslator.toSearchRestrictionEntity(sr);

        PropertyRestrictionEntity pre = (PropertyRestrictionEntity) entity;
        assertEquals("firstName", pre.getProperty().getName());
        assertEquals("STRING", pre.getProperty().getType());
        assertEquals("NULL", pre.getMatchMode());
        assertNull(pre.getValue());
    }
}
