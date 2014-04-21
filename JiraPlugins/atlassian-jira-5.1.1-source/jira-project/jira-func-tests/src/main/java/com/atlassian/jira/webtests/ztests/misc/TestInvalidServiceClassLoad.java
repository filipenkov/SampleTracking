package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * JRA-17582 - test that bad class name is masked by a NoOp class instead
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.QUARTZ })
public class TestInvalidServiceClassLoad extends FuncTestCase
{
    public void testSchedulerCanHandleBadClassInput()
    {
        administration.restoreDataSlowOldWay("TestInvalidServiceClassLoad.xml");
        tester.gotoPage("secure/admin/jira/SchedulerAdmin.jspa");

        //
        // I cant be 100% sure of the order here so I assert each one
        //
        // make sure that the dud classes get synthesized out as NoOps
        //
        text.assertTextSequence(new WebPageLocator(tester), new String[] {
                "GreenHopperIndexes", "com.atlassian.scheduler.NoOpQuartzJob",
        });
        text.assertTextSequence(new WebPageLocator(tester), new String[] {
                "DoesNotImplementJob", "com.atlassian.scheduler.NoOpQuartzJob",
        });

        // and that good jobs still work
        text.assertTextSequence(new WebPageLocator(tester), new String[] {
                "ServicesJob ", "com.atlassian.jira.service.ServiceRunner",
        });
    }
}
