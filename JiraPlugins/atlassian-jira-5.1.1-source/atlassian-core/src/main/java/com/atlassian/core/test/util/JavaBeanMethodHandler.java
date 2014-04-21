package com.atlassian.core.test.util;

import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * Handles getters and setters by storing their values in a map. Calling a method
 * that is not a getter or a setter will throw UnsupportedOperationException.
 * <p/>
 * Make sure you create one of these per test, as they are stateful.
 */
public class JavaBeanMethodHandler implements DuckTypeProxy.UnimplementedMethodHandler
{
    private Map<String, Object> data = new HashMap<String, Object>();

    public Object methodNotImplemented(Method method, Object[] args)
    {
        final String name = method.getName();
        String key = (name.length() < 4) ? null :
            name.substring(3, 4).toLowerCase() + name.substring(4);

        if (name.startsWith("get") && ArrayUtils.getLength(args) == 0)
        {
            return data.get(key);
        }
        else if (name.startsWith("set") && ArrayUtils.getLength(args) == 1)
        {
            data.put(key, args[0]);
            return null;
        }

        throw new UnsupportedOperationException(method.toString());
    }
}
