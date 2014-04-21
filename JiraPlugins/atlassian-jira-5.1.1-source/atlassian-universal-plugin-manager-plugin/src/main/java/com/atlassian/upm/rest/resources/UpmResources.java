package com.atlassian.upm.rest.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.token.TokenException;
import com.atlassian.upm.token.TokenManager;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

public class UpmResources
{
    /**
     * Returns an error {@link Response} if the given plugin does not use Atlassian licensing, {@code none()} otherwise.
     *
     * @param representationFactory the representation factory
     * Returns an error {@link Response} if the given plugin does not use Atlassian licensing, {@code none()} otherwise.
     */
    public static Option<Response> licensingPreconditionFailed(PluginAccessorAndController pluginAccessorAndController,
                                                               Plugin plugin,
                                                               RepresentationFactory representationFactory)
    {
        if (!pluginAccessorAndController.usesLicensing(plugin))
        {
            return some(Response.status(PRECONDITION_FAILED)
                            .entity(representationFactory.createI18nErrorRepresentation(
                                "upm.plugin.error.plugin.not.using.licensing"))
                            .type(ERROR_JSON)
                            .build());
        }
        return none(Response.class);
    }
    
    /**
     * Validates the token parameter and throws an exception if it is invalid or missing.
     * @param token  token parameter from the request
     * @param username  current authenticated user
     * @param responseContentType  content type to use if sending an error response
     * @throws  {@link javax.ws.rs.WebApplicationException} if validation fails or if the token parameter is null
     */
    public static void validateToken(String token,
                                     String username,
                                     String responseContentType,
                                     TokenManager tokenManager,
                                     RepresentationFactory representationFactory)
    {
        String error = "";
        try
        {
            if (token == null || !tokenManager.attemptToMatchAndInvalidateToken(username, token))
            {
                error = "invalid token";
            }
        }
        catch (TokenException e)
        {
            error = e.getMessage();
        }
        if (isNotEmpty(error))
        {
            throw new WebApplicationException(Response.status(FORBIDDEN)
                .entity(representationFactory.createErrorRepresentation("invalid token", "upm.error.invalid.token"))
                .type(responseContentType).build());
        }
    }
}
