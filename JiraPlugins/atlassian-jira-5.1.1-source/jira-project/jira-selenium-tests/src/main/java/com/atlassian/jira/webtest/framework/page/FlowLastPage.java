package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;

/**
 * A page that is a last step in a larger process.
 *
 * @param <P> parent page of the flow
 * @param <F> finish page of the flow
 * @since v4.3
 */
public interface FlowLastPage<P extends ParentPage, F extends Page> extends FlowPage<P,F>
{

    /**
     * Submit this flow page and finish the whole flow
     *
     * @return target page of the flow
     */
    F finish();
}
