package com.atlassian.jira.avatar;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * A container for constants for a specific sized avatar.
 *
 * @since v4.0
 */
public final class AvatarSize
{
    final private int pixels;
    final private String filenameFlag;
    private Selection originSelection;

    AvatarSize(final int pixels, final String filenameFlag)
    {
        this.pixels = pixels;
        this.filenameFlag = Assertions.notNull("filenameFlag", filenameFlag);
        this.originSelection = new Selection(0, 0, pixels, pixels);
    }

    public String getFilenameFlag()
    {
        return filenameFlag;
    }

    public int getPixels()
    {
        return pixels;
    }

    public Selection originSelection() {
        return originSelection;
    }
}
