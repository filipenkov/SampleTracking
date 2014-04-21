package com.atlassian.support.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.support.tools.action.ActionError;
import com.atlassian.support.tools.action.ActionWarning;
import com.atlassian.support.tools.action.Message;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class ValidationLog
{
	private final SupportApplicationInfo appInfo;

	public ValidationLog(SupportApplicationInfo appInfo)
	{
		super();
		this.appInfo = appInfo;
	}

	private final List<ActionError> errors = new ArrayList<ActionError>();
	private final Map<String, List<ActionError>> fieldErrors = new HashMap<String, List<ActionError>>();

	private final List<ActionWarning> warnings = new ArrayList<ActionWarning>();
	private final Map<String, List<ActionWarning>> fieldWarnings = new HashMap<String, List<ActionWarning>>();

	public void addError(ActionError error)
	{
		this.errors.add(error);
	}

	public void addWarning(ActionWarning warning)
	{
		this.warnings.add(warning);
	}

	public void addFieldError(String fieldName, ActionError error)
	{
		addItem(this.fieldErrors, fieldName, error);
		addError(error);
	}

	public void addFieldError(String fieldName, String body)
	{
		String localizedText = this.appInfo.getText(body);
		ActionError error = new ActionError(localizedText, localizedText);
		addItem(this.fieldErrors, fieldName, error);
		addError(error);
	}

	public void addFieldError(String fieldName, String i18nKey, Serializable i18nParameters)
	{
		String localizedText = this.appInfo.getText(i18nKey, i18nParameters);
		ActionError error = new ActionError(localizedText, localizedText);
		addItem(this.fieldErrors, fieldName, error);
		addError(error);
	}

	public void addFieldWarning(String fieldName, ActionWarning warning)
	{
		addItem(this.fieldWarnings, fieldName, warning);
		addWarning(warning);
	}

	public void addFieldWarning(String fieldName, String body)
	{
		String localizedText = this.appInfo.getText(body);
		ActionWarning warning = new ActionWarning(localizedText, localizedText);
		addItem(this.fieldWarnings, fieldName, warning);
		addWarning(warning);
	}

	public void addFieldWarning(String fieldName, String i18nKey, Serializable i18nParameters)
	{
		String localizedText = this.appInfo.getText(i18nKey, i18nParameters);
		ActionWarning warning = new ActionWarning(localizedText, localizedText);
		addItem(this.fieldWarnings, fieldName, warning);
		addWarning(warning);
	}

	private <T extends Message> void addItem(Map<String, List<T>> map, String fieldName, T message)
	{
		List<T> list = map.get(fieldName);
		if(list == null)
		{
			list = new ArrayList<T>();
			map.put(fieldName, list);
		}
		list.add(message);
	}

	public boolean hasErrors()
	{
		return ! this.errors.isEmpty() || ! this.fieldErrors.isEmpty();
	}

	public boolean hasWarnings()
	{
		return ! this.warnings.isEmpty() || ! this.fieldWarnings.isEmpty();
	}

	public boolean hasFieldErrors(String fieldName)
	{
		if(this.fieldErrors.containsKey(fieldName) && this.fieldErrors.get(fieldName) != null
				&& ! this.fieldErrors.get(fieldName).isEmpty()) return true;
		return false;
	}

	public boolean hasFieldWarnings(String fieldName)
	{
		if(this.fieldWarnings.containsKey(fieldName) && this.fieldWarnings.get(fieldName) != null
				&& ! this.fieldWarnings.get(fieldName).isEmpty()) return true;
		return false;
	}

	public List<ActionError> getErrors()
	{
		return this.errors;
	}

	public <T extends Message> List<T> getFieldMessages(String fieldName, Map<String, List<T>> messages)
	{
		List<T> list = messages.get(fieldName);
		if(list != null)
		{
			return list;
		}
		return Collections.emptyList();
	}

	public List<ActionError> getFieldErrors(String fieldName)
	{
		return getFieldMessages(fieldName, this.fieldErrors);
	}

	public List<ActionWarning> getWarnings()
	{
		return this.warnings;
	}

	public List<ActionWarning> getFieldWarnings(String fieldName)
	{
		return getFieldMessages(fieldName, this.fieldWarnings);
	}

	public void addError(String i18nKey, Serializable... arguments)
	{
		addLocalizedError(this.appInfo.getText(i18nKey, arguments));
	}

	public void addWarning(String i18nKey, Serializable... arguments)
	{
		addLocalizedWarning(this.appInfo.getText(i18nKey, arguments));
	}

	public void addLocalizedWarning(String warningText)
	{
		addWarning(new ActionWarning(warningText, warningText));
	}

	public void addLocalizedError(String errorText)
	{
		addError(new ActionError(errorText, errorText));
	}
}
