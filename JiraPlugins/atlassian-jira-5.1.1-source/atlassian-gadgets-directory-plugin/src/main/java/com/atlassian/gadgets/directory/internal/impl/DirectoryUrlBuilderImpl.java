package com.atlassian.gadgets.directory.internal.impl;

import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.util.AbstractUrlBuilder;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

public class DirectoryUrlBuilderImpl extends AbstractUrlBuilder implements DirectoryUrlBuilder
{

    public DirectoryUrlBuilderImpl(ApplicationProperties applicationProperties, WebResourceManager webResourceManager)
    {
        super(applicationProperties, webResourceManager, "com.atlassian.gadgets.dashboard:dashboard-servlet");
    }

    public String buildDirectoryResourceUrl()
    {
        return applicationProperties.getBaseUrl() + "/rest/config/1.0/directory";

    }

    public String buildDirectoryGadgetResourceUrl(ExternalGadgetSpecId id)
    {
        return buildDirectoryResourceUrl() + "/gadget/" + id;

    }

    public String buildSubscribedGadgetFeedsUrl()
    {
        return buildDirectoryResourceUrl() + "/subscribed-gadget-feeds";
    }
    
    public String buildSubscribedGadgetFeedUrl(String feedId)
    {
        return buildSubscribedGadgetFeedsUrl() + "/" + Uri.encodeUriComponent(feedId);
    }
}
