package com.atlassian.mail.server.managers;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.mail.MailException;
import com.atlassian.mail.config.ConfigLoader;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLMailServerManager extends AbstractMailServerManager {
	private static final Logger log = Logger.getLogger(XMLMailServerManager.class);

	// Map of server id -> server
	Map<Long, MailServer> serverIds;

	private static String DEFAULT_CONFIG_FILE = "mail-servers.xml";

	String configFile;

	public void init(Map params) {
		configFile = DEFAULT_CONFIG_FILE;
		serverIds = new HashMap<Long, MailServer>();

		if (params.containsKey("config-file")) {
			configFile = (String) params.get("config-file");
		}

		configure();
	}

	private void configure() {
		// configure from config file
		try {
            Digester digester = newDigester();
            digester.push(this);
			digester.setRules(new ExtendedBaseRules());

			// load pop servers
			digester.addObjectCreate("mail-servers/pop-server", getPopMailServerClass());
			digester.addSetProperties("mail-servers/pop-server");
			digester.addBeanPropertySetter("mail-servers/pop-server/?");
			digester.addSetRoot("mail-servers/pop-server", "create");

			// load smtp servers
			digester.addObjectCreate("mail-servers/smtp-server", getSMTPMailServerClass());
			digester.addSetProperties("mail-servers/smtp-server");
			digester.addBeanPropertySetter("mail-servers/smtp-server/?");
			digester.addBeanPropertySetter("mail-servers/smtp-server/jndi-location", "jndiLocation");
			digester.addSetRoot("mail-servers/smtp-server", "create");

			InputStream is = getConfigurationInputStream(configFile);
			digester.parse(is);


		} catch (Exception e) {
			log.fatal(e, e);
			throw new RuntimeException("Error in mail config: " + e.getMessage(), e);
		}
	}

    protected Digester newDigester()
    {
        return new Digester();
    }

    protected InputStream getConfigurationInputStream(String resource) {
		return ClassLoaderUtils.getResourceAsStream(resource, ConfigLoader.class);
	}

	public String getConfigFile() {
		return configFile;
	}

	@Nullable
	public MailServer getMailServer(Long id) {
		return serverIds.get(id);
	}

	@Nullable
	public MailServer getMailServer(String name) throws MailException {
		if (name == null) {
			throw new MailException("name is null");
		}

		for (MailServer server : serverIds.values()) {
			if (name.equals(server.getName())) {
				return server;
			}
		}

		return null;
	}

	public synchronized Long create(MailServer mailServer) throws MailException {
		Long id = new Long((serverIds.size() + 1));

		// check if this id already exists. If it does, increment until a vacant one is found
		// this will not scale! (we are assuming there won't be that many mail servers)
		while (serverIds.containsKey(id)) {
			id = new Long(id.longValue() + 1);
		}

		mailServer.setId(id);
		serverIds.put(id, mailServer);
		return id;
	}

	public void update(MailServer mailServer) throws MailException {
		serverIds.put(mailServer.getId(), mailServer);
	}

	public void delete(Long mailServerId) throws MailException {
		if (mailServerId == null) {
			throw new MailException("mailServerId is null");
		}
		if (!serverIds.containsKey(mailServerId)) {
			throw new MailException("A mail server with the specified mailServerId does not exist");
		}

		serverIds.remove(mailServerId);
	}

	public List<String> getServerNames() throws MailException {
		List<String> result = new ArrayList<String>();

		for (MailServer server : serverIds.values()) {
			result.add(server.getName());
		}

		return result;
	}

    public List<SMTPMailServer> getSmtpMailServers() {
        final List<SMTPMailServer> result = new ArrayList<SMTPMailServer>();

        for (MailServer server : serverIds.values()) {
            if (server instanceof SMTPMailServer) {
                result.add((SMTPMailServer) server);
            }
        }

        return result;
    }

    public List<PopMailServer> getPopMailServers() {
		final List<PopMailServer> result = new ArrayList<PopMailServer>();

		for (MailServer server : serverIds.values()) {
			if (server instanceof PopMailServer) {
				result.add((PopMailServer) server);
			}
		}

		return result;
	}

	@Nullable
	public SMTPMailServer getDefaultSMTPMailServer() {
		List<SMTPMailServer> smtpServers = getSmtpMailServers();

		if (smtpServers.size() > 0) {
			return smtpServers.get(0);
		}

		return null;
	}

	@Nullable
	public PopMailServer getDefaultPopMailServer() {
		List<PopMailServer> popServers = getPopMailServers();

		if (popServers.size() > 0) {
			return popServers.get(0);
		}

		return null;
	}

	protected Class getSMTPMailServerClass() {
		return SMTPMailServerImpl.class;
	}

	protected Class getPopMailServerClass() {
		return PopMailServerImpl.class;
	}
}
