package com.atlassian.jira.lookandfeel;

/**
 * Defines the choices a user has for uploading a logo
 * and a favicon in the {@link com.atlassian.jira.lookandfeel.EditLookAndFeel}
 *
 * @author jwilson
 */
public enum LogoChoice
{
    JIRA,
    UPLOAD,
    URL;

    public static LogoChoice safeValueOf(String value) {

        value = value.toUpperCase();

        try
        {
            return LogoChoice.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }

    }
}
