package com.atlassian.applinks.core.plugin;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.manifest.ManifestProducer;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

public class ApplicationTypeModuleDescriptor extends AbstractAppLinksTypeModuleDescriptor<ApplicationType>
{
    private Class<ManifestProducer> manifestProducerClass = null;
    private String manifestProducerClassName;


    public ApplicationTypeModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    protected void provideValidationRules(final ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("manifest-producer/@class")
                                .withError("No " + ManifestProducer.class.getSimpleName() + "  class specified.")
                );
    }

    @Override
    public void enabled()
    {
        super.enabled();
        try
        {
            this.manifestProducerClass = plugin.loadClass(manifestProducerClassName, getModuleClass());
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IllegalStateException("Unable to load this application type's " +
                    ManifestProducer.class.getSimpleName() + " class.", cnfe);
        }
    }

    @Override
    public void disabled()
    {
        this.manifestProducerClass = null;
        super.disabled();
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        manifestProducerClassName = element.element("manifest-producer").attributeValue("class");
    }

    /**
     * Used by the unit tests.
     */
    protected Class<ManifestProducer> getManifestProducerClass()
    {
        return manifestProducerClass;
    }

    public ManifestProducer getManifestProducer()
    {
        return ((ContainerManagedPlugin) plugin).getContainerAccessor().createBean(manifestProducerClass);
    }
}
