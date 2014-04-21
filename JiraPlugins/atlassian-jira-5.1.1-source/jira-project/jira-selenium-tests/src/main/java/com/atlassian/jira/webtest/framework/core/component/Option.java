package com.atlassian.jira.webtest.framework.core.component;

/**
 * An option in select and multi-select components. Depending on the context, not all
 * of the option properties may be set, but at least one of: id/value/label should not be <code>null</code>.
 *
 * @see Select
 * @see MultiSelect
 *
 * @since v4.3
 */
public interface Option
{

    /**
     * Option ID.
     *
     * @return id
     */
    String id();

    /**
     * Option value, which is actually its unique key in the collection of options.
     *
     * @return value
     */
    String value();

    /**
     * User-visible label of the option.
     *
     * @return label
     */
    String label();

}
