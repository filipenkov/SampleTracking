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
     * @throws ComponentNotFoundException if the specified component cannot be found in the container
     */
    public static Object getComponent(String key) throws ComponentNotFoundException
    {
        return getInstance().getContainerContext().getComponent(key);
    }

    /**
     * Utility method to get a bean from the current container
     * @throws ComponentNotFoundException if the specified component cannot be found in the container
     * @throws ComponentTypeMismatchException if the specified component is not assignable to the type specified
     */
    public static <T> T getComponent(String key, Class<T> aClass) throws ComponentNotFoundException, ComponentTypeMismatchException
    {
        final Object o = getInstance().getContainerContext().getComponent(key);
        if (aClass.isAssignableFrom(o.getClass()))
        {
            return aClass.cast(o);
        }

        throw new ComponentTypeMismatchException("Component '" + key + "' of type '" + o.getClass() + "' cannot be assigned to requested type '" + aClass + "'");
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
