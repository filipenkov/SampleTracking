package com.atlassian.applinks.core.webfragment;

import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.rest.model.WebItemEntity;
import com.atlassian.applinks.core.rest.model.WebItemEntityList;
import com.atlassian.applinks.core.rest.model.WebPanelEntity;
import com.atlassian.applinks.core.rest.model.WebPanelEntityList;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebIcon;
import com.atlassian.plugin.web.model.WebLink;
import com.atlassian.plugin.web.model.WebPanel;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultWebFragmentHelper implements WebFragmentHelper
{
    private final WebInterfaceManager webInterfaceManager;

    public DefaultWebFragmentHelper(final WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    public WebItemEntityList getWebItemsForLocation(final String location, final WebFragmentContext context)
    {
        final List<WebItemEntity> webItems = new ArrayList<WebItemEntity>();
        final Map<String, Object> contextMap = context.getContextMap();
        final HttpServletRequest request = CurrentContext.getHttpServletRequest();

        for (final WebItemModuleDescriptor descriptor : webInterfaceManager.getDisplayableItems(location, contextMap))
        {
            final WebLink link = descriptor.getLink();
            final WebIcon icon = descriptor.getIcon();

            final WebItemEntity.Builder itemBuilder = new WebItemEntity.Builder();
            itemBuilder.id(link.getId());
            itemBuilder.url(link.getDisplayableUrl(request, new HashMap<String, Object>(contextMap)));
            if (link.hasAccessKey()) {
                itemBuilder.accessKey(link.getAccessKey(new HashMap<String, Object>(contextMap)));
            }
            if (icon != null) {
                itemBuilder.iconUrl(icon.getUrl().getDisplayableUrl(request, new HashMap<String, Object>(contextMap)));
                itemBuilder.iconHeight(icon.getHeight());
                itemBuilder.iconWidth(icon.getWidth());
            }
            if (descriptor.getWebLabel() != null) {
                itemBuilder.label(descriptor.getWebLabel().getDisplayableLabel(request,
                        new HashMap<String, Object>(contextMap)));
            }
            if (descriptor.getTooltip() != null) {
                itemBuilder.tooltip(descriptor.getTooltip().getDisplayableLabel(request,
                                        new HashMap<String, Object>(contextMap)));
            }
            itemBuilder.styleClass(descriptor.getStyleClass());

            webItems.add(itemBuilder.build());
        }

        return new WebItemEntityList(webItems);
    }

    public WebPanelEntityList getWebPanelsForLocation(final String location, final WebFragmentContext context)
    {
        final List<WebPanelEntity> webPanels = new ArrayList<WebPanelEntity>();
        final Map<String, Object> contextMap = context.getContextMap();

        for (final WebPanel webPanel : webInterfaceManager.getDisplayableWebPanels(location, contextMap))
        {
            webPanels.add(new WebPanelEntity(webPanel.getHtml(contextMap)));
        }

        return new WebPanelEntityList(webPanels);
    }

}
