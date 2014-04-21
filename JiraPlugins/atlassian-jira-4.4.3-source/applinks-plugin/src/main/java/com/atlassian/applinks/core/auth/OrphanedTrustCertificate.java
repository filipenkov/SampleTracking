package com.atlassian.applinks.core.auth;

import com.atlassian.velocity.htmlsafe.HtmlSafe;

/**
 * POJO for identifying an orphaned trust relationship
 *
 * @since 3.0
 * @see OrphanedTrustDetector
 */
public class OrphanedTrustCertificate
{
    public static enum Type {
        TRUSTED_APPS("applinks.orphaned.trust.trusted.apps"),
        OAUTH_SERVICE_PROVIDER("applinks.orphaned.trust.oauth.service.provider"),
        OAUTH("applinks.orphaned.trust.oauth");
        private final String i18nKey;

        Type(final String i18nKey)
        {
            this.i18nKey = i18nKey;
        }

        public String getI18nKey()
        {
            return i18nKey;
        }
    }

    private final String id;
    private final String description;
    private final Type type;

    public OrphanedTrustCertificate(final String id, final String description, final Type type)
    {
        this.id = id;
        this.description = description;
        this.type = type;
    }

    /**
     * @return The id of the trust certificate
     */
    @HtmlSafe
    public String getId()
    {
        return id;
    }

    /**
     * @return a description specific to this certificate, or {@code null} if no useful description is available
     */
    @HtmlSafe
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the {@link Type} of the certificate
     */
    public Type getType()
    {
        return type;
    }

}
