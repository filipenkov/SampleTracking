package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.JavaRegexConverter;
import com.atlassian.uwc.converters.PerlConverter;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.converters.twiki.TWikiRegexConverterCleanerWrapper;
import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlEvents;
import com.atlassian.uwc.filters.FilterChain;
import com.atlassian.uwc.hierarchies.HierarchyBuilder;
import com.atlassian.uwc.splitters.PageSplitter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.ConverterErrors;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.State;
import com.atlassian.uwc.ui.UWCForm2;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.listeners.FeedbackHandler;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code taken from Universal Wiki Converter. Adapter (cut down) to our needs.
 */
public class ConverterEngine implements FeedbackHandler {

	/* START CONSTANTS */
	private static final int NUM_REQ_CONVERTERS = 2;
	private static final String NONCONVERTERTYPE_PAGEHISTORYPRESERVATION = "page-history-preservation";
	private static final String NONCONVERTERTYPE_HIERARCHYBUILDER = ".hierarchy-builder";
	private static final String NONCONVERTERTYPE_ILLEGALHANDLING = "illegal-handling";
	private static final String NONCONVERTERTYPE_AUTODETECTSPACEKEYS = "autodetect-spacekeys";
	private static final String NONCONVERTERTYPE_MISCPROPERTIES = ".property";
	private static final String NONCONVERTERTYPE_FILTERS = ".filter";
	private static final String NONCONVERTERTYPE_XMLEVENT = ".xmlevent";
	private static final String CONVERTERTYPE_TWIKICLEANER = ".twiki-cleaner";
	private static final String CONVERTERTYPE_JAVAREGEX = ".java-regex";
	private static final String CONVERTERTYPE_JAVAREGEXTOKEN = ".java-regex-tokenize";
	private static final String CONVERTERTYPE_PERL = ".perl";
	private static final String CONVERTERTYPE_CLASS = ".class";

	private static final String XMLEVENT_PROP_ERROR = "Xmlevent Property must follow this format convention: {tag}xmltag{class}com.something.Class";

	private static final int DEFAULT_NUM_STEPS = 1000;

	/* START FIELDS */
	public boolean running = false; //Methods check this to see if the conversion needs to be cancelled

	/**
	 * used to disable check for illegal names and links.
	 * We want to allow users to override this so they can handle it themselves
	 * with converters.
	 */
	private boolean illegalHandlingEnabled = true; //default = true

    Logger log = Logger.getLogger(this.getClass());

    /**
     * The string that directory separators (e.g., / on Unix and \ on Windows) are replaced
     * with in page titles.
     * This is used by DokuWikiLinkConverter too.
     */
    public static final String CONFLUENCE_SEPARATOR = " -- ";

    protected enum HierarchyHandler {
    	DEFAULT, 			//no hierarchy handling
    	HIERARCHY_BUILDER,	//hierarchyBuilder handles
    	PAGENAME_HIERARCHIES//hierarchy maintained in pagename
    }
    private HierarchyHandler hierarchyHandler = HierarchyHandler.DEFAULT;

    /**
     * This field is set if a hierarchy builder "converter" is used. The field controls the
     * way in which pages are added/updated in Confluence. If hierarchyBuilder is <code>null</code>, all
     * pages are added as top-level pages in the selected space. Otherwise, the hierarchy builder is
     * called on to create a page hierarchy, and the engine will insert the pages correspondingly.
     */
    private HierarchyBuilder hierarchyBuilder = null;
	private UWCUserSettings settings;
	private State state;
	private Properties miscProperties = new Properties(); //instantiate this here - UWC-293
	private Set<String> filterValues;

	/**
	 * the number of properties that are not converters from the properties file.
	 * When we set up the progress bar, we calculate the max number of steps
	 * we're going to encounter with the number of properties that could be converters.
	 * But we update the progress bar a step only foreach converter property. So, we'll use
	 * this field to update the progress bar the extra amount.
	 */
	private Feedback feedback;

    HashMap<String, Converter> converterCacheMap = new HashMap<String, Converter>();
	private long startTotalConvertTime;

	//Error handlers
	private ConverterErrors errors = new ConverterErrors();
	private boolean hadConverterErrors;

    /**
     * converts the files with the converterstrings, and hooks any feedback into the given ui
     * @param inputPages pages from the filesystem to be converted
     * @param converterStrings list of converters as strings which will be run on the pages
     */
    public void convert(File outputDir, List<File> inputPages, List<String> converterStrings, UWCUserSettings settings) {
    	//setup
    	this.running = true;
		resetFeedback();
		resetErrorHandlers();
		resetHierarchy();

		//settings
    	this.settings = settings;
    	if (!this.running) {
    		this.feedback = Feedback.CANCELLED;
    		return;
    	}

    	//convert
		convert(outputDir, inputPages, converterStrings);

		//cleanup
		if (this.feedback == Feedback.NONE)
			this.feedback = Feedback.OK;
		this.running = false;
    }

