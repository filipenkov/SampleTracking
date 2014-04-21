package com.atlassian.upm.rest.resources;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.Plugin;

import org.apache.commons.io.IOUtils;

import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static com.google.common.base.Preconditions.checkNotNull;


@Path("/{pluginKey}/media")
@WebSudoNotRequired
public class PluginMediaResource
{
    public static final String IMAGES_PLUGIN_LOGO_DEFAULT_PNG = "/images/plugin-logo-default.png";
    public static final String IMAGES_PUZZLE_PIECE_PNG = "/images/puzzle-piece.png";
    public static final String IMAGES_CHARLIE64X58_GIF = "/images/Charlie64x58.gif";
    private final PluginAccessorAndController pluginAccessorAndController;

    public PluginMediaResource(PluginAccessorAndController pluginAccessorAndController)
    {
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    @GET
    @Path("plugin-icon")
    public Response getPluginIcon(@PathParam("pluginKey") String pluginKey)
    {
        Plugin plugin = pluginAccessorAndController.getPlugin(unescape(pluginKey));
        return getStreamingOutputDecision(pluginAccessorAndController.getPluginIconInputStream(plugin), IMAGES_PUZZLE_PIECE_PNG);
    }

    @GET
    @Path("plugin-logo")
    public Response getPluginLogo(@PathParam("pluginKey") String pluginKey)
    {
        Plugin plugin = pluginAccessorAndController.getPlugin(unescape(pluginKey));
        return getStreamingOutputDecision(pluginAccessorAndController.getPluginLogoInputStream(plugin), IMAGES_PLUGIN_LOGO_DEFAULT_PNG);
    }

    @GET
    @Path("plugin-banner")
    public Response getPluginBanner(@PathParam("pluginKey") String pluginKey)
    {
        Plugin plugin = pluginAccessorAndController.getPlugin(unescape(pluginKey));
        return getStreamingOutputDecision(pluginAccessorAndController.getPluginBannerInputStream(plugin), IMAGES_CHARLIE64X58_GIF);
    }

    @GET
    @Path("vendor-icon")
    public Response getVendorIcon(@PathParam("pluginKey") String pluginKey)
    {
        Plugin plugin = pluginAccessorAndController.getPlugin(unescape(pluginKey));
        return getStreamingOutputDecision(pluginAccessorAndController.getVendorIconInputStream(plugin), IMAGES_CHARLIE64X58_GIF);
    }

    @GET
    @Path("vendor-logo")
    public Response getVendorLogo(@PathParam("pluginKey") String pluginKey)
    {
        Plugin plugin = pluginAccessorAndController.getPlugin(unescape(pluginKey));
        return getStreamingOutputDecision(pluginAccessorAndController.getVendorLogoInputStream(plugin), IMAGES_CHARLIE64X58_GIF);
    }

    private Response getStreamingOutputDecision(Option<InputStream> optionIS, String defaultLocation)
    {
        for(final InputStream is: optionIS)
        {
            return Response.ok().type(MediaType.WILDCARD).entity(getStreamingOutputForResource(is)).build();
        }

        //This is the default image from the repository
        final InputStream inputStream = getClass().getResourceAsStream(defaultLocation);

        return Response.ok().type(MediaType.WILDCARD).entity(getStreamingOutputForResource(inputStream)).build();
    }

    private StreamingOutput getStreamingOutputForResource(final InputStream is)
    {
        return new StreamingOutput()
        {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException
            {
                try
                {
                    Image image = ImageIO.read(is);
                    ImageIO.write((RenderedImage) image, "png", output);
                }
                finally
                {
                    //Needed to keep resources from leaking
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(output);
                }

            }
        };
    }

}
