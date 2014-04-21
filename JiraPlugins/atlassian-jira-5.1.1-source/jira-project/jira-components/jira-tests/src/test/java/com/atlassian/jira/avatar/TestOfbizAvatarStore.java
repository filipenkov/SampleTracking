package com.atlassian.jira.avatar;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for {@link com.atlassian.jira.avatar.OfbizAvatarStore}.
 *
 * @since v4.0
 */
public class TestOfbizAvatarStore extends MockControllerTestCase
{
    private OfBizDelegator mockOfBizDelegator;

    @Before
    public void setUp() throws Exception
    {
        mockOfBizDelegator = getMock(OfBizDelegator.class);
    }

    @Test
    public void testGetById()
    {
        GenericValue lookedUpGv = new MockGenericValue("pingpong", EasyMap.build("table", "tennis"));
        expect(mockOfBizDelegator.findById("Avatar", 90909L)).andReturn(lookedUpGv);

        replay();
        final AtomicReference<GenericValue> getByIdArg = new AtomicReference<GenericValue>(null);
        final AvatarImpl a = new AvatarImpl(90909L, "foo.png", "image/png", Avatar.Type.PROJECT, "domo kun", false);
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator)
        {
            AvatarImpl gvToAvatar(final GenericValue gv)
            {
                getByIdArg.set(gv);
                return a;
            }
        };
        store.getById(90909L);
        assertEquals(lookedUpGv, getByIdArg.get());
    }

    @Test
    public void testGetByIdNull() {
        expect(mockOfBizDelegator.findById("Avatar", 303L)).andReturn(null);
        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        assertNull(store.getById(303L));
    }

    @Test
    public void testDeleteSucceed()
    {
        final Map fields = EasyMap.build("id", 49L);
        expect(mockOfBizDelegator.removeByAnd("Avatar", fields)).andReturn(1);
        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        assertTrue(store.delete(49L));

    }

    @Test
    public void testDeleteFail()
    {
        final Map fields = EasyMap.build("id", 49L);
        expect(mockOfBizDelegator.removeByAnd("Avatar", fields)).andReturn(0);
        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        assertFalse(store.delete(49L));

    }

    @Test
    public void testCreate()
    {
        // apologies this is a bit cheeky
        AvatarAndGv avatar = new AvatarAndGv(null);
        avatar.gv.getAllFields().remove("id");
        expect(mockOfBizDelegator.createValue("Avatar", avatar.gv.getAllFields())).andReturn(avatar.gv);

        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        final Avatar created = store.create(avatar.avatar);
        assertEquals(avatar.avatar, created);
    }

    @Test
    public void testGetAvatars()
    {
        Map constraint = EasyMap.build("field1", "value1", "field2", "value2");
        List<GenericValue> gvs = new ArrayList<GenericValue>();
        gvs.add(new MockGenericValue("Avatar", EasyMap.build("foo", "bar")));
        gvs.add(new MockGenericValue("Avatar", EasyMap.build("bim", "bo")));
        gvs.add(new MockGenericValue("Avatar", EasyMap.build("gum", "by")));

        expect(mockOfBizDelegator.findByAnd("Avatar", constraint)).andReturn(gvs);

        replay();

        final List<GenericValue> calledWith = new ArrayList<GenericValue>();
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator)
        {
            AvatarImpl gvToAvatar(final GenericValue gv)
            {
                calledWith.add(gv);
                return null;
            }
        };
        final List<Avatar> avatars = store.getAvatars(constraint);
        assertEquals(3, avatars.size());
        assertEquals(null, avatars.get(0));
        assertEquals(null, avatars.get(1));
        assertEquals(null, avatars.get(2));
        assertEquals(gvs, calledWith);
    }

    @Test
    public void testGvToAvatar()
    {
        replay();
        
        AvatarAndGv myAvatar = new AvatarAndGv(1234L);
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        final Avatar retrieved = store.gvToAvatar(myAvatar.gv);
        assertEquals(myAvatar.avatar, retrieved);
    }

    @Test
    public void testGetAllSystemAvatars()
    {
        final AvatarAndGv avatar303 = new AvatarAndGv(303L);
        final AvatarAndGv avatar808 = new AvatarAndGv(808L);
        final AvatarAndGv avatar909 = new AvatarAndGv(909L);

        final ArrayList<GenericValue> systemAvatarGvList = new ArrayList<GenericValue>();
        systemAvatarGvList.add(avatar303.gv);
        systemAvatarGvList.add(avatar808.gv);
        systemAvatarGvList.add(avatar909.gv);

        expect(mockOfBizDelegator.findByAnd("Avatar", EasyMap.build(OfbizAvatarStore.SYSTEM_AVATAR, OfbizAvatarStore.IS_SYSTEM,
                OfbizAvatarStore.AVATAR_TYPE, "project"))).andReturn(systemAvatarGvList);
        replay();

        final ArrayList<Avatar> systemAvatars = new ArrayList<Avatar>();
        systemAvatars.add(avatar303.avatar);
        systemAvatars.add(avatar808.avatar);
        systemAvatars.add(avatar909.avatar);

        /**
         * Store that returns only our own Avatars when the GVs are converted.
         */
        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        final List<Avatar> retrievedSystemAvatars = store.getAllSystemAvatars(Avatar.Type.PROJECT);
        assertEquals(systemAvatars, retrievedSystemAvatars);
    }

    @Test
    public void testUpdate()
    {

        final Avatar avatar = new AvatarImpl(123L, "filename.jpg", "image/jpeg", Avatar.Type.PROJECT, "12345", false);
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final MockGenericValue gv = new MockGenericValue("Avatar", EasyMap.build("test", "update"))
        {
            public void store() throws GenericEntityException
            {
                storeCalled.set(true);
            }
        };

        expect(mockOfBizDelegator.findById("Avatar", 123L)).andReturn(gv);
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);
        store.update(avatar);
        assertTrue(storeCalled.get());
    }

    @Test
    public void testUpdateExceptionCase()
    {
        final Avatar avatar = new AvatarImpl(123L, "filename.jpg", "image/jpeg", Avatar.Type.PROJECT, "12345", false);

        final MockGenericValue gv = new MockGenericValue("Avatar", EasyMap.build("test", "update"))
        {
            public void store() throws GenericEntityException
            {
                throw new GenericEntityException("g'day");
            }
        };

        expect(mockOfBizDelegator.findById("Avatar", 123L)).andReturn(gv);
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(mockOfBizDelegator);

        try
        {
            store.update(avatar);
            fail("expected DAE");
        }
        catch (DataAccessException yay)
        {

        }
    }

    @Test
    public void testGetCustomAvatarsForOwner()
    {
        final List<Avatar> results = new ArrayList<Avatar>();
        results.add(new AvatarAndGv(123L).avatar);
        results.add(new AvatarAndGv(345L).avatar);
        results.add(new AvatarAndGv(101L).avatar);
        final AtomicReference<Map<String, Object>> argReceived = new AtomicReference<Map<String, Object>>(null);

        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(null)
        {
            List<Avatar> getAvatars(final Map constraint)
            {
                argReceived.set(constraint);
                return results;
            }
        };
        Map expectedConstraint = EasyMap.build("avatarType", "project", "owner", "scarlett johanssen", "systemAvatar", 0);
        final List<Avatar> retrieved = store.getCustomAvatarsForOwner(Avatar.Type.PROJECT, "scarlett johanssen");
        assertTrue(argReceived.get().equals(expectedConstraint));
        assertEquals(results, retrieved);
    }


    @Test
    public void testGetCustomAvatarsForOwnerNullType()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.getCustomAvatarsForOwner(null, "foo");
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testGetCustomAvatarsForOwnerNullOwner()
    {
        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.getCustomAvatarsForOwner(Avatar.Type.PROJECT, null);
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testGetByIdNullArg()
    {
        replay();
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.getById(null);
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testDeleteNullArg()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.delete(null);
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateNullArgs()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.create(null);
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }

    }

    @Test
    public void testUpdateNullArgs()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            store.update(null);
            fail("expected exception");
        }
        catch (IllegalArgumentException yay)
        {

        }

    }

    @Test
    public void testCreateNullAvatarFileName()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, null, null, Avatar.Type.PROJECT, "10010", false);
            store.create(a);

            fail("expected NPE on null filename");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateNullAvatarOwner()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, "filename.jpg", "image/jpeg", Avatar.Type.PROJECT, null, false);
            store.create(a);

            fail("expected NPE on null owner");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testCreateNullAvatarType()
    {
        replay();
        
        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, "filename.jpg", "image/jpeg", null, "10234", false);
            store.create(a);

            fail("expected NPE on null type");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testUpdateNullAvatarProperties()
    {
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, null, null, Avatar.Type.PROJECT, "10010", false);
            store.create(a);

            fail("expected NPE on null filename");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testUpdateNullAvatarOwner()
    {
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, "filename.jpg", "image/jpeg", Avatar.Type.PROJECT, null, false);
            store.create(a);

            fail("expected NPE on null owner");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testUpdateNullAvatarContentType()
    {
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, "filename.jpg", null, Avatar.Type.PROJECT, "10234", false);
            store.create(a);

            fail("expected NPE on null owner");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }


    @Test
    public void testUpdateNullAvatarType()
    {
        replay();

        OfbizAvatarStore store = new OfbizAvatarStore(null);
        try
        {
            Avatar a = new AvatarImpl(null, "filename.jpg", "image/jpeg", null, "10234", false);
            store.create(a);

            fail("expected NPE on null type");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    /**
     * Helper.
     */
    private static final class AvatarAndGv
    {
        private Avatar avatar;
        private MockGenericValue gv;

        /**
         * Sets up a mock system project Avatar and Gv that are unique for the given id.
         *
         * @param id the id, also used to populate other fields.
         */
        private AvatarAndGv(Long id)
        {
            final String fileName = id + ".png";
            final String owner = "owner_" + id;
            final String contentType = "image/png";
            avatar = new AvatarImpl(id, fileName, contentType, Avatar.Type.PROJECT, owner, false);
            Map fields = EasyMap.build("id", id, "fileName", fileName, "contentType", contentType, "avatarType", "project", "owner", owner, "systemAvatar", 0);
            gv = new MockGenericValue("Avatar", fields);
        }
    }

}
