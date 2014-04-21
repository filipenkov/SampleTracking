package com.atlassian.gadgets.renderer.internal;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.Feature;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.spec.UserPrefSpec;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetFeatureRegistry;
import org.apache.shindig.gadgets.RenderingContext;
import org.apache.shindig.gadgets.UserPrefs;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.apache.shindig.gadgets.spec.UserPref;
import org.apache.shindig.gadgets.variables.VariableSubstituter;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

/**
 * Default implementation of {@code GadgetSpecFactory}.
 */
public class GadgetSpecFactoryImpl implements GadgetSpecFactory
{
    private static final Log logger = LogFactory.getLog(GadgetSpecFactoryImpl.class);
    
    private final ApplicationProperties applicationProperties;

    private final org.apache.shindig.gadgets.GadgetSpecFactory shindigFactory;
    private final VariableSubstituter substituter;
    private GadgetFeatureRegistry gadgetFeatureRegistry;

    /**
     * Constructor.
     * @param provider the Guice provider that gives access to shindig objects
     * @param applicationProperties the application properties to follow for gadget requests
     */
    public GadgetSpecFactoryImpl(Provider<Injector> provider, ApplicationProperties applicationProperties)
    {
        checkNotNull(provider, "provider");
        checkNotNull(applicationProperties, "applicationProperties");

        this.applicationProperties = applicationProperties;

        shindigFactory = provider.get().getInstance(org.apache.shindig.gadgets.GadgetSpecFactory.class);
        substituter = provider.get().getInstance(VariableSubstituter.class);
        gadgetFeatureRegistry = provider.get().getInstance(GadgetFeatureRegistry.class);
    }

    public GadgetSpec getGadgetSpec(final GadgetState gadgetState, final GadgetRequestContext gadgetRequestContext) throws GadgetParsingException
    {
        return getGadgetSpec(gadgetState.getGadgetSpecUri(), gadgetState.getUserPrefs(), gadgetRequestContext);
    }

    public GadgetSpec getGadgetSpec(URI uri, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException
    {
        return getGadgetSpec(uri, Collections.<String,String>emptyMap(), gadgetRequestContext);
    }

    private GadgetSpec getGadgetSpec(final URI specUri,
                                     final Map<String, String> userPrefs,
                                     final GadgetRequestContext gadgetRequestContext)
        throws GadgetParsingException
    {
        final URI absoluteSpecUri = Uri.resolveUriAgainstBase(applicationProperties.getBaseUrl(), specUri);
        GadgetContext gadgetContext = new GadgetContext()
        {
            @Override
            public URI getUrl()
            {
                return absoluteSpecUri;
            }

            @Override
            public boolean getIgnoreCache()
            {
                return gadgetRequestContext.getIgnoreCache();
            }

            @Override
            public RenderingContext getRenderingContext()
            {
                return RenderingContext.CONTAINER;
            }

            @Override
            public UserPrefs getUserPrefs()
            {
                return new UserPrefs(userPrefs);
            }

            @Override
            public Locale getLocale()
            {
                if (gadgetRequestContext.getLocale() != null)
                {
                    return gadgetRequestContext.getLocale();
                }
                return new Locale("");
            }
            
            @Override
            public boolean getDebug()
            {
                return gadgetRequestContext.isDebuggingEnabled();
            }
        };

        try
        {
            org.apache.shindig.gadgets.spec.GadgetSpec shindigGadgetSpec = substituter.substitute(gadgetContext,
                shindigFactory.getGadgetSpec(gadgetContext));
            ModulePrefs prefs = shindigGadgetSpec.getModulePrefs();

            return GadgetSpec.gadgetSpec(specUri)
                .userPrefs(transform(shindigGadgetSpec.getUserPrefs(), UserPrefToUserPrefSpec.FUNCTION))
                .viewsNames(shindigGadgetSpec.getViews().keySet())
                .scrolling(prefs.getScrolling())
                .height(prefs.getHeight())
                .width(prefs.getWidth())
                .title(prefs.getTitle())
                .titleUrl(nullSafeToJavaUri(prefs.getTitleUrl()))
                .thumbnail(nullSafeToJavaUri(prefs.getThumbnail()))
                .author(prefs.getAuthor())
                .authorEmail(prefs.getAuthorEmail())
                .description(prefs.getDescription())
                .directoryTitle(prefs.getDirectoryTitle())
                .features(unmodifiableMap(transformValues(prefs.getFeatures(),
                                                          ShindigFeatureToFeature.FUNCTION)))
                .unsupportedFeatureNames(getUnsupportedFeatureNames(prefs))
                .build();
        }
        catch (GadgetException e)
        {
            logger.warn("Error occurred while retrieving gadget spec for " + specUri);
            if (logger.isDebugEnabled())
            {
                logger.warn("Full stack trace: ", e);
            }
            throw new GadgetParsingException(e);
        }
    }

    private Iterable<String> getUnsupportedFeatureNames(ModulePrefs prefs)
    {
        Collection<String> unsupportedFeatures = new LinkedList<String>();

        // first, get the required (not optional) features only
        Collection<String> requiredFeatures = new LinkedList<String>();
        Map<String, org.apache.shindig.gadgets.spec.Feature> shindigFeatures = prefs.getFeatures();
        for (Map.Entry<String, org.apache.shindig.gadgets.spec.Feature> shindigFeature : shindigFeatures.entrySet())
        {
            if (shindigFeature.getValue().getRequired())
            {
                requiredFeatures.add(shindigFeature.getKey());
            }
        }

        gadgetFeatureRegistry.getFeatures(requiredFeatures, unsupportedFeatures);
        return unmodifiableCollection(unsupportedFeatures);
    }

    private URI nullSafeToJavaUri(org.apache.shindig.common.uri.Uri shindigUri)
    {
        if (shindigUri != null)
        {
            return shindigUri.toJavaUri();
        }
        return null;
    }

    private static enum ShindigFeatureToFeature implements Function<org.apache.shindig.gadgets.spec.Feature, Feature>
    {
        FUNCTION;

        public Feature apply(org.apache.shindig.gadgets.spec.Feature feature)
        {
            return new FeatureImpl(feature);
        }
    }

    private static enum UserPrefToUserPrefSpec implements Function<UserPref, UserPrefSpec>
    {
        FUNCTION;

        public UserPrefSpec apply(UserPref userPref)
        {
            // Using a LinkedHashMap to preserve the order the values were defined in when iterating.
            Map<String, String> enumValues = new LinkedHashMap<String, String>();
            for (UserPref.EnumValuePair enumValue : userPref.getOrderedEnumValues())
            {
                enumValues.put(enumValue.getValue(), enumValue.getDisplayValue());
            }

            return UserPrefSpec.userPrefSpec(userPref.getName())
                .displayName((userPref.getDisplayName() != null) ? userPref.getDisplayName() : userPref.getName())
                .required(userPref.getRequired())
                .dataType(DataType.parse(userPref.getDataType().toString()))
                .enumValues(Collections.unmodifiableMap(enumValues))
                .defaultValue(userPref.getDefaultValue())
                .build();

        }
    }
}
