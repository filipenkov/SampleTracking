package com.atlassian.voorhees;

import java.io.Serializable;

/**
 * Adapter for internaltionalised text services. This allows the JSON-RPC server components to be entirely
 * independent of the Atlassian web stack.
 */
public interface I18nAdapter
{
    String getText(String key, Serializable... arguments);

    String getText(String key);
}
