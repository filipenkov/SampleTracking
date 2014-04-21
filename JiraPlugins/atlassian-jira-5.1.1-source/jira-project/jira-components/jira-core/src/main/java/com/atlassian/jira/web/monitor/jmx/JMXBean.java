package com.atlassian.jira.web.monitor.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * This class encapsulates a reference to a JMX bean. It can be used to register and unregister JMX beans.
 * <p/>
 * This class is <em>thread-safe</em>.
 *
 * @since v4.3
 */
public class JMXBean<T>
{
    /**
     * Logger for this JMXBean instance.
     */
    private final Logger log = LoggerFactory.getLogger(JMXBean.class);

    /**
     * The bean registered in JMX.
     */
    private final T bean;

    /**
     * The bean name.
     */
    private final ObjectName beanName;

    /**
     * Creates a new JMXBean.
     *
     * @param bean the bean to register
     * @param beanName the bean name to use
     */
    public JMXBean(T bean, String beanName)
    {
        try
        {
            this.bean = bean;
            this.beanName = new ObjectName(beanName);
        }
        catch (MalformedObjectNameException e)
        {
            throw new JMXException("The object name is invalid: " + beanName, e);
        }
    }

    /**
     * Registers this JMXBeanRef's bean the platform MBean server.
     *
     * @return this
     * @throws JMXException if there is a problem registering in JMX
     */
    public JMXBean<T> register() throws JMXException
    {
        try
        {
            ManagementFactory.getPlatformMBeanServer().registerMBean(bean, beanName);
            log.debug("Registered bean '{}' under name: {}", bean, beanName);

            return this;
        }
        catch (Exception e)
        {
            throw new JMXException("Unable to register bean in JMX: " + beanName, e);
        }
    }

    /**
     * Unregsiters this JMXBeanRef's bean in the platform MBean server.
     *
     * @return this
     * @throws JMXException if there is a problem unregistering in JMX
     */
    public JMXBean<T> unregister() throws JMXException
    {
        try
        {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(beanName);
            log.debug("Unregistered bean '{}' under name: {}", bean, beanName);

            return this;
        }
        catch (Exception e)
        {
            throw new JMXException("Unable to unregister bean from JMX: " + beanName, e);
        }
    }

    /**
     * Returns the bean that this JMXBean refers to.
     *
     * @return the bean
     */
    public T getBean()
    {
        return bean;
    }
}
