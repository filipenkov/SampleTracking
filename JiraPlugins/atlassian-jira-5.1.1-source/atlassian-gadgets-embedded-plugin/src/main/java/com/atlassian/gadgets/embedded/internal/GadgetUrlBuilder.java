package com.atlassian.gadgets.embedded.internal;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.spec.UserPrefSpec;
import com.atlassian.gadgets.util.AbstractUrlBuilder;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.SecurityTokenFactory;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

import static com.atlassian.gadgets.util.Uri.encodeUriComponent;
import static com.atlassian.gadgets.util.Uri.resolveUriAgainstBase;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds URLs to the rendered gadget.
 */
public class GadgetUrlBuilder extends AbstractUrlBuilder implements RenderedGadgetUriBuilder
{
    private static final String CONTAINER = "atlassian";
    private final SecurityTokenFactory securityTokenFactory;
    private final GadgetSpecFactory gadgetSpecFactory;
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     * @param applicationProperties the {@code ApplicationProperties} implementation to use
     * @param webResourceManager the {@code WebResourceManager} implementation to use
     * @param securityTokenFactory the {@code SecurityTokenFactory} implementation to use
     * @param gadgetSpecFactory the {@code GadgetSpecFactory} implementation to use
     */
    public GadgetUrlBuilder(ApplicationProperties applicationProperties, WebResourceManager webResourceManager,
                            SecurityTokenFactory securityTokenFactory, GadgetSpecFactory gadgetSpecFactory)
    {
        super(applicationProperties, webResourceManager, "");
        this.securityTokenFactory = securityTokenFactory;
        this.gadgetSpecFactory = gadgetSpecFactory;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated as of 2.0, use {@link #build(GadgetState, ModuleId, View, GadgetRequestContext)}
     */
    @Deprecated
    public URI build(GadgetState gadget, View view, GadgetRequestContext gadgetRequestContext)
    {
        return build(gadget, ModuleId.valueOf(gadget.getId().value()), view, gadgetRequestContext);
    }

    public final URI build(GadgetState gadget, ModuleId moduleId, View view, GadgetRequestContext gadgetRequestContext)
    {
        ViewType viewType = view.getViewType();
        Map<String, String> viewParams = view.paramsAsMap();
        return URI.create(getBaseUrl() + "/ifr?"
                + "container=" + CONTAINER
                + "&mid=" + encodeUriComponent(checkNotNull(moduleId, "moduleId").toString())
                + (gadgetRequestContext.getIgnoreCache() ? "&nocache=1" : "")
                + "&country=" + gadgetRequestContext.getLocale().getCountry()
                + "&lang=" + gadgetRequestContext.getLocale().getLanguage()
                + "&view=" + viewType.getCanonicalName().toLowerCase()
                + (gadgetRequestContext.isDebuggingEnabled() ? "&debug=1" : "")
                + buildViewParams(viewParams)
                + "&st=" + encodeUriComponent(
                securityTokenFactory.newSecurityToken(gadget, gadgetRequestContext.getViewer()))
                + buildUserPrefsParams(gadget, gadgetRequestContext)
                + "&url=" + encodeUriComponent(absoluteGadgetSpecUri(gadget).toASCIIString())
                + "&libs=auth-refresh"
                + "");
    }

    private String buildUserPrefsParams(GadgetState gadgetState, GadgetRequestContext gadgetRequestContext)
    {
        StringBuilder userPrefsParam = new StringBuilder();

        // get the specUri from the gadgetState and then parse it
        URI specUri = gadgetState.getGadgetSpecUri();
        try
        {
            GadgetSpec spec = gadgetSpecFactory.getGadgetSpec(specUri, gadgetRequestContext);

            // go through all the user prefs defined in the spec and use their current values
            // (if there are any) or default otherwise
            for (UserPrefSpec userPrefSpec : spec.getUserPrefs())
            {
                String prefName = userPrefSpec.getName();
                String prefValue = gadgetState.getUserPrefs().get(prefName);
                if (prefValue == null)
                {
                    prefValue = userPrefSpec.getDefaultValue();
                }

                userPrefsParam.append("&up_").append(encodeUriComponent(prefName)).
                        append("=").append(encodeUriComponent(prefValue));
            }
        }
        catch (GadgetParsingException e)
        {
            log.warn("GadgetUrlBuilder: could not parse spec at " + specUri);
            log.debug("GadgetUrlBuilder", e);
        }
        return userPrefsParam.toString();
    }

    /*
     * Shindig can't deal with relative URLs, so we need to resolve any gadget spec URIs against the server base URL.
     */
    private URI absoluteGadgetSpecUri(GadgetState gadget)
    {
        return resolveUriAgainstBase(applicationProperties.getBaseUrl(), gadget.getGadgetSpecUri());
    }

    private String buildViewParams(Map<String, String> viewParams)
    {
        if (viewParams != null && !viewParams.isEmpty())
        {
            return "&view-params=" + encodeUriComponent(new JSONObject(viewParams).toString());
        }
        return "";
    }
}
