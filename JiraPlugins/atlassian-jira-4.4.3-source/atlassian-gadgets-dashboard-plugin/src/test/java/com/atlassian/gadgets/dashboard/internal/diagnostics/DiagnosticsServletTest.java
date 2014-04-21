package com.atlassian.gadgets.dashboard.internal.diagnostics;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;


@RunWith(MockitoJUnitRunner.class)
public class DiagnosticsServletTest {

	@Mock TemplateRenderer renderer;
	@Mock UserManager userManager;
	@Mock Diagnostics diagnostics;
	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	
	/**
	 * @see AG-1363
	 */
	@Test
	public void testDoPostWithNullURIDoesNotThrowNPE() throws Exception 
	{
		when(request.getMethod()).thenReturn("someMethod");
		// is default behaviour, but make it explicit
		when(request.getParameter(eq("uri"))).thenReturn(null);
		
		DiagnosticsServlet servletUT = new DiagnosticsServlet(renderer, userManager, diagnostics);
		
		servletUT.doPost(request, response);
		
		// check that the servlet returned and did not 
		verify(response).sendError(eq(SC_BAD_REQUEST), any(String.class));
		verify(diagnostics, noMoreInteractions()).check(any(URI.class));
	}

}
