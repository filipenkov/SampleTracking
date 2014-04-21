package com.atlassian.support.tools.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.support.tools.ValidationLog;

public interface Validateable
{
	void validate(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog);
}
