package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.page.Page;

/**
 * A dialog bound to a particular page.
 *
 * @since v4.3
 */
public interface PageDialog<D extends  PageDialog<D,P>, P extends Page> extends Dialog<D>
{

    /**
     * Page of this dialog (i.e. page that this dialog was opened from)
     *
     * @return page instance
     */
    P page();
}
