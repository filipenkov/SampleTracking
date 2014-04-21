package com.atlassian.jira.webtest.framework.component;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.component.Component;
import com.atlassian.jira.webtest.framework.core.component.ValueHolder;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * <p>
 * Represents an auto-complete input, in which each key stroke causes rendering of an {@link AjsDropdown}
 * instance containing suggestions on how to complete the input sequence.
 *
 * <p>
 * This input control consists of an input text area and a small drop icon enabling to trigger the drop-down
 * with mouse. 
 *
 * @since v4.3
 */
public interface AutoCompleteInput<P extends Localizable> extends Component<P>, ValueHolder
{

    /*  -------------------------------------------- COMPONENTS ----------------------------------------------------- */

    /**
     * AJS.DropDown instance associated with this auto-complete input.
     *
     * @return drop-down instance associated with this input
     */
    AjsDropdown<P> dropDown();

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    /**
     * Type in a query.The default type mode is {@link com.atlassian.webtest.ui.keys.TypeMode#TYPE}.
     *
     * @param sequence key sequence to enter
     * @return this input instance
     * @see com.atlassian.webtest.ui.keys.TypeMode
     */
    AutoCompleteInput<P> type(KeySequence sequence);

    /**
     * Clear value of this input
     *
     * @return this input instance
     */
    AutoCompleteInput<P> clear();

    /* --------------------------------------------- TRANSITIONS ---------------------------------------------------- */

    /**
     * Click the drop-icon of this input. Depending on the current test state, this should either open, or close
     * the associated drop-down, which may be queried by its appropriate methods: {@link AjsDropdown#isOpen()} and
     * {@link AjsDropdown#isClosed()}.
     * 
     * @return drop-down instance associated with this input
     */
    AjsDropdown<P> clickDropIcon();

    /**
     * Press arrow down key in this input. This should open the associated drop-down,
     * which may be queried by its {@link AjsDropdown#isOpen()} method.
     *
     * @return drop-down instance associated with this input
     */
    AjsDropdown<P> arrowDown();

    /**
     * Take the focus out of this input. This should also close the associated drop-down.
     *
     * @return parent component of this autocomplete
     */
    P focusAway();
}
