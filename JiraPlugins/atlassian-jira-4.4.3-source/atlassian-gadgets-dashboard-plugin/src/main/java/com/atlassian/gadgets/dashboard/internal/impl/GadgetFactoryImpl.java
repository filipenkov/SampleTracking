package com.atlassian.gadgets.dashboard.internal.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetFactory;
import com.atlassian.gadgets.dashboard.internal.util.HelpLinkResolver;
import com.atlassian.gadgets.dashboard.spi.GadgetStateFactory;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.sal.api.message.HelpPathResolver;
import com.atlassian.sal.api.message.I18nResolver;

/**
 * An implementation of {@code GadgetFactory} that delegates to a {@link GadgetStateFactory} and a {@link
 * GadgetSpecFactory} and returns {@link GadgetImpl} instances.
 */
public class GadgetFactoryImpl implements GadgetFactory
{
    private final GadgetStateFactory stateFactory;
    private final GadgetSpecFactory specFactory;
    private final I18nResolver i18n;
    private final HelpLinkResolver linkResolver;
    private final HelpPathResolver helpPathResolver;

    /**
     * Creates a new {@code GadgetFactoryImpl} that delegates to the specified factory.
     *
     * @param stateFactory the factory to use to create new gadget state objects
     * @param specFactory  the factory to use to retrieve gadget specs
     * @param helpPathResolver
     */
    public GadgetFactoryImpl(GadgetStateFactory stateFactory, GadgetSpecFactory specFactory, final I18nResolver i18n, final HelpPathResolver helpPathResolver)
    {
        this.stateFactory = stateFactory;
        this.specFactory = specFactory;
        this.i18n = i18n;
        this.helpPathResolver = helpPathResolver;
        this.linkResolver = new HelpLinkResolver(this.helpPathResolver);
    }

    /**
     * Creates a new gadget from the spec at the specified URL.  This is used when creating new gadget instances.
     *
     * @param gadgetSpecUrl       the URL of the gadget spec file
     * @param gadgetRequestContext context for this request
     * @return the {@code Gadget} created from the specified spec URL
     * @throws GadgetParsingException        if there is an error parsing the gadget spec at the specified URL
     * @throws com.atlassian.gadgets.GadgetSpecUriNotAllowedException if the specified gadget spec URL is not a valid URI
     */
    public Gadget createGadget(String gadgetSpecUrl, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException, GadgetSpecUriNotAllowedException
    {
        URI gadgetSpecUri;
        try
        {
            gadgetSpecUri = new URI(gadgetSpecUrl);
        }
        catch (URISyntaxException e)
        {
            throw new GadgetSpecUriNotAllowedException(e);
        }
        return createGadget(stateFactory.createGadgetState(gadgetSpecUri), gadgetRequestContext);
    }

    /**
     * Constructs a {@code Gadget} instance from the specified {@code GadgetState}.  This is used to reconstruct
     * existing gadget instances.
     *
     * @param state               the persistent gadget state to reconstruct from
     * @param gadgetRequestContext context for this request
     * @return the {@code Gadget} reconstructed from the specified state
     * @throws GadgetParsingException if there is an error parsing the gadget spec at the URL specified by {@code
     *                                state.getGadgetSpecUri()}
     */
    public Gadget createGadget(final GadgetState state, final GadgetRequestContext gadgetRequestContext) throws GadgetParsingException
    {
        try
        {
            return new GadgetImpl(state, specFactory.getGadgetSpec(state, gadgetRequestContext));
        }
        catch (Exception ex)
        {
            String errorMessage;
            final String exceptionMsg = ex.getMessage();
            //this is a bit of a hack but Shindig does not return the error message and I want to avoid patching shindig
            //just for this particular error!
            if(exceptionMsg != null && exceptionMsg.contains("HTTP error 403"))
            {
                errorMessage = i18n.getText("gadget.error.loading", state.getGadgetSpecUri(), i18n.getText("gadget.403.error", state.getGadgetSpecUri(), linkResolver.getLink("whitelist.external.gadget")));
            }
            else
            {
                errorMessage = i18n.getText("gadget.error.loading", state.getGadgetSpecUri(), exceptionMsg);
            }

            if (gadgetRequestContext.isDebuggingEnabled())
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                ex.printStackTrace(pw);

                StringBuilder extraDebugInfo = new StringBuilder(errorMessage);
                extraDebugInfo.append("\nGadget ID: ").append(state.getId());
                extraDebugInfo.append("\nStack trace: ").append(sw.toString());
                return new GadgetImpl(state,  extraDebugInfo.toString());
            }
            else
            {
                return new GadgetImpl(state, errorMessage.toString());
            }
        }
    }
}
