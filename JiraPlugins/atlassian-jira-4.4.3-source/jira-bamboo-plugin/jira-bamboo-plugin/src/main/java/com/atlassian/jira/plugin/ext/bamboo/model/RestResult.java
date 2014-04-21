package com.atlassian.jira.plugin.ext.bamboo.model;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Object that encapsulates data obtained from a REST request and any errors that occurred when trying to make the request
 * @param <T> result object
 */
public class RestResult<T>
{
    private static final Logger log = Logger.getLogger(RestResult.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties

    private final ImmutableList<String> errors;
    private final T result;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    public RestResult(@Nullable final T result, @NotNull final Collection<String> errors)
    {
        this.errors = ImmutableList.copyOf(errors);
        this.result = result;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    /**
     * @return result. There should be errors in {@link #getErrors()} if null
     */
    @Nullable
    public T getResult()
    {
        return result;
    }

    /**
     * @return list of errors
     */
    @NotNull
    public ImmutableList<String> getErrors()
    {
        return errors;
    }

    /**
     * Returns a formatted error message suitable for {@link ErrorMessage}
     * @param defaultMessage
     * @return error message
     */
    @NotNull
    public String getErrorMessage(String defaultMessage)
    {
        if (!errors.isEmpty())
        {
            return StringUtils.join(errors, "<br>");
        }
        return defaultMessage;
    }
}
