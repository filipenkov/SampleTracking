package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.servlet.filter.FilterLocation;
import com.atlassian.plugins.rest.module.servlet.RestServletModuleManager;
import com.google.common.base.Preconditions;
import org.dom4j.Element;
import org.osgi.framework.ServiceRegistration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>The REST module descriptor.</p>
 * <p>Example configuration in your {@link PluginManager#PLUGIN_DESCRIPTOR_FILENAME plugin descriptor}:</p>
 * &lt;rest key="module-key"&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;url-pattern&gt;/myapi/*&lt;/url-pattern&gt;<br/>
 * &lt;/rest&gt;<br/>
 * <p>Your REST apis will then be available at <code>/context/rest/myapi</code></p>
 */
public class RestModuleDescriptor extends AbstractModuleDescriptor<Object>
{
    private final RestServletModuleManager servletModuleManager;
    /**
     * <p>This is the context path of REST APIs.</p>
     * <p>Typically if the application lives at {@code http://localhost:9090/app} and the REST context path is {@code /rest}, then APIs will be available at {@code http://localhost:9090/app/rest}</p>
     */
    private final String restContext;

    private RestApiContext restApiContext;

    private ServiceRegistration serviceRegistration;
    private RestServletFilterModuleDescriptor restServletFilterModuleDescriptor;
    private final ModuleFactory moduleFactory;
    private OsgiPlugin osgiPlugin;
    private Element element;

    public RestModuleDescriptor(ModuleFactory moduleFactory, RestServletModuleManager servletModuleManager, String restContext)
    {
        this.moduleFactory = moduleFactory;
        this.servletModuleManager = Preconditions.checkNotNull(servletModuleManager);
        this.restContext = Preconditions.checkNotNull(restContext);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        this.restApiContext = new RestApiContext(restContext, parsePath(element), parseVersion(element), parsePackages(element));

        osgiPlugin = (OsgiPlugin) plugin;
        this.element = element;
    }

    private String parsePath(Element element)
    {
        return element.attributeValue("path");
    }

    private Set<String> parsePackages(Element rootElement)
    {
        Set<String> packages = new HashSet<String>();
        for (Element pkgElement : (Collection<Element>) rootElement.elements("package"))
        {
            packages.add(pkgElement.getTextTrim());
        }
        return packages;
    }

    private ApiVersion parseVersion(Element element)
    {
        try
        {
            return new ApiVersion(element.attributeValue("version"));
        }
        catch (InvalidVersionException e)
        {
            // rethrowing the exception with more information
            throw new InvalidVersionException(plugin, this, e);
        }
    }

    private Element updateElementForFilterConfiguration(final Element element)
    {
        final Element copy = element.createCopy();

        // adding the default location
        copy.addAttribute("location", FilterLocation.BEFORE_DISPATCH.name());

        // adding the url-pattern
        copy.addElement("url-pattern").addText(restApiContext.getApiPath() + RestApiContext.SLASH + restApiContext.getVersion() + RestApiContext.ANY_PATH_PATTERN);

        copy.addAttribute("key", copy.attributeValue("key") + "-filter");
        return copy;
    }

    /**
     * @return <code>null</code>, the REST module descriptor doesn't instansiate any module.
     */
    public Object getModule()
    {
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        if (this.getClass() != o.getClass())
        {
            return false;
        }

        final RestModuleDescriptor that = (RestModuleDescriptor) o;
        return that.getCompleteKey().equals(getCompleteKey());
    }

    @Override
    public int hashCode()
    {
        return getCompleteKey().hashCode();
    }

    @Override
    public void disabled()
    {
        if (restServletFilterModuleDescriptor != null)
        {
            restServletFilterModuleDescriptor.disabled();
            restServletFilterModuleDescriptor = null;
        }

        if (serviceRegistration != null)
        {
            try
            {
                serviceRegistration.unregister();
            }
            catch (IllegalStateException ex)
            {
                // this has
            }
            serviceRegistration = null;
        }

        super.disabled();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        restServletFilterModuleDescriptor = new RestServletFilterModuleDescriptor(osgiPlugin, moduleFactory, servletModuleManager, restApiContext);
        restServletFilterModuleDescriptor.init(plugin, updateElementForFilterConfiguration(element));
        restServletFilterModuleDescriptor.enabled();

        // dynamically register the servlet filter that serves the REST API requests
        serviceRegistration = osgiPlugin.getBundle().getBundleContext().registerService(
                new String[]{
                        restServletFilterModuleDescriptor.getClass().getName(),
                        ModuleDescriptor.class.getName(),
                }, restServletFilterModuleDescriptor, null);
    }
}
