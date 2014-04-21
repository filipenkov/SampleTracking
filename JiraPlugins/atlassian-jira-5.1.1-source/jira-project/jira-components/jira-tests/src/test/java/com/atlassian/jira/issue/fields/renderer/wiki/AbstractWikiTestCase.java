package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.util.JavaVersionUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import com.mockobjects.dynamic.Mock;

import java.util.List;

/** A base class that will setup a wiki renderer. */
public abstract class AbstractWikiTestCase extends LegacyJiraMockTestCase
{
    protected Mock mockRendererConfiguration;
    protected V2RendererFacade renderer;

    @Override
    protected void setUp() throws Exception
    {
        if (is14OrGreater())
        {
            super.setUp();

            mockRendererConfiguration = new Mock(RendererConfiguration.class);
            mockRendererConfiguration.setStrict(false);
            mockRendererConfiguration.expectAndReturn("getWebAppContextPath", "http://test.example.com:8081");
            mockRendererConfiguration.expectAndReturn("getCharacterEncoding", "UTF-8");
            mockRendererConfiguration.expectAndReturn("isAllowCamelCase", Boolean.FALSE);
            mockRendererConfiguration.expectAndReturn("isNofollowExternalLinks", Boolean.FALSE);

            // make sure we start from a fresh component manager
            ManagerFactory.quickRefresh();

            final List<PluginLoader> pluginLoaders = CollectionBuilder.<PluginLoader>newBuilder(new SinglePluginLoader("system-contentlinkresolvers-plugin.xml"), new SinglePluginLoader(
                "system-renderercomponentfactories-plugin.xml"), new SinglePluginLoader("system-renderers-plugin.xml")).asList();
            //override the default plugin manager with one that can load the renderer components and link resolvers.
            final DefaultPluginManager pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(), pluginLoaders, new JiraModuleDescriptorFactory(new JiraHostContainer()), new DefaultPluginEventManager());
            ManagerFactory.addService(PluginAccessor.class, pluginManager);
            ManagerFactory.addService(PluginController.class, pluginManager);
            ManagerFactory.addService(PluginSystemLifecycle.class, pluginManager);

            // give subclasses oportunity to register any managers they may need injected
            registerManagers();

            // let the factory initialize most components
            ManagerFactory.addService(RendererConfiguration.class, (RendererConfiguration) mockRendererConfiguration.proxy());

            //now that all the components we need for the test are registered, let's init the plugins.
            pluginManager.init();

            new WikiRendererFactory().getWikiRenderer();
            renderer = new V2RendererFacade((RendererConfiguration) mockRendererConfiguration.proxy(),
                (LinkRenderer) ComponentManager.getInstance().getContainer().getComponentInstance(LinkRenderer.class),
                (EmbeddedResourceRenderer) ComponentManager.getInstance().getContainer().getComponentInstance(EmbeddedResourceRenderer.class),
                (Renderer) ComponentManager.getInstance().getContainer().getComponentInstance(Renderer.class));
        }
    }

    protected void registerManagers()
    {
    // default is to do nothing
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (is14OrGreater())
        {
            ManagerFactory.quickRefresh();
        }

        super.tearDown();
    }

    protected V2RendererFacade getRenderer()
    {
        return renderer;
    }

    protected RenderContext getRenderContext()
    {
        final RenderContext context = new RenderContext();
        context.setImagePath("http://localhost:8080/images");
        context.setSiteRoot("http://localhost:8080");
        context.setAttachmentsPath("http://localhost:8080/download/attachments/0");
        return context;
    }

    protected boolean is14OrGreater()
    {
        return JavaVersionUtils.satisfiesMinVersion(1.4F);
    }
}