	/**
	 * gets a new State object, using a default number of steps.
	 * The State object will be used by the converter engine to measure progress.
	 * @param settings
	 * @return
	 */
	public State getState(UWCUserSettings settings) {
		int steps = DEFAULT_NUM_STEPS;
		String initialMessage = "Converting Wiki\n" +
				"Wikitype = " + settings.getWikitype() + "\n";
		this.state = new State(initialMessage, 0, steps);
		return state;
	}

	/**
	 * Counts the number of steps needed to do an entire conversion from start to finish.
	 * Used with progress monitor
	 * @param files number of files or directories chosen by the user. Does not count contents of directories seperately.
	 * @param pages number of individual pages that will be converted
	 * @param properties number of all converter file properties (including non-converter properties)
	 * @param converters number of converters
	 * @return number of steps for performing conversion from start to finish
	 */
	private int getNumberOfSteps(int files, int pages, int properties, int converters) {
		int numReqConverters = isIllegalHandlingEnabled()?NUM_REQ_CONVERTERS:0;
		int steps =
			properties + 					//1. initialize converters (handles both converter and non-converter properties)
			files + 						//2. create page objects (uses the original list of chosen file objects)
			(converters * pages) +			//3. convert the files (uses the number of page objects)
			(numReqConverters) +			//4. create required converters (2, right now)
			(numReqConverters * pages) + 	//5. convert with required converters (2, right now)
			pages; 						//6. save the files
		return steps;
	}

	/**
     * converts the given pages not filtered out with the given filterPattern
     * using the given converterStrings, and sends the pages to Confluence,
     * if sendToConfluence is true
     * @param pages
     * @param converterStrings
     */
    public void convert(File outputDir, List<File> pages, List<String> converterStrings) {
    	log.info("Starting conversion.");

    	initConversion();

    	//create converters
    	List<Converter> converters = createConverters(converterStrings);

    	//create page objects - Recurse through directories, adding all files
    	List<Page> allPages = createPages(pages);

    	//fix progressbar max, which is dependent on the previous two lists
    	int steps = getNumberOfSteps(pages.size(), allPages.size(), converterStrings.size(), converters.size());
    	this.state.updateMax(steps);


    	//convert the files
    	if (convertPages(allPages, converters)) {
    		//in case converting the pages disqualified some pages, we need to break if there are no pages left
    		if (allPages.size() < 1) {
    			String message = "All pages submitted were disqualified for various reasons. Could not complete conversion.";
				log.warn(message);
				this.errors.addError(Feedback.CONVERTER_ERROR, message, true);
				this.state.updateMax(this.state.getStep()); //complete progress bar, prematurely
				return;
    		}
    		//in case converting the pages disqualified some pages, we need to recompute progressbarmax
    		steps = getNumberOfSteps(pages.size(), allPages.size(), converterStrings.size(), converters.size());
    		if (steps != this.state.getMax()) this.state.updateMax(steps);

        	//save pages
			// TODO: This is not needed for the converter, and there should really be an option to turn it off.
			savePages(outputDir, allPages);
		}
    	log.info("Conversion Complete");
    }

    /**
     * handle any cleanup
     */
    protected void initConversion() {
		this.miscProperties.clear();
	}

	/**
     * Instantiate all the converterStrings
     *
     * @param converterStrings a list of converter strings of the form "key=value"
     * @return a list of converters
     */
    public List<Converter> createConverters(List<String> converterStrings) {
    	return createConverters(converterStrings, true);
    }

    public List<Converter> createConverters(List<String> converterStrings, boolean runningState) {
    	String message = "Initializing Converters...";
		if (runningState) this.state.updateNote(message);
		log.info(message);

        new DefaultXmlEvents().clearAll(); 	//everytime this method is called, have a clean slate of events

        ArrayList<Converter> converters = new ArrayList<Converter>();
        for (String converterStr : converterStrings) {
        	if (runningState) this.state.updateProgress();
        	if (runningState && !this.running) {
        		this.feedback = Feedback.CANCELLED;
        		return Collections.<Converter> emptyList();
        	}
            Converter converter;
            if (isNonConverterProperty(converterStr)) {
            	handleNonConverterProperty(converterStr);
            	continue;
            }
        	converter = getConverterFromString(converterStr);
        	if (converter == null) {
        		continue;
        	}
        	converters.add(converter);
        }
        if (runningState) addDefaultMiscProperties();

        return converters;
    }

