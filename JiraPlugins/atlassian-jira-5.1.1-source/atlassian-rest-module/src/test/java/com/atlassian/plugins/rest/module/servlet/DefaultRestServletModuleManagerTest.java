package com.atlassian.plugins.rest.module.servlet;

import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.servlet.filter.FilterLocation;
import com.atlassian.plugin.servlet.util.PathMapper;
import com.atlassian.plugins.rest.module.ApiVersion;
import com.atlassian.plugins.rest.module.RestServletFilterModuleDescriptor;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import java.util.Collections;

public class DefaultRestServletModuleManagerTest
{
    private ServletModuleManager servletModuleManagerDelegate;
    private PathMapper pathMapper;

    private DefaultRestServletModuleManager servletModuleManager;

    @Before
    public void setUp()
    {
        servletModuleManagerDelegate = mock(ServletModuleManager.class);
        pathMapper = mock(PathMapper.class);

        servletModuleManager = new DefaultRestServletModuleManager(servletModuleManagerDelegate, pathMapper, "/rest");
    }

    @Test
    public void testAddServletModule()
    {
        final ServletModuleDescriptor moduleDescriptor = mock(ServletModuleDescriptor.class);
        servletModuleManager.addServletModule(moduleDescriptor);

        verify(servletModuleManagerDelegate).addServletModule(moduleDescriptor);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testGetServlet() throws Exception
    {
        final String path = "path";
        final ServletConfig servletConfig = mock(ServletConfig.class);
        servletModuleManager.getServlet(path, servletConfig);

        verify(servletModuleManagerDelegate).getServlet(path, servletConfig);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testRemoveServletModule()
    {
        final ServletModuleDescriptor moduleDescriptor = mock(ServletModuleDescriptor.class);
        servletModuleManager.removeServletModule(moduleDescriptor);

        verify(servletModuleManagerDelegate).removeServletModule(moduleDescriptor);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testGetFilters() throws Exception
    {
        final FilterLocation filterLocation = FilterLocation.AFTER_ENCODING;
        final String pathInfo = "pathInfo";
        final FilterConfig filterConfig = mock(FilterConfig.class);
        servletModuleManager.getFilters(filterLocation, pathInfo, filterConfig);

        verify(servletModuleManagerDelegate).getFilters(filterLocation, pathInfo, filterConfig);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testAddFilterModuleWithNonRestServletFilterModuleDescriptor()
    {
        final ServletFilterModuleDescriptor servletFilterModuleDescriptor = mock(ServletFilterModuleDescriptor.class);
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor);

        verify(servletModuleManagerDelegate).addFilterModule(servletFilterModuleDescriptor);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testAddFilterModuleWithRestServletFilterModuleDescriptor()
    {
        final String key1 = "completekey1";
        final String basePath = "/basepath";
        final String path1 = "/basepath/1";
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor1 = getMockRestServletModuleDescriptor(key1, basePath, "1");
        when(servletFilterModuleDescriptor1.getPaths()).thenReturn(Collections.singletonList(path1));

        // There is no latest yet, so this one should be added
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor1);

        verify(pathMapper).put(key1, servletModuleManager.getPathPattern(basePath));
        verify(servletModuleManagerDelegate).addFilterModule(servletFilterModuleDescriptor1);

        final String key2 = "completekey2";
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor2 = getMockRestServletModuleDescriptor(key2, basePath, "2");

        // this version is greater than the current latest "completekey1", so should be set as latest
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor2);

        // reset the current latest
        verify(pathMapper).put(key1, null);
        verify(pathMapper).put(key1, path1);
        // add the new latest
        verify(pathMapper).put(key2, servletModuleManager.getPathPattern(basePath));
        verify(servletModuleManagerDelegate).addFilterModule(servletFilterModuleDescriptor2);

        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor3 = getMockRestServletModuleDescriptor("completekey3", basePath, "1.2");

        // this version is lower than latest so should not be set as latest
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor3);

        verifyZeroInteractions(pathMapper);
        verify(servletModuleManagerDelegate).addFilterModule(servletFilterModuleDescriptor3);
    }

    @Test
    public void testRemoveFilterModuleWithRestServletFilterModuleDescriptorWithNoExistingLatest()
    {
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor1 = getMockRestServletModuleDescriptor("completekey1", "/basepath", "1");

        // this is not the latest so far, as no descriptor has been set as the latest, so nothing special happening
        servletModuleManager.removeFilterModule(servletFilterModuleDescriptor1);

        verify(servletModuleManagerDelegate).removeFilterModule(servletFilterModuleDescriptor1);
        verifyZeroInteractions(pathMapper);
    }

    @Test
    public void testRemoveFilterModuleWithRestServletFilterModuleDescriptorWhenRemovingNonLatest()
    {
        final String key1 = "completekey1";
        final String key2 = "completekey2";
        final String basePath = "/basepath";
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor1 = getMockRestServletModuleDescriptor(key1, basePath, "1");
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor2 = getMockRestServletModuleDescriptor(key2, basePath, "2");

        // adding the descriptor so that it becomes the latest
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor1);
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor2);

