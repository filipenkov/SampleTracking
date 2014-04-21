package com.atlassian.templaterenderer.velocity;

/**
 * Utility class for working with DynamicParserPool.
 */
public class DynamicParserPoolUtil
{
    /**
     * The name of the property used as a kill switch.
     */
    private static final String KILL_SWITCH = "atr.velocity.fixed.size.pool";

    /**
     * Returns true if the {@code RuntimeInstance} created under the given ClassLoader should use the
     * {@link DynamicParserPool}. The {@code DynamicParserPool} is only used if:
     * <ol>
     *     <li>the value of the system property <code>{@value #KILL_SWITCH}</code> is <b>not</b> equal to {@code true},
     *     and</li>
     *     <li>the {@code DynamicParserPool} class can be loaded using {@code classLoader}.</li>
     * </ol>
     *
     * @param classLoader a ClassLoader
     * @return true if the RuntimeInstance created under the given ClassLoader should use the DynamicParserPool
     *
     * @see #canLoadDynamicParserPool
     */
    public static boolean canUseDynamicParserPool(ClassLoader classLoader)
    {
        return !Boolean.getBoolean(KILL_SWITCH) && canLoadDynamicParserPool(classLoader);
    }

    /**
     * Returns true if the given class loader can load {@link DynamicParserPool}. In practice this is only the case if
     * the host application exports <code>commons-pool</code> into OSGi.
     *
     * @param classLoader a ClassLoader
     * @return true if the given class loader can load DynamicParserPool
     */
    public static boolean canLoadDynamicParserPool(ClassLoader classLoader)
    {
        try
        {
            Class.forName(DynamicParserPool.class.getName(), true, classLoader);
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
        catch (LinkageError e)
        {
            return false;
        }
    }
}
