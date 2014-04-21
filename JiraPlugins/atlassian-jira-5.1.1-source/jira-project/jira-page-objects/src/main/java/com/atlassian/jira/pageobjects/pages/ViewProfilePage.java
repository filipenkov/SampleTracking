package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 *
 * @since v5.1
 */
public class ViewProfilePage extends AbstractJiraPage
{
    @ElementBy (id = "up-user-title")
    private PageElement userTitle;

    @ElementBy (id = "user_avatar_image")
    private PageElement userAvatarImage;

    @Override
    public TimedCondition isAt()
    {
        return userTitle.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/ViewProfile.jspa";
    }

    public UserAvatarDialog userAvatar() {
        userAvatarImage.click();
        return pageBinder.bind(UserAvatarDialog.class);
    }
}
