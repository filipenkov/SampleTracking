package com.atlassian.support.tools.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.salext.mail.ProductAwareEmail;
import com.atlassian.support.tools.scheduler.settings.HealthReportScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.utils.RenderingUtils;
import com.atlassian.support.tools.zip.ZipUtility;

public class ScheduledHealthReportTask extends AbstractScheduledTask
{
	private static final String TO_ADDRESS = "support-healthcheck@atlassian.com";
	private static final Logger log = Logger.getLogger(ScheduledHealthReportTask.class);

	@Override
	public void doExecute(Map<String, Object> jobDataMap)
	{
		if(log.isInfoEnabled())
			log.info("Executing scheduled health report at " + new Date());

		MailUtility mailUtility = (MailUtility) jobDataMap.get(SupportScheduledTaskControllerImpl.MAIL_UTILITY_KEY);
		SupportApplicationInfo info = (SupportApplicationInfo) jobDataMap.get(SupportScheduledTaskControllerImpl.APP_INFO_KEY);
		HealthReportScheduledTaskSettings settings = (HealthReportScheduledTaskSettings) jobDataMap.get(ScheduledTaskSettings.SETTINGS_KEY);

		if(mailUtility == null || info == null)
		{
			log.error("A required utility class was not provided, can't execute.");
			return;
		}

		ValidationLog validationLog = new ValidationLog(info);

		File zipFile;
		try
		{
			zipFile = ZipUtility.createSupportZip(info.getApplicationFileBundles(), info, validationLog, true);

			// Create a mail from a template that includes all the attached
			// information
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("info",info);
			String mailBody = RenderingUtils.render(info.getTemplateRenderer(), "/templates/email/health-check-confirmation.vm", params);	

			// Mail it to our support email
			String recipients = settings.getCcRecipients();
			String toAddress = TO_ADDRESS;
			if (recipients != null && recipients.length() > 0) {
				toAddress = TO_ADDRESS + "," + recipients;
			}
			
			ProductAwareEmail email = new ProductAwareEmail(toAddress,info);
			email.setFrom(info.getFromAddress());
			email.setCc(recipients);
			email.setSubject(info.getText("stp.health.email.subject", DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
			email.setBody(mailBody);

			byte[] data = IOUtils.toByteArray(new FileInputStream(zipFile));

			MimeMultipart multipart = new MimeMultipart();
			MimeBodyPart attachmentPart = new MimeBodyPart();
			DataSource fds = new ByteArrayDataSource(data, "application/zip");
			attachmentPart.setDataHandler(new DataHandler(fds));
			attachmentPart.setFileName(zipFile.getName());
			multipart.addBodyPart(attachmentPart);
			email.setMultipart(multipart);

			mailUtility.sendMail(email);
		}
		catch(IOException e)
		{
			log.error("There was an error creating the data bundle used by the health report:", e);
		}
		catch(MessagingException e)
		{
			log.error("There was an error attaching the data bundle used by the health report to the email message:", e);
		}
	}

	@Override
	protected String getName() {
		return "Scheduled health report";
	}
}
