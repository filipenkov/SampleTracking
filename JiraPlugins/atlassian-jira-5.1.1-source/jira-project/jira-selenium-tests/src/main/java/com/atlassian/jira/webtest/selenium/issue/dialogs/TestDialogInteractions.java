package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestDialogInteractions extends AbstractAuiDialogTest
{

    private static final String TEST_XML = "issueactionsdialog.xml";

    private static final Map<String, String> DIALOGS = new HashMap<String, String>();

    static {
        DIALOGS.put("link-issue", "link-issue-dialog");
        DIALOGS.put("delete-issue", "delete-issue-dialog");
        DIALOGS.put("clone-issue", "clone-issue-dialog");
    }

     @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData(TEST_XML);
    }
    
    public void testCustomEvents()
    {
        getNavigator().gotoIssue("HSP-1");
        
        client.runScript("jQuery(document).bind(\"dialogContentReady\", function (e, dialog) {\n"
                + "    jQuery(\"body\").addClass(dialog.options.id);\n"
                + "});");

        for (Map.Entry<String, String> entry : DIALOGS.entrySet())
        {

            client.click(entry.getKey());
            assertThat.elementPresentByTimeout("jquery=body." + entry.getValue(), 20000);

        }
    }

}
