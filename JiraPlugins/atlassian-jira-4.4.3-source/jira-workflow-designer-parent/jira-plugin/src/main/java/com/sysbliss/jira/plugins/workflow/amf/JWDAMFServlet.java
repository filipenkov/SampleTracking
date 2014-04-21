package com.sysbliss.jira.plugins.workflow.amf;

import com.sysbliss.jira.plugins.workflow.service.WorkflowDesignerService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWDAMFServlet extends HttpServlet {
    private static final long serialVersionUID = -7051118247920977263L;

    private WorkflowDesignerService workflowDesignerService;

    public JWDAMFServlet(WorkflowDesignerService workflowDesignerService) {
        super();
        this.workflowDesignerService = workflowDesignerService;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        try {
            JWDAMFRequestProcessor.instance().process(request, response, workflowDesignerService);
        } catch (final Exception e) {
            throw new ServletException(e);
        }

    }
}
