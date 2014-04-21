package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class TestFieldConfigSchemePersisterImpl extends MockControllerTestCase
{
    @Test
    public void testIllegalArgumentToRemoveIssue()
    {
        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl(null, null, null, null);
        try
        {
            persister.removeByIssueType(null);
            fail("removeByIssueType Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testIllegalArgumentTogetInvalidFieldConfigSchemeAfterIssueTypeRemoval()
    {
        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl(null, null, null, null);
        try
        {
            persister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval(null);
            fail("getInvalidFieldConfigSchemeAfterIssueTypeRemoval Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testRemoveByIssueType()
    {
        String id = "1111";
        Mock mockIssueType = getMockIssueType(id);

        Mock mockOfBizDelegator = new Mock(OfBizDelegator.class);
        mockOfBizDelegator.setStrict(true);
        mockOfBizDelegator.expectAndReturn("removeByAnd", new Constraint[] {
                P.eq("FieldConfigSchemeIssueType"),
                P.eq(EasyMap.build("issuetype", id)) },
                new Integer(1)
        );


        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl((OfBizDelegator) mockOfBizDelegator.proxy(), null, null, null);
        persister.removeByIssueType((IssueType) mockIssueType.proxy());

        mockIssueType.verify();
        mockOfBizDelegator.verify();
    }

    @Test
    public void testInvalidFieldConfigSchemeAfterIssueTypeRemovalWithNoMatchingSchemes()
    {
        String id = "1111";
        Mock mockIssueType = getMockIssueType(id);

        Mock mockOfBizDelegator = new Mock(OfBizDelegator.class);
        mockOfBizDelegator.setStrict(true);
        mockOfBizDelegator.expectAndReturn("findByAnd", new Constraint[] {
                P.eq("FieldConfigSchemeIssueType"),
                P.eq(EasyMap.build("issuetype", id)) },
                Collections.EMPTY_LIST
        );


        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl((OfBizDelegator) mockOfBizDelegator.proxy(), null, null, null);
        Collection results = persister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval((IssueType) mockIssueType.proxy());
        assertNotNull(results);
        assertEquals(0, results.size());

        mockIssueType.verify();
        mockOfBizDelegator.verify();
    }

    @Test
    public void testInvalidFieldConfigSchemeAfterIssueTypeRemoval_AllWithMoreThanOneAssociation()
    {
        String id = "1111";
        Mock mockIssueType = getMockIssueType(id);

        List gvList = new ArrayList();

        MockControl mockOfBizDelegatorControl = MockControl.createStrictControl(OfBizDelegator.class);
        OfBizDelegator ofBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();
        ofBizDelegator.findByAnd("FieldConfigSchemeIssueType", EasyMap.build("issuetype", id));
        mockOfBizDelegatorControl.setReturnValue(gvList);

        for (int i = 0; i < 5; i++)
        {
            Long fcsId = new Long(i * 1000);
            GenericValue gv = new MockGenericValue("FieldConfigSchemeIssueType", EasyMap.build("fieldconfigscheme", fcsId));
            gvList.add(gv);

            ofBizDelegator.findByAnd("FieldConfigSchemeIssueType", EasyMap.build("fieldconfigscheme", fcsId));
            mockOfBizDelegatorControl.setReturnValue(gvList); // we just need a list with more than 1 member.  Hence we re-use the one we have
        }
        mockOfBizDelegatorControl.replay();

        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl((OfBizDelegator) mockOfBizDelegatorControl.getMock(), null, null, null);
        Collection results = persister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval((IssueType) mockIssueType.proxy());
        assertNotNull(results);
        assertEquals(0, results.size());

        mockIssueType.verify();
        mockOfBizDelegatorControl.verify();
    }

    @Test
    public void testInvalidFieldConfigSchemeAfterIssueTypeRemoval_OneWithOneAssociation()
    {
        String id = "1111";
        Mock mockIssueType = getMockIssueType(id);

        List gvList = new ArrayList();
        List gvSingleAssociationList = new ArrayList();

        MockControl mockOfBizDelegatorControl = MockControl.createStrictControl(OfBizDelegator.class);
        OfBizDelegator ofBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();
        ofBizDelegator.findByAnd("FieldConfigSchemeIssueType", EasyMap.build("issuetype", id));
        mockOfBizDelegatorControl.setReturnValue(gvList);

        for (int i = 0; i < 5; i++)
        {
            Long fcsId = new Long(i * 1000);
            GenericValue gv = new MockGenericValue("FieldConfigSchemeIssueType", EasyMap.build("fieldconfigscheme", fcsId));
            gvList.add(gv);

            ofBizDelegator.findByAnd("FieldConfigSchemeIssueType", EasyMap.build("fieldconfigscheme", fcsId));
            if (i == 1)
            {
                gvSingleAssociationList.add(new MockGenericValue("FieldConfigSchemeIssueType", EasyMap.build("fieldconfigscheme", fcsId)));
                mockOfBizDelegatorControl.setReturnValue(gvSingleAssociationList); // a list with only one member
            }
            else
            {
                mockOfBizDelegatorControl.setReturnValue(gvList);  // we just need a list with more than 1 member.  Hence we re-use the one we have
            }
        }
        mockOfBizDelegatorControl.replay();

        FieldConfigSchemePersisterImpl persister = new FieldConfigSchemePersisterImpl((OfBizDelegator) mockOfBizDelegatorControl.getMock(), null, null, null)
        {
            public FieldConfigScheme getFieldConfigScheme(Long configSchemeId)
            {
                return new FieldConfigScheme.Builder().setName("Name").setDescription("Description").setId(configSchemeId).toFieldConfigScheme();
            }
        };
        Collection results = persister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval((IssueType) mockIssueType.proxy());
        assertNotNull(results);
        assertEquals(1, results.size());
        FieldConfigScheme fcs = (FieldConfigScheme) results.iterator().next();
        assertEquals(1000, fcs.getId().intValue());

        mockIssueType.verify();
        mockOfBizDelegatorControl.verify();
    }

    @Test
    public void testGetConfigSchemeIdsForCustomFieldId()
    {
        final MockController mockController = new MockController();

        final OfBizDelegator mockDelegator = mockController.getMock(OfBizDelegator.class);
        mockDelegator.findByAnd("FieldConfigScheme", EasyMap.build("fieldid", "customfield_10000"));
        mockController.setReturnValue(Collections.emptyList());

        mockDelegator.findByAnd("FieldConfigScheme", EasyMap.build("fieldid", "customfield_10020"));
        mockController.setReturnValue(
                EasyList.build(new MockGenericValue("FieldConfigScheme", EasyMap.build("id", 234L)),
                        new MockGenericValue("FieldConfigScheme", EasyMap.build("id", 567L))));

        final FieldConfigSchemePersisterImpl persister = mockController.instantiate(FieldConfigSchemePersisterImpl.class);
        try
        {
            persister.getConfigSchemeIdsForCustomFieldId(null);
            fail("Should have thrown exception!");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }

        final List<Long> ids = persister.getConfigSchemeIdsForCustomFieldId("customfield_10000");
        assertTrue(ids.isEmpty());

        final List<Long> secondIds = persister.getConfigSchemeIdsForCustomFieldId("customfield_10020");
        assertFalse(secondIds.isEmpty());
        assertEquals(new Long(234), secondIds.get(0));
        assertEquals(new Long(567), secondIds.get(1));

        mockController.verify();
    }

    @Test
    public void testGetConfigSchemeForFieldConfigHappyPath() throws Exception
    {
        final Long fieldConfigId = 10L;
        final Long schemeId = 200L;

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByAnd("FieldConfigSchemeIssueType", MapBuilder.<String, Object>newBuilder().add("fieldconfiguration", fieldConfigId).toMap());
        mockController.setReturnValue(Collections.singletonList(new MockGenericValue("FieldConfigSchemeIssueType", MapBuilder.newBuilder().add("fieldconfigscheme", schemeId).toMap())));

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        fieldConfig.getId();
        mockController.setReturnValue(fieldConfigId);

        final FieldConfigPersister fieldConfigPersister = mockController.getMock(FieldConfigPersister.class);
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final FieldConfigSchemePersisterImpl configSchemePersister = new FieldConfigSchemePersisterImpl(delegator, constantsManager, fieldConfigPersister, null)
        {
            @Override
            public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
            {
                called.set(true);
                assertEquals(configSchemeId, schemeId);
                return null;
            }
        };

        configSchemePersister.getConfigSchemeForFieldConfig(fieldConfig);

        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testGetConfigSchemeForFieldConfigMoreThanOneScheme() throws Exception
    {
        final Long fieldConfigId = 10L;
        final Long schemeId1 = 200L;
        final Long schemeId2 = 400L;

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        fieldConfig.getId();
        mockController.setReturnValue(fieldConfigId);

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByAnd("FieldConfigSchemeIssueType", MapBuilder.<String, Object>newBuilder().add("fieldconfiguration", fieldConfigId).toMap());
        final List<MockGenericValue> values = CollectionBuilder.newBuilder(
                new MockGenericValue("FieldConfigSchemeIssueType", MapBuilder.newBuilder().add("fieldconfigscheme", schemeId1).toMap()),
                new MockGenericValue("FieldConfigSchemeIssueType", MapBuilder.newBuilder().add("fieldconfigscheme", schemeId2).toMap())
        ).asList();
        mockController.setReturnValue(values);

        final FieldConfigPersister fieldConfigPersister = mockController.getMock(FieldConfigPersister.class);
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final FieldConfigSchemePersisterImpl configSchemePersister = new FieldConfigSchemePersisterImpl(delegator, constantsManager, fieldConfigPersister, null)
        {
            @Override
            public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
            {
                called.set(true);
                assertEquals(configSchemeId, schemeId1);
                return null;
            }
        };

        configSchemePersister.getConfigSchemeForFieldConfig(fieldConfig);

        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testGetConfigSchemeForFieldConfigNoSchemes() throws Exception
    {
        final Long fieldConfigId = 10L;

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        fieldConfig.getId();
        mockController.setReturnValue(fieldConfigId);

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByAnd("FieldConfigSchemeIssueType", MapBuilder.<String, Object>newBuilder().add("fieldconfiguration", fieldConfigId).toMap());
        mockController.setReturnValue(Collections.emptyList());

        final FieldConfigSchemePersisterImpl configSchemePersister = mockController.instantiate(FieldConfigSchemePersisterImpl.class);

        try
        {
            configSchemePersister.getConfigSchemeForFieldConfig(fieldConfig);
            fail("Expected exception");
        }
        catch (DataAccessException expected)
        {
        }

        mockController.verify();
    }

    private Mock getMockIssueType(String id)
    {
        Mock mockIssueType = new Mock(IssueType.class);
        mockIssueType.setStrict(true);
        mockIssueType.expectAndReturn("getId", id);
        return mockIssueType;
    }
}