        // removing ths one shouldn't do much, this is not the "latest"
        servletModuleManager.removeFilterModule(servletFilterModuleDescriptor1);

        verify(servletModuleManagerDelegate).removeFilterModule(servletFilterModuleDescriptor1);
        verify(pathMapper).put(key1, servletModuleManager.getPathPattern(basePath));
        verify(pathMapper).put(key1, null);
        verify(pathMapper).put(key2, servletModuleManager.getPathPattern(basePath));
        verifyNoMoreInteractions(pathMapper);
    }

    @Test
    public void testRemoveFilterModuleWithRestServletFilterModuleDescriptorWhenRemovingLatest()
    {
        final String key1 = "completekey1";
        final String key2 = "completekey2";
        final String basePath = "/basepath";
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor1 = getMockRestServletModuleDescriptor(key1, basePath, "1");
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor2 = getMockRestServletModuleDescriptor(key2, basePath, "2");

        servletModuleManager.addFilterModule(servletFilterModuleDescriptor2);
        servletModuleManager.addFilterModule(servletFilterModuleDescriptor1);

        verify(pathMapper).put(key2, servletModuleManager.getPathPattern(basePath));
        verifyNoMoreInteractions(pathMapper);

        servletModuleManager.removeFilterModule(servletFilterModuleDescriptor2);

        verify(servletModuleManagerDelegate).removeFilterModule(servletFilterModuleDescriptor2);
        verify(pathMapper).put(key1, servletModuleManager.getPathPattern(basePath));
        verifyNoMoreInteractions(pathMapper);
    }

    @Test
    public void testRemoveFilterModuleWithNonRestServletFilterModuleDescriptor()
    {
        final ServletFilterModuleDescriptor servletFilterModuleDescriptor = mock(ServletFilterModuleDescriptor.class);
        servletModuleManager.removeFilterModule(servletFilterModuleDescriptor);

        verify(servletModuleManagerDelegate).removeFilterModule(servletFilterModuleDescriptor);
        verifyNoMoreInteractions(servletModuleManagerDelegate);
        verifyZeroInteractions(pathMapper);
    }

    private RestServletFilterModuleDescriptor getMockRestServletModuleDescriptor(String key, String path, String version)
    {
        final RestServletFilterModuleDescriptor servletFilterModuleDescriptor1 = mock(RestServletFilterModuleDescriptor.class);
        when(servletFilterModuleDescriptor1.getCompleteKey()).thenReturn(key);
        when(servletFilterModuleDescriptor1.getBasePath()).thenReturn(path);
        when(servletFilterModuleDescriptor1.getVersion()).thenReturn(new ApiVersion(version));
        return servletFilterModuleDescriptor1;
    }
}
