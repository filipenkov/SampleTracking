package com.atlassian.security.auth.trustedapps;

import java.security.PublicKey;

/**
 * A simple data container
 */
public class SimpleApplication implements Application
{
	private final String id;
	private final PublicKey publicKey;

	public SimpleApplication(String id, PublicKey publicKey)
	{
		this.id = id;
		this.publicKey = publicKey;
	}

	public String getID()
	{
		return id;
	}

	public PublicKey getPublicKey()
	{
		return publicKey;
	}
}