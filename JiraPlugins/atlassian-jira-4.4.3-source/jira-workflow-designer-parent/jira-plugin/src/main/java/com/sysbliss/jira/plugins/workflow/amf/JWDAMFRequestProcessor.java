package com.sysbliss.jira.plugins.workflow.amf;

import com.exadel.flamingo.flex.amf.AMF0Message;
import com.exadel.flamingo.flex.amf.process.AMF0MessageProcessor;
import com.exadel.flamingo.flex.messaging.amf.io.AMF0Deserializer;
import com.exadel.flamingo.flex.messaging.amf.io.AMF0Serializer;
import com.sysbliss.jira.plugins.workflow.service.WorkflowDesignerService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 1/24/11
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JWDAMFRequestProcessor {
    /**
     * Default constructor.
     */
    private JWDAMFRequestProcessor() {
    }

    /**
     * Return object instance of this class
     *
     * @return AMFToSeamRequestProcessor
     */
    public static JWDAMFRequestProcessor instance() {
        return Holder.INSTANCE;
    }

    /**
     * Holder for lazy initialization
     */
    private static class Holder {

        private static final JWDAMFRequestProcessor INSTANCE = new JWDAMFRequestProcessor();
    }

    /**
     * Provides processing of request in a full set of Seam contexts.
     *
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param workflowDesignerService
     * @throws javax.servlet.ServletException If error occur
     * @throws java.io.IOException If error occur
     */
    public void process(final HttpServletRequest request, final HttpServletResponse response, WorkflowDesignerService workflowDesignerService) throws ServletException,
            IOException {

        AMF0Deserializer deserializer = new AMF0Deserializer(new DataInputStream(request.getInputStream()));
        AMF0Message amf0Request = deserializer.getAMFMessage();

        AMF0MessageProcessor amf0MessageProcessor = new AMF0MessageProcessor(new JWDAMF3MessageProcessor(request,workflowDesignerService));
        AMF0Message amf0Response = amf0MessageProcessor.process(amf0Request);

        response.setContentType("application/x-amf");
        AMF0Serializer serializer = new AMF0Serializer(new DataOutputStream(response.getOutputStream()));
        serializer.serializeMessage(amf0Response);

    }

}
