package com.atlassian.jira.webtest.framework.component.fc;

/**
 * Common interface of all components that make up the Frother Control.
 *
 * @since 4.3
 */
public interface FrotherControlComponent<F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>, I extends FcInput<I,F,S>>
{

    /**
     * Frother Control in play.
     *
     * @return Frother control instance
     */
    F fc();
}
