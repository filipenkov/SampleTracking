package com.atlassian.voorhees;

/**
 * Default implementation of the ErrorMapper used if none is provided by the application. All application errors
 * will be mapped to the same error code (500) and a generic exception message.
 */
public class DefaultErrorMapper implements ErrorMapper
{
    public static final int GENERIC_ERROR_CODE = 500;

    private final I18nAdapter i18nAdapter;

    public DefaultErrorMapper(I18nAdapter i18nAdapter)
    {
        this.i18nAdapter = i18nAdapter;
    }

    @Override
    public JsonError mapError(String methodName, Throwable throwable)
    {
        return new JsonError(GENERIC_ERROR_CODE, i18nAdapter.getText("voorhees.something.went.wrong", throwable.toString()), throwable);
    }
}