	/**
     * Instantiates a converter from a correctly formatted String.
     * <p/>
     * Note: This method is now only called once per converter -- first all converters
     * are created, then all pages, then all converters are run on all pages.
     *
     * @param converterStr A string of the form "name.keyword=parameters". The
     *  keyword is used to create the correct type of converter, and the parameters
     *  are then passed to the converter. Finally, the "name.keyword" part is set as
     *  the key in the converter, mainly for debugging purposes.
     * @return converter or null if no converter can be parsed/instantiated
     */
	@Nullable
    public Converter getConverterFromString(String converterStr) {
        Converter converter;
        int equalLoc = converterStr.indexOf("=");
        String key = converterStr.substring(0, equalLoc);
        String value = converterStr.substring(equalLoc + 1);
        try {
            if (key.indexOf(CONVERTERTYPE_CLASS) >= 0) {
                converter = getConverterClassFromCache(value);
            } else if (key.indexOf(CONVERTERTYPE_PERL) >= 0) {
                converter = PerlConverter.getPerlConverter(value);
                converter.setValue(value);
            } else if (key.indexOf(CONVERTERTYPE_JAVAREGEXTOKEN) >= 0) {
                converter = JavaRegexAndTokenizerConverter.getConverter(value);
                converter.setValue(value);
            } else if (key.indexOf(CONVERTERTYPE_JAVAREGEX) >= 0) {
                converter = JavaRegexConverter.getConverter(value);
                converter.setValue(value);
            } else if (key.indexOf(CONVERTERTYPE_TWIKICLEANER) >= 0) {
                //converter = getConverterClassFromCache(value);
                converter = TWikiRegexConverterCleanerWrapper.getTWikiRegexConverterCleanerWrapper(value);
                converter.setValue(value);
            } else {
                String note = "Converter ignored -- name pattern not recognized: " + key;
				this.errors.addError(Feedback.BAD_PROPERTY, note, true);
				log.error(note);
                return null;
            }
            converter.setProperties(this.miscProperties);
        } catch (ClassNotFoundException e) {
            this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- the Java class " + value + " was not found", true);
            return null;
        } catch (IllegalAccessException e) {
            this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- there was a problem creating a converter object", true);
            return null;
        } catch (InstantiationException e) {
            this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- there was a problem creating the Java class " + value, true);
            return null;
        } catch (ClassCastException e) {
            this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- the Java class " + value +
                    " must implement the " + Converter.class.getName() + " interface!", true);
            return null;
        }
        converter.setKey(key);
        return converter;
    }

    /**
     * handles necessary state changes for expected properties
     * that were set in the converter properties file.
     * expected nonconverter properties include hierarchy builder properties
     * and page history preservation properties
     * @param converterStr should be a line from the converter properties file
     * Example:
     * MyWiki.0001.someproperty.somepropertytype=setting
     * <br/>
     * where somepropertytype is an expected property type:
     * <br/>
     * NONCONVERTERTYPE_HIERARCHYBUILDER or NONCONVERTERTYPE_PAGEHISTORYPRESERVATION
     * or NONCONVERTERTYPE_ILLEGALHANDLING
     * or NONCONVERTERTYPE_AUTODETECTSPACEKEYS
     * or NONCONVERTERTYPE_FILTERS
     * or NONCONVERTERTYPE_MISCPROPERTIES
     */
    protected void handleNonConverterProperty(String converterStr) {
    	int equalLoc = converterStr.indexOf("=");
        String key = converterStr.substring(0, equalLoc);
        String value = converterStr.substring(equalLoc + 1);
        String parent = "";
        try {
	    	if (key.indexOf(NONCONVERTERTYPE_HIERARCHYBUILDER) >= 0) {
	    		if (isHierarchySwitch(key))
	    			setHierarchyHandler(value);
	    		else {
					parent = HierarchyBuilder.class.getName();
		            Class c;
						c = Class.forName(value);
		            HierarchyBuilder hierarchy = (HierarchyBuilder) c.newInstance();
		            hierarchyBuilder = hierarchy;
		            this.hierarchyBuilder.setProperties(this.miscProperties);
	    		}
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_PAGEHISTORYPRESERVATION)) {
	    		handlePageHistoryProperty(key, value);
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_ILLEGALHANDLING)) {
	    		handleIllegalHandling(key, value);
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_AUTODETECTSPACEKEYS)) {
	    		handleAutoDetectSpacekeys(key, value);
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_MISCPROPERTIES)) {
	    		handleMiscellaneousProperties(key, value);
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_FILTERS)) {
	    		parent = FileFilter.class.getName();
	    		handleFilters(key, value);
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_XMLEVENT)) {
	    		handleXmlEvents(key, value);
	    	}
        } catch (ClassNotFoundException e) {
            String message = "Property ignored -- the Java class " + value + " was not found";
            log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
        } catch (IllegalAccessException e) {
            String message = "Property ignored -- there was a problem creating the Java class: " + value +
            	".\n" +
            	"Note: A necessary method's permissions were too restrictive. Check the constructor. ";
            log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
        } catch (InstantiationException e) {
            String message = "Property ignored -- there was a problem creating the Java class " + value +
            	".\n" +
            	"Note: The class cannot be instantiated as it is abstract or is an interface.";
            log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
        } catch (ClassCastException e) {
			String message = "Property ignored -- the Java class " + value +
			                    " must implement the " + parent + " interface!";
            log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
        } catch (IllegalArgumentException e) {
        	String message = "Property ignored -- property value was not in expected format.";
        	log.error(message);
        	this.errors.addError(Feedback.BAD_PROPERTY, message, true);
        }
    }

    /**
     * at long last making some performance enhancements
     * here we are creating an object cache which should help a bit
     *
     * @param key A string representing the converter (actually the part after the
     *        equals sign of the converter string).
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Converter getConverterClassFromCache(String key) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Converter converter = converterCacheMap.get(key);
        if (converter == null) {
            Class c = Class.forName(key);
            converter = (Converter) c.newInstance();
            converterCacheMap.put(key, converter);
        }
        return converter;
    }

    /**
     * creates file filter.
     * If we have no filter values, returns null.
     * If we have at least one filter value, uses the FilterChain class
     * to create FileFilter that will handle all of the filter requirements.
     * There are two types of supported filters: Class filters, and endswith filters.
     * Class filters are fully resolved class names for classes that implement FileFilter.
     * Endswith filters are text strings that the end of the filename must conform to.
     * If there are more than one filter invoked, the following will be used to resolve
     * which files to accept: Only pages that all class filters accept as long as
     * any endswith filter accepts as well will be included. (Class filters are ANDed.
     * Endswith filters are ORed.) Example: If you had two endswiths, and a class: ".txt",
     * ".xml", and NoSvnFilter, then .txt and .xml files that the NoSvnFilter accepts
     * will be included.
     * @return FileFilter or null
     */
	@Nullable
    protected FileFilter createFilter(final String pattern) {
    	Set<String> values = getFilterValues();
		if (pattern != null && !"".equals(pattern))
    		values.add(pattern);

    	if (values.isEmpty()) return null;

    	FilterChain chain = new FilterChain(values, this.miscProperties);
    	return chain.getFilter();
    }

    /**
     * Creates PageForXmlRpcOld objects for all the files in inputPages.
     *
     * @param inputPages   A list of files and directories that Pages should be created for.
     * @return A list of PageForXmlRpcOld objects for all files matching the pattern in the settings.
     */
    protected List<Page> createPages(List<File> inputPages) {
       	String message = "Initializing Pages...";
		this.state.updateNote(message);
		log.info(message);

        List<Page> allPages = new LinkedList<Page>();

        for (File fileOrDir : inputPages) {
        	this.state.updateProgress();
            List<Page> pages = recurse(fileOrDir);
            setupPages(fileOrDir, pages);
            allPages.addAll(pages);
        }

        return allPages;
    }

	/**
     * Recurses through a directory structure and adds all files in it matching the filter.
     * Called by createPages.
     *
     * @param fileOrDir A directory or file. Must not be <code>null</code>.
     * @return A list with PageForXmlRpcOld objects for all the matching files in the directory and its subdirectories
     */
    private List<Page> recurse(File fileOrDir) {
        assert fileOrDir != null;
        List<Page> result = new LinkedList<Page>();
        if (fileOrDir.isFile()) {									//it's a file AND
			PageSplitter splitter = getPageSplitter();
			if (splitter == null)
				result.add(new Page(fileOrDir));
			else
				result.addAll(splitter.split(fileOrDir));
        } else if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            for (File file : files) {
                result.addAll(recurse(file));
            }
        }
        else { //some other problem
        	String message = "Could not find file: '" +
			        			fileOrDir.getAbsolutePath() +
			        			"'.\n" +
			        			"Check existence and permissions.";
			log.warn(message);
			this.errors.addError(Feedback.BAD_FILE, message, true);
        }
        return result;
    }

	@Nullable
    private PageSplitter getPageSplitter() {
    	String classname = this.miscProperties.getProperty("pagesplitter", null);
    	if (classname == null) return null;
    	Class c;
		try {
			c = Class.forName(classname);
		} catch (ClassNotFoundException e) {
			log.error("Could not find pagesplitter class named: " + classname, e);
			return null;
		}
		try {
			PageSplitter splitter = (PageSplitter) c.newInstance();
			return splitter;
		} catch (InstantiationException e) {
			log.error("Could not instantiate pagesplitter class named: " + classname, e);
		} catch (IllegalAccessException e) {
			log.error("Pagesplitter class can not legally be accessed: " + classname, e);
		}
		return null;
	}

	/**
     * Set the names of the pages and performs any other setup needed. Called by recurse().
     * If the user selected a directory and this file is inside it, the base directory's
     * path is removed and the rest is used as the page name.
     * <p/>
     * Any directory separators are replaced with the constant CONFLUENCE_SEPARATOR.
     *
     * @param baseDir The directory that the top-level documents are in
     * @param pages A list of pages to set up
     */
    protected void setupPages(File baseDir, List<Page> pages) {
        String basepath = baseDir.getParentFile().getPath() + File.separator;
        int baselength = basepath.length();

        for (Page page : pages) {
            String pagePath = page.getFile().getPath();
            String pageName = getPagename(pagePath.substring(baselength));
            //Strip the file name from the path.
            String path = getPath(pagePath);
            page.setPath(path);
            page.setName(pageName);
            if (isHandlingPageHistoriesFromFilename()) preserveHistory(page, pageName);
        }
    }

	/**
	 * figures out path var for Page based on complete path to page's file
	 * @param pagePath
	 * @return
	 */
	private String getPath(String pagePath) {
		int fileNameStart = pagePath.lastIndexOf(File.separator);
		if (fileNameStart >= 0) {
		    pagePath = pagePath.substring(0, fileNameStart);
		} else {
		    pagePath = "";
		}
		return pagePath;
	}

    /**
     * uses the filename to set the version and name of the given page
     * so that the history is preserved in the conversion. Note:
     * uses the pageHistorySuffix which is set by the handlePageHistoryProperty
     * method
     * @param page object that will be changed to reflect pagename and version of given filename
     * @param filename should use the pageHistorySuffix to indicate version and pagename:
     * <br/>
     * if pageHistorySuffix is -#.txt
     * <br/>
     * then filename should be something like: pagename-2.txt
     * @return Page with changed name and version
     * Will return passed page with no changes if:
     * <ul>
     * <li>suffix is null</li>
     * <li> suffix has no numerical indicator (#)</li>
     * </ul>
     */
    protected Page preserveHistory(Page page, String filename) {
    	//get suffix
    	String suffix = getPageHistorySuffix();
    	if (suffix == null) {
    		log.error("Error attempting to preserve history: Page history suffix is Null.");
    		return page;
    	}
    	//create regex for filename based on the suffix
    	Matcher hashFinder = hashPattern.matcher(suffix);
    	String suffixReplaceRegex = "";
    	if (hashFinder.find()) {
    		suffixReplaceRegex = hashFinder.replaceAll("(\\\\d+)");
    		suffixReplaceRegex = "(.*)" + suffixReplaceRegex;
    	}
    	else {
    		log.error("Error attempting to preserve history: Suffix is invalid. Must contain '#'.");
    		return page;
    	}
    	//get the version and name
    	Pattern suffixReplacePattern = Pattern.compile(suffixReplaceRegex);
    	Matcher suffixReplacer = suffixReplacePattern.matcher(filename);
    	if (suffixReplacer.find()) {
    		String pagename = suffixReplacer.group(1);
    		String versionString = suffixReplacer.group(2);
    		int version = Integer.parseInt(versionString);
    		page.setName(pagename); //set name before version so latestversion data is properly set in Page
    		page.setVersion(version);
    	}
    	return page;
	}

	/**
	 * gets the pagename given the pagepath
	 * @param pagePath
	 * @return pagename
	 */
	protected String getPagename(String pagePath) {
		String pageName = "";
		if (hierarchyHandler == HierarchyHandler.DEFAULT ||
				hierarchyHandler == HierarchyHandler.HIERARCHY_BUILDER) {
		    pageName = pagePath.substring(pagePath.lastIndexOf(File.separator) + 1);
		} else if (hierarchyHandler == HierarchyHandler.PAGENAME_HIERARCHIES) {
			String quotedSeparator = Pattern.quote(File.separator);
			pageName = pagePath.replaceAll(quotedSeparator, CONFLUENCE_SEPARATOR);
		}
		return pageName;
	}

	/**
	 * converts the given pages with the given converts
	 * @param pages
	 * @param converters
	 * @return true if conversion of all pages succeeded
	 */
	protected boolean convertPages(List<Page> pages, List<Converter> converters) {
		return convertPages(pages, converters, "Converting pages...");
	}

	/**
	 * converts the given pages with the given converters
	 * @param pages
	 * @param converters
	 * @param note, message for the progress monitor
	 * @return true if conversion of all pages succeeded
	 */
	protected boolean convertPages(List<Page> pages, List<Converter> converters, String note) {
		boolean result = true;
		this.state.updateNote(note);
		log.info(note);

		this.startTotalConvertTime = (new Date()).getTime();

		//go through each page
		for (Iterator<Page> iter = pages.iterator(); iter.hasNext();) {
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return false;
			}

			Page page = iter.next();

			//some bookkeeping
			long startTimeStamp = conversionBookkeepingNextPage(page);

            //get the file's contents

			if (page.getOriginalText() == null || "".equals(page.getOriginalText())) {
	            File file = getFileContents(page);
	            if (file == null) {
	            	iter.remove(); //get rid of this page from the iterator.
	            	continue;
	            }
			} //else we used a PageSplitter to set the original text, so we can go straight to conversion

            //convert the page
            convertPage(converters, page);
            //more bookkeeping
            conversionBookkeepingEndThisPage(startTimeStamp);
            if (!this.running) {
        		this.feedback = Feedback.CANCELLED;
        		return false;
        	}
        }
		//still more bookkeeping
		conversionBookkeepingEndAll(pages, converters);
		return result;

	}

	/**
	 * make some log entries about the time it took to convert a page
	 * @param startTimeStamp
	 * @return
	 */
	private long conversionBookkeepingEndThisPage(long startTimeStamp) {
		long stopTimeStamp = ((new Date()).getTime());
		log.info("                   time to convert " + (stopTimeStamp - startTimeStamp) + "ms");
		return stopTimeStamp;
	}

	/**
	 * make some log entries regarding the length of time it took to do the entire conversion
	 * @param pages
	 * @param converters
	 */
	private void conversionBookkeepingEndAll(List<Page> pages, List<Converter> converters) {
		long endTotalConvertTime = (new Date()).getTime();
		long totalTimeToConvert = (endTotalConvertTime - startTotalConvertTime)/1000;
		String baseMessage = "::: total time to convert files: "+ totalTimeToConvert+ " seconds.";
		log.info(baseMessage);
	}

	/**
	 * update the progress monitor and write some log entries for this page
	 * @param page
	 * @return
	 */
	private long conversionBookkeepingNextPage(Page page) {
		long startTimeStamp = ((new Date()).getTime());
		log.info("-------------------------------------");
		log.info("converting page file: " + page.getName());
		if (page.getFile() != null && page.getFile().getName() != null)
			log.debug("original file name: " + page.getFile().getName());
		return startTimeStamp;
	}

	/**
	 * get the file of the given page
	 * @param page file for this page
	 * @return the file, or return null if not possible
	 */
	@Nullable
	private File getFileContents(Page page) {
		File file = page.getFile();
		if (file == null) {
			if (page.getOriginalText() != null && !"".equals(page.getOriginalText())) {
				log.warn("This appears to be a unit test. Continue as for Unit Test.");
				String path = page.getPath();
				if (path == null) path = "";
				file = new File(path);
			}
			else {
		        log.warn("No file was set for page " + page.getName() + ". Skipping page.");
		        return null;
			}
		}
		else if (page.getOriginalText() == null){
		    try {
		    	String pageContents = "";
		    	if (changingEncoding()) {
		    		String encoding = getEncoding();
		    		byte[] pagebytes = FileUtils.getBytesFromFile(file);
		    		try {
						pageContents = new String(pagebytes, encoding);
					} catch (UnsupportedEncodingException e) {
						String baseerror = "Could not encode file with encoding: " + encoding + ".";
						log.error(baseerror + " Using utf-8.");
						this.errors.addError(Feedback.BAD_SETTING, baseerror, true);
						pageContents = new String(pagebytes, "utf-8");
					}
		    	}
		    	else pageContents = FileUtils.readTextFile(file);
		        page.setOriginalText(pageContents);
		    } catch (IOException e) {
		        String message = "Could not read file " + file.getAbsolutePath() + ".\n" +
							        			"Check existence and permissions.";
		        log.error(message);
				this.errors.addError(Feedback.BAD_FILE, message, true);
		        return null;
		    }

			// Save the true source since the original will get modified in convert.
			page.setUnchangedSource(page.getOriginalText());
		}
		return file;
	}

	private boolean changingEncoding() {
		if (this.miscProperties != null)
			return this.miscProperties.containsKey("encoding");
		return false;
	}

	private String getEncoding() {
		if (this.miscProperties != null)
			 return this.miscProperties.getProperty("encoding", "utf-8");
		return "utf-8";
	}


	/**
	 * converts one page with the given converters
	 * @param converters list of converters
	 * @param page page object
	 */
	protected Page convertPage(List<Converter> converters, Page page) {
		if (page.getConvertedText() == null)
			page.setConvertedText(page.getOriginalText()); //in case empty converter list

		for (Converter converter : converters) {
		    try {
		    	this.state.updateProgress();
		    	if (this.settings != null) {
		    		converter.setAttachmentDirectory(this.settings.getAttachmentDirectory());
		    	}
		    	else {
		    		//for backwards compatibility with v2
		    		ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
		    		converter.setAttachmentDirectory(confSettings.getAttachmentDirectory());
		    	}
		        converter.convert(page);
		        // Need to reset originalText here because each converted expects
		        // to start with the result of previous conversions.
		        page.setOriginalText(page.getConvertedText());
		    } catch (Exception e) {
		        String note = "Exception thrown by converter " + converter.getKey() +
						                " on page " + page.getName() + ". Continuing with next converter.";
		        log.error(note, e);
				this.errors.addError(Feedback.CONVERTER_ERROR, note, true);
		    }
		    if (converter.getErrors().hasErrors()) {
		    	this.hadConverterErrors = true;
		    	this.state.updateNote(converter.getErrors().getFeedbackWindowErrorMessages());
		    }
		}

		return page;
	}

    /**
     * Write pages to disk. They are saved to the directory output/output below the
     * current working directory.
     *
     * @param pages The pages to save
     */
    private void savePages(File outputDir, List<Page> pages) {
    	String message = "Saving Pages to Filesystem";
		this.state.updateNote(message);
		log.info(message);

		if (!outputDir.exists() && !outputDir.mkdir()) {
            String dirfailMessage = "Directory creation failed for directory " + outputDir.toString();
            log.error(Feedback.BAD_OUTPUT_DIR + ": " + dirfailMessage);
			this.errors.addError(Feedback.BAD_OUTPUT_DIR, dirfailMessage, true);
        }

        for (Page page : pages) {
        	if (!this.running) {
        		this.feedback = Feedback.CANCELLED;
        		return;
        	}
        	this.state.updateProgress();
            FileUtils.writeFile(page.getConvertedText(), new File(outputDir, page.getName()).getPath());
        }
    }

	/**
	 * validates the given spacekey, removing illegal chars,
	 * as necessary
	 * @param spacekey
	 * @return valid spacekey
	 */
	public static String validateSpacekey(String spacekey) {
		String validated = spacekey.replaceAll("[^A-Za-z0-9]", "");
		return validated;
	}

	/**
	 * @return the max attachment size as it's represented in the model
	 */
	private String getMaxAttachmentSizeStringFromModel() {
		return this.settings.getAttachmentSize();
	}

    Pattern switchPattern = Pattern.compile("switch");
    Pattern suffixPattern = Pattern.compile("suffix");
    private boolean handlingPageHistories = false;
	private String pageHistorySuffix = null;

    /**
     * set the page history state to reflect the page history property
     * and associated value that are passed as arguments
     * @param key
     * @param value
     */
    protected void handlePageHistoryProperty(String key, String value) {
    	Matcher switchFinder = switchPattern.matcher(key);
    	if (switchFinder.find()) {
    		//the default should be false, so it's ok to just parse the string.
    		this.handlingPageHistories = Boolean.parseBoolean(value);
    		return;
    	}
    	Matcher suffixFinder = suffixPattern.matcher(key);
    	if (suffixFinder.find()) {
    		setPageHistorySuffix(value);
    		return;
    	}
    }

    protected void handleIllegalHandling(String key, String value) {
    	boolean enabled = true; //default
    	value = value.trim();
    	if ("false".equals(value))
    		enabled = false;
    	illegalHandlingEnabled = enabled;
    }

    protected void handleAutoDetectSpacekeys(String key, String value) {
    	boolean enabled = false; //default
    	value = value.trim();
    	if ("true".equals(value)) {
    		enabled = true;
    	}
    }

    Pattern miscPropsPattern = Pattern.compile("" +
    		"\\w+\\.\\d+\\.([^.]+)\\.property"
    		);
    protected Properties handleMiscellaneousProperties(String key, String value) {
    	Matcher miscKeyFinder = miscPropsPattern.matcher(key);
    	if (miscKeyFinder.matches()) {
    		String misckey = miscKeyFinder.group(1);
    		if (this.miscProperties == null)
    			this.miscProperties = new Properties();
    		this.miscProperties.put(misckey, value);
    		log.debug("Miscellaneous Property set: " + misckey + "=" + value);
    		return this.miscProperties;
    	}
    	String error = "Miscellaneous property was detected, " +
		    			"but key was invalid. Could not instantiate property: " +
		    			key + "=" + value;
		log.error(error);
		this.errors.addError(Feedback.BAD_PROPERTY, error, true);
		return this.miscProperties;
    }

    private void addDefaultMiscProperties() {
    	handleMiscellaneousProperties("Testing.1234.spacekey.property", this.settings.getSpace());
    }

    protected void handleFilters(String key, String value) throws InstantiationException, IllegalAccessException {
    	log.debug("filter property = " + value);
    	getFilterValues().add(value);
    }

	private Set<String> getFilterValues() {
		if (this.filterValues == null)
			this.filterValues = new HashSet<String>();
		return this.filterValues;
	}

    /**
     * sets up .xmlevent properties
     * @param key must end in .xmlevent
     * @param value must follow this format:
     * <tt>
     * {tag}tagname{class}classname
     * </tt>
     * where tagname is the xml tag to associate the event with (b, for bold)
     * and classname is the parser that will manage the events for that tag.
     * tagname can contain a comma-delimited list of tags. For example:
     * {tag}h1, h2, h3{class}com.example.HeaderParser
     */
    private void handleXmlEvents(String key, String value) {
    	String tag = getXmlEventTag(value);
    	String classname = getXmlEventClassname(value);
    	String[] tags = tag.split(",");
    	for (String onetag : tags) {
    		onetag = onetag.trim();
			addOneXmlEvent(onetag, classname);
		}
    }

	/**
	 * adds one xml event object to the events handler, such that the classname becomes
	 * an instantiated class that is associated with the given tag.
	 * The events handler can be custom (using the xmlevents misc property), or the default
	 * xml events handler will be used
	 * @param tag
	 * @param classname
	 */
	private void addOneXmlEvent(String tag, String classname) {
		if (this.miscProperties.containsKey("xmlevents")) {
    		Class eventsClass;
    		String xmleventsclass = this.miscProperties.getProperty("xmlevents");
			try {
				eventsClass = Class.forName(xmleventsclass);
			} catch (ClassNotFoundException e) {
				log.warn("xmlevents property value - " +
						xmleventsclass +
						" - does not exist. Using DefaultXmlEvents.");
				this.miscProperties.remove("xmlevents");
				eventsClass = DefaultXmlEvents.class; //try setting the DefaultXmlEvents
			}
    		XmlEvents events = null;
			try {
				events = (XmlEvents) eventsClass.newInstance();
				events.addEvent(tag, classname); //call the custom events class
				return;
			} catch (Exception e) {
				log.warn("xmlevents property value - " +
						xmleventsclass + " - hasn't implemented XmlEvents. " +
						"Using DefaultXmlEvents.");
				this.miscProperties.remove("xmlevents");
				//continue to DefaultXmlEvents.addEvent below
			}
    	}
		new DefaultXmlEvents().addEvent(tag, classname);
	}

    Pattern xmleventClassPattern = Pattern.compile("" +
    		"\\{class\\}(.*)");
    protected String getXmlEventClassname(String value) {
		Matcher finder = xmleventClassPattern.matcher(value);
		if (finder.find()) {
			return finder.group(1);
		}
		throw new IllegalArgumentException(XMLEVENT_PROP_ERROR);
	}

    Pattern xmleventTagPattern = Pattern.compile("" +
    		"\\{tag\\}([^}]+)\\{class\\}");
	protected String getXmlEventTag(String value) {
		Matcher finder = xmleventTagPattern.matcher(value);
		if (finder.find()) {
			return finder.group(1);
		}
		throw new IllegalArgumentException(XMLEVENT_PROP_ERROR);
	}

	/**
     * @param key
     * @return true if the given key is the switch to turn on the
     * Hierarchy framework
     */
    protected boolean isHierarchySwitch(String key) {
    	Matcher switchFinder = switchPattern.matcher(key);
    	return switchFinder.find();
    }

    /**
     * determines if the given string represents an allowed
     * non converter property: (hierarchy builder, page history preserver,
     * illegalname handler, autodetect spacekeys)
     * @param input represents an entire converter/property string. For example:
     * <br/>
     * Wiki.0011.somefilename.propertytype=something
     * @return true if it's an expected/allowed non converter property
     */
    public boolean isNonConverterProperty(String input) {
    	String converterTypes =
    				"(" +
    					"(" +
    						NONCONVERTERTYPE_HIERARCHYBUILDER +
    					")" +
    					"|" +
    					"(" +
    						NONCONVERTERTYPE_PAGEHISTORYPRESERVATION +
    					")" +
    					"|" +
    					"(" +
							NONCONVERTERTYPE_ILLEGALHANDLING +
						")" +
    					"|" +
    					"(" +
							NONCONVERTERTYPE_AUTODETECTSPACEKEYS +
						")" +
						"|" +
						"(" +
							NONCONVERTERTYPE_FILTERS +
						")" +
						"|" +
						"(" +
							NONCONVERTERTYPE_MISCPROPERTIES +
						")" +
						"|" +
						"(" +
							NONCONVERTERTYPE_XMLEVENT +
						")" +
    				")";
    	String converterPattern = "[-\\w\\d.]+?" + converterTypes + "=" + ".*";
    	return input.matches(converterPattern);
    }

    /**
     * @return true if the converter should handle page histories
     */
    public boolean isHandlingPageHistoriesFromFilename() {
    	return this.handlingPageHistories && this.pageHistorySuffix != null;
    }

    /**
     * @return the current page history suffix
     */
    public String getPageHistorySuffix() {
    	return this.pageHistorySuffix;
    }

	Pattern hashPattern = Pattern.compile("#+");
	/**
	 * sets the page history suffix, if it's a valid suffix.
	 * If not, sets it to null.
	 * @param suffix candidate suffix, a valid candidate will have
	 * a numeric component, represented by a '#' (hash) symbol
	 * <br/>
	 * Example: -v#.txt
	 * @return true, if a valid suffix was saved.
	 * false, if the suffix was invalid, and therefore was not saved.
	 */
	protected boolean setPageHistorySuffix(String suffix) {
		//check for suffix goodness
		Matcher hashFinder = hashPattern.matcher(suffix);
		if (hashFinder.find()) {
			this.pageHistorySuffix = suffix;
			return true;
		}
		log.error("Error trying to preserve page history: Suffix '" + suffix + "' " +
				"does not have a sortable component. Must include '#'.");
		this.pageHistorySuffix = null;
		return false;
	}

	/**
	 * @return HierarchyBuilder object. used by tests.
	 */
	protected HierarchyBuilder getHierarchyBuilder() {
		return hierarchyBuilder;
	}

	/**
	 * @return HierarchyHandler object. used by tests.
	 */
	protected HierarchyHandler getHierarchyHandler() {
		return hierarchyHandler;
	}

	/**
	 * sets how the hierarchy framework is to be used.
	 * @param input "UseBuilder", "UsePagenames", or "Default".
	 * If input is none of these, no changes occur
	 */
	private void setHierarchyHandler(String input) {
		if (input.matches("UseBuilder")) hierarchyHandler = HierarchyHandler.HIERARCHY_BUILDER;
		else if (input.matches("UsePagenames")) hierarchyHandler = HierarchyHandler.PAGENAME_HIERARCHIES;
		else if (input.matches("Default")) hierarchyHandler = HierarchyHandler.DEFAULT;
	}

	/**
	 * @return the feedback as it currently stands
	 */
	public Feedback getConverterFeedback() {
		return this.feedback;
	}

	/**
	 * resets the feedback state to Feedback.NONE
	 */
	public void resetFeedback() {
		this.feedback = Feedback.NONE;
	}

	/**
	 * clears state relating to the error handling
	 */
	public void resetErrorHandlers() {
		this.errors.clear();
		this.hadConverterErrors = false;
	}

	/**
	 * clears state relating to the hierarchy framework
	 */
	public void resetHierarchy() {
		this.hierarchyBuilder = null;
		this.hierarchyHandler = HierarchyHandler.DEFAULT;
	}

	/**
	 * @return object contains information relating to errors triggered during the conversion
	 */
	public ConverterErrors getErrors() {
		return this.errors;
	}

	/**
	 * @return true if the conversion has generated errors
	 */
	public boolean hadConverterErrors() {
		return this.hadConverterErrors;
	}

	/**
	 * @return true if the illegal handling (names and links) should occur.
	 * false if it should be disabled
	 */
	public boolean isIllegalHandlingEnabled() {
		return illegalHandlingEnabled;
	}

}
