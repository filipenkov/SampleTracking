package com.atlassian.jira.bc.favourites;

import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDefaultFavouritesService extends ListeningTestCase
{
    private static final Long ENTITY_ID = (long) 999;
    private MockProviderAccessor mpa = new MockProviderAccessor();
    private User user = new User("admin", mpa, new MockCrowdService());
    private final SharedEntity ENTITY = new SharedEntity.Identifier(ENTITY_ID, SearchRequest.ENTITY_TYPE, user);


    @Test
    public void testAddFavouriteSuccess()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForAdd(ENTITY, mock);

        service.addFavourite(ctx, ENTITY);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testAddFavouriteNoPermission()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForAdd(ENTITY, mock);

        service.addFavourite(ctx, ENTITY);
        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testAddFavouriteNotExist()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForAdd(ENTITY, mock);

        service.addFavourite(ctx, ENTITY);
        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testAddFavouriteNotExistAndNoPerm()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForAdd(ENTITY, mock);

        service.addFavourite(ctx, ENTITY);
        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertFalse(errorCollection.hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testAddFavouriteInPositionSuccess()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForAddInPosition(ENTITY, mock, 1);

        service.addFavouriteInPosition(ctx, ENTITY, 1);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testAddFavouriteInPositionNoPermission() throws PermissionException
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection()) {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };

        final FavouritesManager mockFavouritesManager = EasyMock.createMock(FavouritesManager.class);
        mockFavouritesManager.addFavouriteInPosition(null, ENTITY, 1);
        EasyMock.expectLastCall().andThrow(new PermissionException("blah"));
        EasyMock.replay(mockFavouritesManager);

        FavouritesService service = new DefaultFavouritesService(mockFavouritesManager);

        service.addFavouriteInPosition(ctx, ENTITY, 1);
        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have permission to favourite this item.", errorCollection.getErrorMessages().iterator().next());
        EasyMock.verify(mockFavouritesManager);
    }

    @Test
    public void testRemoveFavourite()
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForRemove(ENTITY, mock);

        service.removeFavourite(ctx, ENTITY);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        mock.verify();
    }

    @Test
    public void testIsFavourite()
    {
        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForIs(ENTITY, mock, Boolean.TRUE);
        assertTrue(service.isFavourite(null, ENTITY));
        mock.verify();
    }

    @Test
    public void testIsNotFavourite()
    {
        Mock mock = new Mock(FavouritesManager.class);
        FavouritesService service = setUpMockAndGetServiceForIs(ENTITY, mock, Boolean.FALSE);
        assertFalse(service.isFavourite(null, ENTITY));
        mock.verify();
    }

    private FavouritesService setUpMockAndGetServiceForAdd(final SharedEntity entity, final Mock mock)
    {
        mock.setStrict(true);
        mock.expectVoid("addFavourite", P.args(P.IS_NULL, P.eq(entity)));

        FavouritesManager mgr = (FavouritesManager) mock.proxy();


        return new DefaultFavouritesService(mgr);
    }

    private FavouritesService setUpMockAndGetServiceForAddInPosition(final SharedEntity entity, final Mock mock, final long position)
    {
        mock.setStrict(true);
        mock.expectVoid("addFavouriteInPosition", P.args(P.IS_NULL, P.eq(entity), P.eq(position)));

        FavouritesManager mgr = (FavouritesManager) mock.proxy();


        return new DefaultFavouritesService(mgr);
    }

    private FavouritesService setUpMockAndGetServiceForRemove(final SharedEntity entity, final Mock mock)
    {
        mock.setStrict(true);
        mock.expectVoid("removeFavourite", P.args(P.IS_NULL, P.eq(entity)));

        FavouritesManager mgr = (FavouritesManager) mock.proxy();


        return new DefaultFavouritesService(mgr);
    }

    private FavouritesService setUpMockAndGetServiceForIs(final SharedEntity entity, final Mock mock, Boolean isFav)
    {
        mock.setStrict(true);
        mock.expectAndReturn("isFavourite", P.args(P.IS_NULL, P.eq(entity)), isFav);

        FavouritesManager mgr = (FavouritesManager) mock.proxy();

        return new DefaultFavouritesService(mgr);
    }

}
