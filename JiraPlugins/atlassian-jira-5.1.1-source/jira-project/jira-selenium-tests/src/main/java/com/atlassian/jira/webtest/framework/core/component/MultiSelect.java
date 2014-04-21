package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.Localizable;

import java.util.List;

/**
 * Represents a standard multi-select HTML component.
 *
 * @since v4.3
 */
public interface MultiSelect extends Localizable
{
    /**
     * All options
     *
     * @return all options of this multi-select
     */
    List<Option> all();

    /**
     * Selected options.
     *
     * @return selected options of this multi-select
     */
    List<Option> selected();

    /**
     * Add given <tt>options</tt> to the current selection.
     *
     * @param options options to add
     * @return this multi-select instance
     */
    public MultiSelect select(Option... options);


    /**
     * Remove given <tt>options</tt> from the current selection
     *
     * @param options options to remove
     * @return this multi-select instance
     */
    public MultiSelect unselect(Option... options);

    /**
     * Add all options to the current selection.
     *
     * @return this multi-select instance
     */
    public MultiSelect selectAll();

    /**
     * Remove all options from the current selection
     *
     * @return this multi-select instance
     */
    public MultiSelect unselectAll();

}
