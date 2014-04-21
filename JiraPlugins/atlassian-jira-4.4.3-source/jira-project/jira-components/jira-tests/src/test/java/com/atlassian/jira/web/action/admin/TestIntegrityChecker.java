/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.appconsistency.integrity.IntegrityCheckManager;
import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.*;

public class TestIntegrityChecker extends LegacyJiraMockTestCase
{
    public TestIntegrityChecker(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testGetIntegrityChecks()
    {
        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);
        integrityCheckManager.expectAndReturn("getIntegrityChecks", Collections.EMPTY_LIST);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy(), new com.atlassian.jira.appconsistency.integrity.IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy()));
        integrityChecker.getIntegrityChecks();

        integrityCheckManager.verify();
    }

    public void testValidation() throws Exception
    {
        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy(), new com.atlassian.jira.appconsistency.integrity.IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy()));

        String result = integrityChecker.execute();
        assertEquals(Action.INPUT, result);
        assertTrue(integrityChecker.getErrorMessages().contains(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("admin.integritychecker.error.no.function")));

        integrityCheckManager.verify();
    }

    public void testPreviewIntegrityChecks() throws Exception
    {
        List result1 = new ArrayList();

        Mock integrityCheck1 = new Mock(IntegrityCheck.class);
        integrityCheck1.setStrict(true);

        Mock check = new Mock(Check.class);
        check.setStrict(true);
        check.expectAndReturn("preview", result1);

        // check.expectAndReturn("getIntegrityCheck", integrityCheck1.proxy());
        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        final Object checkProxy = check.proxy();
        integrityCheckManager.expectAndReturn("getCheck", P.args(new IsEqual(new Long(1))), checkProxy);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy(), new com.atlassian.jira.appconsistency.integrity.IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy()));
        integrityChecker.setCheck("Check");
        ActionContext.setParameters(EasyMap.build(IntegrityChecker.INTEGRITY_CHECK_PREFIX + IntegrityChecker.CHECK_PREFIX + "1", new String[] { "1" }));

        String result = integrityChecker.execute();
        assertEquals("preview", result);

        Map results = integrityChecker.getResults();
        assertEquals(1, results.size());
        assertEquals(result1, results.get(checkProxy));

        integrityCheck1.verify();
        integrityCheck1.verify();
        integrityCheckManager.verify();
        check.verify();
    }

    public void testCorrectIntegrityChecks() throws Exception
    {
        List result1 = new ArrayList();

        Mock integrityCheck1 = new Mock(IntegrityCheck.class);
        integrityCheck1.setStrict(true);

        Mock check = new Mock(Check.class);
        check.setStrict(true);
        check.expectAndReturn("correct", result1);

        // check.expectAndReturn("getIntegrityCheck", integrityCheck1.proxy());
        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        final Object checkProxy = check.proxy();
        integrityCheckManager.expectAndReturn("getCheck", P.args(new IsEqual(new Long(1))), checkProxy);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy(), new com.atlassian.jira.appconsistency.integrity.IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy()));
        integrityChecker.setFix("Fix");
        ActionContext.setParameters(EasyMap.build(IntegrityChecker.INTEGRITY_CHECK_PREFIX + IntegrityChecker.CHECK_PREFIX + "1", new String[] { "1" }));

        String result = integrityChecker.execute();
        assertEquals("correct", result);

        Map results = integrityChecker.getResults();
        assertEquals(1, results.size());
        assertEquals(result1, results.get(checkProxy));

        integrityCheck1.verify();
        integrityCheck1.verify();
        integrityCheckManager.verify();
        check.verify();
    }
}
