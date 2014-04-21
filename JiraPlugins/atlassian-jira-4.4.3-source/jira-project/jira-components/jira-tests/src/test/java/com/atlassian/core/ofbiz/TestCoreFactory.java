package com.atlassian.core.ofbiz;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.association.DefaultAssociationManager;
import com.atlassian.core.action.DefaultActionDispatcher;

import org.ofbiz.core.entity.GenericDelegator;

/**
 * Created by IntelliJ IDEA.
 * User: Mike Cannon-Brookes
 * Date: Dec 2, 2002
 * Time: 10:39:02 PM
 * To change this template use Options | File Templates.
 */
public class TestCoreFactory extends AbstractOFBizTestCase
{

    @Test
    public void testAssociationManager()
    {
        assertTrue(CoreFactory.getAssociationManager() instanceof DefaultAssociationManager);
        final AssociationManager dcfm = new DefaultAssociationManager(CoreFactory.getGenericDelegator());
        CoreFactory.setAssociationManager(dcfm);
        assertTrue(CoreFactory.getAssociationManager() == dcfm);
    }

    @Test
    public void testGenericDelegator()
    {
        assertTrue(CoreFactory.getGenericDelegator() instanceof GenericDelegator);
    }

    @Test
    public void testActionDispatcher()
    {
        assertTrue(CoreFactory.getActionDispatcher() instanceof DefaultActionDispatcher);
        final DefaultActionDispatcher dcfm = new DefaultActionDispatcher();
        CoreFactory.setActionDispatcher(dcfm);
        assertTrue(CoreFactory.getActionDispatcher() == dcfm);
    }
}
