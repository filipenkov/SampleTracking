/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.check.FieldLayoutCheck;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.issue.fields.FieldManager;
import org.ofbiz.core.entity.GenericDelegator;
import org.easymock.MockControl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.List;

public class TestFieldLayoutCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
    private OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
    private FieldLayoutCheck flCheck;
    private MockControl ctrlFieldManager;
    private FieldManager mockFieldManager;


    public TestFieldLayoutCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);
        setUpMocks();

        ctrlFieldManager.expectAndReturn(mockFieldManager.isOrderableField("customfield_1"), true);
        ctrlFieldManager.expectAndReturn(mockFieldManager.isOrderableField("customfield_2"), false);
        ctrlFieldManager.expectAndReturn(mockFieldManager.isOrderableField("customfield_3"), false);

        replayMocks();

        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1001), "fieldidentifier", "customfield_1"));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1002), "fieldidentifier", "customfield_2"));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1003), "fieldidentifier", "customfield_3"));


        flCheck = new FieldLayoutCheck(ofBizDelegator, 1);
        flCheck.setFieldManager(mockFieldManager);
    }

    private void setUpMocks()
    {
        ctrlFieldManager = MockControl.createControl(FieldManager.class);
        mockFieldManager = (FieldManager) ctrlFieldManager.getMock();

    }

    private void replayMocks()
    {
        ctrlFieldManager.replay();
    }

    private void verifyMocks()
    {
        ctrlFieldManager.verify();
    }
    private void resetMocks()
    {
        ctrlFieldManager.reset();
    }

    private void noProblems() throws Exception
    {
        resetMocks();
        ctrlFieldManager.expectAndReturn(mockFieldManager.isOrderableField("customfield_1"), true);


        replayMocks();

        List amendments = flCheck.preview();
        assertEquals(0, amendments.size());

        verifyMocks();

    }

    public void testPreview() throws Exception
    {

        List amendments = flCheck.preview();
        assertEquals(2, amendments.size());

        verifyMocks();
    }

    public void testValidateCorrect() throws Exception
    {

        // This should correct the problem
        List amendments = flCheck.correct();
        assertEquals(2, amendments.size());
        verifyMocks();

        noProblems();
    }



    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.ofBizDelegator = null;
        this.genericDelegator = null;
        this.flCheck = null;
        this.ctrlFieldManager = null;
        this.mockFieldManager = null;
    }
}
