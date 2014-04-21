package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.testing.TestData;
import static com.atlassian.crowd.embedded.testing.TestData.DIRECTORY_ID;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.event.api.EventPublisher;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class OfBizGroupDaoTest extends AbstractTransactionalOfBizTestCase
{
    private OfBizInternalMembershipDao internalmembershipDao;
    private OfBizGroupDao groupDao;
    private DirectoryDao directoryDao;

    @Before
    public void setUp() throws Exception
    {

        final EventPublisher eventPublisher = new MockEventPublisher();
        directoryDao = new OfBizDirectoryDao(getGenericDelegator(), eventPublisher);
        internalmembershipDao = new OfBizInternalMembershipDao(getGenericDelegator(), directoryDao, eventPublisher);
        groupDao = new OfBizGroupDao(getGenericDelegator(), directoryDao, internalmembershipDao, eventPublisher);
    }

    @After
    public void tearDown() throws Exception
    {
        groupDao = null;
    }

    @Test
    public void testAddAndFindGroupByName() throws Exception
    {
        final Group createdGroup = groupDao.add(TestData.Group.getTestData());

        TestData.Group.assertEqualsTestGroup(createdGroup);

        final Group retrievedGroup = groupDao.findByName(TestData.DIRECTORY_ID, TestData.Group.NAME);

        TestData.Group.assertEqualsTestGroup(retrievedGroup);
    }

    @Test
    public void testAddAndStoreAttributesAndFindGroupWithAttributesByName() throws Exception
    {
        final Group createdGroup = groupDao.add(TestData.Group.getTestData());
        TestData.Group.assertEqualsTestGroup(createdGroup);

        groupDao.storeAttributes(createdGroup, TestData.Attributes.getTestData());

        final GroupWithAttributes retrievedGroup = groupDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.Group.NAME);
        TestData.Group.assertEqualsTestGroup(retrievedGroup);
        TestData.Attributes.assertEqualsTestData(retrievedGroup);
    }

    @Test
    public void testUpdateGroup() throws Exception
    {
        final Group createdGroup = groupDao.add(TestData.Group.getTestData());
        TestData.Group.assertEqualsTestGroup(createdGroup);

        final boolean updatedIsActive = false;
        final String updatedDescription = "updated Description";

        groupDao.update(TestData.Group.getGroup(createdGroup.getName(), createdGroup.getDirectoryId(), updatedIsActive, updatedDescription, createdGroup.getType()));

        final Group updatedGroup = groupDao.findByName(TestData.DIRECTORY_ID, TestData.Group.NAME);

        assertEquals(TestData.Group.NAME, updatedGroup.getName());
        assertEquals(TestData.DIRECTORY_ID, updatedGroup.getDirectoryId());
        assertEquals(TestData.Group.TYPE, updatedGroup.getType());
        assertEquals(updatedIsActive, updatedGroup.isActive());
        assertEquals(updatedDescription, updatedGroup.getDescription());
    }

    @Test
    public void testRemoveGroup() throws Exception
    {
        groupDao.add(TestData.Group.getTestData());
        assertNotNull(groupDao.findByName(TestData.DIRECTORY_ID, TestData.Group.NAME));

        groupDao.remove(TestData.Group.getTestData());
        try
        {
            groupDao.findByName(TestData.DIRECTORY_ID, TestData.Group.NAME);
            fail("Should have thrown a user not found exception");
        }
        catch (GroupNotFoundException e)
        {
            assertEquals(TestData.Group.NAME, e.getGroupName());
        }
    }

    @Test
    public void testRemoveAttribute() throws Exception
    {
        final Group createdGroup = groupDao.add(TestData.Group.getTestData());
        groupDao.storeAttributes(createdGroup, TestData.Attributes.getTestData());

        TestData.Attributes.assertEqualsTestData(groupDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.Group.NAME));

        groupDao.removeAttribute(createdGroup, TestData.Attributes.ATTRIBUTE1);
        final GroupWithAttributes groupWithLessAttributes = groupDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.Group.NAME);

        assertNull(groupWithLessAttributes.getValue(TestData.Attributes.ATTRIBUTE1));
    }

    @Test
    public void testSearchAllGroupNames()
    {
        final String groupName2 = "group2";
        groupDao.add(TestData.Group.getTestData());
        groupDao.add(TestData.Group.getGroup(groupName2, DIRECTORY_ID, true, "d", GroupType.GROUP));


        @SuppressWarnings("unchecked")
        final GroupQuery<String> query = mock(GroupQuery.class);
        when(query.getReturnType()).thenReturn(String.class);

        final List<String> groupNames = groupDao.search(DIRECTORY_ID, query);

        assertEquals(2, groupNames.size());
        assertTrue(groupNames.contains(TestData.Group.NAME));
        assertTrue(groupNames.contains(groupName2));
    }
}
