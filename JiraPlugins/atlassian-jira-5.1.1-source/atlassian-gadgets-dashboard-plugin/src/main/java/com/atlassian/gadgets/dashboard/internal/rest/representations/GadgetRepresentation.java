package com.atlassian.gadgets.dashboard.internal.rest.representations;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;

import org.apache.commons.lang.StringUtils;

/**
 * Provides a JAXB view of a Gadget, so that we can build new gadgets on the file in Javascript.
 * TODO: AG-428 add LINK elements to this representation, as the REST Guidleines recommend.
 */

@XmlRootElement
public class GadgetRepresentation
{
    @XmlElement
    private final String id;
    @XmlElement
    private final String title;
    @XmlElement
    private final String titleUrl;
    @XmlElement
    private final String gadgetSpecUrl;
    @XmlElement
    private final Integer height;
    @XmlElement
    private final Integer width;
    @XmlElement
    private final Color color;
    @XmlElement
    private final Integer column;
    @XmlElement
    private final String colorUrl;
    @XmlElement
    private final String gadgetUrl;
    @XmlElement
    private final Boolean isMaximizable;
    @XmlElement
    private final String renderedGadgetUrl;
    @XmlElement
    private final Boolean hasNonHiddenUserPrefs;
    @XmlElement
    private final UserPrefsRepresentation userPrefs;
    @XmlElement
    private final Boolean loaded;
    @XmlElement
    private final String errorMessage;


    // Provided for JAXB.
    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private GadgetRepresentation()
    {
        id = "0";
        title = null;
        titleUrl = null;
        gadgetSpecUrl = null;
        height = null;
        width = null;
        color = null;
        isMaximizable = null;
        userPrefs = null;
        renderedGadgetUrl = null;
        colorUrl = null;
        gadgetUrl = null;
        hasNonHiddenUserPrefs = null;
        column = null;
        loaded = null;
        errorMessage = "";
    }

    /**
     * Constructor. Maps the {@link Gadget} properties onto the JAXB properties.
     *
     * @param gadget The gadget for which to construct a JAXB representation
     * @param gadgetUrls Provides various URLs for this gadget
     * @param columnIndex Defines which column this gadget is in.
     */
    GadgetRepresentation(Gadget gadget, GadgetUrlContainer gadgetUrls, DashboardState.ColumnIndex columnIndex)
    {
        final GadgetId gadgetId = gadget.getId();
        id = gadgetId.value();
        loaded = gadget.isLoaded();
        title = loaded ? gadget.getTitle() : null;
        height = loaded ? gadget.getHeight() : null;
        width = loaded ? gadget.getWidth() : null;
        color = loaded ? gadget.getColor() : null;
        isMaximizable = loaded ? gadget.isMaximizable() : null;
        userPrefs = loaded ? new UserPrefsRepresentation(gadget.getUserPrefs(), gadgetUrls.getUserPrefsUri()) : null;
        hasNonHiddenUserPrefs = loaded ? gadget.hasNonHiddenUserPrefs() : null;
        titleUrl = gadgetUrls.getTitleUri();
        gadgetSpecUrl = gadget.getGadgetSpecUrl();
        colorUrl = gadgetUrls.getColorUri();
        gadgetUrl = gadgetUrls.getGadgetUri();
        renderedGadgetUrl = gadgetUrls.getRenderedGadgetUri();
        column = columnIndex == null ? null : columnIndex.index();
        errorMessage = gadget.getErrorMessage();
    }

    public Boolean getHasNonHiddenUserPrefs()
    {
        return hasNonHiddenUserPrefs;
    }

    public Boolean isLoaded()
    {
        return loaded;
    }

    public Color getColor()
    {
        return color;
    }

    public String getGadgetSpecUrl()
    {
        return gadgetSpecUrl;
    }

    public Integer getHeight()
    {
        return height;
    }

    public String getId()
    {
        return id;
    }

    public Boolean isMaximizable()
    {
        return isMaximizable;
    }

    public String getTitle()
    {
        return title;
    }

    public String getTitleUrl()
    {
        return titleUrl;
    }

    public UserPrefsRepresentation getUserPrefs()
    {
        return userPrefs;
    }

    public Integer getWidth()
    {
        return width;
    }

    public String getRenderedGadgetUrl()
    {
        return renderedGadgetUrl;
    }

    public String getColorUrl()
    {
        return colorUrl;
    }

    public String getGadgetUrl()
    {
        return gadgetUrl;
    }

    public Boolean hasNonHiddenUserPrefs()
    {
        return hasNonHiddenUserPrefs;
    }

    public Integer getColumn()
    {
        return column;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * Helper class to help with constructing all the URLs required for a GadgetRepresentation.
     */
    final static class GadgetUrlContainer
    {
        private final String colorUri;
        private final String gadgetUri;
        private final String userPrefsUri;
        private final String renderedGadgetUri;
        private final String titleUri;

        GadgetUrlContainer(final RenderedGadgetUriBuilder renderedGadgetUriBuilder,
                final DashboardUrlBuilder dashboardUrlBuilder,
                final DashboardId dashboardId,
                final Gadget gadget,
                final GadgetRequestContext gadgetRequestContext,
                final boolean writable)
        {
            final GadgetId gadgetId = gadget.getId();

            final String titleUrlString = ( !gadget.isLoaded() || gadget.getTitleUrl() == null ) ? null : gadget.getTitleUrl().toASCIIString();
            //this seems to come back as an empty string if the gadget spec doesn't define one.
            if(StringUtils.isNotBlank(titleUrlString))
            {
                this.titleUri = titleUrlString;
            }
            else
            {
                this.titleUri = null;
            }
            this.colorUri = dashboardUrlBuilder.buildGadgetColorUrl(dashboardId, gadgetId);
            this.gadgetUri = dashboardUrlBuilder.buildGadgetUrl(dashboardId, gadgetId);
            this.userPrefsUri = dashboardUrlBuilder.buildGadgetUserPrefsUrl(dashboardId, gadgetId);
            final View view = new View.Builder().viewType(ViewType.DEFAULT).writable(writable).build();
            this.renderedGadgetUri =
                renderedGadgetUriBuilder.build(gadget.getState(),
                                               ModuleId.valueOf(gadget.getId().value()),
                                               view,
                                               gadgetRequestContext).toASCIIString();
        }

        public String getColorUri()
        {
            return colorUri;
        }

        public String getGadgetUri()
        {
            return gadgetUri;
        }

        public String getRenderedGadgetUri()
        {
            return renderedGadgetUri;
        }

        public String getUserPrefsUri()
        {
            return userPrefsUri;
        }

        public String getTitleUri()
        {
            return titleUri;
        }
    }
}
