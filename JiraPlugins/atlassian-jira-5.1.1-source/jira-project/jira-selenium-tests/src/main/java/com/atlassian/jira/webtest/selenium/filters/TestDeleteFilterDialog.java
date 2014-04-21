package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;

/**
 * Selenium test for AUI dialog to delete filter.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestDeleteFilterDialog extends AbstractAuiDialogTest
{

    private static final int TEST_FILTER_ID = 10000;
    private static final int[] ALL_DELETABLE_FILTER_IDS = { TEST_FILTER_ID, 10010, 10020};

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestManageFilters.xml");
    }

    public void testOpensDialogsCorrespondingToFilters()
    {
        goToManageFilters();
        goToManageFiltersMy();
        for (int filterId : ALL_DELETABLE_FILTER_IDS)
        {
            openDeleteFilterDialog(filterId);
            assertOnDeleteFilterDialog(filterId);
            closeDialogByClickingCancel();
        }
    }

    public void testDeleteFilterDialogOnMyFilters()
    {
        shouldDisplayDialogOnManageFiltersPageEntry();
        shouldNotDeleteFilterOnMyFiltersGivenDialogClosedUsingCancel();
        shouldDeleteFilterOnMyFiltersGivenDialogSubmitted();
    }

    public void testDeleteFilterDialogOnFavouriteFilters()
    {
        shouldDisplayDialogOnManageFiltersPageEntry();
        shouldNotDeleteFilterOnFavouriteFiltersGivenDialogClosedUsingCancel();
        shouldDeleteFilterOnFavouriteFiltersGivenDialogSubmitted();
    }


    private void shouldDisplayDialogOnManageFiltersPageEntry()
    {
        goToManageFilters();
        openDeleteFilterDialog(TEST_FILTER_ID);
        assertOnDeleteFilterDialog(TEST_FILTER_ID);
    }

    private void shouldNotDeleteFilterOnMyFiltersGivenDialogClosedUsingCancel()
    {
        goToDeleteFilterDialogOnMyFilters(TEST_FILTER_ID);
        closeDialogByClickingCancel();
        assertDialogNotOpen();
        assertOnMyFiltersPage();
        assertFilterPresent(TEST_FILTER_ID);
    }

    private void shouldDeleteFilterOnMyFiltersGivenDialogSubmitted()
    {
        goToDeleteFilterDialogOnMyFilters(TEST_FILTER_ID);
        submitDialogAndWaitForReload();
        assertDialogNotOpen();
        assertOnMyFiltersPage();
        assertFilterNotPresent(TEST_FILTER_ID);
        
    }

    private void shouldNotDeleteFilterOnFavouriteFiltersGivenDialogClosedUsingCancel()
    {
        goToDeleteFilterDialogOnFavourites(TEST_FILTER_ID);
        closeDialogByClickingCancel();
        assertDialogNotOpen();
        assertOnFavouriteFiltersPage();
        assertFilterPresent(TEST_FILTER_ID);
    }

    private void shouldDeleteFilterOnFavouriteFiltersGivenDialogSubmitted()
    {
        goToDeleteFilterDialogOnFavourites(TEST_FILTER_ID);
        submitDialogAndWaitForReload();
        assertDialogNotLoaded();
        assertOnFavouriteFiltersPage();
        assertFilterNotPresent(TEST_FILTER_ID);
    }

    private void goToDeleteFilterDialogOnFavourites(int filterId)
    {
        goToManageFiltersFavourite();
        assertOnFavouriteFiltersPage();
        openDeleteFilterDialog(filterId);
        assertOnDeleteFilterDialog(filterId);
    }

    private void goToDeleteFilterDialogOnMyFilters(int filterId)
    {
        goToManageFiltersMy();
        assertOnMyFiltersPage();
        openDeleteFilterDialog(filterId);
        assertOnDeleteFilterDialog(filterId);
    }

    private void goToManageFilters()
    {
        getNavigator().gotoPage("secure/ManageFilters.jspa", true);
    }

    private void goToManageFiltersFavourite()
    {
        goToManageFilters();
        client.click("jquery=#fav-filters-tab");
        waitFor(500);
    }

    private void goToManageFiltersMy()
    {
        goToManageFilters();
        client.click("jquery=#my-filters-tab");
        waitFor(500);
    }

    private void assertOnFavouriteFiltersPage()
    {
        assertOnManageFiltersPage();
        assertThat.elementHasText("jquery=.content-body h2", "Favourite Filters");
    }

    private void assertOnMyFiltersPage()
    {
        assertOnManageFiltersPage();
        assertThat.elementHasText("jquery=.content-body h2", "My Filters");
    }

    private void assertOnManageFiltersPage()
    {
        assertThat.elementHasText("jquery=#content > header > h1", "Manage Filters");
    }

    private void openDeleteFilterDialog(int filterId)
    {
        client.click(String.format("jquery=a#delete_%d", filterId));
    }

//    private void submitDeleteFilterDialog(int filterId)
//    {
//        submitAuiFormAndWaitForReload(auiFormIdFor(filterId));
//    }

//    private void closeDialogByClickingCancel(final int filterId)
//    {
//        closeDialogByClickingCancel(auiFormIdFor(filterId));
//    }

    private void assertOnDeleteFilterDialog(int filterId)
    {
        assertDialogIsOpenAndReady();
        assertDialogContainsAuiForm(auiFormIdFor(filterId));
    }

    private String auiFormIdFor(int filterId)
    {
        return "delete-filter-confirm-form-" + filterId;
    }

    private void assertFilterPresent(int filterId)
    {
        assertThat.elementPresentByTimeout(filterLinkLocator(filterId), DEFAULT_TIMEOUT);
    }

    private void assertFilterNotPresent(int filterId)
    {
        assertThat.elementNotPresentByTimeout(filterLinkLocator(filterId), DEFAULT_TIMEOUT);
    }

    private String filterLinkLocator(int filterId)
    {
        return String.format("jquery=a#filterlink_%d", filterId);
    }
}
