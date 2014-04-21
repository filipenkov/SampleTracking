/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheckImpl;
import com.mockobjects.constraint.IsInstanceOf;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.util.Collections;

public class TestIntegrityCheckImpl extends LegacyJiraMockTestCase
{
    private IntegrityCheckImpl integrityCheck;
    private static final String DESCRIPTION = "Check Desc";
    private Mock check;

    public TestIntegrityCheckImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        check = new Mock(Check.class);
        check.setStrict(true);
        check.expectVoid("setIntegrityCheck", P.args(new IsInstanceOf(IntegrityCheck.class)));

        integrityCheck = new IntegrityCheckImpl(1, DESCRIPTION, (Check)check.proxy());
    }

    public void testGets()
    {
        assertEquals(new Long(1), integrityCheck.getId());
        assertEquals(DESCRIPTION, integrityCheck.getDescription());
        assertEquals(Collections.singletonList(check), integrityCheck.getChecks());

        check.verify();
    }
}
