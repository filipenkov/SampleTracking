package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.plugin.rest.entity.MultiValuedAttributeEntity;
import com.atlassian.crowd.plugin.rest.entity.MultiValuedAttributeEntityList;
import com.atlassian.crowd.plugin.rest.entity.GroupEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URI;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator}.
 *
 * @since v2.1
 */
public class EntityTranslatorTest
{
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "First";
    private static final String LAST_NAME = "Last";
    private static final String DISPLAY_NAME = "First Last";
    private static final String EMAIL = "email@email.com";

    private static final String GROUP_NAME = "groupName";
    private static final String DESCRIPTION = "description";
    private static final GroupType GROUP_TYPE = GroupType.GROUP;

    private static final boolean ACTIVE = true;

    private static final int NUM_ATTRIBUTES = 2;

    private static final String ATTR1_KEY = "key1";
    private static final String ATTR2_KEY = "key2";

    private static final String ATTR1_VALUE = "val1";
    private static final String ATTR2_VALUE = "val2";

    @Mock private UserWithAttributes user;
    @Mock private GroupWithAttributes group;
    @Mock private Link link;

    @Before
    public void setUp()
    {
        initMocks(this);
        when(user.getName()).thenReturn(USERNAME);
        when(user.getFirstName()).thenReturn(FIRST_NAME);
        when(user.getLastName()).thenReturn(LAST_NAME);
        when(user.getEmailAddress()).thenReturn(EMAIL);
        when(user.getDisplayName()).thenReturn(DISPLAY_NAME);
        when(user.isActive()).thenReturn(ACTIVE);
        when(user.getKeys()).thenReturn(Sets.newHashSet(ATTR1_KEY, ATTR2_KEY));
        when(user.getValues(ATTR1_KEY)).thenReturn(Sets.newHashSet(ATTR1_VALUE));
        when(user.getValues(ATTR2_KEY)).thenReturn(Sets.newHashSet(ATTR2_VALUE));

        when(group.getName()).thenReturn(GROUP_NAME);
        when(group.getDescription()).thenReturn(DESCRIPTION);
        when(group.getType()).thenReturn(GROUP_TYPE);
        when(group.isActive()).thenReturn(ACTIVE);
        when(group.getKeys()).thenReturn(Sets.newHashSet(ATTR1_KEY, ATTR2_KEY));
        when(group.getValues(ATTR1_KEY)).thenReturn(Sets.newHashSet(ATTR1_VALUE));
        when(group.getValues(ATTR2_KEY)).thenReturn(Sets.newHashSet(ATTR2_VALUE));

        when(link.getHref()).thenReturn(URI.create("/path"));
    }

    @After
    public void tearDown()
    {
        user = null;
        link = null;
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator#toUserEntity(com.atlassian.crowd.model.user.User, com.atlassian.plugins.rest.common.Link)}
     * creates a UserEntity.
     * 
     * @throws Exception
     */
    @Test
    public void testToUserEntity() throws Exception
    {
        UserEntity userEntity = EntityTranslator.toUserEntity(user, link);
        assertNotNull(userEntity);
        assertEquals(USERNAME, userEntity.getName());
        assertEquals(FIRST_NAME, userEntity.getFirstName());
        assertEquals(LAST_NAME, userEntity.getLastName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(DISPLAY_NAME, userEntity.getDisplayName());
        assertEquals(ACTIVE, userEntity.isActive());
        assertNotNull(userEntity.getAttributes());
        assertTrue(userEntity.getAttributes().isEmpty());
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator#toUserEntity(com.atlassian.crowd.model.user.User, com.atlassian.crowd.embedded.api.Attributes, com.atlassian.plugins.rest.common.Link)}
     * creates a UserEntity with attributes included.
     *
     * @throws Exception
     */
    @Test
    public void testToUserEntity_WithAttributes() throws Exception
    {
        UserEntity userEntity = EntityTranslator.toUserEntity(user, user, link);
        assertNotNull(userEntity);
        assertEquals(USERNAME, userEntity.getName());
        assertEquals(FIRST_NAME, userEntity.getFirstName());
        assertEquals(LAST_NAME, userEntity.getLastName());
        assertEquals(EMAIL, userEntity.getEmail());
        assertEquals(DISPLAY_NAME, userEntity.getDisplayName());
        assertEquals(ACTIVE, userEntity.isActive());
        assertNotNull(userEntity.getAttributes());
        assertEquals(NUM_ATTRIBUTES, userEntity.getAttributes().size());
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator#toGroupEntity(com.atlassian.crowd.model.group.Group, com.atlassian.plugins.rest.common.Link)}
     * creates a GroupEntity.
     *
     * @throws Exception
     */
    @Test
    public void testToGroupEntity() throws Exception
    {
        GroupEntity groupEntity = EntityTranslator.toGroupEntity(group, link);
        assertNotNull(groupEntity);
        assertEquals(GROUP_NAME, groupEntity.getName());
        assertEquals(DESCRIPTION, groupEntity.getDescription());
        assertEquals(GROUP_TYPE, groupEntity.getType());
        assertEquals(ACTIVE, groupEntity.isActive());
        assertNotNull(groupEntity.getAttributes());
        assertTrue(groupEntity.getAttributes().isEmpty());
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator#toGroupEntity(com.atlassian.crowd.model.group.Group, com.atlassian.crowd.embedded.api.Attributes, com.atlassian.plugins.rest.common.Link)}
     * creates a GroupEntity with attributes included.
     *
     * @throws Exception
     */
    @Test
    public void testToGroupEntity_WithAttributes() throws Exception
    {
        GroupEntity groupEntity = EntityTranslator.toGroupEntity(group, group, link);
        assertNotNull(groupEntity);
        assertEquals(GROUP_NAME, groupEntity.getName());
        assertEquals(DESCRIPTION, groupEntity.getDescription());
        assertEquals(GROUP_TYPE, groupEntity.getType());
        assertEquals(ACTIVE, groupEntity.isActive());
        assertNotNull(groupEntity.getAttributes());
        assertEquals(NUM_ATTRIBUTES, groupEntity.getAttributes().size());
    }

    /**
     * Tests that {@link com.atlassian.crowd.plugin.rest.util.EntityTranslator#toMultiValuedAttributeEntityList(com.atlassian.crowd.embedded.api.Attributes, com.atlassian.plugins.rest.common.Link)}
     * creates an AttributeEntityList.
     *
     * @throws Exception
     */
    @Test
    public void testToAttributeEntityList() throws Exception
    {
        MultiValuedAttributeEntityList attributeEntityList = EntityTranslator.toMultiValuedAttributeEntityList(user, link);
        assertNotNull(attributeEntityList);
        assertEquals(NUM_ATTRIBUTES, attributeEntityList.size());

        Set<String> keys = Sets.newHashSet();
        for (MultiValuedAttributeEntity attributeEntity : attributeEntityList)
        {
            keys.add(attributeEntity.getName());
        }
        assertEquals(Sets.<String>newHashSet(ATTR1_KEY, ATTR2_KEY), keys);
    }
}
