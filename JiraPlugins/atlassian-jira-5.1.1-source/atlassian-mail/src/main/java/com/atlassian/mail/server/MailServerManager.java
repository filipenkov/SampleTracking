package com.atlassian.mail.server;

import com.atlassian.mail.MailException;

import javax.annotation.Nullable;
import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface MailServerManager
{
    /**
     * This class allows MailServers to be created, updated and deleted.
     * It also allows MailServers to be retrieved by id and name
     */
    String[] SERVER_TYPES = new String[]{"pop", "smtp"};

	@Nullable
    MailServer getMailServer(Long id) throws MailException;

	@Nullable
    MailServer getMailServer(String name) throws MailException;

    Long create(MailServer mailServer) throws MailException;

    void update(MailServer mailServer) throws MailException;

    void delete(Long mailServerId) throws MailException;

    List<String> getServerNames() throws MailException;

    List<SMTPMailServer> getSmtpMailServers();

    List<PopMailServer> getPopMailServers();

    @Nullable
	SMTPMailServer getDefaultSMTPMailServer();

    /**
     * Whether a &quot;default&quot; SMTP Mail Server has been defined.
     * @return true if a &quot;default&quot; SMTP Mail Server has been defined; otherwise, false.
     */
    boolean isDefaultSMTPMailServerDefined();

	@Nullable
    PopMailServer getDefaultPopMailServer();

    Session getSession(Properties props, @Nullable Authenticator auth) throws MailException;

    void init(Map params);

	/**
	 * Implementations of this interface are supposed to call registered here MailServerConfigurationHandler
	 * immediately after they construct MailServer objects.
	 *
	 * You can use it to further customise MailServer objects managed by this manager - e.g. initialize MailServer
	 * fields basing on application specific needs.
	 * This method is used to make up for lack of dependency injection while constructing MailServerManager implementation
	 * by {@link com.atlassian.mail.config.ConfigLoader#ConfigLoader(String)}
	 *
	 * @param mailServerConfigurationHandler callback called upon creation of MailServer objects
	 */
	void setMailServerConfigurationHandler(@Nullable MailServerConfigurationHandler mailServerConfigurationHandler);
}
