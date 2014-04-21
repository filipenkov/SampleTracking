/*
 * Atlassian Source Code Template.
 * User: mike
 * Date: Dec 22, 2001
 * Time: 3:06:17 PM
 * CVS Revision: $Revision: 1.1 $
 * Last CVS Commit: $Date: 2002/12/02 13:20:59 $
 * Author of last CVS Commit: $Author: mike $
 */
package com.atlassian.core;

import org.apache.commons.lang.exception.NestableException;

///CLOVER:OFF

public class AtlassianCoreException extends NestableException
{
    public AtlassianCoreException()
    {
    }

    public AtlassianCoreException(String s)
    {
        super(s);
    }

    public AtlassianCoreException(Throwable throwable)
    {
        super(throwable);
    }

    public AtlassianCoreException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
