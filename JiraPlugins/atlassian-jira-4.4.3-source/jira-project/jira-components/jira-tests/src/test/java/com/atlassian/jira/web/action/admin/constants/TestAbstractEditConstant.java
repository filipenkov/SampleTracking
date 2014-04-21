/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.priorities.EditPriority;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestAbstractEditConstant extends LegacyJiraMockTestCase
{
    private EditPriority ec;
    private GenericValue constant;

    public TestAbstractEditConstant(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // use priorities to test the abstract class
        ec = new EditPriority();
    }

    public void testGetSets()
    {
        assertNull(ec.getId());
        assertNull(ec.getName());
        assertNull(ec.getDescription());
        assertNull(ec.getIconurl());

        ec.setId("1");
        ec.setName("TEST CONSTANT");
        ec.setDescription("A Test Constant");
        ec.setIconurl("c:\test");

        assertEquals("1", ec.getId());
        assertEquals("TEST CONSTANT", ec.getName());
        assertEquals("A Test Constant", ec.getDescription());
        assertEquals("c:\test", ec.getIconurl());
    }

    public void testGetConstant() throws GenericEntityException
    {
        assertNull(ec.getConstant());
        ec.setId("1");
        assertNull(ec.getConstant());

        addConstant();

        assertEquals(constant, ec.getConstant());
    }

    public void testValidation() throws Exception
    {
        ec.setId("1");

        String result = ec.execute();

        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(ec.getErrorMessages(), "Specified constant does not exist.");
        assertEquals(3, ec.getErrors().size());
        assertEquals("You must specify a URL for the icon of the constant.", ec.getErrors().get("iconurl"));
        assertEquals("You must specify a name.", ec.getErrors().get("name"));
        assertEquals("You must specify a color for the status bar.", ec.getErrors().get("statusColor"));
    }

    public void testDefault() throws Exception
    {
        addConstant();
        ec.setId("1");

        ec.doDefault();
        assertEquals("TEST", ec.getName());
        assertEquals("This is a test Constant", ec.getDescription());
        assertEquals("C:\test", ec.getIconurl());
    }

    public void testExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPriorities.jspa");

        addConstant();
        ec.setId("1");

        ec.setName("MODIFIED");
        ec.setDescription("Description Modified");
        ec.setIconurl("c:\test");
        ec.setStatusColor("AAAAAA");

        String result = ec.execute();
        assertEquals(Action.NONE, result);

        GenericValue retrievedConstant = ec.getConstantsManager().getConstant("Priority", "1");
        assertNotNull(retrievedConstant);
        assertEquals("MODIFIED", retrievedConstant.get("name"));
        assertEquals("Description Modified", retrievedConstant.get("description"));
        assertEquals("c:\test", retrievedConstant.get("iconurl"));

        assertEquals(1, ec.getConstantsManager().getPriorities().size());

        response.verify();
    }

    private void addConstant() throws GenericEntityException
    {
        constant = EntityUtils.createValue("Priority", EasyMap.build("id", "1", "name", "TEST", "description", "This is a test Constant", "sequence", new Long(1), "iconurl", "C:\test"));
        ec.getConstantsManager().refreshPriorities();
    }
}
