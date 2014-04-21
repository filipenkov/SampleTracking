package com.atlassian.streams.spi;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;

import org.dom4j.Element;

public class ActivityProviderModuleDescriptor extends AbstractModuleDescriptor<StreamsActivityProvider>
{
    private StreamsActivityProvider provider;
    
    private String commentHandlerClassName;
    private StreamsCommentHandler commentHandler;
    
    private String filterOptionProviderClassName;
    private StreamsFilterOptionProvider filterOptionProvider;
    
    private String entityAssociationProviderClassName;
    private StreamsEntityAssociationProvider entityAssociationProvider;
    
    private String keyProviderClassName;
    private StreamsKeyProvider keyProvider;
    
    private String validatorClassName;
    private StreamsValidator validator;

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        
        commentHandlerClassName = classAttribute(element.element("comment-handler"));
        filterOptionProviderClassName = classAttribute(element.element("filter-provider"));
        entityAssociationProviderClassName = classAttribute(element.element("entity-association-provider"));
        keyProviderClassName = classAttribute(element.element("key-provider"));
        validatorClassName = classAttribute(element.element("validator"));
    }
    
    private String classAttribute(Element element)
    {
        if (element == null)
        {
            return null;
        }
        return element.attributeValue("class");
    }

    @Override
    public synchronized void enabled()
    {
        super.enabled();
        initModules();
    }
    
    @Override
    public StreamsActivityProvider getModule()
    {
        initModules();  // we need to call this here in case enabled() wasn't called, as is in the case in Bamboo 2.7
        return provider;
    }
    
    public StreamsCommentHandler getCommentHandler()
    {
        initModules();
        return commentHandler;
    }
    
    public StreamsFilterOptionProvider getFilterOptionProvider()
    {
        initModules();
        return filterOptionProvider;
    }
    
    public StreamsEntityAssociationProvider getEntityAssociationProvider()
    {
        initModules();
        return entityAssociationProvider;
    }
    
    public StreamsKeyProvider getKeyProvider()
    {
        initModules();
        return keyProvider;
    }

    public StreamsValidator getValidator()
    {
        initModules();
        return validator;
    }
    
    @Override
    public synchronized void disabled()
    {
        super.disabled();
        disable(provider, commentHandler, filterOptionProvider, keyProvider, validator);
        provider = null;
        commentHandler = null;
        filterOptionProvider = null;
        keyProvider = null;
        validator = null;
    }
    
    private void initModules()
    {
        if (provider != null)
        {
            return;
        }
        
        provider = newInstance(loadModuleClass());
        commentHandler = newInstance(loadSubModuleClass("comment-handler", commentHandlerClassName, StreamsCommentHandler.class));
        filterOptionProvider = newInstance(loadSubModuleClass("filter-provider", filterOptionProviderClassName, StreamsFilterOptionProvider.class));
        entityAssociationProvider = newInstance(loadSubModuleClass("entity-association-provider", entityAssociationProviderClassName, StreamsEntityAssociationProvider.class));
        keyProvider = newInstance(loadSubModuleClass("key-provider", keyProviderClassName, StreamsKeyProvider.class));
        validator = newInstance(loadSubModuleClass("validator", validatorClassName, StreamsValidator.class));
    }
    
    private void disable(Object... os)
    {
        for (Object o : os)
        {
            if (o != null && o instanceof StateAware)
            {
                ((StateAware) o).disabled();
            }
        }
    }
    
    private Class<? extends StreamsActivityProvider> loadModuleClass()
    {
        if (getModuleClass() == null)
        {
            loadClass(plugin, moduleClassName);
        }
        return getModuleClass();
    }
    
    @SuppressWarnings("unchecked")
    private <A> Class<? extends A> loadSubModuleClass(String subModuleName, String subModuleClassName, Class<A> subModuleClassParentType)
    {
        if (subModuleClassName == null)
        {
            return null;
        }
        
        try
        {
            Class<?> subModuleClass = plugin.loadClass(subModuleClassName, getModuleClass());
            if (!subModuleClassParentType.isAssignableFrom(subModuleClass))
            {
                throw new IllegalArgumentException("Sub module '" + subModuleName + "' class '" + subModuleClassName + "' must be of type '" + subModuleClassParentType.getName() + "'");
            }
            return (Class<? extends A>) subModuleClass;
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Sub module '" + subModuleName + "' class '" + subModuleClassName + "' not found ");
        }
    }

    private <T> T newInstance(final Class<T> type)
    {
        if (type == null)
        {
            return null;
        }
        
        T instance = ((AutowireCapablePlugin) plugin).autowire(type);
        if (instance instanceof StateAware)
        {
            ((StateAware) instance).enabled();
        }
        return instance;
    }
}
