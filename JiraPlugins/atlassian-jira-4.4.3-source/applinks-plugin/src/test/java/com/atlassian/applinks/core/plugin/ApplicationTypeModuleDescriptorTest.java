package com.atlassian.applinks.core.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationException;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

public class ApplicationTypeModuleDescriptorTest
{
    private Plugin plugin;
    private ApplicationTypeModuleDescriptor descriptor;

    @Before
    public void before()
    {
        plugin = new StaticPlugin();
        descriptor = new ApplicationTypeModuleDescriptor(Mockito.mock(ModuleFactory.class));
    }

    @After
    public void after()
    {
        plugin = null;
        descriptor = null;
    }

    @Test
    public void invalidConfiguration()
    {
        final String invalidXml =
                "    <applinks-application-type name=\"fecru\" key=\"fisheyeCrucible\"\n" +
                "                               class=\"com.atlassian.applinks.application.fecru.FishEyeCrucibleApplicationTypeImpl\"\n" +
                "                               interface=\"com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType\">\n" +
                "        <faulty-manifest-producer class=\"com.atlassian.applinks.application.fecru.FishEyeCrucibleManifestProducer\"/>\n" +
                "    </applinks-application-type>";

        try
        {
            final Element root = DocumentHelper.parseText(invalidXml).getRootElement();
            descriptor.init(plugin, root);
            fail("Invalid module descriptor should have raised a ValidationException.");
        }
        catch (DocumentException de)
        {
            fail("Failed to parse valid xml: " + de.getMessage());
        }
        catch (ValidationException ve)
        {
            // very good
        }
    }

    @Test
    public void validConfiguration()
    {
        final String xml =
                "    <applinks-application-type name=\"fecru\" key=\"fisheyeCrucible\"\n" +
                "                               class=\"com.atlassian.applinks.application.fecru.FishEyeCrucibleApplicationTypeImpl\"\n" +
                "                               interface=\"com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType\">\n" +
                "        <manifest-producer class=\"com.atlassian.applinks.application.fecru.FishEyeCrucibleManifestProducer\"/>\n" +
                "    </applinks-application-type>";

        try
        {
            final Element root = DocumentHelper.parseText(xml).getRootElement();
            descriptor.init(plugin, root);
            descriptor.enabled();
            assertNotNull(descriptor.getManifestProducerClass());
        }
        catch (DocumentException de)
        {
            fail("Failed to parse valid xml: " + de.getMessage());
        }
    }
}
