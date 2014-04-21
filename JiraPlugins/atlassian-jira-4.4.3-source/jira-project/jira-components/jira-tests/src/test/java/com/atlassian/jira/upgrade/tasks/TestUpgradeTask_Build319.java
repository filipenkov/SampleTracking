package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.favourites.OfBizFavouritesStore;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.sharing.SharedEntity;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TestUpgradeTask_Build319 extends LegacyJiraMockTestCase
{
    private MockController mockController;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockController = new MockController();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        mockController.verify();
        UtilsForTests.cleanUsers();
        UtilsForTests.cleanOFBiz();
    }

    public void testUpgradeNoFilters()
    {
        final OfBizFavouritesStore favouritesStore = mockController.getMock(OfBizFavouritesStore.class);

        mockController.replay();

        final List<SharedEntity> addedEnt = new ArrayList<SharedEntity>();
        final List<String> addedUser = new ArrayList<String>();
        UpgradeTask_Build319 task = new UpgradeTask_Build319(CoreFactory.getGenericDelegator(), favouritesStore)
        {
            @Override
            boolean addAsFavourite(final String author, final SharedEntity request)
            {
                addedEnt.add(request);
                addedUser.add(author);
                return true;
            }
        };

        task.doUpgrade(false);

        assertEquals(EasyList.build(), addedEnt);
        assertEquals(EasyList.build(), addedUser);

    }

    public void testUpgradeSuccess() throws GenericEntityException
    {
        final String nickStr = "nick";
        final User nick = UtilsForTests.getTestUser(nickStr);
        final String adminStr = "admin";
        final User admin = UtilsForTests.getTestUser(adminStr);

        EntityUtils.createValue("SearchRequest", EasyMap.build("id", new Long(1), "author", nickStr));
        EntityUtils.createValue("SearchRequest", EasyMap.build("id", new Long(2), "author", adminStr));
        EntityUtils.createValue("SearchRequest", EasyMap.build("id", new Long(4), "author", nickStr));

        final SharedEntity filter1 = new SharedEntity.Identifier(1L, SearchRequest.ENTITY_TYPE, nick);
        final SharedEntity filter2 = new SharedEntity.Identifier(2L, SearchRequest.ENTITY_TYPE, admin);
        final SharedEntity filter4 = new SharedEntity.Identifier(4L, SearchRequest.ENTITY_TYPE, nick);


        final OfBizFavouritesStore favouritesStore = mockController.getMock(OfBizFavouritesStore.class);

        mockController.replay();

        final List<SharedEntity> addedEnt = new ArrayList<SharedEntity>();
        final List<String> addedUser = new ArrayList<String>();
        UpgradeTask_Build319 task = new UpgradeTask_Build319(CoreFactory.getGenericDelegator(), favouritesStore)
        {
            @Override
            boolean addAsFavourite(final String author, final SharedEntity request)
            {
                addedEnt.add(request);
                addedUser.add(author);
                return true;
            }
        };

        task.doUpgrade(false);

        assertEquals(EasyList.build(filter1, filter2, filter4), addedEnt);
        assertEquals(EasyList.build(nickStr, adminStr, nickStr), addedUser);
    }

}