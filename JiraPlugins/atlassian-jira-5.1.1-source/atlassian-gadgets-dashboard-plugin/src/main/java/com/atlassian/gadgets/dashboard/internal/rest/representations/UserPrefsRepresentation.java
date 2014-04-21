package com.atlassian.gadgets.dashboard.internal.rest.representations;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.dashboard.internal.UserPref;

import com.google.common.base.Function;

import static com.atlassian.plugin.util.Assertions.notNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Provides a JAXB view of a collection of all {@code UserPref}s for a {@code GadgetRepresentation}
 */
@XmlRootElement
public final class UserPrefsRepresentation
{
    /**
     * Represents REST resource for updating userprefs for this gadget.
     */
    @XmlElement
    private final String action;
    @XmlElement
    private final List<UserPrefRepresentation> fields;

    // Provided for JAXB.
    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private UserPrefsRepresentation()
    {
        action = null;
        fields = new ArrayList<UserPrefRepresentation>();
    }

    public UserPrefsRepresentation(final Iterable<UserPref> prefs, final String actionUrl)
    {
        notNull("prefs", prefs);
        notNull("actionUrl", actionUrl);

        this.action = actionUrl;
        this.fields = transformCollectionUserPrefsToNameStrings(prefs);
    }

    /**
     * Trade a Collection of UserPref objects for a Map of Strings that are the names and Values of those prefs.
     *
     * @param userPrefs the collection of userprefs
     * @return the transformed map of userprefs
     */
    private List<UserPrefRepresentation> transformCollectionUserPrefsToNameStrings(Iterable<UserPref> userPrefs)
    {
        return newArrayList(transform(userPrefs, new Function<UserPref, UserPrefRepresentation>()
        {
            public UserPrefRepresentation apply(@Nullable final UserPref userPref)
            {
                return new UserPrefRepresentation(userPref);
            }
        }));
    }

    public String getAction()
    {
        return action;
    }

    public List<UserPrefRepresentation> getFields()
    {
        return fields;
    }  
}
