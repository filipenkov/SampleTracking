package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestUpgradeTask_Build608
{
    @Test
    public void testMetaData()
    {
        final UpgradeTask_Build608 upgradeTask = new UpgradeTask_Build608(null, null, null, null);
        assertEquals("608", upgradeTask.getBuildNumber());
        assertEquals("Updating system user avatars.", upgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        final AvatarManager mockAvatarManager = createMock(AvatarManager.class);
        final OfBizDelegator mockOfBizDelegator = createMock(OfBizDelegator.class);
        final UserPropertyManager mockUserPropertyManager = createMock(UserPropertyManager.class);

        final Avatar mockAvatar = createMock(Avatar.class);
        final Avatar mockCreatedAvatar = createMock(Avatar.class);
        final Avatar mockAnonymousAvatar = createMock(Avatar.class);
        expect(mockCreatedAvatar.getId()).andReturn(9999L).times(2);
        expect(mockAvatar.getId()).andReturn(12323L);
        expect(mockAnonymousAvatar.getId()).andReturn(7777L);
        expect(mockAvatarManager.getAllSystemAvatars(Avatar.Type.USER)).andReturn(list(mockAvatar));

        for (int i = 1; i <= 22; i++)
        {
            expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-" + i + ".png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        }

        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-default.png", "image/png", Avatar.Type.USER))).andReturn(mockCreatedAvatar);
        mockApplicationProperties.setString("jira.avatar.user.default.id", 9999L + "");

        expect(mockAvatarManager.create(AvatarImpl.createSystemAvatar("Avatar-unknown.png", "image/png", Avatar.Type.USER))).andReturn(mockAnonymousAvatar);
        mockApplicationProperties.setString("jira.avatar.user.anonymous.id", 7777L + "");

        final EntityCondition entityCondition = new MockEntityConditionList(Arrays.asList(new MockEntityExpr("propertyKey", EntityOperator.EQUALS, "user.avatar.id"),
                new MockEntityExpr("propertyValue", EntityOperator.IN, list(12323L))), EntityOperator.AND);
        final GenericValue mockResultGv = new MockGenericValue("OSUserPropertySetNumberView", MapBuilder.singletonMap("id", 45678L));
        expect(mockOfBizDelegator.findByCondition("OSUserPropertySetNumberView", entityCondition, list("id"))).andReturn(list(mockResultGv));
        expect(mockOfBizDelegator.bulkUpdateByPrimaryKey("OSPropertyNumber", MapBuilder.singletonMap("value", 9999L), list(45678L))).andReturn(1);

        expect(mockAvatarManager.delete(12323L)).andReturn(true);

        replay(mockApplicationProperties, mockAvatarManager, mockAvatar, mockCreatedAvatar, mockAnonymousAvatar, mockOfBizDelegator, mockUserPropertyManager);

        final UpgradeTask_Build608 upgradeTask = new UpgradeTask_Build608(mockAvatarManager, mockApplicationProperties, mockOfBizDelegator, mockUserPropertyManager);

        upgradeTask.doUpgrade(false);

        verify(mockApplicationProperties, mockAvatarManager, mockAvatar, mockCreatedAvatar, mockAnonymousAvatar, mockOfBizDelegator, mockUserPropertyManager);
    }
}
