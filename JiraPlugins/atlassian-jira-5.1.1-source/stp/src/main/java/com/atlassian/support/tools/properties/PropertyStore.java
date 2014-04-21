package com.atlassian.support.tools.properties;

import java.util.Map;

public interface PropertyStore {
	public PropertyStore addCategory(String key);
	public PropertyStore addCategory(String key, PropertyStore store);
	
	public Map<String,PropertyStore> getCategories();
	
	public void setValue(String key, String value);
	public Map<String,String> getValues();
	public void putValues(Map<String, String> values);
}
