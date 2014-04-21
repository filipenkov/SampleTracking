package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;

/**
 * Quick Create subtask dialog
 *
 * @since v4.4
 */
public class QuickCreateSubtask extends AbstractSubmittableDialog<QuickCreateSubtask>
{
    private static final String CREATE_LINK_LOCATOR = JQUERY.create(".issueaction-create-subtask");
    private static final String SUBMIT_BUTTON_LOCATOR = JQUERY.create("#create-subtask-dialog #create-issue-submit");
    private static final String CANCEL_LINK_LOCATOR = JQUERY.create("#create-subtask-dialog a.cancel");
    private static final String CONTENT_LOCATOR = JQUERY.create("#create-subtask-dialog .content");
    private static final String PROJECT_SELECT_LOCATOR = "pid";
    private static final String ISSUE_TYPE_SELECT_LOCATOR = "issuetype";
    private static final String JQUERY_QF_UNCONFIGURABLE = "jquery=.qf-unconfigurable";

    public QuickCreateSubtask(SeleniumContext ctx)
    {
        super(QuickCreateSubtask.class, ActionType.AJAX, ctx);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected String visibleDialogContentsLocator()
    {
        return CONTENT_LOCATOR;
    }

    public String cancelTriggerLocator()
    {
        return CANCEL_LINK_LOCATOR;
    }

    public String submitTriggerLocator()
    {
        return SUBMIT_BUTTON_LOCATOR;
    }

    public String projectSelectLocator()
    {
        return PROJECT_SELECT_LOCATOR;
    }

    public String issueTypeSelectLocator()
    {
        return ISSUE_TYPE_SELECT_LOCATOR;
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    public boolean isOpenable()
    {
        return client.isElementPresent(CREATE_LINK_LOCATOR);
    }

    public List<String> getAllProjectNames()
    {
        return Arrays.asList(client.getSelectOptions(projectSelectLocator()));
    }

    public List<String> getAllIssueTypes()
    {
        return Arrays.asList(client.getSelectOptions(issueTypeSelectLocator()));
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    public QuickCreateSubtask open()
    {
        client.click(CREATE_LINK_LOCATOR);
        return this;
    }

    private String getFieldLocator(String id)
    {
        return JQUERY.create(CONTENT_LOCATOR + " #" + id);
    }


    public QuickCreateSubtask setFieldValue(String id, String value)
    {
        client.type(getFieldLocator(id), value);
        return this;
    }

    public QuickCreateSubtask showFullView()
    {
        client.click("qf-field-picker-trigger");
        assertThat.elementPresentByTimeout(JQUERY_QF_UNCONFIGURABLE);
        client.click(JQUERY_QF_UNCONFIGURABLE);
        assertThat.elementPresentByTimeout("jquery=a.qf-configurable", 5000);
        return this;
    }
}
