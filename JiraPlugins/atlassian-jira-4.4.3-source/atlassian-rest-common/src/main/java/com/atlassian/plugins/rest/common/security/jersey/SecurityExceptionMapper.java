package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.Status;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * <p>Exception mapper that takes care of {@link SecurityException security exceptions}</p>
 * @since 1.0
 */
@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException>
{
    public Response toResponse(SecurityException exception)
    {
        return Status.unauthorized().message(exception.getMessage()).response();
    }
}
