package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.component.Component;


/**
 * Page object that represents a static section of a page.
 *
 * @param <P> type of the parent page
 * @since v4.3
 */
public interface PageSection<P extends Page> extends Component<P>
{
    /**
     * Reference to a page, within which this section resides. 
     *
     * @return page of this section
     */
    P page();
}
