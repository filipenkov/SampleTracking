/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer;

import com.atlassian.jira.util.dbc.Assertions;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;

import java.text.MessageFormat;
import java.util.List;

public class MockI18nHelper extends com.atlassian.jira.mock.i18n.MockI18nHelper {
	public String getText(final String key, final Object parameters)
	{
		Assertions.notNull("key", key);
		Object[] params = resolveParams(parameters);
		String message = ITUtils.getText(key, params);
		if (message == null)
		{
			return key;
		}
		return new MessageFormat(message).format(params);
	}

	private Object[] resolveParams(final Object parameters)
	{
		final Object[] params;
		if (parameters instanceof List)
		{
			params = ((List<?>) parameters).toArray();
		}
		else if (parameters instanceof Object[])
		{
			params = (Object[]) parameters;
		}
		else
		{
			params = new Object[] { parameters };
		}
		return params;
	}
}
