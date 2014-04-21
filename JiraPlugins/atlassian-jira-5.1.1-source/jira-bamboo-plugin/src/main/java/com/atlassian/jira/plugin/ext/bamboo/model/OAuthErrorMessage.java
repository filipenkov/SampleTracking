package com.atlassian.jira.plugin.ext.bamboo.model;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;

public class OAuthErrorMessage extends ErrorMessage
{
    private static final Logger log = Logger.getLogger(OAuthErrorMessage.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties

    private final URI oauthCallbackUri;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    public OAuthErrorMessage(String description, URI oauthCallbackUri)
    {
        super("Authentication Required", description);
        this.oauthCallbackUri = oauthCallbackUri;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    @NotNull
    @Override
    protected Map<String, String> getExtraValues()
    {
        final Map<String, String> map = Maps.newHashMap();
        map.put("oauthCallback", oauthCallbackUri.toString());
        return map;
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
