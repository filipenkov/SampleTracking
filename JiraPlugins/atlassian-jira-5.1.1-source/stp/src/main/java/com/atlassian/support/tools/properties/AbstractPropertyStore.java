package com.atlassian.support.tools.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;

public abstract class AbstractPropertyStore implements PropertyStore {
	private Map<String,String> values = new HashMap<String, String>();
	
	@Override
	public void setValue(String key, String value) {
		values.put(escapeXmlElementName(key), StringEscapeUtils.escapeXml(value));
	}

	@Override
	public Map<String, String> getValues() {
		return Collections.unmodifiableMap(values);
	}

	@Override
	public void putValues(Map<String, String> newValues) {
		for (Entry<String, String> entry : newValues.entrySet()) {
			setValue(entry.getKey(),entry.getValue());
		}
	}

	/**
	 * Escape a key that will be used as an XML element name, replacing colons with dashes to avoid 
	 * "this:that" being interpreted as element "that" in namespace "this". 
	 * 
	 * @param A key that is intended for use as an XML element name.
	 * @return The escaped value of the key.
	 */
	private String escapeXmlElementName(String key) {
		String escapedKey = StringEscapeUtils.escapeXml(key);
		return escapedKey.replace(":", "-");
	}
}
