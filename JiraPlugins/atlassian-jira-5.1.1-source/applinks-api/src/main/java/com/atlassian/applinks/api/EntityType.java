package com.atlassian.applinks.api;

import com.atlassian.sal.api.message.I18nResolver;

import java.net.URI;

/**
 * Represents the type of an {@link EntityLink}.
 *
 * See the {@link com.atlassian.applinks.api.application} package for a list of {@link EntityType}s bundled
 * with the Unified Application Links plugin. Additional types can be added via the extension APIs in the
 * <strong>applinks-spi</strong> module.
 *
 * @since 3.0
 */
public interface EntityType
{

    /**
     * @return the {@link ApplicationType} that houses this type of entity.
     */
    Class<? extends ApplicationType> getApplicationType();

    /**
     * @return the key of an internationalized display name of this type e.g. "JIRA Project". You can resolve
     * this key using the {@link I18nResolver} component provided by the SAL plugin.
     * @see #getPluralizedI18nKey()
     * @see #getShortenedI18nKey()
     */
    String getI18nKey();

    /**
     * @return the pluralized version of {@link #getI18nKey} for this type name e.g. "JIRA Projects".
     * @see #getI18nKey()
     */
    String getPluralizedI18nKey();

    /**
     * @return the shortened version of {@link #getI18nKey} for this type name e.g. "Project".
     * @see #getI18nKey()
     */
    String getShortenedI18nKey();

    /**
     * @since   3.1
     * @return  the icon url for this type, or {@code null} if an icon is not
     * available.
     */
    URI getIconUrl();

    /**
     * <p>
     * Given an {@link com.atlassian.applinks.api.ApplicationLink} and the key
     * of an entity on that peer, this method returns that entity's "display
     * URL". This would typically be the address where a user's browser is
     * sent to when it follows the entity link.
     * </p>
     * <p>
     * Note that the caller does not guarantee that the specified entity key
     * actually exists.
     * </p>
     * <p>
     * The implementation of this method is stringly recommended not to
     * contact the peer either for validation of the supplied entity key, or
     * help creating the display URL, as this method can be called multiple
     * times during a page render.
     * </p>
     *
     * @since   3.1
     * @param link  the link MUST be of the same type as {@link #getApplicationType()}
     * or the result is unspecified.
     * @param entityKey the key of an enitity on the remote system.
     * @return  the (remote) url for the specified entity key, or {@code null}
     * when such URL is not available.
     */
    URI getDisplayUrl(ApplicationLink link, String entityKey);
}
