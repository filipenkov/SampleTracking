package com.atlassian.jira.avatar;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestCachingAvatarStore extends ListeningTestCase
{

    @Test
    public void testGetById()
    {
        AvatarStore mockDelegate = createMock(AvatarStore.class);

        expect(mockDelegate.getById(1023L)).andReturn(null);
        final AvatarImpl mockAvatar = new AvatarImpl(1024L, "somefile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        expect(mockDelegate.getById(1024L)).andReturn(mockAvatar).once();

        replay(mockDelegate);
        CachingAvatarStore store = new CachingAvatarStore(mockDelegate);

        //complete miss
        Avatar avatar = store.getById(1023L);
        assertNull(avatar);

        //cache miss
        avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);

        //warmed up cache now
        avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);

        verify(mockDelegate);
    }

    @Test
    public void testDelete()
    {
        AvatarStore mockDelegate = createMock(AvatarStore.class);

        final AvatarImpl mockAvatar = new AvatarImpl(1024L, "somefile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        expect(mockDelegate.getById(1024L)).andReturn(mockAvatar);
        expect(mockDelegate.delete(1024L)).andReturn(true);
        expect(mockDelegate.getById(1024L)).andReturn(null);

        replay(mockDelegate);
        CachingAvatarStore store = new CachingAvatarStore(mockDelegate);

        //cache miss
        Avatar avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);

        //warmed up cache now
        avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);

        //now delete the avatar ...this should clear the cache entry!
        assertTrue(store.delete(1024L));

        //this should call through to the delegate again!
        avatar = store.getById(1024L);
        assertNull(avatar);

        verify(mockDelegate);
    }

    @Test
    public void testUpdate()
    {
        final AvatarStore mockDelegate = createMock(AvatarStore.class);

        final AvatarImpl mockAvatar = new AvatarImpl(1024L, "somefile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        final AvatarImpl mockUpdateAvatar = new AvatarImpl(1024L, "anotherfile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        expect(mockDelegate.getById(1024L)).andReturn(mockAvatar);
        mockDelegate.update(mockUpdateAvatar);
        expect(mockDelegate.getById(1024L)).andReturn(mockUpdateAvatar);

        replay(mockDelegate);
        CachingAvatarStore store = new CachingAvatarStore(mockDelegate);

        //cache miss
        Avatar avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);
        //warmed up cache now
        avatar = store.getById(1024L);
        assertEquals(mockAvatar, avatar);

        //now update the avatar ...this should clear the cache entry!
        store.update(mockUpdateAvatar);

        //this should call through to the delegate again!
        avatar = store.getById(1024L);
        assertEquals(mockUpdateAvatar, avatar);

        verify(mockDelegate);
    }

    @Test
    public void testCreate()
    {
         final AvatarStore mockDelegate = createMock(AvatarStore.class);

        final AvatarImpl mockAvatar = new AvatarImpl(null, "somefile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        final AvatarImpl mockCreatedAvatar = new AvatarImpl(1024L, "somefile.png", "img/png", Avatar.Type.PROJECT, "HSP", false);
        expect(mockDelegate.getById(1024L)).andReturn(null);
        expect(mockDelegate.create(mockAvatar)).andReturn(mockCreatedAvatar);

        replay(mockDelegate);
        CachingAvatarStore store = new CachingAvatarStore(mockDelegate);

        //cache miss
        Avatar avatar = store.getById(1024L);
        assertNull(avatar);

        //now create the avatar ...this should populate the cache entry!
        final Avatar createdAvatar = store.create(mockAvatar);
        assertEquals(mockCreatedAvatar, createdAvatar);


        avatar = store.getById(1024L);
        assertEquals(mockCreatedAvatar, avatar);

        verify(mockDelegate);
    }
}
