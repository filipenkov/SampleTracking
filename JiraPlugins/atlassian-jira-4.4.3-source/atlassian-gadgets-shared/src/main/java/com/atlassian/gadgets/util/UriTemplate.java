package com.atlassian.gadgets.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Used to match and extract parameter values from request URIs against a URI template. A URI template is a regular
 * URI, but with "templates" where where parameter values should be.  As an example 
 * <code>/dashboards/{dashboardId}/gadgets/{gadgetId}</code> is a URI template with 2 parameters,
 * <code>dashboardId</code> and <code>gadgetId</code>.
 */
public class UriTemplate
{
    private String[] uriTemplateComponents;

    public UriTemplate(String uriTemplate)
    {
        this.uriTemplateComponents = uriTemplate.split("/");
    }
    
    /**
     * Returns {@code true} if the URI matches this template, {@code false}.
     * 
     * @param uri URI to check against the template
     * @return {@code true} if the URI matches this template, {@code false} otherwise.
     */
    public boolean matches(String uri)
    {
        String[] uriComponents = uri.split("/");
        if (uriTemplateComponents.length != uriComponents.length)
        {
            return false;
        }
        for (int i = 0; i < uriTemplateComponents.length; i++)
        {
            if (!isTemplateVariable(uriTemplateComponents[i]) && !uriTemplateComponents[i].equals(uriComponents[i]))
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns {@code Map} of parameter names and their values.
     * 
     * @param uri URI which matches this template
     * @return {@code Map} of parameter names and their values
     * @throws IllegalArgumentException if {@code uri} does not match this template
     */
    public Map<String, String> extractParameters(String uri)
    {
        if (!matches(uri))
        {
            throw new IllegalArgumentException(uri + " is not a match for the URI template " + StringUtils.join(uriTemplateComponents, '/'));
        }
        Map<String, String> parameters = new HashMap<String, String>();
        String[] uriComponents = uri.split("/");
        for (int i = 0; i < uriTemplateComponents.length; i++)
        {
            if (isTemplateVariable(uriTemplateComponents[i]))
            {
                parameters.put(getTemplateVariableName(uriTemplateComponents[i]), uriComponents[i]);
            }
        }
        return Collections.unmodifiableMap(parameters);
    }

    private boolean isTemplateVariable(String s)
    {
        return s.startsWith("{") && s.endsWith("}");
    }

    private String getTemplateVariableName(String s)
    {
        return s.substring(1, s.length() - 1);
    }
}
