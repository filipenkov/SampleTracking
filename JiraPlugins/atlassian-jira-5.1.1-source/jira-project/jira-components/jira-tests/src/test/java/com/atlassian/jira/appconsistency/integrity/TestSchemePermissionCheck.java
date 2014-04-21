/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.appconsistency.integrity.check.SchemePermissionCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.List;

public class TestSchemePermissionCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
    private OfBizDelegator ofBizDelegator  = new DefaultOfBizDelegator(genericDelegator);
    private SchemePermissionCheck spCheck;

    public TestSchemePermissionCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        UtilsForTests.getTestEntity("SchemePermissions",
                EasyMap.build("id", new Long(1000), "scheme", new Long(1000), "permission", new Long(1000), "type", "type1", "parameter", "parameter1"));
        UtilsForTests.getTestEntity("SchemePermissions",
                EasyMap.build("id", new Long(1001), "scheme", new Long(1001), "permission", new Long(1000), "type", "type1", "parameter", "parameter1"));
        UtilsForTests.getTestEntity("SchemePermissions",
                EasyMap.build("id", new Long(1002), "scheme", new Long(1001), "permission", new Long(1000), "type", "type1", "parameter", "parameter1"));
        UtilsForTests.getTestEntity("SchemePermissions",
                EasyMap.build("id", new Long(1003), "scheme", new Long(1002), "permission", new Long(1000), "type", "type3", "parameter", "parameter1"));
        UtilsForTests.getTestEntity("SchemePermissions",
                EasyMap.build("id", new Long(1004), "scheme", new Long(1002), "permission", new Long(1000), "type", "type3", "parameter", "parameter1"));


        spCheck = new SchemePermissionCheck(ofBizDelegator, 1);

    }

    public void testPreview() throws IntegrityException
    {
        List amendments = spCheck.preview();
        assertEquals(2, amendments.size());
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem
        List amendments = spCheck.correct();
        assertEquals(2, amendments.size());

        // There should be 3 entry
        List allIssues = genericDelegator.findAll("SchemePermissions");
        assertEquals(3, allIssues.size());

        // This should return no amendments as they have just been corrected.
        amendments = spCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.ofBizDelegator = null;
        this.spCheck = null;
    }
}
