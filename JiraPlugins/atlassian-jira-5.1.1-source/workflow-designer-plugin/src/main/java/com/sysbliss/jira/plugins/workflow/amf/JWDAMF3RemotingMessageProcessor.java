package com.sysbliss.jira.plugins.workflow.amf;

import com.exadel.flamingo.service.exception.ServiceInvokationException;
import com.exadel.flamingo.service.spring.utils.AMFSpringMethodInvoker;
import com.sysbliss.jira.plugins.workflow.service.WorkflowDesignerService;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 1/24/11
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class JWDAMF3RemotingMessageProcessor {
    private static final Log LOGGER = LogFactory.getLog(JWDAMF3RemotingMessageProcessor.class);

    private final HttpServletRequest servletRequest;

    /**
     * Constructor.
     *
     * @param servletRequest HttpServletRequest
     */
    public JWDAMF3RemotingMessageProcessor(final HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    /**
     * <p>
     * Process <code>RemotingMessage</code>. It extracts all needed for invokation information from message: <code>bean</code>'s name, method's name
     * and arguments.
     * </p>
     *
     * @param remotingMessage RemotingMessage
     * @return AcknowledgeMessage, if errors occur it returns ErrorMessage
     */
    public Message process(final RemotingMessage remotingMessage, final WorkflowDesignerService workflowDesignerService) {

        Message response = null;


        try {
            final String methodName = remotingMessage.getOperation();
            final Object[] args = (Object[]) remotingMessage.getBody();

            final AMFSpringMethodInvoker invoker = new AMFSpringMethodInvoker(null);

            final Object result = invoker.makeCall(workflowDesignerService, methodName, args);

            response = new AcknowledgeMessage(remotingMessage);

            response.setBody(result);

        } catch (final ServiceInvokationException e) {
            // As these exceptions are expected and the way to hand back errors to
            // the flex client log them only at the debug level.
            LOGGER.debug(e.getMessage(), e);
            response = new ErrorMessage(remotingMessage, e);
        }

        return response;
    }
}
