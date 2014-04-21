/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;

import java.util.List;

public class TestIntegrityCheckManagerImpl extends LegacyJiraMockTestCase
{
    private IntegrityCheckManagerImpl integrityCheckManager;

    public TestIntegrityCheckManagerImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        integrityCheckManager = new IntegrityCheckManagerImpl(null, ComponentAccessor.getI18nHelperFactory(), ComponentAccessor.getApplicationProperties());
    }

    public void testGetIntegrityChecks()
    {
        List integrityChecks = integrityCheckManager.getIntegrityChecks();
        assertEquals(6, integrityChecks.size());

        IntegrityCheck integrityCheck = (IntegrityCheck) integrityChecks.get(0);
        List checks = integrityCheck.getChecks();
        assertEquals(3, checks.size());

        Check check = (Check) checks.get(0);
        assertSame(integrityCheck, check.getIntegrityCheck());
        check = (Check) checks.get(1);
        assertSame(integrityCheck, check.getIntegrityCheck());
        integrityCheck = (IntegrityCheck) integrityChecks.get(1);
        checks = integrityCheck.getChecks();
        assertEquals(1, checks.size());
        check = (Check) checks.get(0);
        assertSame(integrityCheck, check.getIntegrityCheck());
    }

    public void testGetCheck()
    {
        Check check = integrityCheckManager.getCheck(new Long(1));
        assertNotNull(check);
        assertNotNull(check.getIntegrityCheck());
    }
    public void tearDown()
    {
        integrityCheckManager = null;
    }

}
