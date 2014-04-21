package com.atlassian.jira.web.util.component;

import com.atlassian.util.profiling.object.ObjectProfiler;
import org.apache.log4j.Logger;
import webwork.util.InjectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProfilingWebworkInjector implements InjectionUtils.InjectionImpl
{
    private static final Logger log = Logger.getLogger(ProfilingWebworkInjector.class);

    public Object invoke(Method method, Object object, Object[] objects) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            return ObjectProfiler.profiledInvoke(method, object, objects);
        }
        catch (Throwable throwable)
        {
           log.error("Error profiling method " + throwable, throwable);
           throw new RuntimeException(throwable);
        }
    }
}
