package com.atlassian.jira.plugin.profile;

import java.util.Map;

/**
 * Provides a format to display a user on screen.  This may be simply the user's full name surrounded by
 * a link to the user's profile, or something more advanced such as the user's name and profile picture.
 *
 * @see com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor
 * @see com.atlassian.jira.plugin.userformat.UserFormats
 * @since v3.13
 */
public interface UserFormat
{
    /**
     * The default velocity template name that is used to render the view.
     */
    public static final String VIEW_TEMPLATE = "view";

    /**
     * Renders the formatted user for a given section on the screen. Please note that the
     * username may be null (eg for empty fields, anonymous user's, etc) or the user may not exist.  Implementations
     * should handle these cases.
     * <p/>
     * Extra context can be passed to the renderer via the id attribute.  Ideally an implementation might include
     * this id in the rendered output such that it can be used for test assertions.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param username the user's username to format.
     * @param id       A value providing extra context to the rendering process.
     * @return the formatted user
     */
    String format(String username, String id);

    /**
     * Renders the formatted user for a given section on the screen. Please note that the
     * username may be null (eg for empty fields, anonymous user's, etc) or the user may not exist.  Implementations
     * should handle these cases.
     * <p/>
     * Extra context can be passed to the renderer via the params attribute.
     * <p/>
     * The result should always be ready to display in an HTML response i.e. it should be HTML encoded if necessary.
     *
     * @param username the user's username to format.
     * @param id       A value providing extra context to the rendering process.
     * @param params   Additional context to pass to the renderer
     * @return the formatted user
     */
    String format(String username, String id, Map<String, Object> params);
}
