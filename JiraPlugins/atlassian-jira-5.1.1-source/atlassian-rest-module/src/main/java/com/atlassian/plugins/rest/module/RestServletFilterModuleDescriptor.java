package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.plugins.rest.doclet.generators.AtlassianWadlGeneratorConfig;
import com.google.common.base.Preconditions;
import org.dom4j.Element;

import javax.servlet.Filter;

import static com.atlassian.plugins.rest.doclet.generators.AtlassianWadlGeneratorConfig.APPLICATION_XML;
import static com.atlassian.plugins.rest.doclet.generators.AtlassianWadlGeneratorConfig.GRAMMARS_XML;
import static com.atlassian.plugins.rest.doclet.generators.AtlassianWadlGeneratorConfig.RESOURCE_XML;

/**
 * The module descriptor for the REST servlet. Registered dynamically by the {@link RestModuleDescriptor}.
 * Uses the specific {@link RestDelegatingServletFilter}.
 */
public class RestServletFilterModuleDescriptor extends ServletFilterModuleDescriptor
{
    private final OsgiPlugin osgiPlugin;
    private final RestDelegatingServletFilter restDelegatingServletFilter;
    private final RestApiContext restApiContext;

    RestServletFilterModuleDescriptor(OsgiPlugin plugin, ModuleFactory moduleFactory, ServletModuleManager servletModuleManager, RestApiContext restApiContext)
    {
        super(Preconditions.checkNotNull(moduleFactory), Preconditions.checkNotNull(servletModuleManager));
        this.restApiContext = Preconditions.checkNotNull(restApiContext);
        this.osgiPlugin = Preconditions.checkNotNull(plugin);
        this.restDelegatingServletFilter = new RestDelegatingServletFilter(plugin, restApiContext);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        //see if we have a resourcedoc.xml and if so, use the extended WADL generator
        if (resourcesAvailable(plugin, APPLICATION_XML, GRAMMARS_XML, RESOURCE_XML))
        {
            getInitParams().put("com.sun.jersey.config.property.WadlGeneratorConfig", AtlassianWadlGeneratorConfig.class.getName());
        }
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
    }

    @Override
    public String getName()
    {
        return "Rest Servlet Filter";
    }

    protected void autowireObject(Object obj)
    {
        osgiPlugin.autowire(obj);
    }

    @Override
    public Filter getModule()
    {
        return restDelegatingServletFilter;
    }

    public String getBasePath()
    {
        return restApiContext.getApiPath();
    }

    public ApiVersion getVersion()
    {
        return restApiContext.getVersion();
    }

    private static boolean resourcesAvailable(Plugin plugin, String...resources)
    {
        for (final String resource : resources)
        {
            if (plugin.getResource(resource) == null)
            {
                return false;
            }
        }
        return true;
    }
}
