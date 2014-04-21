/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.portlet;

import com.atlassian.jira.plugin.ConfigurableModuleDescriptor;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * The portlet plugin allows end users to write plugins for JIRA.
 */
//@RequiresRestart
public class PortletModuleDescriptor extends ConfigurableModuleDescriptor<Portlet>
{

    String label = "Unknown";
    String labelKey;
    private String thumbnail;
    private int permission = -1;

    /**
     * @since Jira 3.7
     */
    private boolean lazyLoad = false;

    public PortletModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(Portlet.class);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final Element labelEl = element.element("label");
        if (labelEl != null)
        {
            if (labelEl.attribute("key") != null)
            {
                labelKey = labelEl.attributeValue("key");
            }
            else
            {
                label = labelEl.getTextTrim();
            }
        }

        final Element thumbnailEl = element.element("thumbnail");
        final List<ResourceDescriptor> descriptors = new ArrayList<ResourceDescriptor>(resources.getResourceDescriptors());
        descriptors.addAll(createResourceDescriptorsForThumbnail(thumbnailEl));
        resources = new Resources(descriptors);
        thumbnail = (thumbnailEl == null ? "" : thumbnailEl.getTextTrim());

        if (element.attribute("lazy") != null)
        {
            lazyLoad = "true".equalsIgnoreCase(element.attribute("lazy").getText());
        }

        if (element.element("permission") != null)
        {
            permission = Permissions.getType(element.element("permission").getTextTrim());
        }
    }

    private List<ResourceDescriptor> createResourceDescriptorsForThumbnail(final Element thumbnailEl)
    {
        final List<ResourceDescriptor> descriptors = new ArrayList<ResourceDescriptor>();
        if (thumbnailEl == null)
        {
            return descriptors;
        }
        String source = null;
        if (thumbnailEl.attribute("source") != null)
        {
            source = thumbnailEl.attributeValue("source");
        }
        descriptors.add(createThumbnailResourceElement(thumbnailEl.getTextTrim(), source));
        descriptors.add(createCornerThumbnailResourceElement(thumbnailEl.getTextTrim(), source));
        return descriptors;
    }

    private ResourceDescriptor createThumbnailResourceElement(final String name, final String source)
    {
        return createDownloadResourceDescriptor(name, source);
    }

    private ResourceDescriptor createCornerThumbnailResourceElement(String name, final String source)
    {
        // find the last "/" and place the corner before the image filename
        final int i = name.lastIndexOf("/");
        name = name.substring(0, i + 1) + "corner_" + name.substring(i + 1);

        return createDownloadResourceDescriptor(name, source);
    }

    private ResourceDescriptor createDownloadResourceDescriptor(final String resource, final String source)
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Element resourceElement = factory.createElement("resource");
        resourceElement.addAttribute("type", "download");
        resourceElement.addAttribute("name", resource);
        if ("webContext".equals(source))
        {
            final Element paramElement = factory.createElement("param");
            paramElement.addAttribute("name", "source");
            paramElement.addAttribute("value", source);
            resourceElement.add(paramElement);

            // since the thumbnail images are in the webContext we need to make sure that the servlet forwarding
            // doesn't forward relative to the resource, without this the resource path shows up twice in the 
            // forwarded request uri
            resourceElement.addAttribute("location", "/" + resource);
        }
        else
        {
            resourceElement.addAttribute("location", resource);
        }
        return new ResourceDescriptor(resourceElement);
    }

    public String getLabel()
    {
        return label;
    }

    public String getLabelKey()
    {
        return labelKey;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public int getPermission()
    {
        return permission;
    }

    /**
     * Returns a boolean indicating whether the Portlet should be lazily loaded.
     *
     * @return true if the portlet should be lazily loaded
     * @since Jira 3.7
     */
    public boolean isLazyLoad()
    {
        return lazyLoad;
    }

}
