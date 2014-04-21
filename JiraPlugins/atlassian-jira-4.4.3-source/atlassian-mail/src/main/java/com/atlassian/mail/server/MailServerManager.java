package com.atlassian.mail.server;

import alt.javax.mail.Session;
import alt.javax.mail.Transport;
import com.atlassian.mail.MailException;

import javax.mail.Authenticator;
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

    MailServer getMailServer(Long id) throws MailException;

    MailServer getMailServer(String name) throws MailException;

    Long create(MailServer mailServer) throws MailException;

    void update(MailServer mailServer) throws MailException;

    void delete(Long mailServerId) throws MailException;

    List getServerNames() throws MailException;

    List getSmtpMailServers() throws MailException;

    List getPopMailServers() throws MailException;

    SMTPMailServer getDefaultSMTPMailServer() throws MailException;

    PopMailServer getDefaultPopMailServer() throws MailException;

    Session getSession(Properties props, Authenticator auth) throws MailException;

    void init(Map params);
}
