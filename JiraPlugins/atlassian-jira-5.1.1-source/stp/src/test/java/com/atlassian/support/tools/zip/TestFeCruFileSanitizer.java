package com.atlassian.support.tools.zip;

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.atlassian.support.tools.salext.FisheyeApplicationInfo;

/**
 * This tests the regexes used to sanitize config.xml
 */
public class TestFeCruFileSanitizer extends TestCase
{
	private static final String ATTRIBUTE_EXPECTED = "<foo password=\"Sanitized by Support Utility\" the-password=\"Sanitized by Support Utility\"/> <bar passwords-file=\"zzz\"> <baz password=\"Sanitized by Support Utility\">";
	private static final String ATTRIBUTE_DIRTY = "<foo password=\"secret\" the-password=\"secret2\"/> <bar passwords-file=\"zzz\"> <baz password=\"secret3\">";
	private static final String PROPERTIES_EXPECTED = "<property name=\"applinks.admin.d3eb6609-1589-321b-9c2e-41005083ffdd.auth.cd7044beec9a3e9e9c52eaf03ee5b435\" value=\"%23java.util.Properties%0A%23Fri+Apr+01+11%3A09%3A54+EST+2011%0Apassword%3DSanitized by Support Utility%0Ausername%3Dmatt%0Apassword%3DSanitized by Support Utility%0A\"/>";
	private static final String PROPERTIES_DIRTY = "<property name=\"applinks.admin.d3eb6609-1589-321b-9c2e-41005083ffdd.auth.cd7044beec9a3e9e9c52eaf03ee5b435\" value=\"%23java.util.Properties%0A%23Fri+Apr+01+11%3A09%3A54+EST+2011%0Apassword%3Dmatt%0Ausername%3Dmatt%0Apassword%3Dfred%0A\"/>";

	private final FileSanitizer sanitizer = new FileSanitizer(FisheyeApplicationInfo.FILE_PATTERNS);
	private final List<Pattern> patterns = FisheyeApplicationInfo.FILE_PATTERNS.get("config.xml");

	public TestFeCruFileSanitizer(String name)
	{
		super(name);
	}
	
	private String sanitize(String line)
	{
		return sanitizer.sanitizeLine(patterns, line);
	}

	public void testAttribute()
	{
		assertEquals(ATTRIBUTE_EXPECTED, sanitize(ATTRIBUTE_DIRTY));
	}

	public void testEncodedProperties()
	{
		assertEquals(
				PROPERTIES_EXPECTED,
				sanitize(PROPERTIES_DIRTY));
	}

	public void testCombination()
	{
		assertEquals(
				PROPERTIES_EXPECTED + ATTRIBUTE_EXPECTED,
				sanitize(PROPERTIES_DIRTY + ATTRIBUTE_DIRTY));
	}

	public void testCrowdAuth()
	{
		assertEquals(
				"application.password=Sanitized by Support Utility",
				sanitize("application.password=secret"));
	}

	public void testLDAP()
	{
		assertEquals(
				"<ldap auto-add=\"true\" url=\"ldap://localhost:389\" base-dn=\"dc=example,dc=com\" filter=\"(uid=${USERNAME})\" uid-attr=\"uid\" positive-cache-ttl=\"5 minutes\" resyncPeriod=\"1 hour\" initial-dn=\"bind.dn\" resync=\"false\" displayname-attr=\"\" initial-secret=\"Sanitized by Support Utility\"/><admins><system-admins/></admins><avatar><disabled/></avatar><emailVisibility/></security><properties/><repository-defaults>",
				sanitize("<ldap auto-add=\"true\" url=\"ldap://localhost:389\" base-dn=\"dc=example,dc=com\" filter=\"(uid=${USERNAME})\" uid-attr=\"uid\" positive-cache-ttl=\"5 minutes\" resyncPeriod=\"1 hour\" initial-dn=\"bind.dn\" resync=\"false\" displayname-attr=\"\" initial-secret=\"secret\"/><admins><system-admins/></admins><avatar><disabled/></avatar><emailVisibility/></security><properties/><repository-defaults>"));
	}

	// note that it's better if an empty password stays empty from a support POV
	public void testEmptyPassword()
	{
		assertEquals("<foo password=\"\">", sanitize("<foo password=\"\">"));
	}
}