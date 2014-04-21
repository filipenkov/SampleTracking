package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.SearchRequest;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Loaded.LOADED;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Loaded.UNLOADED;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Modified.CLEAN;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Modified.DIRTY;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Valid.ERROR;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Valid.OKAY;
import com.atlassian.query.QueryImpl;

/**
 * Unit test for the {@link TestFilterOperationsBean}.
 *
 * @since v4.0
 */
public class TestFilterOperationsBean extends ListeningTestCase
{
    private static final String TEST_USERNAME = "testUser";

    static enum Valid { OKAY, ERROR }
    static enum Loaded { UNLOADED, LOADED }
    static enum Modified { CLEAN, DIRTY }

    /**
     * Make sure the constructor returns false.
     */
    @Test
    public void testConstructor()
    {
        FilterOperationsBean fob = new FilterOperationsBean();
        assertFalse(fob.isShowEdit());
        assertFalse(fob.isShowReload());
        assertFalse(fob.isShowSave());
        assertFalse(fob.isShowSaveNew());
        assertFalse(fob.isShowSaveAs());
        assertFalse(fob.isShowViewSubscriptions());
    }

    /**
     * Make sure no request results in no operations.
     */
    @Test
    public void testSearchRequestNull()
    {
        FilterOperationsBean none = new FilterOperationsBean();

        assertNullFilterOperations(none, OKAY);
        assertNullFilterOperations(none, ERROR);

    }

    /**
     * Make sure the operations are correct when the user is the owner.
     */
    @Test
    public void testOwner()
    {
        FilterOperationsBean fob = new FilterOperationsBean();
        fob.setShowReload(true);
        fob.setShowInvalid(true);
        fob.setShowEdit(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, LOADED, DIRTY);
        fob = new FilterOperationsBean();
        fob.setShowInvalid(true);
        fob.setShowEdit(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, LOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowInvalid(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, UNLOADED, DIRTY);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, UNLOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowReload(true);
        fob.setShowEdit(true);
        fob.setShowSave(true);
        fob.setShowSaveAs(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, LOADED, DIRTY);

        fob = new FilterOperationsBean();
        fob.setShowEdit(true);
        fob.setShowSaveAs(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, LOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowSaveNew(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, UNLOADED, DIRTY);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, UNLOADED, CLEAN);

    }

    /**
     * Make sure the operations are correct when the user is not the owner.
     */
    @Test
    public void testNotOwner()
    {
        FilterOperationsBean reaload = new FilterOperationsBean();
        reaload.setShowReload(true);
        reaload.setShowInvalid(true);

        FilterOperationsBean none = new FilterOperationsBean();
        none.setShowInvalid(true);

        assertFilterOperationsBeanStateDifferentOwner(reaload, ERROR, LOADED, DIRTY);
        assertFilterOperationsBeanStateDifferentOwner(none, ERROR, UNLOADED, DIRTY);

        reaload = new FilterOperationsBean();
        reaload.setShowInvalid(true);
        
        assertFilterOperationsBeanStateDifferentOwner(reaload, ERROR, LOADED, CLEAN);
        assertFilterOperationsBeanStateDifferentOwner(none, ERROR, UNLOADED, CLEAN);

        none = new FilterOperationsBean();
        reaload = new FilterOperationsBean();
        reaload.setShowReload(true);
        assertFilterOperationsBeanStateDifferentOwner(reaload, OKAY, LOADED, DIRTY);
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, UNLOADED, DIRTY);

        reaload = new FilterOperationsBean();
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, LOADED, CLEAN);
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, UNLOADED, CLEAN);
    }

    private void assertNullFilterOperations(final FilterOperationsBean expectedState, final Valid hasErrors)
    {
        assertEquals(expectedState, FilterOperationsBean.create(null, hasErrors == ERROR, TEST_USERNAME, false));
    }

    private void assertFilterOperationsBeanStateSameOwner(final FilterOperationsBean expectedState, final Valid hasErrors, final Loaded isLoaded, final Modified wasModified)
    {
        assertFilterOperationsBeanState(expectedState, hasErrors, isLoaded, wasModified, TEST_USERNAME);
    }

    private void assertFilterOperationsBeanStateDifferentOwner(final FilterOperationsBean expectedState, final Valid hasErrors, final Loaded isLoaded, final Modified wasModified)
    {
        assertFilterOperationsBeanState(expectedState, hasErrors, isLoaded, wasModified, TEST_USERNAME + "NotMe");
    }

    private void assertFilterOperationsBeanState(final FilterOperationsBean expectedState, final Valid validFilter, final Loaded isLoaded, final Modified wasModified, final String userName)
    {
        SearchRequest sr = new SearchRequest(new QueryImpl(), userName, "test", "desc", ((Loaded.LOADED == isLoaded) ? 123L : null), 0L);
        sr.setModified(wasModified == DIRTY);

        assertEquals(expectedState, FilterOperationsBean.create(sr, validFilter == OKAY, TEST_USERNAME, false));
    }
}
