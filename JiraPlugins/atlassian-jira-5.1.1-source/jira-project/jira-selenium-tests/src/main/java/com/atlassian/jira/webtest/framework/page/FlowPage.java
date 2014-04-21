package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.Cancelable;
import com.atlassian.jira.webtest.framework.core.Submittable;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;

/**
 * <p>
 * A page that is a step in a larger process. E.g. converting to sub-tasks, importing data, bulk editing etc.
 * all consist of several steps, each of them represented by a single page.
 *
 * <p>
 * {@link #submit()} operation of a flow page should have the same result as {@link #next()}.
 *
 * @param <P> parent page of the flow
 * @param <N> next page in the flow
 * @since v4.3
 */
public interface FlowPage<P extends ParentPage, N extends Page> extends Page, Cancelable<P>, Submittable<N>
{

    /**
     * Submit this flow page and go to the next one in the flow. Synonym for {@link #submit()}.
     *
     * @return next page in the flow
     */
    N next();

    /**
     * Step number in the flow.
     *
     * @return step number of this step
     */
    int stepNumber();
}
