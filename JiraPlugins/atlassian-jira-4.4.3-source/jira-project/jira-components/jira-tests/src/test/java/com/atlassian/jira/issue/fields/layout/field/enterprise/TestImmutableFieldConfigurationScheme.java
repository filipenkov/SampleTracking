package com.atlassian.jira.issue.fields.layout.field.enterprise;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * @since v4.0
 */
public class TestImmutableFieldConfigurationScheme extends ListeningTestCase
{
    @Test
    public void testGetFieldLayoutIdDefaultToSystemFieldConfig() throws Exception
    {
        GenericValue schemeGV = new MockGenericValue("Scheme");
        Collection<GenericValue> entities = CollectionBuilder.list(entity("1", 10000L), entity(null, null));
        ImmutableFieldConfigurationScheme fieldConfigurationScheme = new ImmutableFieldConfigurationScheme(schemeGV, entities);
        assertEquals(new Long(10000), fieldConfigurationScheme.getFieldLayoutId("1"));
        assertEquals(null, fieldConfigurationScheme.getFieldLayoutId(null));
        assertEquals(null, fieldConfigurationScheme.getFieldLayoutId("2"));
    }

    @Test
    public void testGetFieldLayoutIdBugToSystemFieldConfig() throws Exception
    {
        GenericValue schemeGV = new MockGenericValue("Scheme");
        Collection<GenericValue> entities = CollectionBuilder.list(entity("1", null), entity(null, 10000L));
        ImmutableFieldConfigurationScheme fieldConfigurationScheme = new ImmutableFieldConfigurationScheme(schemeGV, entities);
        assertEquals(null, fieldConfigurationScheme.getFieldLayoutId("1"));
        assertEquals(new Long(10000), fieldConfigurationScheme.getFieldLayoutId(null));
        assertEquals(new Long(10000), fieldConfigurationScheme.getFieldLayoutId("2"));
    }

    @Test
    public void testGetFieldLayoutIdNoneToSystemFieldConfig() throws Exception
    {
        GenericValue schemeGV = new MockGenericValue("Scheme");
        Collection<GenericValue> entities = CollectionBuilder.list(entity("1", 10011L), entity(null, 10000L));
        ImmutableFieldConfigurationScheme fieldConfigurationScheme = new ImmutableFieldConfigurationScheme(schemeGV, entities);
        assertEquals(new Long(10011), fieldConfigurationScheme.getFieldLayoutId("1"));
        assertEquals(new Long(10000), fieldConfigurationScheme.getFieldLayoutId(null));
        assertEquals(new Long(10000), fieldConfigurationScheme.getFieldLayoutId("2"));
    }

    private GenericValue entity(String issueType, Long fieldLayout)
    {
        return new MockGenericValue("Entity", FieldMap.build("issuetype", issueType).add("fieldlayout", fieldLayout));
    }
}
