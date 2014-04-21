package com.atlassian.support.tools.hercules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.sisyphus.DefaultSisyphusPatternMatcher;
import com.atlassian.sisyphus.PatternMatchSet;
import com.atlassian.sisyphus.SisyphusPatternMatcher;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.ActionError;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SupportToolsHerculesScanAction implements SupportToolsAction
{
	static final String ACTION_NAME = "hercules";

	public static final String FIELD_LOG_FILE_PATH = "logFilePath";

	private static final Logger log = Logger.getLogger(SupportToolsHerculesScanAction.class);
	private static final ThreadPoolExecutor scannerExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadFactory()
			{
				private final AtomicInteger threadCount = new AtomicInteger(0);

				@Override
				public Thread newThread(Runnable runnable)
				{
					Thread thread = new Thread(runnable, "HerculesWorker_" + this.threadCount.incrementAndGet());
					return thread;
				}
			});

	private static final String DEFAULT_REGEX_URL = SupportApplicationInfo.CONFLUENCE_REGEX_XML;

	private WebMatchResultVisitor visitor;
	private String patternSourceURL = DEFAULT_REGEX_URL;
	private String logFilePath;

	private final SupportApplicationInfo applicationInfo;

	public SupportToolsHerculesScanAction(SupportApplicationInfo info)
	{
		this.applicationInfo = info;
	}

	public SortedSet<PatternMatchSet> getResults()
	{
		SortedSet<PatternMatchSet> set = new TreeSet<PatternMatchSet>(new Comparator<PatternMatchSet>()
		{
			@Override
			public int compare(PatternMatchSet o1, PatternMatchSet o2)
			{
				return o2.getLastMatchedLine() - o1.getLastMatchedLine();
			}
		});
		set.addAll(this.visitor.getResults().values());
		return set;
	}

	public WebMatchResultVisitor getResultsVisitor()
	{
		return this.visitor;
	}

	public String getLogFilePath()
	{
		return this.logFilePath;
	}

	public void setLogFilePath(String logFilePath)
	{
		this.logFilePath = logFilePath;
	}

	public void setPatternSourceURL(String patternSourceURL)
	{
		this.patternSourceURL = patternSourceURL;
	}

	public String getPatternSourceURL()
	{
		return this.patternSourceURL;
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/hercules-execute.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/hercules-start.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/hercules-start.vm";
	}

	@Override
	public void prepare(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		if(req.getParameter("startAgain") != null)
		{
			this.visitor = WebMatchResultVisitor.getAttachedInstance(req.getSession());
			if(this.visitor != null)
			{
				this.visitor.setCancelled();
				this.visitor.detach(req.getSession());
			}
		}
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new SupportToolsHerculesScanAction(this.applicationInfo);
	}

	@Override
	public void validate(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		// Make sure we have either a log file path or a visitor
		this.visitor = WebMatchResultVisitor.getAttachedInstance(req.getSession());

		this.logFilePath = req.getParameter("logFilePath");
		if(this.logFilePath == null && this.visitor != null)
		{
			this.logFilePath = this.visitor.getLogFilePath();
		}
		context.put("logFilePath", this.logFilePath);

		if(this.visitor == null)
		{
			if(this.logFilePath == null || this.logFilePath.length() == 0)
			{
				validationLog.addFieldError(FIELD_LOG_FILE_PATH, "You must provide the location of a valid log file.");
			}
			else if( ! new File(this.logFilePath).exists())
			{
				validationLog.addFieldError(FIELD_LOG_FILE_PATH, "You must provide the location of a valid log file.");
			}
		}
		else if(this.visitor.getScanException() != null)
		{
			validationLog.addError(new ActionError("Error Scanning File",
					"The following exception occured when scanning your log file: "
							+ this.visitor.getScanException().getMessage()));
		}
	}

	@Override
	public void execute(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		this.visitor = WebMatchResultVisitor.getAttachedInstance(req.getSession());
		if(this.visitor == null)
		{
			this.logFilePath = req.getParameter("logFilePath");
			this.visitor = new WebMatchResultVisitor(this.logFilePath);
			this.visitor.attach(req.getSession());

			// Get the rules using a RemoteXMLPatternSource
			SisyphusPatternSource patternSource;
			try
			{
				patternSource = this.applicationInfo.getPatternSource();

				// Run SisyphusPatternMatcher.match against the results
				final SisyphusPatternMatcher spm = new DefaultSisyphusPatternMatcher(patternSource);

				scannerExecutor.execute(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							InputStream in = new FileProgressMonitorInputStream(new File(SupportToolsHerculesScanAction.this.logFilePath), SupportToolsHerculesScanAction.this.visitor);
							final BufferedReader br = new BufferedReader(new InputStreamReader(in));
							try
							{
								spm.match(br, SupportToolsHerculesScanAction.this.visitor);
							}
							catch(InterruptedException e)
							{
								SupportToolsHerculesScanAction.this.visitor.setCancelled();
							}
							catch(IOException e)
							{
								SupportToolsHerculesScanAction.this.visitor.scanFailed(e);
							}
							finally
							{
								SupportToolsHerculesScanAction.this.visitor.scanCompleted();
								try
								{
									br.close();
								}
								catch(Exception e)
								{
									log.debug(e.getMessage(), e);
								}
							}
						}
						catch(IOException e)
						{
							SupportToolsHerculesScanAction.this.visitor.scanFailed(e);
						}
					}
				});
			}
			catch(MalformedURLException e)
			{
				this.visitor.scanFailed(e);
			}
			catch(IOException e)
			{
				this.visitor.scanFailed(e);
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			this.logFilePath = this.visitor.getLogFilePath();
		}
	}
	
	@Override
	public String getCategory()
	{
		return "stp.troubleshooting.category.title";
	}

	@Override
	public String getTitle()
	{
		return "stp.hercules.tool.title";
	}
}