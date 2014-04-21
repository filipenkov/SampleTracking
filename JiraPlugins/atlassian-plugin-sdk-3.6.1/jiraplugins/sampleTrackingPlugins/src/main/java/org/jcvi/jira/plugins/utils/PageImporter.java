package org.jcvi.jira.plugins.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.velocity.VelocityManager;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ImportTool;
import webwork.action.ServletActionContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: pedworth
 * Date: 9/28/11
   <h2>Summary</h2>
 * <p> A utility class that provides access to the functionality of
 * ImportTool and some methods of its own.</p>
 * <h2>Uses</h2>
 * <ul>
 *     <li>get a properly configured instance of ImportTool</li>
 *     <li>call existing JSP pages</li>
 *     <li>get a block of javascript to alter the action of a form</li>
 *     <li>add and remove an instance from the session</li>
 * </ul>
 * <h3>From Java</h3>
 * <pre>
 *     String pagesContents =
 *          PageImporter.importJSP("/secure/views/createsubtaskissue-start.jsp");
 *     String renameAction  =
 *          PageImporter.getReplaceFormActionJS("jiraform","Test.jspa");
 *
 *     return pageContents + renameAction;
 * </pre>
 * <h3>From Velocity</h3>
 * <pre>
 *  #set ($importer = $action.getJSPImporter())
 *  $importer.importJSP("/secure/views/createsubtaskissue-start.jsp")
 *  $importer.getReplaceFormActionJS('jiraform','Test.jspa')
 * </pre>
 * <p>For the velocity version the action will need to provide the following
 * method</p>
 * <pre>
 *     public PageImporter getJSPImporter() {
 *         return PageImporter.getVelocityInstance();
 *     }
 * </pre>
 * <h2>Notes</h2>
 * <h3>Testing</h3>
 * <p>The setup of ImportTool requires ServletActionContext to have been
 * initialized. If the action was called by webworks this should have
 * already happened. If testing then ServletActionContext.setRequest etc
 * need to be called first. The values are associated with the thread and
 * so must be set in the same thread as the test.</p>
 * <h3>atlassian-plugins.xml</h3>
 * <p>when using ReuseJSP in plugins.xml watch out for urls that end
 * in jspa even if that is part of a parameter it will be treated as an
 * action. To avoid this either put newAction first or add a dummy param
 * to the end.</p>
 */
public class PageImporter {
    //todo: move to language pack
    private static final String ERROR = "The following errors occurred:";
    private static final String ERROR_NO_TEMPLATE = "The template must not be null";
    private static final String OVERRIDE_FORM_ACTION_TEMPLATE = "templates/org/jcvi/jira/plugins/util/OverrideFormAction.vm";
    //copied from GenericActionMessage as there was no other way to use it
//    public static final String PLUGIN_TEMPLATES = "templates/plugins/";

    /**
     * This constructor is provided to allow access to the
     * methods from Velocity
     */
    public PageImporter() {
    }

    //uses the passed in context to evaluate the template
    public static String insertTemplate(Map<String,Object> context,
                                        String template)
            throws VelocityException {
        if (template == null) {
            return errorBuilder(ERROR_NO_TEMPLATE);
        }
        VelocityManager velocityManager = ComponentAccessor.getVelocityManager();
        return
        velocityManager.getEncodedBodyForContent(template,     //String content,
                                                 getBaseURL(), //String base url
                                                 context);     //Map contextParameters);
    }

    //---------------------------------------------------------------------
    // Velocity interface
    //---------------------------------------------------------------------
    /**
     * The name includes JSP because import on its own is a protected word
     * @param url   The location of the JSP, relative to this context's root
     * @return The output from the JSP
     * @throws IOException  If the response output stream cannot be flushed
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static String importJSP(String url) throws IOException {
        ServletActionContext.getResponse().flushBuffer();
        return getVelocityImportTool().read(url);
    }

    /**
     * creates a block of javascript (including the wrapping script tags)
     * that on load modifies the action of the form. The script uses
     * onLoad so that the block of HTML/Script can safely be inserted
     * before the form.
     * @param formName  The value of the 'name' parameter of the form element
     *                  Normally this is jiraform.
     * @param newAction The URI to use instead of the existing action.
     *                  If the path doesn't begin with a / then it
     *                  is interpreted as being relative to the existing
     *                  action.
     *                  Using just a Filename replaces only the filename
     *                  part of the URI.
     *                  Absolute URIs will need to include the context
     *                  path. (e.g. /jira/)
     * @return A block of HTML that can be directly inserted into a page
     * @throws VelocityException    If there is a problem rendering the template
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public String getReplaceFormActionJS(String formName, String newAction)
            throws VelocityException {
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("formName",formName);
        context.put("newAction",newAction);
        return importVM(context,OVERRIDE_FORM_ACTION_TEMPLATE);
    }

    //---------------------------------------------------------------------
    // private methods
    //---------------------------------------------------------------------

    /**
     * <p>Creates and configures an ImportTool</p>
     * @return a properly configured ImportTool
     */
    private static ImportTool getVelocityImportTool() {
        //this chainedContext constructor is deprecated, but
        //it is also more reliable than trying to get or create
        //a VelocityEngine (a required param in the other versions)
        @SuppressWarnings({"deprecation"})
        ViewContext vContext =
                new ChainedContext(new VelocityContext(),
                                   //new VelocityEngine(),
                                   ServletActionContext.getRequest(),
                                   ServletActionContext.getResponse(),
                                   ServletActionContext.getServletContext());
        ImportTool importer = new ImportTool();
        importer.init(vContext);
        return importer;
    }

    private static String getBaseURL() {
        if (ServletActionContext.getRequest() != null) {
            return ServletActionContext.getRequest().getContextPath();
        }
        return "";
    }

    private static String errorBuilder(String error) {
        return wrap("P",ERROR) + wrap("P",error);
    }

    private static String wrap(String tag, String content) {
        return "<"+tag+">"+content+"</"+tag+">";
    }

    //uses the passed in context to evaluate the template
    private static String importVM(Map<String,Object> context,
                                        String template)
            throws VelocityException {
        VelocityManager velocityManager = ComponentAccessor.getVelocityManager();
        return
        velocityManager.getBody("",//PLUGIN_TEMPLATES, //String templateDirectory
                template,         //String template
                getBaseURL(),     //String base url
                context);         //Map contextParameters);
    }
}
