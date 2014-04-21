package com.atlassian.jira.web.action.util;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Needed to cleanup the webwork ActionContext to avoid it from throwing thread corrupted errors.
 *
 * @since v4.4
 */
public class ImportResult extends JiraWebActionSupport
{
}
