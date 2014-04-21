package com.atlassian.core.util;

import com.atlassian.core.util.ClassLoaderUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassHelper
{
    public static Object instantiateClass(Class clazz, Object[] constructorArgs) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Class args[] = new Class[constructorArgs.length];
        for (int i = 0; i < constructorArgs.length; i++)
        {
            if (constructorArgs == null)
            {
                args[i] = null;
            }
            else
            {
                args[i] = constructorArgs[i].getClass();
            }
        }
        Constructor ctor = null;
        if (clazz.getConstructors().length == 1)
        {
            ctor = clazz.getConstructors()[0];
        } else {
            //Obtaioning the ctor this way the class types need to be an exact match
            //We could obtain the required behaviour by checkinng that our args are assignable
            //for a given ctor... but I can't be bothered right now
            ctor = clazz.getConstructor(args);
        }
        return ctor.newInstance(constructorArgs);
    }

    public static Object instantiateClass(String name, Object[] constructorArgs) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Class clazz = ClassLoaderUtils.loadClass(name, ClassHelper.class);
        return instantiateClass(clazz, constructorArgs);
    }
}
