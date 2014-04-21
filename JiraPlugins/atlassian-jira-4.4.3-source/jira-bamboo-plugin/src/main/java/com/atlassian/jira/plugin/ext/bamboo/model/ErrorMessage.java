package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

public class ErrorMessage
{
    private static final Logger log = Logger.getLogger(ErrorMessage.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties

    private final String title;
    private final String description;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    public ErrorMessage(String title, String description)
    {
        this.title = title;
        this.description = description;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    public Response.ResponseBuilder createJSONEntity(Response.ResponseBuilder builder)
    {
        final JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("message", title);
            jsonObject.put("messageBody", description);

            for (Map.Entry<String, String> entry : getExtraValues().entrySet())
            {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        }
        catch (JSONException e)
        {
            throw new RuntimeException("Could not build JSON object", e);
        }
        return builder.entity(jsonObject.toString());
    }

    @NotNull
    protected Map<String, String> getExtraValues()
    {
        return Collections.emptyMap();
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
