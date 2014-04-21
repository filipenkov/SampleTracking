package com.atlassian.jira.webtest.framework.component.fc;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.locator.Locator;

/**
 * Represents the AJS.MultiSelect JavaScript control. Occasionally they may be referred to as the 'pickers'.
 * But most of the time people just call it the Frother Control&trade;.
 *
 * @since v4.3
 */
public interface FrotherControl<F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>, I extends FcInput<I,F,S>>
        extends Localizable
{

    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    /**
     * Locator of the FC input area.
     *
     * @return input area locator
     */
    Locator inputAreaLocator();

    /**
     * Locator of the (open) suggestions component of this FC.
     *
     * @return suggestions locator
     */
    Locator suggestionsLocator();

    /**
     * Locator of this picker's underlying select model.
     *
     * @return select model locator
     */
    Locator selectModelLocator();


    /* -------------------------------------------------- QUERIES --------------------------------------------------- */

    

    /* ------------------------------------------------- COMPONENTS ------------------------------------------------- */

    /**
     * Input control of this FC
     *
     * @return input control instance
     * @see com.atlassian.jira.webtest.framework.component.fc.FcInput
     */
    I input();


    /**
     * Suggestions of this Frother Control.
     *
     * @return suggestions of this FC
     */
    S suggestions();


    /* --------------------------------------------------- ACTIONS -------------------------------------------------- */

    

}
