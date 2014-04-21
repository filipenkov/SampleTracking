package com.atlassian.jira.rest.v1.images;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.jira.image.dropdown.DropDownCreatorService;
import com.atlassian.jira.image.separator.HeaderSeparatorService;
import static com.atlassian.jira.rest.v1.util.CacheControl.CACHE_FOREVER;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for retrieving a header separator of a given colour.
 *
 * @since v4.0
 */
@Path("header-separator")
@AnonymousAllowed
@Produces({"image/png"})
public class HeaderSeparatorResouce
{
    private final HeaderSeparatorService headerSeparatorService;

    public HeaderSeparatorResouce(HeaderSeparatorService headerSeparatorService)
    {
        this.headerSeparatorService = headerSeparatorService;
    }

    /**
     * Retrieve a header separator for the passed in colors.
     * <p/>
     * Input strings can ontain a leading hash (#) and can be a 3 char or 6 char hex string.  See any web tutorial for
     * what colour the string represents.
     * <p/>
     * This is cached effectively forever
     *
     * @param colorHex           The main color of the separator
     * @param backgroundColorHex The background colour of the separator.
     * @return An array of bytes that represent the returned png. This is cached effectively forever
     */
    @GET
    public Response getImage(@QueryParam("color") String colorHex, @QueryParam("bgcolor") String backgroundColorHex)
    {

        final byte[] separator = headerSeparatorService.getSeparator(colorHex, backgroundColorHex);
        return Response.ok(separator).cacheControl(CACHE_FOREVER).build();
    }
}
