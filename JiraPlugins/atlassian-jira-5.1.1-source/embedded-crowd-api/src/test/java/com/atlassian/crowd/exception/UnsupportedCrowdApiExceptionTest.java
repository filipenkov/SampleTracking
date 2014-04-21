package com.atlassian.crowd.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnsupportedCrowdApiExceptionTest
{
    @Test
    public void toStringGeneratesCoherentSentence()
    {
        Exception e;
        
        e = new UnsupportedCrowdApiException("1.1", "to retrieve membership data with a single request");
        
        assertEquals(
                "Crowd REST API version 1.1 or greater is required on the server to retrieve membership data with a single request.",
                e.getMessage());
        
        e = new UnsupportedCrowdApiException("1.2", "for event-based synchronisation");
        assertEquals(
                "Crowd REST API version 1.2 or greater is required on the server for event-based synchronisation.",
                e.getMessage());
    }
}
