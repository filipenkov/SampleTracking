/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 11:37:45
 * To change this template use File | Settings | File Templates.
 */
package com.atlassian.spring.container;

public class ContainerManager
{
    private static ContainerManager instance = new ContainerManager();

    private ContainerContext containerContext = null;

    private static boolean containerSetup = false;

    public static ContainerManager getInstance()
    {
        return instance;
    }

    private ContainerManager()
    {
    }

    /**
     * Utility method to get a bean from the current container
     */
    public static Object getComponent(String key)
    {
        return getInstance().getContainerContext().getComponent(key);
    }

    /**
     * Utility method to autowire a bean
     * @param component
     */
    public static void autowireComponent(Object component)
    {
        getInstance().getContainerContext().autowireComponent(component);
    }

    /**
     * @return Returns the containerContext.
     */
    public ContainerContext getContainerContext()
    {
        return containerContext;
    }

    /**
     * @param containerContext The containerContext to set.
     */
    public void setContainerContext(ContainerContext containerContext)
    {
        this.containerContext = containerContext;
    }

    public static void resetInstance()
    {
        instance = new ContainerManager();
        containerSetup = false;
    }

    public static boolean isContainerSetup()
    {
        return getInstance().containerContext != null && getInstance().containerContext.isSetup();
    }
}
