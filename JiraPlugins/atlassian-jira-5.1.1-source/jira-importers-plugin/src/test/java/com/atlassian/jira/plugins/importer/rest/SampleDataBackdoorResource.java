/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.plugins.importer.sample.SampleDataImporter;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Path("sampleData")
@AnonymousAllowed
public class SampleDataBackdoorResource {
	public static final Logger logger = Logger.getLogger(SampleDataBackdoorResource.class);

    private final SampleDataImporter sampleDataImporter;

    public SampleDataBackdoorResource(SampleDataImporter sampleDataImporter) {
        this.sampleDataImporter = sampleDataImporter;
    }

    @POST
    @Path("/create")
    @Produces({MediaType.TEXT_PLAIN})
    public Response sampleData(InputStream is) {
        try {
            final String json;
            try {
                json = IOUtils.toString(is);
            } finally {
                IOUtils.closeQuietly(is);
            }

            sampleDataImporter.createSampleData(json, Collections.<String, Object>emptyMap(), null, null, new SimpleErrorCollection());

            return Response.ok().build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }
}
