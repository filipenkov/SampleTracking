package com.atlassian.support.tools.mail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.Validateable;
import com.atlassian.support.tools.mock.MockApplicationInfo;
import com.atlassian.support.tools.request.CreateSupportRequestAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.servlet.SafeHttpServletRequestImpl;

public class TestRequestValidator extends TestCase
{
	private static final String VALID_EMAIL = "aatkins@atlassian.com";
	private static final String INVALID_EMAIL = "aatkins@foobar";
	private static final String EMAIL_PARAM_KEY = "contactEmail";

	public void testEmailValidation()
	{
		SupportApplicationInfo info = new MockApplicationInfo();
		Map<String, Object> context = new HashMap<String, Object>();
		HttpServletRequest req = mock(HttpServletRequest.class);

		Validateable validator = new CreateSupportRequestAction(info, null);
		// validate once with no parameters, we should definitely see errors
		ValidationLog log = new ValidationLog(info);
		when(req.getParameter("subject")).thenReturn("Some subject");
		when(req.getParameter("description")).thenReturn("Some description");
		validator.validate(context, new SafeHttpServletRequestImpl(req), log);
		assertTrue("Email validation with no CGI data did not return any errors.", log.hasErrors());

		// validate once with an invalid email address
		ValidationLog log2 = new ValidationLog(info);
		when(req.getParameter("subject")).thenReturn("Some subject");
		when(req.getParameter("description")).thenReturn("Some description");
		when(req.getParameter(EMAIL_PARAM_KEY)).thenReturn(INVALID_EMAIL);
		validator.validate(context, new SafeHttpServletRequestImpl(req), log2);
		assertTrue("Email validation with an invalid email address did not return any errors.", log2.hasErrors());

		// validate once with a valid email address
		ValidationLog log3 = new ValidationLog(info);
		when(req.getParameter("subject")).thenReturn("Some subject");
		when(req.getParameter("description")).thenReturn("Some description");
		when(req.getParameter(EMAIL_PARAM_KEY)).thenReturn(VALID_EMAIL);
		validator.validate(context, new SafeHttpServletRequestImpl(req), log3);
		assertFalse("Email validation with a valid email address returns errors.", log3.hasErrors());
	}

	public void testValidEmail()
	{
		assertFalse("Null email is flagged as valid.", CreateSupportRequestAction.isValidEmail(null));
		assertFalse("Invalid email is flagged as valid.", CreateSupportRequestAction.isValidEmail(INVALID_EMAIL));
		assertTrue("Null email is flagged as valid.", CreateSupportRequestAction.isValidEmail(VALID_EMAIL));
	}
}
