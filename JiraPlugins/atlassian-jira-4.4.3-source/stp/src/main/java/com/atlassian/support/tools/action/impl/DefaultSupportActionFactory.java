package com.atlassian.support.tools.action.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.support.tools.action.SupportActionFactory;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.hercules.SupportToolsHerculesScanAction;
import com.atlassian.support.tools.hercules.SupportToolsHerculesScanPercentageAction;
import com.atlassian.support.tools.request.CreateSupportRequestAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.zip.SupportZipAction;

public class DefaultSupportActionFactory implements SupportActionFactory
{
	private final Map<String, SupportToolsAction> ACTIONS = new HashMap<String, SupportToolsAction>();
	private final Map<String, List<SupportToolsAction>> actionsByCategory = new HashMap<String, List<SupportToolsAction>>();
	
	public DefaultSupportActionFactory(SupportApplicationInfo info, MailUtility mailUtility) throws GeneralSecurityException
	{
		addAction(new HomeAction(info));
		addAction(new SystemInfoAction(info));
		addAction(new SupportToolsHerculesScanAction(info));
		addAction(new SupportToolsHerculesScanPercentageAction(info));
		addAction(new SupportZipAction(info));
		addAction(new CreateSupportRequestAction(info, mailUtility));
	}

	private void addAction(SupportToolsAction action)
	{
		this.ACTIONS.put(action.getName(), action);
		
		// Build a map of actions by category
		if (action.getCategory() != null && action.getTitle() != null) {
			List<SupportToolsAction> actions = this.actionsByCategory.get(action.getCategory());
			if (actions == null) {
				actions = new ArrayList<SupportToolsAction>();
				this.actionsByCategory.put(action.getCategory(), actions);
			}
			
			actions.add(action);
		}
	}

	@Override
	public SupportToolsAction getAction(String name)
	{
		if(name == null) name = HomeAction.ACTION_NAME;
		SupportToolsAction action = this.ACTIONS.get(name);
		if(action != null)
			return action.newInstance();
		else
			return this.ACTIONS.get(HomeAction.ACTION_NAME).newInstance();
	}

	@Override
	public List<String> getActionCategories()
	{
		return new ArrayList<String>(this.actionsByCategory.keySet());
	}

	@Override
	public List<SupportToolsAction> getActionsByCategory(String category)
	{
		return this.actionsByCategory.get(category);
	}

	@Override
	public List<SupportToolsAction> getActions()
	{
		return (List<SupportToolsAction>) this.ACTIONS.values();
	}
}