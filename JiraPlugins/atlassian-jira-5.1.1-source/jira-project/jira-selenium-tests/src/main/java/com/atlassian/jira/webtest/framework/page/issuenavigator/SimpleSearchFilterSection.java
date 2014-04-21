package com.atlassian.jira.webtest.framework.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.Collapsible;
import com.atlassian.jira.webtest.framework.core.component.Component;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;

/**
 * Collapsible section in the simple search filter of the Issue Navigator.
 *
 * @see SimpleSearchFilter
 * @since v4.3
 */
public interface SimpleSearchFilterSection<S extends SimpleSearchFilterSection<S>> extends Component<SimpleSearchFilter>,
        Collapsible<S>
{

    /**
     * Type of the section.
     *
     * @return this section type
     */
    SimpleSearchSection type();
}
