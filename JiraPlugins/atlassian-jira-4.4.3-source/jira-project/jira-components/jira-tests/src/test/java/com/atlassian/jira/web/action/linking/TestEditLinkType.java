/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.linking;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.linking.EditLinkType;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestEditLinkType extends LegacyJiraMockTestCase
{
    private Mock mockIssueLinkTypeManager;

    public TestEditLinkType(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);
    }

    public void testGetSets()
    {
        String name = "link name";
        String inward = "inward desc";
        String outward = "outward desc";
        Long id = new Long(1);

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward(inward);
        editLinkType.setOutward(outward);

        assertEquals(name, editLinkType.getName());
        assertEquals(inward, editLinkType.getInward());
        assertEquals(outward, editLinkType.getOutward());
        assertEquals(id, editLinkType.getId());
    }

    public void testValidationFailsWithInvalidLink() throws Exception
    {
        GenericValue existingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));

        Long id = new Long(10);
        final String name = "existing link name";

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual(name)), Collections.EMPTY_LIST);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(id)), null);

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");


        String result = editLinkType.execute();
        assertEquals(Action.INPUT, result);

        Collection errors = editLinkType.getErrorMessages();
        assertEquals(1, errors.size());
        checkSingleElementCollection(errors, "No link type with ID " + id + " found.");

        verifyMocks();
    }

    public void testValidationFailsWithNoOutwardNoInward() throws Exception
    {
        GenericValue existingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));

        Long id = new Long(1);

        final String name = "existing link name";
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual(name)), Collections.EMPTY_LIST);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(id)), null);

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);
        editLinkType.setName(name);

        String result = editLinkType.execute();
        assertEquals(Action.INPUT, result);

        Map errors = editLinkType.getErrors();
        assertEquals(2, errors.size());
        assertEquals("Please specify a description for the outward link.", errors.get("outward"));
        assertEquals("Please specify a description for the inward link.", errors.get("inward"));

        verifyMocks();
    }

    public void testValidationFailsWithNoName() throws Exception
    {
        GenericValue existingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", null, "inward", "inward desc", "outward", "outward desc"));

        Long id = new Long(1);

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(id)), new IssueLinkType(existingLinkType));

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");

        String result = editLinkType.execute();
        assertEquals(Action.INPUT, result);

        Map errors = editLinkType.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Please specify a name for this link type.", errors.get("name"));

        verifyMocks();
    }

    public void testValidationFailsWithSameLinkNameAndDifferentID() throws Exception
    {
        GenericValue existingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));
        GenericValue anotherExistingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(2), "linkname", "another existing link name", "inward", "inward desc", "outward", "outward desc"));

        Long id = new Long(1);
        final String name = "another existing link name";

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual(name)), EasyList.build(new IssueLinkType(anotherExistingLinkType)));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(id)), new IssueLinkType(existingLinkType));

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");

        String result = editLinkType.execute();
        assertEquals(Action.INPUT, result);

        Map errors = editLinkType.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Another link type with this name already exists.", errors.get("name"));

        verifyMocks();
    }

    public void testValidationSucceedsWithNewLinkNameAndSameID() throws Exception
    {
        GenericValue existingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));
        GenericValue anotherExistingLinkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(2), "linkname", "another existing link name", "inward", "inward desc", "outward", "outward desc"));

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewLinkTypes!default.jspa");

        final Long id = new Long(1);
        final String name = "new link name";
        final String inwardDesc = "inward desc";
        final String outwardDesc = "outward desc";

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual(name)), Collections.EMPTY_LIST);
        final IssueLinkType expectedIssueLinkType = new IssueLinkType(existingLinkType);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(id)), expectedIssueLinkType);
        mockIssueLinkTypeManager.expectVoid("updateIssueLinkType", new Constraint[]{new IsEqual(expectedIssueLinkType), new IsEqual(name), new IsEqual(outwardDesc), new IsEqual(inwardDesc)});

        EditLinkType editLinkType = new EditLinkType((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy());
        editLinkType.setId(id);

        editLinkType.setName(name);
        editLinkType.setInward(inwardDesc);
        editLinkType.setOutward(outwardDesc);

        String result = editLinkType.execute();
        assertEquals(Action.NONE, result);
        response.verify();

        verifyMocks();
    }

    private void verifyMocks()
    {
        mockIssueLinkTypeManager.verify();
    }
}
