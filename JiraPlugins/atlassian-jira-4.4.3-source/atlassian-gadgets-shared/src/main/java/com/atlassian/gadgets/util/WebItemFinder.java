package com.atlassian.gadgets.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebLink;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class WebItemFinder
{
    public static final String DASHBOARD_MENU_SECTION = "gadgets.dashboard.menu"; 
    public static final String DASHBOARD_TOOLS_MENU_SECTION = "gadgets.dashboard.tools.menu";

    private final WebInterfaceManager webInterfaceManager;

    private final ApplicationProperties applicationProperties;

    private final I18nResolver i18n;

    public WebItemFinder(WebInterfaceManager webInterfaceManager, ApplicationProperties applicationProperties, I18nResolver i18n)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.applicationProperties = applicationProperties;
        this.i18n = i18n;
    }

    @SuppressWarnings("unchecked")
    public Iterable<WebItem> findMenuItemsInSection(String section, Map<String, ?> initialContext)
    {
        final Map<String, Object> context = ImmutableMap.<String, Object>builder().putAll(initialContext).put("i18n", i18n).build();
        List<WebItemModuleDescriptor> descriptors = webInterfaceManager.getDisplayableItems(section, context);
        return Iterables.transform(descriptors, new Function<WebItemModuleDescriptor, WebItem>()
        {
            public WebItem apply(WebItemModuleDescriptor descriptor)
            {
                return new WebItem(descriptor, context);
            }
        });
    }
    
    public class WebItem
    {
        private final WebItemModuleDescriptor descriptor;
        private final Map<String, Object> context;

        public WebItem(WebItemModuleDescriptor descriptor, Map<String, Object> context)
        {
            this.descriptor = descriptor;
            this.context = context;
        }
        
        public GadgetWebLink getLink()
        {
            return new GadgetWebLink(descriptor.getLink(), context);
        }
        
        public GadgetWebLabel getLabel()
        {
            return new GadgetWebLabel(descriptor.getWebLabel(), context);
        }
        
        public String getStyleClass()
        {
            return descriptor.getStyleClass();
        }
    }
    
    public class GadgetWebLink
    {
        private final Map<String, Object> context;
        private final WebLink link;

        public GadgetWebLink(WebLink link, Map<String, Object> context)
        {
            this.link = link;
            this.context = ImmutableMap.<String, Object>builder().putAll(context).put("hash", "#").build();
        }

        public String getId()
        {
            return link.getId();
        }

        public String getDisplayableUrl()
        {
            // copying the context to a mutable map is a work-around for PLUG-255
            String url = link.getRenderedUrl(new HashMap<String, Object>(context));
            if (isRelative(url))
            {
                url = applicationProperties.getBaseUrl() + url;
            }
            return url;
        }
        
        private boolean isRelative(String url)
        {
            return !(url.startsWith("http://") || url.startsWith("https://"));
        }
    }
    
    public class GadgetWebLabel
    {
        private final WebLabel webLabel;
        private final Map<String, Object> context;

        public GadgetWebLabel(WebLabel webLabel, Map<String, Object> context)
        {
            this.webLabel = webLabel;
            this.context = context;
        }
        
        public String getDisplayableLabel()
        {
            // copying the context to a mutable map is a work-around for PLUG-255
            return webLabel.getDisplayableLabel(null, new HashMap<String, Object>(context));
        }
    }
}
