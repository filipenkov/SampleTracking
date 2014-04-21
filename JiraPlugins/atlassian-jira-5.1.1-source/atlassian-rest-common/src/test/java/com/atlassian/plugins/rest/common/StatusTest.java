package com.atlassian.plugins.rest.common;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Variant;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusTest
{
    @Test
    public void variantForUsesRequestToSelectVariant()
    {
        Request req = mock(Request.class);
        when(req.selectVariant(Mockito.<List<Variant>>any())).thenReturn(new Variant(MediaType.APPLICATION_OCTET_STREAM_TYPE, null, null));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, Status.variantFor(req));
        verify(req).selectVariant(Mockito.<List<Variant>>any());
    }
}
