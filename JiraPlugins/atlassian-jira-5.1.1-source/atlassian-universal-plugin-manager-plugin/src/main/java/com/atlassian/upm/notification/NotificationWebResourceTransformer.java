package com.atlassian.upm.notification;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.rest.UpmUriBuilder;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * {@link WebResourceTransformer} to transform notification javascript such that
 * it contains all of the notification data.
 */
public class NotificationWebResourceTransformer implements WebResourceTransformer
{
    private final ApplicationProperties applicationProperties;
    private final I18nResolver i18nResolver;
    private final UserManager userManager;
    private final UpmUriBuilder uriBuilder;

    public NotificationWebResourceTransformer(
        @Qualifier("applicationProperties") ApplicationProperties applicationProperties,
        I18nResolver i18nResolver,
        UserManager userManager,
        UpmUriBuilder uriBuilder)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.userManager = checkNotNull(userManager, "userManager");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
    }

    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new AbstractStringTransformedDownloadableResource(nextResource)
        {
            @Override
            protected String transform(String originalContent)
            {
                //add the information about the current environment
                String baseContent = originalContent
                    .replaceFirst("productId", format("productId = '%s'", applicationProperties.getDisplayName().toLowerCase()))
                    .replaceFirst("productVersion", format("productVersion = '%s'", applicationProperties.getVersion()))
                    .replaceFirst("pluginNotificationsTitle", format("pluginNotificationsTitle = '%s'", i18nResolver.getText(
                        "upm.notification.plugin.notifications")))
                    .replaceFirst("noNotificationsText", format("noNotificationsText = '%s'", i18nResolver.getText(
                        "upm.notification.no.notifications")));

                if (userManager.getRemoteUsername() == null)
                {
                    return baseContent.replaceFirst("notificationsUrl", format("notificationsUrl = '%s'",
                                    uriBuilder.buildNotificationCollectionUri()));
                }
                else
                {
                    return baseContent.replaceFirst("notificationsUrl", format("notificationsUrl = '%s'",
                                    uriBuilder.buildNotificationCollectionUri(userManager.getRemoteUsername())));
                }
            }
        };
    }
}
