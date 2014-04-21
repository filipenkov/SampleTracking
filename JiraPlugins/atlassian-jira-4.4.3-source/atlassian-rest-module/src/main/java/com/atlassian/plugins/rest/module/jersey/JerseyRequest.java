package com.atlassian.plugins.rest.module.jersey;

import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFilePart;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.net.auth.Authenticator;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.notNull;

public class JerseyRequest implements Request<JerseyRequest, JerseyResponse>
{
    private final Request delegateRequest;
    private final JerseyEntityHandler jerseyEntityHandler;

    private Object entity;

    public JerseyRequest(final Request delegateRequest, final JerseyEntityHandler jerseyEntityHandler)
    {
        this.delegateRequest = delegateRequest;
        this.jerseyEntityHandler = jerseyEntityHandler;
    }

    public JerseyRequest setEntity(final Object entity)
    {
        notNull(entity);
        this.entity = entity;
        return this;
    }

    public JerseyRequest setConnectionTimeout(final int i)
    {
        delegateRequest.setConnectionTimeout(i);
        return this;
    }

    public JerseyRequest setSoTimeout(final int i)
    {
        delegateRequest.setSoTimeout(i);
        return this;
    }

    public JerseyRequest setUrl(final String s)
    {
        delegateRequest.setUrl(s);
        return this;
    }

    public JerseyRequest setRequestBody(final String s)
    {
        delegateRequest.setRequestBody(s);
        return this;
    }

    public JerseyRequest setFiles(List<RequestFilePart> files)
    {
        delegateRequest.setFiles(files);
        return this;
    }

    public JerseyRequest setRequestContentType(final String s)
    {
        delegateRequest.setRequestContentType(s);
        return this;
    }

    public JerseyRequest addRequestParameters(final String... strings)
    {
        delegateRequest.addRequestParameters(strings);
        return this;
    }

    public JerseyRequest addAuthentication(final Authenticator authenticator)
    {
        delegateRequest.addAuthentication(authenticator);
        return this;
    }

    public JerseyRequest addTrustedTokenAuthentication()
    {
        delegateRequest.addTrustedTokenAuthentication();
        return this;
    }

    public JerseyRequest addTrustedTokenAuthentication(final String s)
    {
        delegateRequest.addTrustedTokenAuthentication(s);
        return this;
    }

    public JerseyRequest addBasicAuthentication(final String s, final String s1)
    {
        delegateRequest.addBasicAuthentication(s, s1);
        return this;
    }

    public JerseyRequest addSeraphAuthentication(final String s, final String s1)
    {
        delegateRequest.addSeraphAuthentication(s, s1);
        return this;
    }

    public JerseyRequest addHeader(final String s, final String s1)
    {
        delegateRequest.addHeader(s, s1);
        return this;
    }

    public JerseyRequest setHeader(final String s, final String s1)
    {
        delegateRequest.setHeader(s, s1);
        return this;
    }
	
	public JerseyRequest setFollowRedirects(final boolean follow)
    {
        delegateRequest.setFollowRedirects(follow);
        return this;
    }

    public Map<String, List<String>> getHeaders()
    {
        return delegateRequest.getHeaders();
    }

    public void execute(final ResponseHandler<JerseyResponse> responseHandler)
            throws ResponseException
    {
        executeAndReturn(new ReturningResponseHandler<JerseyResponse, Void>()
        {
            public Void handle(JerseyResponse jerseyResponse) throws ResponseException
            {
                responseHandler.handle(jerseyResponse);
                return null;
            }
        });
    }

    public String execute()
            throws ResponseException
    {
        marshallEntity();
        return delegateRequest.execute();
    }

    public <RET> RET executeAndReturn(final ReturningResponseHandler<JerseyResponse, RET> responseHandler) throws ResponseException
    {
        marshallEntity();
        final Object result = delegateRequest.executeAndReturn(new ReturningResponseHandler<Response, RET>()
        {
            public RET handle(final Response response) throws ResponseException
            {
                JerseyResponse res = new JerseyResponse(response, jerseyEntityHandler);
                return responseHandler.handle(res);
            }
        });

        return (RET)result;
    }

    private void marshallEntity() throws EntityConversionException
    {
        if (entity != null)
        {
            String contentType = getOrSetSingleHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
            String encoding = getOrSetSingleHeaderValue(HttpHeaders.CONTENT_ENCODING, "UTF-8");
            getOrSetSingleHeaderValue(HttpHeaders.ACCEPT, contentType);
            Charset charset = Charset.forName(encoding);
            MediaType type;
            try
            {
                type = MediaType.valueOf(contentType);
            }
            catch (IllegalArgumentException e)
            {
                throw new UnsupportedContentTypeException(e.getMessage(), e);
            }

            try
            {
                String body = jerseyEntityHandler.marshall(entity, type, charset);
                setRequestBody(body);
                setRequestContentType(contentType);
            }
            catch (IOException e)
            {
                throw new EntityConversionException(e);
            }
        }
    }

    /**
     * Retrieve the specified header value, or set it to the supplied defaultValue if the header is currently unset.
     */
    private String getOrSetSingleHeaderValue(final String headerName, final String defaultValue)
    {
        String value = defaultValue;
        List<String> headers = getHeaders().get(headerName);
        if (headers != null && !headers.isEmpty())
        {
            if (headers.size() == 1)
            {
                value = headers.get(0);
            }
        }
        else
        {
            setHeader(headerName, defaultValue);
        }
        return value;
    }

}
