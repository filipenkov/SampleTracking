package com.atlassian.jira.servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper that gets "browse project" URL details like version, component and avatar ids from path info.
 *
 * @since 3.10
 */
final class BrowseProjectUrlHelper
{
    /** Identifies browse fix-for version requests. */
    private static final String VERSION = "/fixforversion/";

    /** Identifies browse component requests. */
    private static final String COMPONENT = "/component/";

    /**
     * Special value to indicate no specific avatar requested, just whatever is current for the project.
     */
    static final long CURRENT_PROJECT_AVATAR = -1L;

    /**
     * Captures a request for a project avatar image 0 or 1 group. 0 groups indicates a current project icon URL,
     * 1 group captures the avatar id.
     */
    private static final Pattern PROJECT_AVATAR = Pattern.compile(".*\\/icon\\/(?:(\\d+)_[^/]+)?");

    /**
     * A string to identify icon requests.
     */
    private static final String AVATAR = "/icon/";

    private final String pathInfo;
    private final String projectKey;
    private final Long componentId;
    private final Long versionId;

    /**
     * Holds the id of the project avatar, null if no avatar was requested, {@link #CURRENT_PROJECT_AVATAR} if
     * the request is for the currently configured project avatar.
     */
    private final Long projectAvatarId;

    /**
     * Extracts project key, version id and
     * component id and stores it locally, so they can be accessed via
     * {@link #getProjectKey()}, {@link #getVersionId()} and
     * {@link #getComponentId()} methods.
     *
     * @param pathInfo path info from request
     * @throws IllegalArgumentException if given pathInfo is null or less than 2 characters
     */
    public BrowseProjectUrlHelper(String pathInfo)
    {
        if (pathInfo == null || pathInfo.length() < 2)
        {
            throw new IllegalArgumentException("path is invalid");
        }
        this.pathInfo = pathInfo;
        this.componentId = findIdFor(COMPONENT);
        this.versionId = findIdFor(VERSION);
        this.projectAvatarId = findProjectAvatarId();
        this.projectKey = extractProjectKey();
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public Long getComponentId()
    {
        return componentId;
    }

    public Long getProjectAvatarId()
    {
        return projectAvatarId;
    }

    Long findProjectAvatarId()
    {
        Long avatarId = null;
        final Matcher matcher = PROJECT_AVATAR.matcher(pathInfo);
        if (matcher.matches())
        {
            if (matcher.groupCount() == 1)
            {
                // requesting a specific project avatar by id
                try
                {
                    avatarId = new Long(matcher.group(1));
                }
                catch (NumberFormatException e)
                {
                    avatarId = CURRENT_PROJECT_AVATAR;
                }
            }
            else
            {
                avatarId = CURRENT_PROJECT_AVATAR; // indicates current project avatar
            }
        }
        return avatarId;
    }

    /**
     * Extracts project key
     *
     * @return project key
     */
    private String extractProjectKey()
    {
        // remove starting slash
        String path = pathInfo.substring(1, pathInfo.length());
        // look for the starting position of each of the possible followers of /PROJECTKEY
        String[] browseSubparts = new String[] { VERSION, COMPONENT, AVATAR };
        for (String browseSubpart : browseSubparts)
        {
            final int index = path.indexOf(browseSubpart);
            if (index >= 0)
            {
                return path.substring(0, index);
            }
        }
        return path;
    }

    private Long findIdFor(String foo)
    {
        int versionIndex = pathInfo.indexOf(foo);
        if (versionIndex < 0)
        {
            return null;
        }
        else
        {
            try
            {
                return new Long(pathInfo.substring(versionIndex + foo.length()));
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }
}
