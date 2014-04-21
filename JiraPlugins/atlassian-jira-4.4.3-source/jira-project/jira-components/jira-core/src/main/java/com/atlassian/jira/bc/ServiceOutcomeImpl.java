package com.atlassian.jira.bc;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * Generic service outcome that can optionally hold a value.
 *
 * @since v4.2
 */
public class ServiceOutcomeImpl<T> extends ServiceResultImpl implements ServiceOutcome<T>
{
    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided
     * returned value.
     *
     * @param <T> the type of the returned value
     * @param returnedValue the returned value
     * @return a new ServiceOutcomeImpl
     */
    public static <T> ServiceOutcomeImpl<T> ok(T returnedValue)
    {
        return new ServiceOutcomeImpl<T>(new SimpleErrorCollection(), returnedValue);
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing the provided error message, and no
     * return value.
     *
     * @param <T> the type of the returned value
     * @param errorMessage the error message to include in the ServiceOutcomeImpl
     * @return a new ServiceOutcomeImpl
     */
    public static <T> ServiceOutcomeImpl<T> error(String errorMessage)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage(errorMessage);
        return new ServiceOutcomeImpl<T>(errors);
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl containing the given errors and returned value.
     *
     * @param errorCollection an ErrorCollection
     * @param value the returned value
     * @param <T> the type of the returned value
     * @return a new ServiceOutcomeImpl instance
     */
    public static <T> ServiceOutcomeImpl<T> from(ErrorCollection errorCollection, T value)
    {
        return new ServiceOutcomeImpl<T>(errorCollection, value);
    }

    /**
     * The wrapped result.
     */
    private final T value;

    /**
     * Creates a new ServiceOutcomeImpl with the given errors. The returned value will be set to null.
     *
     * @param errorCollection an ErrorCollection
     */
    public ServiceOutcomeImpl(ErrorCollection errorCollection)
    {
        this(errorCollection, null);
    }

    /**
     * Creates a new ServiceOutcomeImpl with the given errors and returned value.
     *
     * @param errorCollection an ErrorCollection
     * @param value the wrapped value
     */
    public ServiceOutcomeImpl(ErrorCollection errorCollection, T value)
    {
        super(errorCollection);
        this.value = value;
    }

    /**
     * Returns the value that was returned by the service, or null.
     *
     * @return the value returned by the service, or null
     */
    public T getReturnedValue()
    {
        return value;
    }
}
