package com.atlassian.jira.security.auth.rememberme;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.Clock;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.seraph.service.rememberme.DefaultRememberMeToken;
import com.atlassian.seraph.service.rememberme.RememberMeToken;
import com.atlassian.seraph.spi.rememberme.RememberMeConfiguration;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class TestJiraRememberMeTokenDao extends LegacyJiraMockTestCase
{
    private OfBizDelegator delegator;
    private Clock clock;
    private JiraRememberMeTokenDao dao;
    private Date now;
    private static final String FRED_FLINSTONE = "Fred Flinstone";
    private static final String TABLE_NAME = JiraRememberMeTokenDao.TABLE;
    private static final Long ID_123 = 123L;
    private static final String RANDOM_STRING = "token";
    private static final long ID_456 = 456L;
    private static final long ID_789 = 789L;
    private static final String BARNEY_RUBBLE = "Barney Rubble";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        now = new Date(1275617972945L);
        clock = new ConstantClock(now);
        delegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);

        dao = new JiraRememberMeTokenDao(delegator, ComponentManager.getComponentInstanceOfType(RememberMeConfiguration.class), clock);

    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanOFBiz();
        delegator = null;
    }


    public void testFindById_NotFound()
    {
        final RememberMeToken token = dao.findById(ID_123);
        assertNull(token);
    }

    public void testFindById_Expired()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(10), FRED_FLINSTONE);

        final RememberMeToken token = dao.findById(ID_123);
        assertNull(token);

        // and it got deleted because it was expired
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    public void testFindById()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);

        final RememberMeToken token = dao.findById(ID_123);
        assertNotNull(token);
        assertEquals(ID_123, token.getId());
        assertEquals(RANDOM_STRING, token.getRandomString());
        assertEquals(FRED_FLINSTONE, token.getUserName());
        assertEquals(now.getTime(), token.getCreatedTime());

        // and it did not get deleted because it was WANT expired
        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    public void testCountAll()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);

        final long count = dao.countAll();
        assertEquals(3, count);
    }

    public void testFindByName()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName(FRED_FLINSTONE);
        assertEquals(1,tokens.size());
        RememberMeToken token = tokens.get(0);
        
        assertEquals(ID_123, token.getId());
        assertEquals(RANDOM_STRING, token.getRandomString());
        assertEquals(FRED_FLINSTONE, token.getUserName());
        assertEquals(now.getTime(), token.getCreatedTime());

        // and it did not get deleted because it was WANT expired
        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    public void testFindByName_NonFound()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName("NotNone");
        assertEquals(0,tokens.size());
    }

    public void testFindByName_ButExpired()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(10), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        final List<RememberMeToken> tokens = dao.findForUserName(FRED_FLINSTONE);
        assertEquals(0,tokens.size());

        // and it got deleted because it was expired
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
    }

    public void testSave()
    {
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));

        final RememberMeToken inputToken = DefaultRememberMeToken.builder(RANDOM_STRING).setUserName(FRED_FLINSTONE).build();
        assertNull(inputToken.getId());

        final RememberMeToken persistedToken = dao.save(inputToken);

        assertNotNull(persistedToken);
        assertNotNull(persistedToken.getId());
        assertEquals(RANDOM_STRING, persistedToken.getRandomString());
        assertEquals(FRED_FLINSTONE, persistedToken.getUserName());
        assertEquals(now.getTime(), persistedToken.getCreatedTime());

        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, persistedToken.getId()));

    }

    private GenericValue addRow(Long id, String token, Timestamp timestamp, final Object userName)
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(JiraRememberMeTokenDao.Columns.ID, id);
        map.put(JiraRememberMeTokenDao.Columns.TOKEN, token);
        map.put(JiraRememberMeTokenDao.Columns.CREATED, timestamp);
        map.put(JiraRememberMeTokenDao.Columns.USERNAME, userName);
        final GenericValue gv = UtilsForTests.getTestEntity(TABLE_NAME, map);
        assertNotNull(gv);
        return gv;
    }


    public void testRemoveByUserName()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.removeAllForUser(FRED_FLINSTONE);

        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

    public void testRemoveById()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.remove(ID_456);

        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        assertNotNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

    public void testRemoveAll()
    {
        addRow(ID_123, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_456, RANDOM_STRING, new Timestamp(now.getTime()), FRED_FLINSTONE);
        addRow(ID_789, RANDOM_STRING, new Timestamp(now.getTime()), BARNEY_RUBBLE);

        dao.removeAll();

        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_123));
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_456));
        assertNull(delegator.findByPrimaryKey(TABLE_NAME, ID_789));
    }

}
