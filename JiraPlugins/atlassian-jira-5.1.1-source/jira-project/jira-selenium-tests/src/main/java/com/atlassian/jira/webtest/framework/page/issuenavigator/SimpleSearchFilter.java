package com.atlassian.jira.webtest.framework.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.Collapsible;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.page.PageSection;

/**
 * Represents the simple search filter on the issue navigator page. This is collapsible the search form on the left side
 * of the issue navigator page used to enter traditional form-based search queries. 
 *
 * @since v4.3
 */
public interface SimpleSearchFilter extends PageSection<IssueNavigator>, Collapsible<SimpleSearchFilter>
{

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    /**
     * Get section of given type.
     *
     * @param sectionType section type
     * @param <S> section type param
     * @return section instance
     */
    <S extends SimpleSearchFilterSection<S>> S section(Class<S> sectionType);

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    /**
     * Timed condition checking, whether all sections of this filter are expanded.
     *
     * NOTE: this condition will also return <code>false</code>, if {@link #isExpanded()} ()} returns <code>false</code>,
     * i.e. no section is considered expanded, if this filter is not expanded.
     *
     * @return condition that will return <code>true</code>, if all sections of this filter are expanded
     */
    TimedCondition allSectionsExpanded();

    /**
     * <p>
     * Timed condition checking, whether all sections of this filter are collapsed.
     *
     * <p>
     * NOTE: this condition will also return <code>true</code>, if {@link #isCollapsed()} returns <code>true</code>,
     * i.e. all sections are considered collapsed, if this filter is collapsed.
     *
     * @return condition that will return <code>true</code>, if all sections of this filter are collapsed
     */
    TimedCondition allSectionsCollapsed();

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    /**
     * Expand all sections of this search filter.
     *
     * @return this filter instance
     */
    SimpleSearchFilter expandAllSections();

    /**
     * Collapse all sections of this search filter.
     *
     * @return this filter instance
     */
    SimpleSearchFilter collapseAllSections();
}
