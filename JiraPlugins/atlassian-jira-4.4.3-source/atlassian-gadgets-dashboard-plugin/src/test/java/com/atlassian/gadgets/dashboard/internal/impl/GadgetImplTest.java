package com.atlassian.gadgets.dashboard.internal.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.UserPrefSpec;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetState.gadget;
import static com.atlassian.gadgets.spec.GadgetSpec.gadgetSpec;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class GadgetImplTest
{
    private static final GadgetState gadgetState =
        gadget(GadgetId.valueOf("1")).specUri(URI.create("http://gadget/url")).build();

    GadgetSpec gadgetSpec;
    Gadget gadget;

    @Test(expected=IllegalStateException.class)
    public void assertThatTryingToGetTitleOfNonLoadedGadgetThrowsException()
    {
        gadget = new GadgetImpl(gadgetState, "something bad, oh no!");
        gadget.getTitle();
    }

    @Test(expected=IllegalStateException.class)
    public void assertThatTryingToGetHeightOfNonLoadedGadgetThrowsException()
    {
        gadget = new GadgetImpl(gadgetState, "something bad, oh no!");
        gadget.getHeight();
    }

    @Test(expected=IllegalStateException.class)
    public void assertThatTryingToGetWidthOfNonLoadedGadgetThrowsException()
    {
        gadget = new GadgetImpl(gadgetState, "something bad, oh no!");
        gadget.getWidth();
    }

    public void assertThatGetSameStateBackFromNonLoadedGadget()
    {
        gadget = new GadgetImpl(gadgetState, "something bad, oh no!");
        assertEquals(gadgetState, gadget.getState());
    }

    public void assertNonLoadedGadgetErrorMessageCorrect()
    {
        String errorMessage = "something bad, oh no!";
        gadget = new GadgetImpl(gadgetState, errorMessage);
        assertEquals(errorMessage, gadget.getErrorMessage());
    }

    public void assertNonLoadedGadgetKnowsItIsntLoaded()
    {
        gadget = new GadgetImpl(gadgetState, "something bad, oh no!");
        assertEquals(false, gadget.isLoaded());
    }

    @Test
    public void assertThatNonHiddenUserPrefsReturnsTrueWhenThereAreAMixOfHiddenAndNonHiddenUserPrefs()
    {
        gadgetSpec = gadgetSpec(gadgetState.getGadgetSpecUri())
            .userPrefs(ImmutableList.of(
                UserPrefSpec.userPrefSpec("pref1").dataType(DataType.STRING).defaultValue("defaultvalue1").displayName("Pref 1").required(true).build(),
                UserPrefSpec.userPrefSpec("pref2").dataType(DataType.HIDDEN).defaultValue("hiddenvalue").build(),
                UserPrefSpec.userPrefSpec("pref3").dataType(DataType.STRING).defaultValue("defaultvalue3").displayName("Pref 3").required(true).build(),
                UserPrefSpec.userPrefSpec("pref4").dataType(DataType.STRING).defaultValue("defaultvalue4").displayName("Pref 4").required(true).build()
                ))
            .build();
        gadget = new GadgetImpl(gadgetState, gadgetSpec);
        assertTrue("hasNonHiddenUserPrefs should return true for mix of hidden and non hidden user prefs", gadget.hasNonHiddenUserPrefs());
    }
    
    @Test
    public void assertThatNonHiddenUserPrefsReturnsFalseWhenThereAreOnlyHiddenUserPrefs()
    {
        gadgetSpec = gadgetSpec(gadgetState.getGadgetSpecUri())
            .userPrefs(ImmutableList.of(
                UserPrefSpec.userPrefSpec("pref1").dataType(DataType.HIDDEN).defaultValue("hiddenvalue1").build(),
                UserPrefSpec.userPrefSpec("pref2").dataType(DataType.HIDDEN).defaultValue("hiddenvalue2").build()
                ))
            .build();
        gadget = new GadgetImpl(gadgetState, gadgetSpec);
        assertFalse("hasNonHiddenUserPrefs should return false when there are only hidden user prefs", gadget.hasNonHiddenUserPrefs());
    }

    @Test
    public void assertThatNonHiddenUserPrefsReturnsTrueWhenThereAreOnlyNonHiddenUserPrefs()
    {
        gadgetSpec = gadgetSpec(gadgetState.getGadgetSpecUri())
            .userPrefs(ImmutableList.of(
                UserPrefSpec.userPrefSpec("pref1").defaultValue("defaultvalue1").displayName("Pref 1").required(true).build(),
                UserPrefSpec.userPrefSpec("pref2").defaultValue("defaultvalue2").displayName("Pref 2").required(true).build(),
                UserPrefSpec.userPrefSpec("pref3").defaultValue("defaultvalue3").displayName("Pref 3").required(true).build()
                ))
            .build();
        gadget = new GadgetImpl(gadgetState, gadgetSpec);
        assertTrue("hasNonHiddenUserPrefs should return true when there are only non hidden user prefs", gadget.hasNonHiddenUserPrefs());
    }
    
    @Test
    public void assertThatNonHiddenUserPrefsReturnsFalseWhenThereAreNoUserPrefs()
    {
        gadgetSpec = gadgetSpec(gadgetState.getGadgetSpecUri())
            .userPrefs(Collections.<UserPrefSpec>emptyList())
            .build();
        gadget = new GadgetImpl(gadgetState, gadgetSpec);
        assertFalse("hasNonHiddenUserPrefs should return false when there are no user prefs", gadget.hasNonHiddenUserPrefs());        
    }

    @RunWith(Theories.class)
    public static class GadgetImplTheories
    {
        @DataPoints public static final Color[] COLORS = Color.values();

        @Theory
        public void gadgetColorIsReadFromGadgetState(Color color) throws URISyntaxException
        {
            GadgetState gadgetState =
                gadget(GadgetId.valueOf("1")).specUri("http://example.org/gadget.xml").color(color).build();
            Gadget gadget = new GadgetImpl(gadgetState, gadgetSpec(gadgetState.getGadgetSpecUri()).build());
            assertThat(gadget.getColor(), is(color));
        }
    }
}
