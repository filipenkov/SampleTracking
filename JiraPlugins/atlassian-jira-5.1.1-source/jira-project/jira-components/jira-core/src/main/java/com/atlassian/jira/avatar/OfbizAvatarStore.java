package com.atlassian.jira.avatar;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.util.UserNames;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Store implementation for Avatars. Nearly all methods could throw a DataAccessException.
 *
 * @since v4.0
 */
public class OfbizAvatarStore implements AvatarStore
{

    static final String AVATAR_ENTITY = "Avatar";
    static final String ID = "id";
    static final String FILE_NAME = "fileName";
    static final String CONTENT_TYPE = "contentType";
    static final String AVATAR_TYPE = "avatarType";
    static final String OWNER = "owner";
    static final String SYSTEM_AVATAR = "systemAvatar";

    static final Integer IS_SYSTEM = 1;
    static final Integer NOT_SYSTEM = 0;

    private OfBizDelegator ofBizDelegator;

    public OfbizAvatarStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public Avatar getById(final Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);

        final GenericValue gv = ofBizDelegator.findById(AVATAR_ENTITY, avatarId);
        return gv == null ? null : gvToAvatar(gv);
    }

    public boolean delete(final Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);

        int numRemoved = ofBizDelegator.removeByAnd(AVATAR_ENTITY, EasyMap.build(ID, avatarId));
        return numRemoved != 0;
    }

    public void update(final Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Long avatarId = Assertions.notNull("avatar.id", avatar.getId());
        Assertions.notNull("avatar.fileName", avatar.getFileName());
        Assertions.notNull("avatar.contentType", avatar.getContentType());
        Assertions.notNull("avatar.avatarType", avatar.getAvatarType());
        Assertions.notNull("avatar.owner", avatar.getOwner());

        final GenericValue gv = ofBizDelegator.findById(AVATAR_ENTITY, avatarId);
        gv.setNonPKFields(getNonPkFields(avatar));
        try
        {
            gv.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Avatar create(final Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.stateTrue("avatar.id must be null", avatar.getId() == null);
        Assertions.notNull("avatar.fileName", avatar.getFileName());
        Assertions.notNull("avatar.contentType", avatar.getContentType());
        Assertions.notNull("avatar.avatarType", avatar.getAvatarType());

        return gvToAvatar(ofBizDelegator.createValue(AVATAR_ENTITY, getNonPkFields(avatar)));
    }

    public List<Avatar> getAllSystemAvatars(final Avatar.Type type)
    {
        return getAvatars(EasyMap.build(SYSTEM_AVATAR, IS_SYSTEM, AVATAR_TYPE, type.getName()));
    }

    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
    {
        Assertions.notNull("type", type);
        Assertions.notNull("ownerId", ownerId);

        return getAvatars(EasyMap.build(SYSTEM_AVATAR, NOT_SYSTEM, AVATAR_TYPE, type.getName(), OWNER, UserNames.toKey(ownerId)));
    }

    List<Avatar> getAvatars(final Map constraint)
    {
        ArrayList<Avatar> systemAvatars = new ArrayList<Avatar>();
        for (GenericValue gv : ofBizDelegator.findByAnd(AVATAR_ENTITY, constraint))
        {
            systemAvatars.add(gvToAvatar(gv));
        }
        return systemAvatars;
    }

    private Map getNonPkFields(Avatar avatar)
    {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FILE_NAME, avatar.getFileName());
        fields.put(CONTENT_TYPE, avatar.getContentType());
        fields.put(AVATAR_TYPE, avatar.getAvatarType().getName());
        fields.put(OWNER, UserNames.toKey(avatar.getOwner()));
        fields.put(SYSTEM_AVATAR, avatar.isSystemAvatar() ? IS_SYSTEM : NOT_SYSTEM);
        return fields;
    }

    Avatar gvToAvatar(final GenericValue gv)
    {
        return new AvatarImpl(gv.getLong(ID),
                gv.getString(FILE_NAME),
                gv.getString(CONTENT_TYPE),
                Avatar.Type.getByName(gv.getString(AVATAR_TYPE)),
                gv.getString(OWNER),
                gv.getInteger(SYSTEM_AVATAR) != 0);
    }
}
