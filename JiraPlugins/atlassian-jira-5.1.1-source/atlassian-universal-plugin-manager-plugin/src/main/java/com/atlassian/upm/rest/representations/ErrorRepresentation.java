package com.atlassian.upm.rest.representations;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Provides a JSON representation of the error response.
 */
public class ErrorRepresentation
{
    @JsonProperty private final String errorMessage;
    @JsonProperty private final String subCode;

    /**
     * Constructor for use by Jackson
     */
    @JsonCreator
    public ErrorRepresentation(@JsonProperty("errorMessage") String errorMessage,
        @JsonProperty("subCode") String subCode)
    {
        this.errorMessage = errorMessage;
        this.subCode = subCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public String getSubCode()
    {
        return subCode;
    }
}
