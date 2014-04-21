package com.atlassian.jira.webtest.framework.model;

/**
 * Collapsible sections in the 'simple search' mode of the Issue Navigator.
 *
 * @since v4.3
 */
public enum SimpleSearchSection
{

    ISSUE_ATRIBUTES("navigator-filter-subheading-issueattributes-group"),
    DATES_AND_TIMES("navigator-filter-subheading-datesandtimes-group"),
    WORK_RATIO("navigator-filter-subheading-workratio-group"),
    CUSTOM_FIELDS("navigator-filter-subheading-customfields-group");

    private final String headerId;

    SimpleSearchSection(String headerId)
    {
        this.headerId = headerId;
    }

    public String headerId()
    {
        return headerId;
    }
}
