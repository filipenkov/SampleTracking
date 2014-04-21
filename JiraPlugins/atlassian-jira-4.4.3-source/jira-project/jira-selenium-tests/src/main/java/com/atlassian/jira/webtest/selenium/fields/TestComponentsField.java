package com.atlassian.jira.webtest.selenium.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestComponentsField extends AbstractTestMultiSelectField
{
    private static final String JQUERY_COMPONENTS = "components";
    private static final String NEW_COMPONENT_1 = "New Component 1";
    private static final String NEW_COMPONENT_2 = "New Component 2";
    private static final String COMPONENT_SCOTT = "Component Scott";
    private static final String TEST_COMPONENTS_FIELD_XML = "TestMultiSelectField.xml";
    private static final String JQUERY_COMPONENTS_VAL = "jquery=#components-val";

    public String getXMLFileName()
    {
        return TEST_COMPONENTS_FIELD_XML;
    }

    public void testAllForms() throws Exception
    {
        doTestForm(new CreateIssueForm(JQUERY_COMPONENTS, NEW_COMPONENT_2, JQUERY_COMPONENTS_VAL));
        doTestForm(new EditIssueForm(JQUERY_COMPONENTS, NEW_COMPONENT_1, JQUERY_COMPONENTS_VAL));
        doTestForm(new CreateSubTask(JQUERY_COMPONENTS, NEW_COMPONENT_1, JQUERY_COMPONENTS_VAL));

        doTestForm(new CreateIssueForm(JQUERY_COMPONENTS, NEW_COMPONENT_1, JQUERY_COMPONENTS_VAL)); // create hsp3 (used for moving)
        doTestForm(new MoveIssueForm(JQUERY_COMPONENTS, COMPONENT_SCOTT, JQUERY_COMPONENTS_VAL));

        doTestForm(new ConvertSubTaskToIssue(JQUERY_COMPONENTS, COMPONENT_SCOTT, JQUERY_COMPONENTS_VAL));
        doTestForm(new ConvertToIssue(JQUERY_COMPONENTS, NEW_COMPONENT_1, JQUERY_COMPONENTS_VAL));
        doTestForm(new ResolveIssue(JQUERY_COMPONENTS, NEW_COMPONENT_2, JQUERY_COMPONENTS_VAL));

        doTestForm(new BulkEditIssue(JQUERY_COMPONENTS, COMPONENT_SCOTT, JQUERY_COMPONENTS_VAL));
    }
}
