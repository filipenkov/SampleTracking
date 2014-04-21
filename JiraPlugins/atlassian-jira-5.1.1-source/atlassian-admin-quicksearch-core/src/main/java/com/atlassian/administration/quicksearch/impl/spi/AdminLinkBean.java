package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link com.atlassian.administration.quicksearch.spi.AdminLink} as an immutable
 * bean.
 *
 * @since 1.0
 */
public class AdminLinkBean extends AbstractAdminWebItemBean implements AdminLink
{

    private final String linkUrl;

    public AdminLinkBean(String id, String label, Map<String, String> params, String linkUrl)
    {
        super(id, label, params);
        this.linkUrl = checkNotNull(linkUrl, "linkUrl");
    }

    @Override
    @Nonnull
    public String getLinkUrl()
    {
        return linkUrl;
    }
}
