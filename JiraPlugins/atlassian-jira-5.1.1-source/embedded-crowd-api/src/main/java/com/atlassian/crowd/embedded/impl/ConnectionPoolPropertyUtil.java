package com.atlassian.crowd.embedded.impl;

import com.google.common.collect.ImmutableList;

import java.util.Set;

public class ConnectionPoolPropertyUtil
{
    public static boolean isValidProtocol(String userInput)
    {
        return isValidEntry(userInput, ConnectionPoolPropertyConstants.VALID_PROTOCOL_TYPES);
    }

    public static boolean isValidAuthentication(String userInput)
    {
        return isValidEntry(userInput, ConnectionPoolPropertyConstants.VALID_AUTHENTICATION_TYPES);
    }

    private static boolean isValidEntry(String userInput, Set<String> validValues)
    {
        // We expect the input values to be space separated
        return validValues.containsAll(ImmutableList.of(userInput.split(" ")));
    }
}
