package com.atlassian.gadgets.dashboard.internal.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.UserPref;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.UserPrefSpec;
import com.atlassian.gadgets.view.ViewType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.gadgets.GadgetState.gadget;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.transform;

/**
 * Basic {@code Gadget} implementation.
 */
public final class GadgetImpl implements Gadget
{
    private final GadgetId id;
    private final URI storedSpecUri;
    private final GadgetSpec gadgetSpec;
    
    private Color color;
    private Iterable<UserPref> userPrefs;
    private String errorMessage;
    private GadgetState stateOnLoadAttempt;

    /**
     * Constructor.
     * @param state the {@code GadgetState} for this instance
     * @param gadgetSpec the Shindig {@code GadgetSpec} to build this instance from
     */
    public GadgetImpl(GadgetState state, GadgetSpec gadgetSpec)
    {
        this.id = state.getId();
        this.storedSpecUri = state.getGadgetSpecUri();
        this.gadgetSpec = gadgetSpec;
        this.color = state.getColor();
        this.errorMessage = null;
        this.stateOnLoadAttempt = null;

        // transform the user prefs from spec to changeable objects
        if (gadgetSpec.getUserPrefs() != null)
        {
            this.userPrefs = transform(gadgetSpec.getUserPrefs(), toUserPrefs(state.getUserPrefs()));
        }
        else
        {
            this.userPrefs = ImmutableList.of();
        }
    }

    /**
     * Constructor used by the dashboard when there is an error loading the gadget.
     * @param stateOnLoadAttempt the {@code GadgetState} when the gadget failed to load
     * @param errorMessage the error message explaining why the gadget failed to load
     */
    public GadgetImpl(GadgetState stateOnLoadAttempt, String errorMessage)
    {
        this.id = stateOnLoadAttempt.getId();
        this.storedSpecUri = stateOnLoadAttempt.getGadgetSpecUri();
        this.errorMessage = errorMessage;
        this.stateOnLoadAttempt = stateOnLoadAttempt;
        this.gadgetSpec = null;
    }
    
    public GadgetId getId()
    {
        return id;
    }

    public String getTitle()
    {
        checkLoaded();
        return gadgetSpec.getTitle();
    }

    public URI getTitleUrl()
    {
        checkLoaded();
        return gadgetSpec.getTitleUrl();
    }

    public String getGadgetSpecUrl()
    {
        return storedSpecUri.toASCIIString();
    }

    public Integer getHeight()
    {
        checkLoaded();
        return gadgetSpec.getHeight() != 0 ? gadgetSpec.getHeight() : null;
    }

    public Integer getWidth()
    {
        checkLoaded();
        return gadgetSpec.getWidth() != 0 ? gadgetSpec.getWidth() : null;
    }

    public Color getColor()
    {
        return color;
    }
    
    public boolean isMaximizable()
    {
        checkLoaded();
        return gadgetSpec.supportsViewType(ViewType.CANVAS);
    }

    public boolean hasNonHiddenUserPrefs()
    {
        checkLoaded();
        return any(userPrefs, isNotHidden);
    }

    private static final Predicate<UserPref> isNotHidden = new Predicate<UserPref>()
    {
        public boolean apply(UserPref userPref)
        {
            return !(DataType.HIDDEN.equals(userPref.getDataType()));
        }
    };

    public Iterable<UserPref> getUserPrefs()
    {
        checkLoaded();
        return userPrefs;
    }

    public GadgetState getState()
    {
        if (isLoaded())
        {
            return gadget(id)
                .specUri(storedSpecUri)
                .color(color)
                .userPrefs(createStateFrom(userPrefs))
                .build();
        }

        return stateOnLoadAttempt;
    }

    private Map<String, String> createStateFrom(Iterable<UserPref> userPrefs)
    {
        Map<String, String> userPrefValues = new HashMap<String, String>();
        for (UserPref userPref : userPrefs)
        {
            // (AG-136) don't put required prefs without values into states,
            // since they'll trigger errors in Gadget.updateUserPrefs
            if (!userPref.isRequired() || StringUtils.isNotBlank(userPref.getValue()))
            {
                userPrefValues.put(userPref.getName(), userPref.getValue());
            }
        }
        return userPrefValues;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public boolean isLoaded()
    {
        return gadgetSpec != null;
    }

    /**
     * @throws IllegalStateException if this gadget isn't loaded
     */
    private void checkLoaded()
    {
        if (!isLoaded())
        {
            throw new IllegalStateException("gadget could not be loaded");
        }
    }

    private Function<UserPrefSpec, UserPref> toUserPrefs(Map<String, String> userPrefValues)
    {
        return new UserPrefSpecToUserPref(userPrefValues);
    }

    private static final class UserPrefSpecToUserPref implements Function<UserPrefSpec, UserPref>
    {
        private final Map<String, String> userPrefValues;

        public UserPrefSpecToUserPref(Map<String, String> userPrefValues)
        {
            this.userPrefValues = userPrefValues;
        }

        public UserPref apply(UserPrefSpec userPrefSpec)
        {
            return new UserPrefImpl(userPrefSpec, userPrefValues.get(userPrefSpec.getName()));
        }
    }
}
