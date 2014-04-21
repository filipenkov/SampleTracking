/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 21/06/2002
 * Time: 13:33:21
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import com.atlassian.core.AtlassianCoreException;

///CLOVER:OFF

public class InvalidDurationException extends AtlassianCoreException
{
    public InvalidDurationException()
    {
    }

    public InvalidDurationException(String msg)
    {
        super(msg);
    }

    public InvalidDurationException(Exception e)
    {
        super(e);
    }

    public InvalidDurationException(String msg, Exception e)
    {
        super(msg, e);
    }
}
