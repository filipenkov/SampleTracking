package com.atlassian.mail.server;

/**
 * Whenever a {@link MailServer} object is created by {@link MailServerManager} implementation, it is supposed
 * to call such callback and let the implementation of it decide which extra configuration settings
 * needs to be applied to such newly created server object.
 *
 * @since v2.0-m3
 */
public interface MailServerConfigurationHandler {
	void configureMailServer(MailServer mailServer);
}
