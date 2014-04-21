package com.atlassian.core.ofbiz;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;

import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import com.opensymphony.module.propertyset.PropertySet;

public class TestOFBizPropertyUtils extends AbstractOFBizTestCase
{
    @Test
    public void testCreateRemovePropertySet()
    {
        // first create it and then check we can get/set a string
        final GenericValue entity = UtilsForTests.getTestEntity("Project", UtilMisc.toMap("id", new Long(1)));
        PropertySet ps = OFBizPropertyUtils.getPropertySet(entity);
        ps.setString("foo", "bar");
        assertEquals("bar", ps.getString("foo"));

        // remove
        OFBizPropertyUtils.removePropertySet(entity);

        // now make sure it's blank
        ps = OFBizPropertyUtils.getPropertySet(entity);
        assertNull(ps.getString("foo"));
    }
}
