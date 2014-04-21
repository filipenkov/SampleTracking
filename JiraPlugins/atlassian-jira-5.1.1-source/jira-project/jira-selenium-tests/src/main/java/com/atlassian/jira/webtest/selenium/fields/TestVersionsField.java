package com.atlassian.jira.webtest.selenium.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestVersionsField extends AbstractTestMultiSelectField
{
    private static final String JQUERY_FIX_VERSION = "fixVersions";
    private static final String VERSION_BLUE = "blue";
    private static final String VERSION_GREEN = "green";
    private static final String VERSION_1 = "1.0";
    private static final String XML_LOCATION = "TestFixVersionField.xml";
    private static final String JQUERY_FIXFOR_VAL = "jquery=#fixfor-val";

    public String getXMLFileName()
    {
        return XML_LOCATION;
    }
    
    public void testAllForms() throws Exception
    {
        doTestForm(new CreateIssueForm(JQUERY_FIX_VERSION, VERSION_GREEN, JQUERY_FIXFOR_VAL));
        doTestForm(new EditIssueForm(JQUERY_FIX_VERSION, VERSION_BLUE, JQUERY_FIXFOR_VAL));
        doTestForm(new CreateSubTask(JQUERY_FIX_VERSION, VERSION_BLUE, JQUERY_FIXFOR_VAL));

        doTestForm(new CreateIssueForm(JQUERY_FIX_VERSION, VERSION_BLUE, JQUERY_FIXFOR_VAL)); // create hsp3 (used for moving)
        doTestForm(new MoveIssueForm(JQUERY_FIX_VERSION, VERSION_1, JQUERY_FIXFOR_VAL));

        doTestForm(new ConvertSubTaskToIssue(JQUERY_FIX_VERSION, VERSION_1, JQUERY_FIXFOR_VAL));
        doTestForm(new ConvertToIssue(JQUERY_FIX_VERSION, VERSION_BLUE, JQUERY_FIXFOR_VAL));
        doTestForm(new ResolveIssue(JQUERY_FIX_VERSION, VERSION_GREEN, JQUERY_FIXFOR_VAL));

        doTestForm(new BulkEditIssue(JQUERY_FIX_VERSION, VERSION_1, JQUERY_FIXFOR_VAL));
    }
}