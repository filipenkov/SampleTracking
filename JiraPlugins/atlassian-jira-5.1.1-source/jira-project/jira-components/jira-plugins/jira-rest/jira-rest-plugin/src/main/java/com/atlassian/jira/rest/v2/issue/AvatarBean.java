package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name = "avatar")
public class AvatarBean
{

    /**
     * Avatar bean example used in auto-generated documentation.
     */
    public static final AvatarBean DOC_EXAMPLE;
    public static final AvatarBean DOC_EXAMPLE_2;
    public static final Map<String, List<AvatarBean>> DOC_EXAMPLE_LIST;
    public static final Map<String, List<AvatarBean>> DOC_EXAMPLE_SYSTEM_LIST;

    static
    {
        AvatarBean avatar = new AvatarBean();
        avatar.id = "1000";
        avatar.owner = "fred";
        avatar.isSystemAvatar = true;
        avatar.isSelected = true;
        DOC_EXAMPLE = avatar;
        avatar = new AvatarBean();
        avatar.id = "1010";
        avatar.owner = "andrew";
        avatar.isSystemAvatar = false;
        avatar.isSelected = false;
        DOC_EXAMPLE_2 = avatar;

        DOC_EXAMPLE_LIST = new HashMap<String, List<AvatarBean>>();
        DOC_EXAMPLE_LIST.put("system", Collections.singletonList(DOC_EXAMPLE));
        DOC_EXAMPLE_LIST.put("custom", Collections.singletonList(DOC_EXAMPLE_2));

        DOC_EXAMPLE_SYSTEM_LIST = new HashMap<String, List<AvatarBean>>();
        DOC_EXAMPLE_SYSTEM_LIST.put("system", Collections.singletonList(DOC_EXAMPLE));
    }

    @XmlElement
    private String id;

    @XmlElement
    private String owner;

    @XmlElement
    private boolean isSystemAvatar;

    @XmlElement
    private boolean isSelected;

    public AvatarBean() {}

    public AvatarBean(final String id, final String owner)
    {
        this.id = id;
        this.owner = owner;
        this.isSelected = false;
    }

    public String getId()
    {
        return id;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }

    public boolean getSystemAvatar()
    {
        return isSystemAvatar;
    }

    public String getOwner()
    {
        return owner;
    }
}
