package com.atlassian.upm.rest.async;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.MediaTypes.TASK_ERROR_JSON;

public class ErrorTaskStatus extends TaskStatus
{

    @JsonProperty private final String subCode;

    @JsonCreator
    public ErrorTaskStatus(@JsonProperty("subCode") String subCode)
    {
        super(true, TASK_ERROR_JSON);
        this.subCode = subCode;
    }

    public String getSubCode()
    {
        return subCode;
    }
}
