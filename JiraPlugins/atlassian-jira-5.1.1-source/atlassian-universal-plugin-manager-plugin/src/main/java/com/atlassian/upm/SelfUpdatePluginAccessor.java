package com.atlassian.upm;

import java.io.File;
import java.net.URI;

/**
 * Interface used by {@link SelfUpdateController} to abstract away references to a
 * dynamically loaded component.
 */
public interface SelfUpdatePluginAccessor
{
    URI prepareUpdate(File jarToInstall, String expectedPluginKey, URI pluginUri, URI selfUpdatePluginUri);
}
