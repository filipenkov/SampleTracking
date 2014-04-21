package com.atlassian.upm.rest.representations;

import com.atlassian.plugin.PluginRestartState;

final class RestartState
{
    public static String toString(PluginRestartState restartState)
    {
        switch (restartState)
        {
            case NONE:
                return null;
            default:
                return restartState.toString().toLowerCase();
        }
    }
}
