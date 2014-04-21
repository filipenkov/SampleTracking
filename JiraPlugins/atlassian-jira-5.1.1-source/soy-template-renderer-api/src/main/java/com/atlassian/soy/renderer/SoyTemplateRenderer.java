package com.atlassian.soy.renderer;

import java.util.Map;

/**
 * Provides methods for rending Closure (Soy) templates on the server.
 *
 * @since v1.0
 */
public interface SoyTemplateRenderer
{
    /**
     * Render a single template with the supplied data.
     *
     * @param completeModuleKey the module key which defines the soy template
     * @param templateName name of the soy template to render
     * @param data a string keyed map of data to render the template with
     *
     * @return the rendered template string
     * @throws SoyException when an error occurs in rendering the soy template
     */
    String render(String completeModuleKey, String templateName, Map<String, Object> data) throws SoyException;

    /**
     * Render a single template with the supplied data to an appendable
     *
     * @param appendable the appendable to render the template to
     * @param completeModuleKey the module key which defines the soy template
     * @param templateName name of the soy template to render
     * @param data a string keyed map of data to render the template with
     *
     * @throws SoyException when an error occurs in rendering the soy template
     */
    void render(Appendable appendable, String completeModuleKey, String templateName, Map<String, Object> data) throws SoyException;

    /**
     * Render a single template with the supplied data to an appendable
     *
     * @param appendable the appendable to render the template to
     * @param completeModuleKey the module key which defines the soy template
     * @param templateName name of the soy template to render
     * @param data a string keyed map of data to render the template with
     * @param injectedData a string keyed map of injected data to render the template with
     *
     * @throws SoyException when an error occurs in rendering the soy template
     */
    void render(Appendable appendable, String completeModuleKey, String templateName, Map<String, Object> data, Map<String, Object> injectedData) throws SoyException;

}
