package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.AuthorisationException;
import com.atlassian.plugins.rest.common.security.AuthenticationRequiredException;
import com.atlassian.sal.api.user.UserManager;
import com.sun.jersey.spi.container.ContainerRequest;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertSame;

public class TestSysadminOnlyResourceFilter
{
    private SysadminOnlyResourceFilter sysadminOnlyResourceFilter;
    @Mock
    private UserManager mockUserManager;
    @Mock
    private ContainerRequest containerRequest;

    @Before
    public void setUp()
    {
        initMocks(this);
        sysadminOnlyResourceFilter = new SysadminOnlyResourceFilter(mockUserManager);
    }

    @Test
    public void filterPassed()
    {
        when(mockUserManager.getRemoteUsername()).thenReturn("dusan");
        when(mockUserManager.isSystemAdmin("dusan")).thenReturn(true);
        assertSame(containerRequest, sysadminOnlyResourceFilter.getRequestFilter().filter(containerRequest));
        verify(mockUserManager).isSystemAdmin("dusan");
    }

    @Test(expected = AuthenticationRequiredException.class)
    public void filterRejectedNoLogin()
    {
        sysadminOnlyResourceFilter.getRequestFilter().filter(containerRequest);
    }


    @Test(expected = AuthorisationException.class)
    public void filterRejectedNotAdmin()
    {
        when(mockUserManager.getRemoteUsername()).thenReturn("dusan");
        sysadminOnlyResourceFilter.getRequestFilter().filter(containerRequest);
        verify(mockUserManager).isSystemAdmin("dusan");
    }

}
