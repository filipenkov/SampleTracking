package com.atlassian.jira.webtest.framework.component.fc;

import com.atlassian.jira.webtest.framework.component.AutoCompleteInput;
import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.component.ValueHolder;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Input component of the {@link FrotherControl}.
 *
 * @param <I> type of this input
 * @param <F> type of the FC parent
 * @param <S> type of the FC suggestions
 * @since v4.3
 */
public interface FcInput<I extends  FcInput<I,F,S>, F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>>
        extends AutoCompleteInput<F>, ValueHolder, Localizable
{
    /*  -------------------------------------------- COMPONENTS ----------------------------------------------------- */

    /**
     * Suggestions associated with this FC input.
     *
     * @return FC suggestions
     */
    S suggestions();

    /**
     * <p>
     * Retrieves lozenge with given <tt>label</tt>.
     *
     * @return lozenge in this Frother Control input with given label, as a timed query waiting for this input to be
     * ready and for the lozenge to be available. The query will return <code>null</code> if those conditions are not
     * met by given timeout
     * @see com.atlassian.jira.webtest.framework.component.fc.FcLozenge
     */
    TimedQuery<FcLozenge> lozenge(String label);

    /* -------------------------------------------------- QUERIES --------------------------------------------------- */

    /**
     * Check if this input has lozenge with given <tt>text</tt>.
     *
     * @param text text to find
     * @return timed condition querying whether any loznge of this input matches given text
     */
    TimedCondition hasLozenge(String text);

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    /**
     * {@inheritDoc}
     * 
     */
    FcInput<I,F,S> type(KeySequence sequence);

    /**
     * Clear value of this input. This <b>will not</b> remove the already existing lozenges.
     *
     * @return this input instance
     */
    FcInput<I,F,S> clear();


    /* --------------------------------------------- TRANSITIONS ---------------------------------------------------- */

    /**
     * Click the drop-icon of this input. Depending on the current test state, this should either open, or close
     * the associated suggestions, which may be queried by its appropriate methods: {@link FcSuggestions#isOpen()} and
     * {@link FcSuggestions#isClosed()}.
     *
     * @return suggestions instance associated with this input
     */
    S clickDropIcon();

    /**
     * Press arrow down key in this input. This should open the associated suggestions,
     * which may be queried by its {@link FcSuggestions#isOpen()} method.
     *
     * @return suggestions instance associated with this input
     */
    S arrowDown();

}
