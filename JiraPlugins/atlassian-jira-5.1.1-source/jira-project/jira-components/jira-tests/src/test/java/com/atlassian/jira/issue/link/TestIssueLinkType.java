package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericValue;

public class TestIssueLinkType extends LegacyJiraMockTestCase
{
    private IssueLinkType issueLinkType;
    private GenericValue issueLinkTypeGV;

    public TestIssueLinkType(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        setupIssueLinkType("jira_test style");
    }

    private void setupIssueLinkType(String style)
    {
        issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward", "style", style));
        issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
    }

    public void testConstructor()
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));

        try
        {
            new IssueLinkTypeImpl(issue);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Entity must be an 'IssueLinkType', not '" + issue.getEntityName() + "'.", e.getMessage());
        }
    }

    public void testGetters()
    {
        assertEquals(issueLinkTypeGV.getLong("id"), issueLinkType.getId());
        assertEquals(issueLinkTypeGV.getString("linkname"), issueLinkType.getName());
        assertEquals(issueLinkTypeGV.getString("outward"), issueLinkType.getOutward());
        assertEquals(issueLinkTypeGV.getString("inward"), issueLinkType.getInward());
        assertEquals(issueLinkTypeGV.getString("style"), issueLinkType.getStyle());
    }

    public void testIsSystemLinkType()
    {
        assertTrue(issueLinkType.isSystemLinkType());
        setupIssueLinkType(null);
        assertFalse(issueLinkType.isSystemLinkType());
    }

    public void testIsSubTaskLinkType()
    {
        assertFalse(issueLinkType.isSubTaskLinkType());
        setupIssueLinkType("jira_subtask");
        assertTrue(issueLinkType.isSubTaskLinkType());
    }

    public void testCompareToNull()
    {
        assertEquals(1, issueLinkType.compareTo((IssueLinkType) null));
    }

    public void testCompareToOtherNull()
    {
        GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType2 = new IssueLinkTypeImpl(issueLinkTypeGV2);

        issueLinkTypeGV2.set("linkname", null);
        assertEquals(1, issueLinkType.compareTo(issueLinkType2));
    }

    public void testCompareToNameNull()
    {
        GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType2 = new IssueLinkTypeImpl(issueLinkTypeGV2);

        issueLinkTypeGV.set("linkname", null);
        assertEquals(-1, issueLinkType.compareTo(issueLinkType2));
    }

    public void testCompareToAllNamesNull()
    {
        GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType2 = new IssueLinkTypeImpl(issueLinkTypeGV2);

        issueLinkTypeGV.set("linkname", null);
        issueLinkTypeGV2.set("linkname", null);
        assertEquals(0, issueLinkType.compareTo(issueLinkType2));
    }

    public void testCompareToNoNulls()
    {
        GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType2 = new IssueLinkTypeImpl(issueLinkTypeGV2);

        assertEquals(issueLinkTypeGV.getString("linkname").compareTo(issueLinkTypeGV2.getString("linkname")), issueLinkType.compareTo(issueLinkType2));
    }
}
