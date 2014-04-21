package com.atlassian.jira.webtests.ztests.issue.subtasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestMoveSubtask extends FuncTestCase
{
    // JRA-14232: when subtasks use different field configs the move link was invalid.
    public void testMoveSubtask() throws Exception
    {
        administration.restoreData("TestMoveSubtask.xml");

        navigation.issue().viewIssue("RAT-14");

        // Click Link 'Edit' (id='edit_issue').
        tester.clickLink("edit-issue");
        tester.clickLinkWithText("moving");

        tester.assertTextNotPresent("Sub-tasks cannot be moved independently of the parent issue.");
        tester.assertTextPresent("Move Sub-Task: Choose Operation");
        tester.assertTextPresent("Change issue type for this subtask");
    }

    //JRA-13011: sub-task components not reset when parent is moved.
    public void testMoveSubtaskWithComponents() throws Exception
    {
        administration.restoreData("TestMoveSubtaskWithComponent.xml");

        //Goto the first issue.
        navigation.issue().viewIssue("SRC-1");

        tester.clickLink("move-issue");

        //Select the project to move to.
        tester.selectOption("pid", "TARGET");
        tester.submit();

        //Select the component for the parent issue.
        tester.selectOption("components", "TGT2");
        tester.submit();

        //Move the issue.
        tester.assertTextPresent("Move Issue: Confirm");
        assertions.getTextAssertions().assertTextSequence(new TableLocator(tester, "move_confirm_table"), new String[]{"Component/s", "SRC1", "TGT2"});
        tester.submit("Move");

        //Goto the sub issue.
        tester.clickLinkWithText("Source Subtask for Issue 1");

        //Make sure the sub-issue has no components.
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "components-val"), "None");
    }
}
