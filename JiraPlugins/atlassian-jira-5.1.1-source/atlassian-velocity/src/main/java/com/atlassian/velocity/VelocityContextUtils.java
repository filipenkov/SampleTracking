package com.atlassian.velocity;

import com.opensymphony.util.TextUtils;

import java.util.Map;

public class VelocityContextUtils
{
    private static final TextUtils TEXT_UTILS = new TextUtils();
    private static final VelocityHelper VELOCITY_HELPER = new VelocityHelper();

    /**
     * Add context params needed by the body of common messages
     */
    public static Map getContextParamsBody(Map contextParams)
    {
        contextParams.put("velocityhelper", VELOCITY_HELPER);
        contextParams.put("textutils", TEXT_UTILS);

        return contextParams;
    }
}
