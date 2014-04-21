package com.atlassian.jira.license;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.easymock.EasyMock;

/**
 *
 * @since v4.0
 */
public class TestJiraLicenseStoreImpl extends MockControllerTestCase
{
    private static final String GOOD_LICENSE_STRING = "Goodlicensestring";

    // keys from APKeys.
    private static final String POST20_LICENSE_STRING_KEY = "License20";
    private static final String OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT = "License Message Text";
    private static final String OLD_PRE20_LICENSE_HASH_KEY_TEXT = "License Hash 1 Text";
    private static final String REALLY_OLD_MESSAGE_KEY = "License Message";
    private static final String REALLY_OLD_MESSAGE_HASH = "License Hash 1";

    private ApplicationProperties applicationProperties;
    private LicenseStringFactory licenseStringFactory;

    /*
     * The license recomposed from license message and hash is never the same, the end of the license string however is
     * constant, that's what we test with here.
     */
    private static final String LIC_MSG = "License message\u00C9";
    private static final String LIC_HASH = "License hash\u00C9";
    private static final String ENTERPRISE = "enterprise";
    private static final String JIRA_EDITION = "jira.edition";
    private static final String USER_NAME = "userName";
    private static final String LIC_WITH_WHITE_SPACE_IN_IT = "Some Lic\nwith\twhite\rspace in it";
    private static final String LIC_WITH_NO_WHITE_SPACE_IN_IT = "SomeLicwithwhitespaceinit";

    @Before
    public void setUp()
    {
        applicationProperties = getMock(ApplicationProperties.class);
        licenseStringFactory = getMock(LicenseStringFactory.class);
    }

    @Test
    public void testRetrieve_From20Key()
    {
        expect(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).andReturn(GOOD_LICENSE_STRING);

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        final String actualLicenseString = store.retrieve();
        assertEquals(GOOD_LICENSE_STRING, actualLicenseString);
    }

    @Test
    public void testRetrieve_FromPre20TextKeys()
    {
        expect(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).andReturn(null);

        expect(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).andReturn(LIC_MSG);
        expect(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).andReturn(LIC_HASH);

        expect(licenseStringFactory.create(LIC_MSG, LIC_HASH)).andReturn(GOOD_LICENSE_STRING);

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        assertEquals(GOOD_LICENSE_STRING, store.retrieve());
    }

    @Test
    public void testRetrieve_FromReallyOldPre20TextKeys()
    {
        expect(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).andReturn(null);

        expect(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).andReturn(null);
        expect(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).andReturn(null);

        expect(applicationProperties.getString(REALLY_OLD_MESSAGE_KEY)).andReturn(LIC_MSG);
        expect(applicationProperties.getString(REALLY_OLD_MESSAGE_HASH)).andReturn(LIC_HASH);

        expect(licenseStringFactory.create(LIC_MSG, LIC_HASH)).andReturn(GOOD_LICENSE_STRING);

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        assertEquals(GOOD_LICENSE_STRING, store.retrieve());
    }
    
    @Test
    public void testRetrieve_NotSetAnywhere()
    {
        expect(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).andReturn(null);

        expect(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).andReturn(null);
        expect(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).andReturn(null);

        expect(applicationProperties.getString(REALLY_OLD_MESSAGE_KEY)).andReturn(null);
        expect(applicationProperties.getString(REALLY_OLD_MESSAGE_HASH)).andReturn(null);

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        final String actualLicenseString = store.retrieve();
        assertNull(actualLicenseString);
    }

    @Test
    public void testStoreEmptyString()
    {
        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        try
        {
            store.store(null);
            fail("Should have barfed");
        }
        catch (Exception expected)
        {

        }
        try
        {
            store.store("");
            fail("Should have barfed");
        }
        catch (Exception expected)
        {

        }
    }

    @Test
    public void testStore_HappyPath() throws Exception
    {
        applicationProperties.setText(POST20_LICENSE_STRING_KEY, GOOD_LICENSE_STRING);
        expectLastCall().once();
        applicationProperties.setString(JIRA_EDITION, ENTERPRISE);
        expectLastCall().once();

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        store.store(GOOD_LICENSE_STRING);
    }

    @Test
    public void testStore_WithWhitespaceInIt() throws Exception
    {
        applicationProperties.setText(POST20_LICENSE_STRING_KEY, LIC_WITH_NO_WHITE_SPACE_IN_IT);
        expectLastCall().once();
        applicationProperties.setString(JIRA_EDITION, ENTERPRISE);
        expectLastCall().once();

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        store.store(LIC_WITH_WHITE_SPACE_IN_IT);
    }

    @Test
    public void testConfirmProceed() throws Exception
    {
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true); expectLastCall();
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER, USER_NAME); expectLastCall();
        applicationProperties.setString(eq(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP), EasyMock.<String>anyObject()); expectLastCall();

        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        store.confirmProceedUnderEvaluationTerms(USER_NAME);
    }

    @Test
    public void testRetriveServerId()
    {
        final String serverId = "A server ID";
        expect(applicationProperties.getString(APKeys.JIRA_SID)).andReturn(serverId);
        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        assertEquals(serverId, store.retrieveServerId());
    }
    
    @Test
    public void testStoreServerId()
    {
        final String serverId = "A server ID";
        applicationProperties.setString(APKeys.JIRA_SID, serverId); expectLastCall();
        final JiraLicenseStoreImpl store = instantiate(JiraLicenseStoreImpl.class);
        store.storeServerId(serverId);
    }
}
