package com.atlassian.jira.local;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;

/**
 * This test is to be extended by any test case which creates
 * mock users. It will handle all removals after each test.
 *
 * @deprecated v4.3 - Please stop using these TestCases
 */
@Deprecated
public abstract class  AbstractUsersTestCase extends AbstractWebworkTestCase
{
    public AbstractUsersTestCase(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        UtilsForTests.cleanUsers();
    }

    /**
     * We need to remove any users we have created between each test. Should be fairly fast.
     */
    protected void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(false);
        UtilsForTests.cleanUsers();
        super.tearDown();
    }
}
