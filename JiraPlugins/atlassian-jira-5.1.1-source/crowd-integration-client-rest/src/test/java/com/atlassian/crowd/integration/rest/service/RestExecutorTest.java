package com.atlassian.crowd.integration.rest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.bind.DataBindingException;

import com.atlassian.crowd.exception.InvalidCrowdServiceException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.rest.entity.MembershipEntity;
import com.atlassian.crowd.integration.rest.entity.MembershipsEntity;
import com.atlassian.crowd.integration.rest.service.RestExecutor.MethodExecutor;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.service.client.ClientProperties;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestExecutorTest
{
    @Test
    public void testPathParamCount()
    {
        assertEquals(1, RestExecutor.pathArgumentCount("%s"));
        assertEquals(2, RestExecutor.pathArgumentCount("%s%s"));
        assertEquals(2, RestExecutor.pathArgumentCount("%s%s?%s"));
        assertEquals(2, RestExecutor.pathArgumentCount("%s%s?%s%s"));
        assertEquals(2, RestExecutor.pathArgumentCount("%%"));
    }

    @Test
    public void testBuildUrl()
    {
        assertEquals("base/search/group?start-index=0&max-results=0", RestExecutor.buildUrl("base", "/search/group?start-index=%d&max-results=%d", 0, 0));
    }
    
    @Test(expected = OperationFailedException.class)
    public void throwErrorCopesWithJaxbFailureToParseXml() throws Exception
    {
        HttpMethod method;
        method = Mockito.mock(HttpMethod.class);
        Mockito.when(method.getResponseBodyAsStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        RestExecutor.throwError(HttpStatus.SC_MOVED_TEMPORARILY, method);
    }
    
    private static void assertMembershipsAsExpected(MembershipsEntity entity)
    {
        assertMembershipsAsExpected(entity.getList());
    }
    
    private static void assertMembershipsAsExpected(Iterable<? extends Membership> ms)
    {
        Iterator<? extends Membership> i = ms.iterator();

        Membership m = i.next();
        
        assertEquals("parent-group", m.getGroupName());
        assertEquals(Collections.singleton("parent-group-user"), m.getUserNames());
        assertEquals(Collections.singleton("child-group"), m.getChildGroupNames());
        
        m = i.next();
        assertEquals("child-group", m.getGroupName());
        assertEquals(Collections.singleton("child-group-user"), m.getUserNames());
        assertEquals(Collections.emptySet(), m.getChildGroupNames());
        
        assertFalse(i.hasNext());
    }
    
    RestExecutor.MethodExecutor executorConsumingStream(InputStream in) throws Exception
    {
        HttpMethod method = mock(HttpMethod.class);
        when(method.getResponseBodyAsStream()).thenReturn(in);
        
        ClientProperties fakeProps = mock(ClientProperties.class);
        when(fakeProps.getBaseURL()).thenReturn("http://localhost/");
        when(fakeProps.getApplicationName()).thenReturn("");
        
        RestExecutor exec = new RestExecutor(fakeProps);
        RestExecutor.MethodExecutor m = exec.new MethodExecutor(method) {
            @Override
            int executeCrowdServiceMethod(HttpMethod method) throws InvalidCrowdServiceException, IOException
            {
                return 200;
            }
        };

        return m;
    }
    
    @Test
    public void ableToUnmarshalJaxbResponses() throws Exception
    {
        InputStream in = getClass().getResourceAsStream("sample-memberships.xml");

        MethodExecutor m = executorConsumingStream(in);
        
        MembershipsEntity entity = m.andReceive(MembershipsEntity.class);
     
        assertMembershipsAsExpected(entity);
    }
}
