package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.plugins.rest.common.expand.*;
import com.atlassian.plugins.rest.common.expand.parameter.*;
import org.junit.*;
import org.mockito.*;

import java.util.*;

import javax.servlet.http.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil}.
 *
 * @since v2.1
 */
public class EntityExpansionUtilTest
{
    private final static String EXPANDABLE_FIELD_NAME = "expandableField";
    private final static String NON_EXISTENT_FIELD_NAME = "nonExistent";

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil#shouldExpandField(Class, String, com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter)}
     * returns true when the expandable field name is in the ExpandParameter.
     *
     * @throws Exception
     */
    @Test
    public void testShouldExpandField() throws Exception
    {
        ExpandParameter expandParameter = new DefaultExpandParameter(Arrays.asList(EXPANDABLE_FIELD_NAME));
        boolean shouldExpand = EntityExpansionUtil.shouldExpandField(MockEntityWithExpandableField.class, EXPANDABLE_FIELD_NAME, expandParameter);
        assertTrue(shouldExpand);
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil#shouldExpandField(Class, String, com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter)}
     * returns false when the expandable field name is not in the ExpandParameter.
     *
     * @throws Exception
     */
    @Test
    public void testShouldExpandField_NoExpansion() throws Exception
    {
        ExpandParameter expandParameter = new DefaultExpandParameter(Arrays.asList("other"));
        boolean shouldExpand = EntityExpansionUtil.shouldExpandField(MockEntityWithExpandableField.class, EXPANDABLE_FIELD_NAME, expandParameter);
        assertFalse(shouldExpand);
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil#shouldExpandField(Class, String, com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter)}
     * throws an <tt>IllegalArgumentException</tt> when the specified field does not exist.
     *
     * @throws Exception
     */
    @Test (expected = IllegalArgumentException.class)
    public void testShouldExpandField_NoField() throws Exception
    {
        ExpandParameter expandParameter = new DefaultExpandParameter(Arrays.asList(EXPANDABLE_FIELD_NAME));
        EntityExpansionUtil.shouldExpandField(MockEntityWithExpandableField.class, NON_EXISTENT_FIELD_NAME, expandParameter);
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil#getExpandParameter(javax.servlet.http.HttpServletRequest)}
     * returns an ExpandParameter. ExpandParameter should not be empty.
     *
     * @throws Exception
     */
    @Test
    public void testGetExpandParameter() throws Exception
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues(EntityExpansionUtil.EXPAND_PARAM)).thenReturn(new String[] { "attributes" });
        ExpandParameter expandParameter = EntityExpansionUtil.getExpandParameter(request);
        assertNotNull(expandParameter);
        assertFalse(expandParameter.isEmpty());
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil#getExpandParameter(javax.servlet.http.HttpServletRequest)}
     * still returns an ExpandParameter when the expand parameter is missing from the HttpServletRequest. The
     * ExpandParameter should be empty.
     *
     * @throws Exception
     */
    @Test
    public void testGetExpandParameter_NoExpandParam() throws Exception
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues(EntityExpansionUtil.EXPAND_PARAM)).thenReturn(null);
        ExpandParameter expandParameter = EntityExpansionUtil.getExpandParameter(request);
        assertNotNull(expandParameter);
        assertTrue(expandParameter.isEmpty());
    }

    private static class MockEntityWithExpandableField
    {
        @Expandable
        private Object expandableField;
    }
}
