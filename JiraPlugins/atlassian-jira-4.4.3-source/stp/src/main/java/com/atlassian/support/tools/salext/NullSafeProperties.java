package com.atlassian.support.tools.salext;

import java.util.Properties;

public class NullSafeProperties extends Properties
{
	@Override
	public synchronized Object put(Object key, Object value)
	{
		if(key != null && value != null)
		{
			return super.put(key, value);
		}
		return null;
	}

}
