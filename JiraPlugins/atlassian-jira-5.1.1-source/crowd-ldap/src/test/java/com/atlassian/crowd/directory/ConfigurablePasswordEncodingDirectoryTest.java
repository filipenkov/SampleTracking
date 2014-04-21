package com.atlassian.crowd.directory;

import java.util.Collections;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.util.InstanceFactory;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurablePasswordEncodingDirectoryTest
{
    @Test
    public void nullPasswordIsReturnedAsIs()
    {
        ConfigurablePasswordEncodingDirectory ds = new Impl(null, null);
        assertNull(ds.encodePassword(null));
    }

    @Test
    public void passwordsArePlaintextIfUnspecified()
    {
        InstanceFactory instanceFactory = mock(InstanceFactory.class);
        when(instanceFactory.getInstance(LDAPPropertiesMapperImpl.class)).thenReturn(new LDAPPropertiesMapperImpl(null));

        ConfigurablePasswordEncodingDirectory ds = new Impl(instanceFactory, null);
        ds.setAttributes(Collections.<String, String>emptyMap());

        assertEquals("plain", ds.encodePassword("plain"));
    }

    @Test
    public void passwordsUseSpecifiedEncoding()
    {
        PasswordEncoder dummyPasswordEncoder = mock(PasswordEncoder.class);
        when(dummyPasswordEncoder.encodePassword("pass", null)).thenReturn("encoded");

        PasswordEncoderFactory pef = mock(PasswordEncoderFactory.class);
        when(pef.getLdapEncoder("dummy")).thenReturn(dummyPasswordEncoder);

        InstanceFactory instanceFactory = mock(InstanceFactory.class);
        when(instanceFactory.getInstance(LDAPPropertiesMapperImpl.class)).thenReturn(new LDAPPropertiesMapperImpl(null));

        ConfigurablePasswordEncodingDirectory ds = new Impl(instanceFactory, pef);
        ds.setAttributes(ImmutableMap.of("ldap.user.encryption", "dummy"));

        assertEquals("encoded", ds.encodePassword("pass"));
    }

    private static class Impl extends ConfigurablePasswordEncodingDirectory
    {
        public Impl(InstanceFactory instanceFactory, PasswordEncoderFactory passwordEncoderFactory)
        {
            super(null, null, instanceFactory, passwordEncoderFactory);
        }

        @Override
        public String getDescriptiveName()
        {
            throw new UnsupportedOperationException();
        }
    }
}
