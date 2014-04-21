package com.sysbliss.jira.plugins.workflow.amf;

import com.exadel.flamingo.flex.amf.process.IAMF3MessageProcessor;
import com.exadel.flamingo.service.spring.amf.process.AMF3CommandMessageProcessor;
import com.sysbliss.jira.plugins.workflow.service.WorkflowDesignerService;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 1/24/11
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JWDAMF3MessageProcessor implements IAMF3MessageProcessor {
    private final HttpServletRequest servletRequest;
    private final WorkflowDesignerService workflowDesignerService;

    /**
     * Constructor.
     *
     * @param servletRequest HttpServletRequest
     */
    public JWDAMF3MessageProcessor(final HttpServletRequest servletRequest, final WorkflowDesignerService workflowDesignerService) {
        this.servletRequest = servletRequest;
        this.workflowDesignerService = workflowDesignerService;
    }

    /**
     * Dispatch message to corresponded processor:<br>
     * CommandMessage to AMF3CommandMessageProcessor, RemotingMessage to AMF3RemotingMessageProcessor.
     *
     * @param amf3Message Message to process
     * @return Result of processing
     */
    public Message process(final Message amf3Message) {

        Message result = null;

        if (amf3Message instanceof CommandMessage) {
            result = new AMF3CommandMessageProcessor(servletRequest).process((CommandMessage) amf3Message);
        } else if (amf3Message instanceof RemotingMessage) {
            result = new JWDAMF3RemotingMessageProcessor(servletRequest).process((RemotingMessage) amf3Message, workflowDesignerService);
        }

        return result;
    }
}
