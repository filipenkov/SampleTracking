package com.atlassian.rpc.jsonrpc;

import com.atlassian.voorhees.ApplicationException;
import com.atlassian.voorhees.RpcMethodMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps JSON-RPC method calls to an existing Confluence SOAP service. Confluence's native RPC token authentication
 * is NOT supported, it is expected that JSON-RPC clients will have already authenticated some other way.
 *
 * If the SOAP service is configured as authenticated, JSON-RPC clients should omit all token parameters: e.g.
 * a call that was getSpace(token, spaceKey) will be exposed to JSON-RPC as  getSpace(spaceKey). Access to
 * the login() method on authenticated services will be blocked entirely.
 */
public class SoapModuleMethodMapper implements RpcMethodMapper
{
    private Object mappedObject;
    private Class publishedInterface;
    private boolean authenticated;
    private Map<String, List<Method>> methodCache;

    public SoapModuleMethodMapper(Object mappedObject, Class publishedInterface, boolean authenticated)
    {
        this.mappedObject = mappedObject;
        this.publishedInterface = publishedInterface;
        this.authenticated = authenticated;

        initMethodCache();
    }

    @Override
    public boolean methodExists(String methodName)
    {
        if (authenticated && methodName.equals("login"))
            return false;

        return methodCache.containsKey(methodName);
    }

    @Override
    public boolean methodExists(String methodName, int arity)
    {
        if (!methodExists(methodName))
            return false;

        if (authenticated)
            arity++;

        for (Method method : methodCache.get(methodName))
        {
            if (method.getParameterTypes().length == arity)
                return true;
        }

        return false;
    }

    @Override
    public List<Class[]> getPossibleArgumentTypes(String methodName, int arity)
    {
        if (!methodExists(methodName))
            throw new IllegalStateException("No method exists with name " + methodName);

        if (authenticated)
            arity++;

        List<Class[]> possibleArgumentTypes = new ArrayList<Class[]>();

        for (Method method : methodCache.get(methodName))
        {
            if (method.getParameterTypes().length == arity)
                possibleArgumentTypes.add(filterIfAuthenticated(authenticated, method.getParameterTypes()));
        }

        if (possibleArgumentTypes.size() == 0)
            throw new IllegalStateException("No method exists with name " + methodName + " and arity " + arity);


        return possibleArgumentTypes;
    }

    private Class[] filterIfAuthenticated(boolean authenticated, Class[] parameterTypes)
    {
        if (authenticated)
        {
            Class[] replacement = new Class[parameterTypes.length - 1];
            if (replacement.length > 0)
                System.arraycopy(parameterTypes, 1, replacement, 0, replacement.length);

            parameterTypes = replacement;
        }

        return parameterTypes;
    }

    @Override
    public Object call(String methodName, Class[] argumentTypes, Object[] arguments) throws Exception
    {
        if (authenticated)
        {
            Class[] replacementTypes = new Class[argumentTypes.length + 1];
            replacementTypes[0] = String.class;
            System.arraycopy(argumentTypes, 0, replacementTypes, 1, argumentTypes.length);
            argumentTypes = replacementTypes;

            Object[] replacementArguments = new Object[arguments.length + 1];
            replacementArguments[0] = "";
            System.arraycopy(arguments, 0, replacementArguments, 1, arguments.length);
            arguments = replacementArguments;
        }

        try
        {
            return publishedInterface.getMethod(methodName, argumentTypes).invoke(mappedObject, arguments);
        }
        catch (InvocationTargetException e)
        {
            throw new ApplicationException(e.getCause());
        }
    }

    // ONLY CALL DURING CONSTRUCTION. NOT THREAD SAFE
    private void initMethodCache()
    {
        methodCache = new HashMap<String, List<Method>>();

        for (Method method : publishedInterface.getMethods())
        {
            if (!methodCache.containsKey(method.getName()))
                methodCache.put(method.getName(), new ArrayList<Method>());

            methodCache.get(method.getName()).add(method);
        }
    }
}
