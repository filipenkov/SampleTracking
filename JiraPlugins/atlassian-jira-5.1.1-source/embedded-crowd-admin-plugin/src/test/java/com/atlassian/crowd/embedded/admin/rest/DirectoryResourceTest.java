package com.atlassian.crowd.embedded.admin.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;

import com.sun.jersey.api.uri.UriBuilderImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryResourceTest
{
    @Mock
    CrowdDirectoryService crowdDirectoryService;

    @Mock
    I18nResolver i18nResolver;

    @Test
    public void doesNotTryToTranslateNullWhenDirectoryHasNotSynchronisedYet()
    {
        DirectorySynchronisationInformation noSynchYetInfo = new DirectorySynchronisationInformation(null, null);

        when(crowdDirectoryService.getDirectorySynchronisationInformation(anyLong())).thenReturn(noSynchYetInfo);

        InternalEntityTemplate t = new InternalEntityTemplate();
        t.setId(Long.valueOf(1));
        t.setName("Test");

        DirectoryImpl dir = new DirectoryImpl(t);

        List<Directory> dirs = Arrays.<Directory>asList(dir);

        when(crowdDirectoryService.findAllDirectories()).thenReturn(dirs);

        DirectoryResource dr = new DirectoryResource(crowdDirectoryService, i18nResolver) {
            protected UriBuilder getDirectoryUriBuilder()
            {
                return new UriBuilderImpl();
            }
        };

        dr.get();

        verify(i18nResolver, never()).getText((Message) null);
    }

    @Test
    public void notFoundDirectoryReturns404()
    {
        DirectoryResource dr = new DirectoryResource(crowdDirectoryService, i18nResolver);
        Response resp = dr.getDirectory(0L);
        assertEquals(404, resp.getStatus());
    }
}
