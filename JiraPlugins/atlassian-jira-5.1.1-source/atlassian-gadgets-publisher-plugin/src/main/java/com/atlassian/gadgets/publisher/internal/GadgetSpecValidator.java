package com.atlassian.gadgets.publisher.internal;

import java.io.InputStream;

/**
 * Interface for validating a gadget spec.
 */
public interface GadgetSpecValidator
{
    /**
     * Returns true if the specified gadget spec is valid.
     *
     * @param spec source of the gadget spec XML to validate
     * @return true if the specified gadget spec is valid
     */
    boolean isValid(InputStream spec);
}
