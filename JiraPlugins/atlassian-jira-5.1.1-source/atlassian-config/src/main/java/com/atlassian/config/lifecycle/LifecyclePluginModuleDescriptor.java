package com.atlassian.config.lifecycle;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.StateAware;
import com.atlassian.spring.container.ContainerManager;
import org.dom4j.Element;
import org.dom4j.Attribute;

import javax.servlet.ServletContextListener;

/**
 * Descriptor for lifecycle plugin modules.
 *
 * <p>A lifecycle plugin module must either implement {@link LifecycleItem}, or {@link ServletContextListener}. The
 * latter will automatically be wrapped in a {@link ServletContextListenerWrapper} by the descriptor, so
 * {@link #getModule()} will <i>always</i> return a LifecycleItem instance.
 *
 * <p>Each module has a sequence number. On startup, the modules will be invoked in ascending order of sequence (lowest
 * to highest), and then on shutdown, the order will be reversed.
 */
public class LifecyclePluginModuleDescriptor extends AbstractModuleDescriptor implements Comparable
{
    private Object module;
    private int sequence;

    /**
     * Default no-arg constructor
     */
    public LifecyclePluginModuleDescriptor()
    {
    }

    /**
     * Helpful constructor for tests
     */
    LifecyclePluginModuleDescriptor(Object module, int sequence)
    {
        this.module = module;
        this.sequence = sequence;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        sequence = determineSequenceNumber(element);
    }

    private void ensureCompatibleModuleType() throws PluginParseException
    {
        Class moduleClass = getModuleClass();
        if (!LifecycleItem.class.isAssignableFrom(moduleClass) && !ServletContextListener.class.isAssignableFrom(moduleClass))
            throw new PluginParseException("Lifecycle classes must extend LifecycleItem or ServletContextListener. Module class: " + moduleClass.getName());
    }

    private int determineSequenceNumber(Element element) throws PluginParseException
    {
        Attribute att = element.attribute("sequence");
        if (att != null)
        {
            String value = att.getValue();
            try
            {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                throw new PluginParseException("Could not determine sequence from: " + value);
            }
        }

        throw new PluginParseException("Missing required attribute: sequence");
    }

    public Object getModule()
    {
        return module;
    }

    private Object makeModule()
    {
        Object module = ContainerManager.getInstance().getContainerContext().createComponent(getModuleClass());

        if (module instanceof ServletContextListener)
            module = new ServletContextListenerWrapper((ServletContextListener) module);

        return module;
    }

    public void enabled()
    {
        super.enabled();
        ensureCompatibleModuleType();
        module = makeModule();
        if  (module instanceof StateAware)
            ((StateAware) module).enabled();
    }

    public void disabled()
    {
        if  (module instanceof StateAware)
            ((StateAware) module).disabled();
        module = null;
        super.disabled();
    }

    public int getSequence()
    {
        return sequence;
    }

    public int compareTo(Object o)
    {
        int otherSequence = ((LifecyclePluginModuleDescriptor)o).sequence;

        return (sequence == otherSequence) ? 0 : (sequence < otherSequence) ? -1 : 1;
    }
}
