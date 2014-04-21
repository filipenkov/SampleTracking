package com.atlassian.upm;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.upm.rest.representations.RepresentationFactory;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

@Provider
public class EnterSafeModePreconditionNotMetExceptionMapper implements ExceptionMapper<EnterSafeModePreconditionNotMetException>
{
    private RepresentationFactory representationFactory;

    public EnterSafeModePreconditionNotMetExceptionMapper(RepresentationFactory representationFactory)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
    }

    public Response toResponse(EnterSafeModePreconditionNotMetException exception)
    {
        return Response.status(PRECONDITION_FAILED)
            .entity(representationFactory.createI18nErrorRepresentation("upm.safeMode.error.cannot.enter.precondition.not.met"))
            .type(ERROR_JSON)
            .build();
    }
}
