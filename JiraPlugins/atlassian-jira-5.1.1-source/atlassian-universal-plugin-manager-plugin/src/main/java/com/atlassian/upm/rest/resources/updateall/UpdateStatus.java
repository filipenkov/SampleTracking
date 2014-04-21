package com.atlassian.upm.rest.resources.updateall;

import com.atlassian.upm.rest.async.TaskStatus;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_COMPLETE_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_DOWNLOADING_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_ERR_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_FINDING_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_UPDATING_JSON;

public class UpdateStatus extends TaskStatus
{
    public UpdateStatus(UpdateStatus.State state)
    {
        super(state.isDone(), state.getContentType());
    }

    public enum State
    {
        FINDING_UPDATES(UPDATE_ALL_FINDING_JSON),
        DOWNLOADING(UPDATE_ALL_DOWNLOADING_JSON),
        UPDATING(UPDATE_ALL_UPDATING_JSON),
        COMPLETE(UPDATE_ALL_COMPLETE_JSON),
        ERR(UPDATE_ALL_ERR_JSON);

        private final String contentType;

        private State(String contentType)
        {
            this.contentType = contentType;
        }

        public boolean isDone()
        {
            return this == COMPLETE;
        }

        public String getContentType()
        {
            return contentType;
        }
    }

    public static Err err(String subCode)
    {
        return new Err(subCode);
    }

    public static class Err extends UpdateStatus
    {
        @JsonProperty private final String subCode;

        @JsonCreator
        public Err(@JsonProperty("subCode") String subCode)
        {
            super(State.ERR);
            this.subCode = subCode;
        }

        public String getSubCode()
        {
            return subCode;
        }
    }
}