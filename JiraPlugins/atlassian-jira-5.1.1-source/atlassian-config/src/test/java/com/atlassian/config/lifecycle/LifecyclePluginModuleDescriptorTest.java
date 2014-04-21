package com.atlassian.config.lifecycle;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.InputStream;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LifecyclePluginModuleDescriptorTest extends MockObjectTestCase
{
    private Mock mockContainer;

    public void testSortOrder()
    {
        List descriptors = new ArrayList();
        descriptors.add(new LifecyclePluginModuleDescriptor("blah", 80));
        descriptors.add(new LifecyclePluginModuleDescriptor("blah", 10));
        descriptors.add(new LifecyclePluginModuleDescriptor("blah", 10));
        descriptors.add(new LifecyclePluginModuleDescriptor("blah", 40));
        descriptors.add(new LifecyclePluginModuleDescriptor("blah", -90));

        Collections.sort(descriptors);

        assertEquals(-90, ((LifecyclePluginModuleDescriptor)descriptors.get(0)).getSequence());
        assertEquals(10, ((LifecyclePluginModuleDescriptor)descriptors.get(1)).getSequence());
        assertEquals(10, ((LifecyclePluginModuleDescriptor)descriptors.get(2)).getSequence());
        assertEquals(40, ((LifecyclePluginModuleDescriptor)descriptors.get(3)).getSequence());
        assertEquals(80, ((LifecyclePluginModuleDescriptor)descriptors.get(4)).getSequence());
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        mockContainer = new Mock(ContainerContext.class);
        ContainerManager.getInstance().setContainerContext((ContainerContext) mockContainer.proxy());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        ContainerManager.resetInstance();
        mockContainer = null;
    }

    public void testParseElement() throws DocumentException, PluginParseException
    {
        mockContainer.expects(once()).method("createComponent").with(eq(DummyLifecycleItem.class)).will(returnValue(null));
        mockContainer.expects(once()).method("createComponent").with(eq(DummyServletContextListener.class)).will(returnValue(null));

        testLoadedPluginModule("lifecycle1", DummyLifecycleItem.class, 50);
        testLoadedPluginModule("lifecycle2", DummyServletContextListener.class, 30);
        try
        {
            testLoadedPluginModule("lifecycle3", String.class, 90);
            fail("Expected parse exception - bad class");
        }
        catch (PluginParseException e)
        {
            // expected
        }
    }

    public void testWrappedElement() throws DocumentException, PluginParseException
    {
        mockContainer.expects(once()).method("createComponent").with(eq(DummyServletContextListener.class)).will(returnValue(new DummyServletContextListener()));

        LifecyclePluginModuleDescriptor descriptor = loadDescriptor("lifecycle2");
        assertEquals(ServletContextListenerWrapper.class, descriptor.getModule().getClass());
        assertEquals(DummyServletContextListener.class, ((ServletContextListenerWrapper)descriptor.getModule()).getWrappedListener().getClass());
    }

    private void testLoadedPluginModule(String elementId, Class expectedClass, int expectedSequence) throws DocumentException, PluginParseException
    {
        LifecyclePluginModuleDescriptor descriptor = loadDescriptor(elementId);
        assertEquals(expectedClass, descriptor.getModuleClass());
        assertEquals(expectedSequence, descriptor.getSequence());
    }

    private LifecyclePluginModuleDescriptor loadDescriptor(String elementId)
            throws DocumentException, PluginParseException
    {
        LifecyclePluginModuleDescriptor descriptor = new LifecyclePluginModuleDescriptor();
        Document doc = getDocument();
        descriptor.init(new StaticPlugin(), (Element) doc.selectSingleNode("/atlassian-plugin/lifecycle[@key='" + elementId + "']"));
        descriptor.enabled();
        return descriptor;
    }

    private Document getDocument() throws DocumentException
    {
        SAXReader reader = new SAXReader();
        final InputStream is = ClassLoaderUtils.getResourceAsStream("lifecycle-plugins.xml", LifecyclePluginModuleDescriptorTest.class);
        return reader.read(is);
    }
}
