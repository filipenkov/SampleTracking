package com.atlassian.upm.rest.async;

import static com.atlassian.upm.rest.MediaTypes.PENDING_TASK_JSON;

import javax.ws.rs.core.Response.Status;

public class TaskStatus
{
    private final boolean done;
    private final String contentType;
    private final int statusCode;

    public TaskStatus(boolean done, String contentType, int statusCode)
    {
        this.done = done;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }

    public TaskStatus(boolean done, String contentType)
    {
        this(done, contentType, Status.OK.getStatusCode());
    }

    public TaskStatus(boolean done)
    {
        this(done, PENDING_TASK_JSON);
    }

    public final boolean isDone()
    {
        return done;
    }

    public final String getContentType()
    {
        return contentType;
    }
    
    public final int getStatusCode()
    {
        return statusCode;
    }
}
