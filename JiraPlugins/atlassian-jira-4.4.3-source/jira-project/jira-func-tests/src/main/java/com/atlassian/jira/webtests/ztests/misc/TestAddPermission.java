package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the AddPermission action.
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS })
public class TestAddPermission extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testValidationOnPermission()
    {
        administration.permissionSchemes().defaultScheme();
        
        tester.clickLinkWithText("Grant permission");
        tester.checkCheckbox("type", "group");

        // if we don't choose any permissions, we should get a validation error
        tester.submit(" Add ");
        text.assertTextPresent("Errors");
        text.assertTextPresent("You must select a permission to add.");
    }

}
