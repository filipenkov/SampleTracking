package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.sal.api.net.Response;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BambooRestResponse
{

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(BambooRestResponse.class);
    public static final String MESSAGE = "message";
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private int statusCode = -1;
    private String statusMessage;
    private String responseBody;
    private final List<String> errors = new ArrayList<String>();
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors


    public BambooRestResponse(int statusCode, @Nullable String statusMessage, @Nullable String responseBody, @NotNull List<String> errors)
    {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseBody = responseBody;
        this.errors.addAll(errors);
    }

    public BambooRestResponse(@NotNull Response response)
    {
        this.statusCode = response.getStatusCode();
        this.statusMessage = response.getStatusText();
        try
        {
            this.responseBody = IOUtils.toString(response.getResponseBodyAsStream());
            JSONObject jsonObject = new JSONObject(responseBody);
            if (jsonObject.has(MESSAGE))
            {
                errors.add(jsonObject.getString(MESSAGE));
            }
        }
        catch (JSONException e)
        {
            //Ignore exception. This should mean that the body is used instead.
        }
        catch (Throwable e)
        {
            errors.add("Failed to retrieve response body from Bamboo: " + e.getMessage());
        }
    }

    public BambooRestResponse(String... errors)
    {
        Collections.addAll(this.errors, errors);
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public int getStatusCode()
    {
        return statusCode;
    }

    @Nullable
    public String getStatusMessage()
    {
        return statusMessage;
    }

    @Nullable
    public String getResponseBody()
    {
        return responseBody;
    }

    @NotNull
    public List<String> getErrors()
    {
        return errors;
    }

    public boolean isValidStatusCode()
    {
        return statusCode < 300;
    }
}
