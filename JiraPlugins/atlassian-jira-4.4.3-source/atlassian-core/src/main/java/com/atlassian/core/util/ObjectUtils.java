/*
 * Created by IntelliJ IDEA.
 * User: Scott Farquhar
 * Date: 12/02/2002
 * Time: 00:52:58
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import org.apache.commons.collections.Predicate;

import java.lang.reflect.Method;

/**
 * Some common methods used against {@link Object} objects
 */
public class ObjectUtils
{
    private static final Predicate NOT_EMPTY_PREDICATE = new Predicate()
    {
        public boolean evaluate(Object o)
        {
            return isNotEmpty(o);
        }
    };

    protected static Method hibernateGetClassMethod = null;

    static
    {
       try
       {
           Class hibernateClass = ClassLoaderUtils.loadClass("net.sf.hibernate.Hibernate", ObjectUtils.class);
           hibernateGetClassMethod = hibernateClass.getMethod("getClass", new Class[]{Object.class});
       }
       catch (Exception e)
       {
       }
    }


    /**
     * @return True if both are null or equal, otherwise returns false
     */
    public static boolean isIdentical(Object a, Object b)
    {
        return !isDifferent(a, b);
    }

    /**
     * @return False if both are null or equal, otherwise returns true
     */
    public static boolean isDifferent(Object a, Object b)
    {
        if ((a == null && b == null) || (a != null && a.equals(b)))
        {
            return false;
        }

        return true;
    }

    /**
     * Similar to {@link org.apache.commons.lang.StringUtils#isNotEmpty} but accepts a Sttring. Type safe
     * @param o
     * @return true if the object is not null && != ""
     */
    public static boolean isNotEmpty(Object o)
    {
        return o != null && !"".equals(o);
    }

    /**
     * Returns a predicate for {@link #isNotEmpty(Object)}
     * @return Predicate
     */
    public static Predicate getIsSetPredicate()
    {
        return NOT_EMPTY_PREDICATE;
    }

    /**
     * Gets the true class of an object, trying to use Hibernate's proxy unwrapping tools if available on the classpath.
     * <p />
     * Otherwise simply returns the class of the object passed in if Hibernate not on the classpath.
     * @param o The object to examine
     * @return The true class of the object (unwrapping Hibernate proxies etc)
     */
    public static Class getTrueClass(Object o)
    {
        if (hibernateGetClassMethod != null)
        {
            try
            {
                return (Class)hibernateGetClassMethod.invoke(null, new Object[] {o});
            }
            catch (Exception e)
            {
            }
        }

        return o.getClass();
    }
}
