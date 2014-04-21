package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.MockPropertySet;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.opensymphony.module.propertyset.PropertySet;

import electric.xml.Document;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class TestUpgradeTask_Build401 extends MockControllerTestCase
{
    @Test
    public void testDetails()
    {
        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);
        assertEquals("401", upgradeTask.getBuildNumber());
        assertEquals(
            "Resolution Date System Field: Calculating values for the resolution date system field for all issues. " + "Replacing usages of the old charting plugin resolution date field with the new system field.",
            upgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final AtomicBoolean calculateResolutionDateCalled = new AtomicBoolean(false);
        final AtomicBoolean upgradePortletConifgurationsCalled = new AtomicBoolean(false);
        final AtomicBoolean removeCFsCalled = new AtomicBoolean(false);
        final AtomicBoolean updateIssueNavigatorColumnsCalled = new AtomicBoolean(false);
        final AtomicBoolean updateSearchRequestCalled = new AtomicBoolean(false);

        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L))));
        mockController.replay();

        final UpgradeTask_Build401 upgradeTask_build401 = new UpgradeTask_Build401(ofBizDelegator, null, null, null, null)
        {
            @Override
            void calculateResolutionDateForAllIssues()
            {
                calculateResolutionDateCalled.set(true);
            }

            @Override
            void updatePortletConfigurations()
            {
                upgradePortletConifgurationsCalled.set(true);
            }

            @Override
            void removeCustomFields()
            {
                removeCFsCalled.set(true);
            }

            @Override
            void updateIssueNavigatorColumns()
            {
                updateIssueNavigatorColumnsCalled.set(true);
            }

            @Override
            void updateSearchRequests()
            {
                updateSearchRequestCalled.set(true);
            }
        };

        upgradeTask_build401.doUpgrade(false);

        assertTrue(calculateResolutionDateCalled.get());
        assertTrue(upgradePortletConifgurationsCalled.get());
        assertTrue(removeCFsCalled.get());
        assertTrue(updateIssueNavigatorColumnsCalled.get());
        assertTrue(updateSearchRequestCalled.get());

        mockController.verify();
    }

    @Test
    public void testDoUpgradeNoCustomFields() throws Exception
    {
        final AtomicBoolean calculateResolutionDateCalled = new AtomicBoolean(false);
        final AtomicBoolean upgradePortletConifgurationsCalled = new AtomicBoolean(false);
        final AtomicBoolean removeCFsCalled = new AtomicBoolean(false);
        final AtomicBoolean updateIssueNavigatorColumnsCalled = new AtomicBoolean(false);
        final AtomicBoolean updateSearchRequestCalled = new AtomicBoolean(false);

        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        final UpgradeTask_Build401 upgradeTask_build401 = new UpgradeTask_Build401(ofBizDelegator, null, null, null, null)
        {
            @Override
            void calculateResolutionDateForAllIssues()
            {
                calculateResolutionDateCalled.set(true);
            }

            @Override
            void updatePortletConfigurations()
            {
                upgradePortletConifgurationsCalled.set(true);
            }

            @Override
            void removeCustomFields()
            {
                removeCFsCalled.set(true);
            }

            @Override
            void updateIssueNavigatorColumns()
            {
                updateIssueNavigatorColumnsCalled.set(true);
            }

            @Override
            void updateSearchRequests()
            {
                updateSearchRequestCalled.set(true);
            }
        };

        upgradeTask_build401.doUpgrade(false);

        assertTrue(calculateResolutionDateCalled.get());
        assertFalse(upgradePortletConifgurationsCalled.get());
        assertFalse(removeCFsCalled.get());
        assertFalse(updateIssueNavigatorColumnsCalled.get());
        assertFalse(updateSearchRequestCalled.get());

        mockController.verify();
    }

    @Test
    public void testCalculateResolutionDateNoHistory() throws GenericEntityException, SQLException
    {
        final MockIssue issue = new MockIssue(10000);
        final Timestamp now = new Timestamp(new Date().getTime());
        issue.setUpdated(now);

        final OfBizDelegator mockDelegator = mockController.getMock(OfBizDelegator.class);
        mockDelegator.findByCondition("IssueResolutionCount", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null),
            EasyList.build("count"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("count", EasyMap.build("count", 1L))));
        final OfBizListIterator mockOfBizListIterator = mockController.getMock(OfBizListIterator.class);
        mockOfBizListIterator.next();
        mockController.setReturnValue(issue.getGenericValue());
        mockOfBizListIterator.next();
        mockController.setReturnValue(null);
        mockOfBizListIterator.close();

        mockDelegator.findListIteratorByCondition("Issue", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null));
        mockController.setReturnValue(mockOfBizListIterator);

        final EntityCondition fieldCondition = new MockEntityConditionList(EasyList.build(new MockEntityExpr("field", EntityOperator.EQUALS,
            "Resolution"), new MockEntityExpr("field", EntityOperator.EQUALS, "resolution")), EntityOperator.OR);
        final EntityCondition newStringCondition = new MockEntityExpr("newstring", EntityOperator.NOT_EQUAL, null);
        final EntityCondition oldValueCondition = new MockEntityExpr("oldvalue", EntityOperator.EQUALS, null);
        final EntityCondition issueCondition = new MockEntityExpr("issue", EntityOperator.IN, EasyList.build(10000L));

        final EntityCondition allConditions = new MockEntityConditionList(EasyList.build(issueCondition, fieldCondition, newStringCondition,
            oldValueCondition), EntityOperator.AND);
        mockDelegator.findByCondition("ChangeGroupChangeItemMax", allConditions, EasyList.build("maxcreated", "issue"));
        mockController.setReturnValue(Collections.emptyList());

        final OfBizListIterator mockOfBizListIterator2 = mockController.getMock(OfBizListIterator.class);
        mockOfBizListIterator2.next();
        mockController.setReturnValue(issue.getGenericValue());
        mockOfBizListIterator2.next();
        mockController.setReturnValue(null);
        mockOfBizListIterator2.close();

        mockDelegator.findListIteratorByCondition("Issue", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null));
        mockController.setReturnValue(mockOfBizListIterator2);

        mockDelegator.bulkUpdateByPrimaryKey("Issue", EasyMap.build("resolutiondate", now), EasyList.build(10000L));
        mockController.setReturnValue(1);

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);

        //check we don't have a res date set first.
        assertNull(issue.getResolutionDate());

        upgradeTask.calculateResolutionDateForAllIssues();

        mockController.verify();
    }

    @Test
    public void testCalculateResolutionDate() throws GenericEntityException, SQLException
    {
        final MockIssue issue = new MockIssue(10000);
        final Timestamp now = new Timestamp(new Date().getTime());
        issue.setUpdated(now);

        final OfBizDelegator mockDelegator = mockController.getMock(OfBizDelegator.class);
        mockDelegator.findByCondition("IssueResolutionCount", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null),
            EasyList.build("count"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("count", EasyMap.build("count", 1L))));
        final OfBizListIterator mockOfBizListIterator = mockController.getMock(OfBizListIterator.class);
        mockOfBizListIterator.next();
        mockController.setReturnValue(issue.getGenericValue());
        mockOfBizListIterator.next();
        mockController.setReturnValue(null);
        mockOfBizListIterator.close();

        mockDelegator.findListIteratorByCondition("Issue", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null));
        mockController.setReturnValue(mockOfBizListIterator);

        final EntityCondition fieldCondition = new MockEntityConditionList(EasyList.build(new MockEntityExpr("field", EntityOperator.EQUALS,
            "Resolution"), new MockEntityExpr("field", EntityOperator.EQUALS, "resolution")), EntityOperator.OR);
        final EntityCondition newStringCondition = new MockEntityExpr("newstring", EntityOperator.NOT_EQUAL, null);
        final EntityCondition oldValueCondition = new MockEntityExpr("oldvalue", EntityOperator.EQUALS, null);
        final EntityCondition issueCondition = new MockEntityExpr("issue", EntityOperator.IN, EasyList.build(10000L));

        final EntityCondition allConditions = new MockEntityConditionList(EasyList.build(issueCondition, fieldCondition, newStringCondition,
            oldValueCondition), EntityOperator.AND);
        mockDelegator.findByCondition("ChangeGroupChangeItemMax", allConditions, EasyList.build("maxcreated", "issue"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("MaxCount", EasyMap.build("maxcreated", new Timestamp(1), "issue", 10000L))));

        final OfBizListIterator mockOfBizListIterator2 = mockController.getMock(OfBizListIterator.class);
        mockOfBizListIterator2.next();
        mockController.setReturnValue(issue.getGenericValue());
        mockOfBizListIterator2.next();
        mockController.setReturnValue(null);
        mockOfBizListIterator2.close();

        mockDelegator.findListIteratorByCondition("Issue", new MockEntityExpr("resolution", EntityOperator.NOT_EQUAL, null));
        mockController.setReturnValue(mockOfBizListIterator2);

        mockDelegator.bulkUpdateByPrimaryKey("Issue", EasyMap.build("resolutiondate", new Timestamp(1)), EasyList.build(10000L));
        mockController.setReturnValue(1);

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);

        //check we don't have a res date set first.
        assertNull(issue.getResolutionDate());

        upgradeTask.calculateResolutionDateForAllIssues();

        mockController.verify();
    }

    @Test
    public void testUpgradePortletConfigurations() throws GenericEntityException, ObjectConfigurationException
    {
        final OfBizDelegator mockOfBizDelegator = mockController.getMock(OfBizDelegator.class);

        mockOfBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L))));

        mockOfBizDelegator.findByLike("OSUserPropertySetView", EasyMap.build("propertyValue", "customfield_10020"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("PropertySetView", EasyMap.build("entityId", 20020L)),
            new MockGenericValue("PropertySetView", EasyMap.build("entityId", 20025L)), new MockGenericValue("PropertySetView", EasyMap.build(
                "entityId", 10095L))));

        final PropertySet mockProperties = new MockPropertySet();
        mockProperties.setString("dateField", "customfield_10020");

        final PropertySet mockProperties2 = new MockPropertySet();
        mockProperties2.setString("dateField", "created");

        //this is the one that will have its datefield changed.
        final MockControl mockPortletConfigurationControl = MockControl.createControl(PortletConfiguration.class);
        final PortletConfiguration mockPortletConfiguration = (PortletConfiguration) mockPortletConfigurationControl.getMock();
        mockPortletConfiguration.getKey();
        mockPortletConfigurationControl.setReturnValue("com.atlassian.jira.ext.charting:timesince");
        mockPortletConfiguration.getProperties();
        mockPortletConfigurationControl.setReturnValue(mockProperties);
        mockPortletConfigurationControl.replay();

        //this one matches the portlet, but isn't using the customfield.
        final PortletConfiguration mockPortletConfiguration2 = mockController.getMock(PortletConfiguration.class);
        mockPortletConfiguration2.getKey();
        mockController.setReturnValue("com.atlassian.jira.ext.charting:timesince");
        mockPortletConfiguration2.getProperties();
        mockController.setReturnValue(mockProperties2);

        //finally this is some randome portlet using the customfield.  It shouldn't even have its properties queried,
        //but its ID will be queried for the log message.
        final PortletConfiguration mockPortletConfiguration3 = mockController.getMock(PortletConfiguration.class);
        mockPortletConfiguration3.getKey();
        mockController.setReturnValue("com.atlassian.jira.ext.charting:someotherportlet");
        mockPortletConfiguration3.getDashboardPageId();
        mockController.setReturnValue(new Long(999));
        mockPortletConfiguration3.getId();
        mockController.setReturnValue(new Long(10095));

        final PortletConfigurationManager portletConfigurationManager = mockController.getMock(PortletConfigurationManager.class);
        portletConfigurationManager.getByPortletId(20020L);
        mockController.setReturnValue(mockPortletConfiguration);
        portletConfigurationManager.store(mockPortletConfiguration);
        portletConfigurationManager.getByPortletId(20025L);
        mockController.setReturnValue(mockPortletConfiguration2);
        portletConfigurationManager.getByPortletId(10095L);
        mockController.setReturnValue(mockPortletConfiguration3);

        final PortalPage mockPortalPage = PortalPage.id(999L).name("Charts Dashboard").owner("andreask").build();

        final PortalPageManager mockPortalPageManager = mockController.getMock(PortalPageManager.class);
        mockPortalPageManager.getPortalPageById(999L);
        mockController.setReturnValue(mockPortalPage);

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);
        upgradeTask.updatePortletConfigurations();

        //check that only the datefield linked to the resolution date customfield was updated to use the new system
        //field
        assertEquals("resolutiondate", mockProperties.getString("dateField"));
        assertEquals("created", mockProperties2.getString("dateField"));

        mockPortletConfigurationControl.verify();
    }

    @Test
    public void testRemoveCustomFields() throws RemoveException
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L)), new MockGenericValue(
            "CustomField", EasyMap.build("id", 10030L))));

        final CustomFieldManager customFieldManager = mockController.getMock(CustomFieldManager.class);
        customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(10020L);
        customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(10030L);

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);

        upgradeTask.removeCustomFields();
        //no assertions, but the mockController verify should tell us if we don't call .remove() correctly.
    }

    @Test
    public void testUpgradeIssueNavigatorColumns() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L)), new MockGenericValue(
            "CustomField", EasyMap.build("id", 10030L))));

        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        ofBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new MockEntityExpr("fieldidentifier", EntityOperator.IN, EasyList.build(
            "customfield_10020", "customfield_10030")), null, EasyList.build("id", "columnlayout"), EasyList.build("columnlayout ASC",
            "horizontalposition ASC"), null);
        mockController.setReturnValue(mockIterator);

        // first columnlayoutitem is an instance of a single custom field in a column layout
        // this columnlayoutitem will be simply changed to use the system field
        final MockGenericValue resLayoutItem1 = new MockGenericValue("ColumnLayoutItem", EasyMap.build("id", 100L, "columnlayout", 2000L));

        // next 3 columnlayoutitems are an instance of 3 separate custom fields (all resolution date) in the same column layout
        // the first of these will be simply changed to use the system field
        // the other 2 will be removed
        final MockGenericValue resLayoutItem2 = new MockGenericValue("ColumnLayoutItem", EasyMap.build("id", 101L, "columnlayout", 2010L));
        final MockGenericValue resLayoutItem3 = new MockGenericValue("ColumnLayoutItem", EasyMap.build("id", 102L, "columnlayout", 2010L));
        final MockGenericValue resLayoutItem4 = new MockGenericValue("ColumnLayoutItem", EasyMap.build("id", 103L, "columnlayout", 2010L));

        mockIterator.next();
        mockController.setReturnValue(resLayoutItem1);
        mockIterator.next();
        mockController.setReturnValue(resLayoutItem2);
        mockIterator.next();
        mockController.setReturnValue(resLayoutItem3);
        mockIterator.next();
        mockController.setReturnValue(resLayoutItem4);
        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        ofBizDelegator.removeByOr("ColumnLayoutItem", "id", EasyList.build(102L, 103L));
        mockController.setReturnValue(2);
        ofBizDelegator.bulkUpdateByPrimaryKey("ColumnLayoutItem", EasyMap.build("fieldidentifier", "resolutiondate"), EasyList.build(100L, 101L));
        mockController.setReturnValue(2);

        final ColumnLayoutManager columnLayoutManager = mockController.getMock(ColumnLayoutManager.class);
        columnLayoutManager.refresh();

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);
        upgradeTask.updateIssueNavigatorColumns();
    }

    @Test
    public void testUpgradeIssueNavigatorColumnsWithNoLayoutsToUpdate() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L)), new MockGenericValue(
            "CustomField", EasyMap.build("id", 10030L))));

        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        ofBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new MockEntityExpr("fieldidentifier", EntityOperator.IN, EasyList.build(
            "customfield_10020", "customfield_10030")), null, EasyList.build("id", "columnlayout"), EasyList.build("columnlayout ASC",
            "horizontalposition ASC"), null);
        mockController.setReturnValue(mockIterator);

        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        final ColumnLayoutManager columnLayoutManager = mockController.getMock(ColumnLayoutManager.class);
        columnLayoutManager.refresh();

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);
        upgradeTask.updateIssueNavigatorColumns();
    }

    @Test
    public void testUpdateSearchRequests() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);

        final String sr1Xml = "<searchrequest name='asdfdf'>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter'>" + "  <customfield_10020 name='customfield_10020:absolute'>" + "    <fromDate>1239544800000</fromDate>" + "  </customfield_10020>" + "</parameter>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>" + "  <customfield_10030 name='customfield_10030:relative'>" + "    <previousOffset>4233600000</previousOffset>" + "    <nextOffset>604800000</nextOffset>" + "  </customfield_10030>" + "</parameter>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter'>" + "  <projid andQuery='false'>" + "    <value>10000</value>" + "  </projid>" + "</parameter>" + "<sort class='com.atlassian.jira.issue.search.SearchSort'>" + "  <searchSort field='customfield_10020' order='DESC'/>" + "</sort>" + "<sort class='com.atlassian.jira.issue.search.SearchSort'>" + "  <searchSort field='customfield_10030' order='ASC'/>" + "</sort>" + "</searchrequest>";

        final AtomicBoolean mockSR1StoreCalled = new AtomicBoolean(false);
        final MockGenericValue mockSR1 = new MockGenericValue("SearchRequest", MapBuilder.newBuilder().add("request", sr1Xml).toHashMap())
        {
            @Override
            public void store() throws GenericEntityException
            {
                mockSR1StoreCalled.set(true);
            }
        };

        final String sr2Xml = "<searchrequest name='asdfdf'>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>" + "  <customfield_10030 name='customfield_10030:relative'>" + "    <previousOffset>4233600000</previousOffset>" + "    <nextOffset>604800000</nextOffset>" + "  </customfield_10030>" + "</parameter>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter'>" + "  <customfield_10020 name='customfield_10020:absolute'>" + "    <fromDate>1239544800000</fromDate>" + "  </customfield_10020>" + "</parameter>" + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter'>" + "  <projid andQuery='false'>" + "    <value>10000</value>" + "  </projid>" + "</parameter>" + "<sort class='com.atlassian.jira.issue.search.SearchSort'>" + "  <searchSort field='customfield_10030' order='ASC'/>" + "</sort>" + "<sort class='com.atlassian.jira.issue.search.SearchSort'>" + "  <searchSort field='customfield_10020' order='DESC'/>" + "</sort>" + "</searchrequest>";

        final AtomicBoolean mockSR2StoreCalled = new AtomicBoolean(false);
        final MockGenericValue mockSR2 = new MockGenericValue("SearchRequest", MapBuilder.newBuilder().add("request", sr2Xml).toHashMap())
        {
            @Override
            public void store() throws GenericEntityException
            {
                mockSR2StoreCalled.set(true);
            }
        };

        expect(ofBizDelegator.findByCondition(eq("SearchRequest"), EasyMock.<EntityCondition> eq(null), eq(CollectionBuilder.list("id")))).andReturn(
            CollectionBuilder.<GenericValue> newBuilder(mockSR1, mockSR2).asList());

        expect(ofBizDelegator.findByCondition(eq("SearchRequest"), isA(EntityCondition.class), EasyMock.<Collection> eq(null))).andReturn(
            CollectionBuilder.<GenericValue> newBuilder(mockSR1, mockSR2).asList());

        ofBizDelegator.findByAnd("CustomField", EasyMap.build("customfieldtypekey", "com.atlassian.jira.ext.charting:resolutiondate"));
        mockController.setReturnValue(EasyList.build(new MockGenericValue("CustomField", EasyMap.build("id", 10020L)), new MockGenericValue(
            "CustomField", EasyMap.build("id", 10030L))));

        final UpgradeTask_Build401 upgradeTask = mockController.instantiate(UpgradeTask_Build401.class);
        upgradeTask.updateSearchRequests();

        // Get the set XML and verify it has been modified
        String modifiedRequest = mockSR1.getString("request");
        Document modifiedXml = new Document(modifiedRequest);
        assertEquals(2, modifiedXml.getRoot().getElements("parameter").size());
        assertEquals("resolutiondate", modifiedXml.getRoot().getElements("parameter").next().getFirstElement().getName());
        assertEquals(1, modifiedXml.getRoot().getElements("sort").size());
        assertEquals("resolutiondate", modifiedXml.getRoot().getElements("sort").next().getFirstElement().getAttribute("field"));
        assertFalse(modifiedRequest.contains("name='customfield_10020:absolute'"));
        assertFalse(modifiedRequest.contains("field='customfield_10020'"));
        assertFalse(modifiedRequest.contains("name='customfield_10030:relative'"));
        assertFalse(modifiedRequest.contains("field='customfield_10030'"));
        assertTrue(mockSR1StoreCalled.get());

        modifiedRequest = mockSR2.getString("request");
        modifiedXml = new Document(modifiedRequest);
        assertEquals(2, modifiedXml.getRoot().getElements("parameter").size());
        assertEquals("resolutiondate", modifiedXml.getRoot().getElements("parameter").next().getFirstElement().getName());
        assertEquals(1, modifiedXml.getRoot().getElements("sort").size());
        assertEquals("resolutiondate", modifiedXml.getRoot().getElements("sort").next().getFirstElement().getAttribute("field"));
        assertFalse(modifiedRequest.contains("name='customfield_10020:absolute'"));
        assertFalse(modifiedRequest.contains("field='customfield_10020'"));
        assertFalse(modifiedRequest.contains("name='customfield_10030:relative'"));
        assertFalse(modifiedRequest.contains("field='customfield_10030'"));
        assertTrue(mockSR2StoreCalled.get());
    }

}
