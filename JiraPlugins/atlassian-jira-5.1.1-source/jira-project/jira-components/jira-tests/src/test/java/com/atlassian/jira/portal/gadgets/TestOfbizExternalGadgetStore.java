package com.atlassian.jira.portal.gadgets;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import java.net.URI;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestOfbizExternalGadgetStore extends LegacyJiraMockTestCase
{
    private OfbizExternalGadgetStore externalGadgetStore;
    private static final String TABLE_EXTERNALGADGET = "ExternalGadget";
    private static final String COLUMN_ID = "id";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        externalGadgetStore = new OfbizExternalGadgetStore(ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
    }

    public void testStore()
    {
        final URI google = URI.create("http://www.google.com.au/");
        final URI msn = URI.create("http://www.msn.com.au/");
        final URI atlassian = URI.create("http://www.atlassian.com/");
        assertFalse(externalGadgetStore.containsSpecUri(google));
        assertFalse(externalGadgetStore.containsSpecUri(msn));
        assertFalse(externalGadgetStore.containsSpecUri(atlassian));

        ExternalGadgetSpec googleSpec = externalGadgetStore.addGadgetSpecUri(google);
        assertTrue(externalGadgetStore.containsSpecUri(google));
        assertFalse(externalGadgetStore.containsSpecUri(msn));
        assertFalse(externalGadgetStore.containsSpecUri(atlassian));

        Set<ExternalGadgetSpec> gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(1, gadgetSpecUris.size());
        assertEquals(google, gadgetSpecUris.iterator().next().getSpecUri());

        //duplicates are not allowed

        try
        {
            googleSpec = externalGadgetStore.addGadgetSpecUri(google);
            fail("Should have complained about duplicates!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }

        ExternalGadgetSpec msnSpec = externalGadgetStore.addGadgetSpecUri(msn);
        ExternalGadgetSpec atlassianSpec = externalGadgetStore.addGadgetSpecUri(atlassian);

        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(3, gadgetSpecUris.size());
        assertTrue(gadgetSpecUris.contains(googleSpec));
        assertTrue(gadgetSpecUris.contains(msnSpec));
        assertTrue(gadgetSpecUris.contains(atlassianSpec));

        externalGadgetStore.removeGadgetSpecUri(googleSpec.getId());
        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(2, gadgetSpecUris.size());
        assertTrue(gadgetSpecUris.contains(msnSpec));
        assertTrue(gadgetSpecUris.contains(atlassianSpec));

        //try removing a URI that doesn't exist. Should just work (tm)
        externalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf(Long.toString(-999)));
        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(2, gadgetSpecUris.size());
        assertTrue(gadgetSpecUris.contains(msnSpec));
        assertTrue(gadgetSpecUris.contains(atlassianSpec));
    }

    //JRA-20554: The call to removeByAnd must contain a Long for the ID to match that database column definition
    //as otherwise PostgreSQL83+  will throw an "operator does not exist" exception.
    public void testRemoveGadgetSpecUri() throws Exception
    {
        final long gadgetId = 10;

        final OfBizDelegator ofBizDelegator = createMock(OfBizDelegator.class);

        expect(ofBizDelegator.removeByAnd(TABLE_EXTERNALGADGET, MapBuilder.<String, Object>singletonMap(COLUMN_ID, gadgetId))).andReturn(1);

        replay(ofBizDelegator);

        OfbizExternalGadgetStore store = new OfbizExternalGadgetStore(ofBizDelegator);
        store.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("" + gadgetId));

        verify(ofBizDelegator);
    }
}
