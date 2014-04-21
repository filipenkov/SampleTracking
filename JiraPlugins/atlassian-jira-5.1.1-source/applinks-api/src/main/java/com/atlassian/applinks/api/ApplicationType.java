package com.atlassian.applinks.api;

import com.atlassian.sal.api.message.I18nResolver;

import java.net.URI;

/**
 * Represents the type of an {@link ApplicationLink}.
 *
 * See the {@link com.atlassian.applinks.api.application} package for a list of {@link ApplicationType}s bundled
 * with the Unified Application Links plugin. Additional types can be added via the extension APIs in the
 * <strong>applinks-spi</strong> module.
 *
 * @since 3.0
 */
public interface ApplicationType
{

    /**
     * @return the key of an internationalised display name of the type e.g. "FishEye / Crucible". You can resolve
     * this key using the {@link I18nResolver} component provided by the SAL plugin.
     */
    String getI18nKey();

    /**
     * @since   3.1
     * @return  the icon url for this type (e.g. http://jira.atlassian.com/favicon.ico),
     * or {@code null} if an icon is not available.
     */
    URI getIconUrl();

}
