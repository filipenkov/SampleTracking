package com.atlassian.gadgets.dashboard.internal.rest.representations;

import com.atlassian.gadgets.dashboard.internal.impl.UserPrefImpl;
import com.atlassian.gadgets.spec.DataType;
import com.atlassian.gadgets.spec.UserPrefSpec;
import junit.framework.TestCase;

import java.util.Locale;

public class UserPrefRepresentationTest extends TestCase
{

    public static final String MY_PREF = "myPref";

    public void testUserPrefSpecTypeIsLowercasedCorrectlyWithTurkishLocaleAsDefault() throws Exception
    {
        Locale previous = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try
        {
            UserPrefSpec hiddenPref = UserPrefSpec.userPrefSpec(MY_PREF)
                    .dataType(DataType.HIDDEN)
                    .build();

            UserPrefRepresentation pref = new UserPrefRepresentation(new UserPrefImpl(hiddenPref, MY_PREF));
            assertEquals("preference data type should be 'hidden'", "hidden", pref.getType());
        }
        finally
        {
            Locale.setDefault(previous);
        }
    }
}
