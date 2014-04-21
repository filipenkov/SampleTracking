package com.atlassian.jira.security.auth.trustedapps;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;

public class TestCurrentApplicationFactory extends ListeningTestCase
{
    @Test
    public void testCreatesPropertiesIfNotThere() throws Exception
    {
        final Map strings = new HashMap();
        final Map text = new HashMap();
        ApplicationProperties properties = new MyApplicationProperties(strings, text);

        CurrentApplicationFactory factory = new DefaultCurrentApplicationFactory(properties, stubServerId("THIS.ISNT.AREA.LSID"));

        final CurrentApplication currentApplication = factory.getCurrentApplication();
        assertNotNull(currentApplication);

        assertNotNull(properties.getText("jira.trustedapp.key.private.data"));
        assertNotNull(properties.getText("jira.trustedapp.key.public.data"));
        assertNotNull(properties.getString("jira.trustedapp.uid"));

        final PublicKey key = currentApplication.getPublicKey();
        assertNotNull(key);
        assertFalse(key instanceof KeyFactory.InvalidPublicKey);

        // just make sure we have a key
        final EncryptedCertificate encoded = currentApplication.encode("this little sentence");
        assertNotNull(encoded.getCertificate());
        assertNotNull(encoded.getID());
        assertNotNull(encoded.getSecretKey());
    }

    private JiraLicenseService stubServerId(final String serverId)
    {
        final JiraLicenseService licenseService = EasyMock.createMock(JiraLicenseService.class);
        expect(licenseService.getServerId()).andStubReturn(serverId);
        replay(licenseService);
        return licenseService;
    }

    @Test
    public void testUsesExistingDodgyProperties()
    {
        final Map strings = new HashMap();
        final Map text = new HashMap();
        text.put("jira.trustedapp.key.private.data", "CRAPPYNOTENCODEDKEYDATA");
        text.put("jira.trustedapp.key.public.data", "CRAPPYNOTENCODEDKEYDATA");
        strings.put("jira.trustedapp.uid", "jira.uid");
        ApplicationProperties properties = new MyApplicationProperties(strings, text)
        {
            public void setText(String name, String value)
            {
                throw new RuntimeException("don't call me");
            }

            public void setString(String name, String value)
            {
                throw new RuntimeException("don't call me");
            }
        };

        CurrentApplicationFactory manager = new DefaultCurrentApplicationFactory(properties, stubServerId("THIS.ISNT.AREA.LSID"));

        final CurrentApplication currentApplication = manager.getCurrentApplication();
        assertNotNull(currentApplication);

        final PublicKey key = currentApplication.getPublicKey();
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPublicKey);

        try
        {
            currentApplication.encode("this little sentence");
            fail("RuntimeException expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testUsesExistingGoodProperties() throws Exception
    {
        final Map strings = new HashMap();
        final Map text = new HashMap();

        text.put("jira.trustedapp.key.private.data", "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=");
        text.put("jira.trustedapp.key.public.data", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB");
        strings.put("jira.trustedapp.uid", "jira.uid");
        ApplicationProperties properties = new MyApplicationProperties(strings, text)
        {
            public void setText(String name, String value)
            {
                throw new RuntimeException("don't call me");
            }

            public void setString(String name, String value)
            {
                throw new RuntimeException("don't call me");
            }
        };

        CurrentApplicationFactory factory = new DefaultCurrentApplicationFactory(properties, stubServerId("THIS.ISNT.AREA.LSID"));

        final CurrentApplication currentApplication = factory.getCurrentApplication();
        assertNotNull(currentApplication);

        assertNotNull(properties.getString("jira.trustedapp.uid"));

        final PublicKey key = currentApplication.getPublicKey();
        assertNotNull(key);
        assertFalse(key instanceof KeyFactory.InvalidPublicKey);

        // just make sure we have a key
        final EncryptedCertificate encoded = currentApplication.encode("this little sentence");
        assertNotNull(encoded.getCertificate());
        assertNotNull(encoded.getID());
        assertNotNull(encoded.getSecretKey());
    }

    @Test
    public void testUsesExistingPropertiesNoServerIdOrUid() throws Exception
    {
        final Map strings = new HashMap();
        final Map text = new HashMap();

        // we need null in these so we can test that the UID is generated correctly in the absence of a server id.
        strings.put("jira.trustedapp.uid", null);

        text.put("jira.trustedapp.key.private.data", "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=");
        text.put("jira.trustedapp.key.public.data", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB");
        ApplicationProperties properties = new MyApplicationProperties(strings, text);

        CurrentApplicationFactory factory = new DefaultCurrentApplicationFactory(properties, stubServerId(null));

        final CurrentApplication currentApplication = factory.getCurrentApplication();
        assertNotNull(currentApplication);
        assertNotNull(currentApplication.getID());

        final PublicKey key = currentApplication.getPublicKey();
        assertNotNull(key);
        assertFalse(key instanceof KeyFactory.InvalidPublicKey);

        // just make sure we have a key
        final EncryptedCertificate encoded = currentApplication.encode("this little sentence");
        assertNotNull(encoded.getCertificate());
        assertNotNull(encoded.getID());
        assertNotNull(encoded.getSecretKey());
    }

    private class MyApplicationProperties implements ApplicationProperties
    {
        private final Map strings;
        private final Map text;

        private MyApplicationProperties(Map strings, Map text)
        {
            this.strings = strings;
            this.text = text;
        }

        public String getText(String name)
        {
            return (String) text.get(name);
        }

        public String getDefaultBackedText(String name)
        {
            return (String) text.get(name);
        }

        public void setText(String name, String value)
        {
            text.put(name, value);
        }

        public String getString(String name)
        {
            return (String) strings.get(name);
        }

        public Collection<String> getDefaultKeys()
        {
            return Collections.EMPTY_SET;
        }

        public String getDefaultBackedString(String name)
        {
            return (String) strings.get(name);
        }

        public String getDefaultString(String name)
        {
            return (String) strings.get(name);
        }

        public void setString(String name, String value)
        {
            strings.put(name, value);
        }

        public boolean exists(String key)
        {
            return strings.containsKey(key) || text.containsKey(key);
        }

        public boolean getOption(String key)
        {
            return false;
        }

        public Collection getKeys()
        {
            return null;
        }

        public void setOption(String key, boolean value)
        {
        }

        public String getEncoding()
        {
            return null;
        }

        public String getMailEncoding()
        {
            return null;
        }

        public String getContentType()
        {
            return null;
        }

        public void refresh()
        {
        }

        public Locale getDefaultLocale()
        {
            return null;
        }

        public Collection getStringsWithPrefix(String prefix)
        {
            return null;
        }

        public Map<String, Object> asMap()
        {
            return strings;
        }

    }
}
