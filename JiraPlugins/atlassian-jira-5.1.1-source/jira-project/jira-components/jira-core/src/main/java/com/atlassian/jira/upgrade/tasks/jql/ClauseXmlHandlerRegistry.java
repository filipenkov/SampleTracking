package com.atlassian.jira.upgrade.tasks.jql;

/**
 * Looks up the ClauseXmlHandler by the "old" SearchParameter class name string and the element name that will be
 * processed so that we can convert the old saved search parameter elements to JQL clauses.
 *
 * @since v4.0
 */
public interface ClauseXmlHandlerRegistry
{
    /**
     *
     * @param searchParameterClassName the string that represents the fully qualified class name of the "old" SearchParameter
     * that used to represent this search element in JIRA.
     * @param elementName the XML element that will be handled.
     *
     * @return a ClauseXmlHandler that knows how to build a {@link com.atlassian.query.clause.TerminalClause} provided
     * the old-style SearchParameter XML.
     */
    ClauseXmlHandler getClauseXmlHandler(String searchParameterClassName, String elementName);
}
