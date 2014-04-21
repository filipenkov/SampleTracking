package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.Localizable;

import java.util.List;

/**
 * Represents a standard select HTML component.
 *
 * @since v4.3
 */
public interface Select extends Localizable
{
    /**
     * All options
     *
     * @return all options of this select
     */
    List<Option> all();

    /**
     * Selected option of this select.
     *
     * @return selected option of this select
     */
    Option selected();

    /**
     * Select given <tt>option</tt>.
     *
     * @param option option to select
     * @return this select instance
     */
    public Select select(Option option);


    /**
     * Select default '-1' option of this select.
     *
     * @return this select instance
     */
    public Select selectDefault();
    
}
