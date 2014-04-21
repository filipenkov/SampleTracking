package com.atlassian.velocity;

import com.atlassian.core.user.UserUtils;
import com.opensymphony.util.TextUtils;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Owen
 * Date: 19/11/2003
 * Time: 11:28:02
 * To change this template use Options | File Templates.
 */
public class VelocityContextUtils
{
    private static final UserUtils USER_UTILS = new UserUtils();
    private static final TextUtils TEXT_UTILS = new TextUtils();
    private static final VelocityHelper VELOCITY_HELPER = new VelocityHelper();

    /**
     * Add context params needed by the body of common messages
     */
    public static Map getContextParamsBody(Map contextParams)
    {
        contextParams.put("userutils", USER_UTILS);
        contextParams.put("velocityhelper", VELOCITY_HELPER);
        contextParams.put("textutils", TEXT_UTILS);

        return contextParams;
    }
}
