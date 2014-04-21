/*
 * Created on Nov 17, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.atlassian.spring.container;

public class ComponentNotFoundException extends RuntimeException
{
    public ComponentNotFoundException(String message)
    {
        super(message);
    }

    public ComponentNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
