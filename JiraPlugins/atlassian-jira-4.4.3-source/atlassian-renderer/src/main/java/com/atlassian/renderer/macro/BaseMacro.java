/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 11, 2004
 * Time: 6:28:11 PM
 */
package com.atlassian.renderer.macro;

public abstract class BaseMacro extends org.radeox.macro.BaseMacro implements Macro
{
    String description;
    String resourcePath;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getResourcePath()
    {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath)
    {
        this.resourcePath = resourcePath;
    }
 }