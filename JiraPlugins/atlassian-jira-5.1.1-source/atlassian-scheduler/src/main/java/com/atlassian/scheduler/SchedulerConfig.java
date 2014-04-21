/*
 * Atlassian Source Code Template.
 * User: sfarquhar
 * Date: Dec 5, 2002
 * Time: 5:40:48 PM
 * CVS Revision: $Revision: 1.11 $
 * Last CVS Commit: $Date: 2006/10/24 02:41:17 $
 * Author of last CVS Commit: $Author: jwesleysmith $
 */
package com.atlassian.scheduler;

import java.util.*;
import java.net.URL;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.XMLUtils;
import com.opensymphony.util.ClassLoaderUtil;
import org.apache.log4j.Category;
import org.quartz.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;

public class SchedulerConfig
{
    private static final Category log = Category.getInstance(SchedulerConfig.class);

    public static final String SCHEDULER_LOCATION = "atlassian.scheduler.location";

    private Map jobs;
    private List triggers;

    public SchedulerConfig()
    {
        this("scheduler-config.xml");
    }

    public SchedulerConfig(String configFile)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            URL fileUrl = ClassLoaderUtil.getResource(configFile, this.getClass());

            if (fileUrl == null)
                throw new IllegalArgumentException("No such XML file:" + configFile);

            // Parse document
            Document doc = factory.newDocumentBuilder().parse(fileUrl.toString());
            Element root = doc.getDocumentElement();

            loadJobs(root);
            loadTriggers(root);
        }
        catch (Exception e)
        {
            log.error("Could not create scheduler: " + e);
        }
    }

    private void loadJobs(Element root) throws ClassNotFoundException
    {
        NodeList jobEls = XMLUtils.getSingleChildElement(root, "jobs").getElementsByTagName("job");
        jobs = new HashMap();

        for (int i = 0; i < jobEls.getLength(); i++)
        {
            Element jobEl = (Element) jobEls.item(i);
            String name = jobEl.getAttribute("name");
            String clazz = jobEl.getAttribute("class");
            String group = XMLUtils.getAttributeWithDefault(jobEl, "group", Scheduler.DEFAULT_GROUP);

            JobDetail jobDetail = new JobDetail(name, group, ClassLoaderUtils.loadClass(clazz, this.getClass()), true, true, false);
            jobs.put(jobDetail.getFullName(), jobDetail);
        }
    }

    private void loadTriggers(Element root) throws Exception
    {
        NodeList triggerEls = root.getElementsByTagName("trigger");
        triggers = new ArrayList();

        for (int i = 0; i < triggerEls.getLength(); i++)
        {
            Element triggerEl = (Element) triggerEls.item(i);
            String type = XMLUtils.getAttributeWithDefault(triggerEl, "type", "simple");
            String name = triggerEl.getAttribute("name");
            String group = XMLUtils.getAttributeWithDefault(triggerEl, "group", Scheduler.DEFAULT_GROUP);
            String jobname = triggerEl.getAttribute("job");
            String jobgroup = XMLUtils.getAttributeWithDefault(triggerEl, "jobgroup", Scheduler.DEFAULT_GROUP);

            long startDelay = 0;

            if (XMLUtils.getSingleChildElement(triggerEl, "startDelay") != null)
            {
                String startDelayStr = XMLUtils.getContainedText(triggerEl, "startDelay");
                startDelay = DateUtils.getDuration(startDelayStr) * 1000;
            }

            Trigger trigger = null;
            Date start = new Date(System.currentTimeMillis() + startDelay);

            if ("cron".equalsIgnoreCase(type))
            {
                trigger = new CronTrigger(name, group, jobname, jobgroup, start, null, XMLUtils.getContainedText(triggerEl, "expression"));
                trigger.setVolatility(true);
            }
            else
            {
                int repeat = SimpleTrigger.REPEAT_INDEFINITELY;

                if (XMLUtils.getSingleChildElement(triggerEl, "repeat") != null)
                {
                    repeat = Integer.parseInt(XMLUtils.getContainedText(triggerEl, "repeat"));
                }

                long period = DateUtils.getDuration(XMLUtils.getContainedText(triggerEl, "period")) * 1000;

                try
                {
                    trigger = new AtlassianSimpleTrigger(name, group, jobname, jobgroup, start, null, repeat, period);
                    trigger.setVolatility(true);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            triggers.add(trigger);
        }
    }

    public Map getJobs()
    {
        return jobs;
    }

    public List getTriggers()
    {
        return triggers;
    }
}