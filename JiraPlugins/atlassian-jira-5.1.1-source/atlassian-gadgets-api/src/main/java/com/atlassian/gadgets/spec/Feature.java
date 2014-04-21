package com.atlassian.gadgets.spec;

/**
 * Represents a feature that is declared to be used in a gadget spec. A feature is a defined bit of functionality
 * outside the core OpenSocial/Gadgets API, 
 */
public interface Feature
{
    /**
     * Get the name of this feature
     * @return feature name
     */
    public String getName();

    /**
     * Get the value for this parameter passed in the spec
     * @param paramName name of the feature param we want the value of
     * @return the value of this feature parameter
     */
    public String getParameterValue(String paramName);
}
