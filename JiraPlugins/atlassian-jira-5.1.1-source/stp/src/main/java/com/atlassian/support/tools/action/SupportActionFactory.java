package com.atlassian.support.tools.action;

import java.util.List;

public interface SupportActionFactory
{
	SupportToolsAction getAction(String name);
	
	List<String> getActionCategories();
	List<SupportToolsAction> getActionsByCategory(String category);
	List<SupportToolsAction> getActions();
}
