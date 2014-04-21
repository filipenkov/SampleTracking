package com.atlassian.voorhees;

import java.util.List;

/**
 * Responsible for mapping JSON-RPC calls back to the Java implementation of the method. The interface might look
 * a bit wacky, but separating the concerns this way allows the mapper (and all things behind it) to be entirely
 * ignorant of the JSON-RPC protocol and error-handling.
 */
public interface RpcMethodMapper
{
    /**
     * Returns true if a method is available to be served with the given name
     *
     * @param methodName the method name
     * @return true if that method can be served, false otherwise
     */
    boolean methodExists(String methodName);

    /**
     * Returns true if a method is available to be served with the given name and number of arguments
     *
     * @param methodName the method name
     * @param arity the number of arguments taken by the method
     * @return true if that method can be served, false otherwise
     */
    boolean methodExists(String methodName, int arity);

    /**
     * Returns the argument types of all methods with the given name and arity, in order from
     * most preferred to least. It is strongly recommended that you don't overload methods with the same
     * number of arguments, but if you do the server will at least attempt to make a guess.
     *
     * @param methodName the method name
     * @param arity the number of arguments taken by the method
     * @return an array of Class[] arrays, each one possible list of argument types for that
     *         method.
     * @throws IllegalStateException if no method exists with that name and arity
     */
    List<Class[]> getPossibleArgumentTypes(String methodName, int arity);

    /**
     * Call the given method.
     *
     * @param methodName the name of the method
     * @param argumentTypes the argument types of the method to call. This should be one of the arrays returned
     *        from {@link #getPossibleArgumentTypes(String, int)}
     * @param arguments the arguments to pass into the method
     * @return the return value of the method being called. If the method being called is void, the return value
     *         will be null
     * @throws IllegalArgumentException if the types of the arguments do not agree with the argumentTypes array
     * @throws UnsupportedOperationException if no method exists with that name and types of arguments
     * @throws ApplicationException if something went wrong in the underlying application during the method call
     * @throws Exception some internal server exception occurred that made it impossible to call an otherwise
     *         valid method
     */
    Object call(String methodName, Class[] argumentTypes, Object[] arguments) throws Exception;
}
