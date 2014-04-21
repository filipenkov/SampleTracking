package com.atlassian.support.tools.salext.mail;

import com.atlassian.mail.Email;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class ProductAwareEmail extends Email {
	private static final String PRODUCT_HEADER = "Atlassian-Product";

	public ProductAwareEmail(String to, String cc, String bcc, SupportApplicationInfo info) {
		super(to, cc, bcc);
		addProductHeader(info);
	}

	private void addProductHeader(SupportApplicationInfo info) {
		addHeader(PRODUCT_HEADER, String.valueOf(info.getApplicationName()));
	}

	public ProductAwareEmail(String to, SupportApplicationInfo info) {
		super(to);
	}
}
