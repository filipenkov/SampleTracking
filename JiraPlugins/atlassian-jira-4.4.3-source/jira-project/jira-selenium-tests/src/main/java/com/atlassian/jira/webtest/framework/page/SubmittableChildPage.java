package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.Submittable;

/**
 * A child page that is able to submit itself and go back to its parent.
 *
 * @since v4.3
 */
public interface SubmittableChildPage<P extends Page> extends ChildPage<P>, Submittable<P>
{
}
