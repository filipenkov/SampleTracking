package com.atlassian.applinks.core.docs;

import java.net.URI;

/**
 * Provides URIs for UAL documentation.
 *
 * @since 3.0
 */
public interface DocumentationLinker
{

    /**
     * @param pageKey a key matching a <strong>help page</strong> property defined in ual-help-paths.properties
     * @return a URI targeting the specified help page in the host application's documentation space
     */
    URI getLink(String pageKey);

    /**
     * @param pageKey a key matching a <strong>help page</strong> property defined in ual-help-paths.properties
     * @param sectionKey a key matching a <strong>help page anchor</strong> property defined in
     * ual-help-paths.properties
     * @return a URI targeting the specified help page in the host application's documentation space
     */
    URI getLink(String pageKey, String sectionKey);

}
