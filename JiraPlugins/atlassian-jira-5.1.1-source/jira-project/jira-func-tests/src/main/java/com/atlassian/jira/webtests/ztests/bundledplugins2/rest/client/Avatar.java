package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;

import java.util.List;
import java.util.Map;

/**
 * A represenation of an avatar bean in the REST API
 *
 * @since v5.0
 */
public class Avatar
{
    static GenericType<Map<String, List<Avatar>>> ALLAVATARS_TYPE = new GenericType<Map<String, List<Avatar>>>()
    {
    };
    static GenericType<Avatar> AVATAR_TYPE = new GenericType<Avatar>()
    {
    };

    private Long id;
    private String owner;
    private boolean isSelected;



    private boolean selected;
    private boolean isSystemAvatar;

    public boolean getIsSystemAvatar() {
        return isSystemAvatar;
    }

    public void setIsSystemAvatar(boolean systemAvatar) {
        isSystemAvatar = systemAvatar;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getId()
    {
        return id;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean selected) {
        isSelected = selected;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }


    public String getOwner()
    {
        return owner;
    }


}
