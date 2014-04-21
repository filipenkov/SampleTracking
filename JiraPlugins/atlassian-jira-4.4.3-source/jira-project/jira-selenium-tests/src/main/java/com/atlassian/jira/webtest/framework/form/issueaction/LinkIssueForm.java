package com.atlassian.jira.webtest.framework.form.issueaction;

import com.atlassian.jira.webtest.framework.component.CommentInput;
import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.form.Form;

/**
 * Represents contents of the link issue dialogs/pages.
 *
 * @since v4.3
 */
public interface LinkIssueForm extends Form
{
    /**
     * Select of the link type
     *
     * @return HTML select to select the link type.
     */
    Select linkTypeSelect();

    /**
     * Issue picker of this Link Issue form.
     *
     * @return issue picker of this form
     */
    IssuePicker issuePicker();

    /**
     * Comment input of this form
     *
     * @return comment input
     */
    CommentInput comment();
}
