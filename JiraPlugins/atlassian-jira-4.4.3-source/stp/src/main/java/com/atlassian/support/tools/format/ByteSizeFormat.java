package com.atlassian.support.tools.format;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class ByteSizeFormat extends NumberFormat
{

	@Override
	public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2)
	{
		DecimalFormat decimalFormatter = new DecimalFormat("#,###.#");
		
		// Bytes
		if (arg0 < 1024) {
			arg1.append(arg0 + "B");
		}
		// KiloBytes
		else if (arg0 < (1024 * 1024)) {
			arg1.append(decimalFormatter.format(arg0/1024) + "KB");
		}
		// MegaBytes
		else if (arg0 < (1024 * 1024 * 1024)) {
			arg1.append(decimalFormatter.format(arg0/(1024 * 1024)) + "MB");
		}
		// GigaBytes
		else if (arg0 < (1024 * 1024 * 1024)) {
			arg1.append(decimalFormatter.format(arg0/(1024 * 1024 * 1024))  + "GB");
		}

		return arg1;
	}

	@Override
	public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2)
	{
		return this.format((double) arg0,arg1,arg2);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition)
	{
		return null;
	}
}
