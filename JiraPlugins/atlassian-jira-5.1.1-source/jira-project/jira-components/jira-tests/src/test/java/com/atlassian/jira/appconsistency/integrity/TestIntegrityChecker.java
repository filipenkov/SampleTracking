/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.core.util.collection.EasyList;
import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestIntegrityChecker extends LegacyJiraMockTestCase
{
    public TestIntegrityChecker(String s)
    {
        super(s);
    }

    public void testMultiplePreviewAmendments() throws IntegrityException
    {
        ArrayList testList1 = new ArrayList();
        ArrayList testList2 = new ArrayList();

        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        Mock integrityCheck1 = new Mock(IntegrityCheck.class);
        integrityCheck1.setStrict(true);

        Mock check1 = new Mock(Check.class);
        check1.setStrict(true);
        check1.expectAndReturn("preview", testList1);

        // check1.expectAndReturn("getIntegrityCheck", integrityCheck1.proxy());
        Mock check2 = new Mock(Check.class);
        check2.setStrict(true);
        check2.expectAndReturn("preview", testList2);

        // check2.expectAndReturn("getIntegrityCheck", integrityCheck1.proxy());
        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy());
        final Check checkProxy1 = (Check) check1.proxy();
        Map amendments = integrityChecker.preview(EasyList.build(checkProxy1, (Check) check2.proxy()));

        assertEquals(2, amendments.size());
        assertEquals(Collections.EMPTY_LIST, amendments.get(checkProxy1));

        check1.verify();
        check2.verify();
        integrityCheck1.verify();
        integrityCheckManager.verify();
    }

    public void testMultipleCorrectAmendments() throws IntegrityException
    {
        ArrayList testList1 = new ArrayList();
        ArrayList testList2 = new ArrayList();

        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        Mock integrityCheck1 = new Mock(IntegrityCheck.class);
        integrityCheck1.setStrict(true);

        Mock integrityCheck2 = new Mock(IntegrityCheck.class);
        integrityCheck2.setStrict(true);

        Mock check1 = new Mock(Check.class);
        check1.setStrict(true);
        check1.expectAndReturn("correct", testList1);

        // check1.expectAndReturn("getIntegrityCheck", integrityCheck1.proxy());
        Mock check2 = new Mock(Check.class);
        check2.setStrict(true);
        check2.expectAndReturn("correct", testList2);

        // check2.expectAndReturn("getIntegrityCheck", integrityCheck2.proxy());
        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy());
        final Check checkProxy1 = (Check) check1.proxy();
        final Check checkProxy2 = (Check) check2.proxy();
        Map amendments = integrityChecker.correct(EasyList.build(checkProxy1, checkProxy2));

        assertEquals(2, amendments.size());
        assertSame(testList1, amendments.get(checkProxy1));
        assertSame(testList2, amendments.get(checkProxy2));

        check1.verify();
        check2.verify();
        integrityCheck1.verify();
        integrityCheck2.verify();
        integrityCheckManager.verify();
    }

    public void testPreviewAmendments() throws IntegrityException
    {
        ArrayList testList = new ArrayList();

        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        Mock check = new Mock(Check.class);
        check.setStrict(true);
        check.expectAndReturn("preview", testList);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy());
        List amendments = integrityChecker.preview((Check) check.proxy());

        assertSame(testList, amendments);

        check.verify();
        integrityCheckManager.verify();
    }

    public void testCorrectAmendments() throws IntegrityException
    {
        ArrayList testList = new ArrayList();

        Mock integrityCheckManager = new Mock(IntegrityCheckManager.class);
        integrityCheckManager.setStrict(true);

        Mock check = new Mock(Check.class);
        check.setStrict(true);
        check.expectAndReturn("correct", testList);

        IntegrityChecker integrityChecker = new IntegrityChecker((IntegrityCheckManager) integrityCheckManager.proxy());
        List amendments = integrityChecker.correct((Check) check.proxy());

        assertSame(testList, amendments);

        check.verify();
        integrityCheckManager.verify();
    }
}
