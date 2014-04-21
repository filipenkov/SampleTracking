package com.atlassian.upm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.util.Either;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.base.Function;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * This is a servlet filter that will perform the same tasks as the pdk-install-plugin,
 * https://labs.atlassian.com/svn/PDKI/trunk.
 * <p/>
 * The servlet filter has a weight of 1 so should come before the pdk plugin.
 * <p/>
 * This will use the UPM to do the install of plugins.
 */
public class PluginManagerPdkUsurperFilter implements Filter
{
    private static final String UPLOAD_FILENAME = "file_0";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PluginInstaller pluginInstaller;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final I18nResolver i18nResolver;
    private final PermissionEnforcer permissionEnforcer;
    private Function<HttpServletRequest, Either<FileExtractorError, PluginFile>> pluginFileExtractor;

    public PluginManagerPdkUsurperFilter(final PluginInstaller pluginInstaller,
        final I18nResolver i18nResolver,
        final PermissionEnforcer permissionEnforcer,
        final PluginAccessorAndController pluginAccessorAndController)
    {
        this(pluginInstaller, i18nResolver, permissionEnforcer, pluginAccessorAndController, new FileUploadExtractor());
    }

    PluginManagerPdkUsurperFilter(PluginInstaller pluginInstaller,
        I18nResolver i18nResolver,
        PermissionEnforcer permissionEnforcer,
        PluginAccessorAndController pluginAccessorAndController,
        Function<HttpServletRequest, Either<FileExtractorError, PluginFile>> pluginFileExtractor)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.pluginInstaller = checkNotNull(pluginInstaller, "pluginInstaller");
        this.pluginFileExtractor = pluginFileExtractor;
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        res.setContentType("text/plain");

        // Check that the request is multipart and a post
        if (!requestIsValid(req, res))
        {
            return;
        }

        // Check that the user has permission to upload the plugin
        if (!permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL))
        {
            writeError(res, SC_UNAUTHORIZED, "Must have permission to access this resource.");
            return;
        }

        // We don't want to install any plugins if we are currently in safe mode
        if (pluginAccessorAndController.isSafeMode())
        {
            writeError(res, SC_CONFLICT, i18nResolver.getText("upm.pluginInstall.error.safe.mode"));
            return;
        }

        // Now lets grab the plugin from the request and install it
        final Either<FileExtractorError, PluginFile> extractionResult = pluginFileExtractor.apply(req);
        if (extractionResult.isLeft())
        {
            FileExtractorError error = extractionResult.left().get();
            switch (error.type)
            {
                case FILE_NOT_FOUND:
                    // Guess we never had one after all, oh well
                    writeError(res, SC_BAD_REQUEST, "Missing plugin file");
                    return;
                case FILE_UPLOAD:
                    logger.warn(error.cause.getMessage(), error.cause);
                    writeError(res, SC_BAD_REQUEST, "Unable to process file upload");
                    return;
                default:
                    logger.warn(error.cause.getMessage(), error.cause);
                    writeError(res, SC_INTERNAL_SERVER_ERROR, "Unable to process file upload");
                    return;
            }
        }
        installPlugin(res, extractionResult.right().get());
    }

    void installPlugin(final HttpServletResponse res, final PluginFile plugin) throws IOException
    {
        try
        {
            pluginInstaller.install(plugin.file, plugin.name);
        }
        catch (SafeModeException sme)
        {
            writeError(res, SC_CONFLICT, "System is in safe mode");
            return;
        }
        catch (PluginInstallException pie)
        {
            writeError(res, SC_INTERNAL_SERVER_ERROR, "Failed to install plugin: " + pie.getMessage());
            return;
        }
        catch (LegacyPluginsUnsupportedException lpue)
        {
            writeError(res, SC_BAD_REQUEST, "The legacy plugin '" + plugin.name + "' cannot be dynamically installed");
            return;
        }
        catch (UnrecognisedPluginVersionException upve)
        {
            writeError(res, SC_BAD_REQUEST, "The plugin '" + plugin.name + "' with unrecognised version '" + upve.getVersion() + "' cannot be installed");
            return;
        }           
        catch (UnknownPluginTypeException upte)
        {
            writeError(res, SC_BAD_REQUEST, "File '" + plugin.name + "' is not a valid plugin: " + upte.getMessage());
            return;
        }     
        catch (XmlPluginsUnsupportedException xpue)
        {
            writeError(res, SC_BAD_REQUEST, "The xml plugin '" + plugin.name + "' cannot be dynamically installed");
            return;
        }
        catch (RuntimeException re)
        {
            writeError(res, SC_INTERNAL_SERVER_ERROR, re.getMessage());
            return;
        }

        plugin.file.delete();

        res.setStatus(SC_OK);
        res.getWriter().println("Installed plugin " + plugin.file.getPath());
    }

    private void writeError(HttpServletResponse res, int status, String err) throws IOException
    {
        res.sendError(status, "Unable to install plugin:\n\t - " + err);
    }

    private boolean requestIsValid(final HttpServletRequest req, final HttpServletResponse res)
        throws IOException
    {
        if (!req.getMethod().equalsIgnoreCase("post"))
        {
            writeError(res, SC_METHOD_NOT_ALLOWED, "Requires post");
            return false;
        }
        // Check that we have a file upload request
        else if (!ServletFileUpload.isMultipartContent(req))
        {
            writeError(res, SC_BAD_REQUEST, "Missing plugin file");
            return false;
        }
        return true;
    }

    public void destroy()
    {
    }

    private static String getFileName(String pathName)
    {
        int index = checkNotNull(pathName, "pathName").lastIndexOf('/');
        int index2 = pathName.lastIndexOf('\\');

        return pathName.substring(Math.max(index, index2) + 1);
    }

    static class FileExtractorError
    {
        final Type type;
        final Throwable cause;

        FileExtractorError(Type type)
        {
            this(type, null);
        }

        FileExtractorError(Type type, Throwable cause)
        {
            this.type = checkNotNull(type, "type");
            this.cause = cause;
        }

        enum Type
        {
            FILE_NOT_FOUND, FILE_UPLOAD, OTHER
        }
    }

    final static class FileUploadExtractor implements Function<HttpServletRequest, Either<FileExtractorError, PluginFile>>
    {
        private final FileItemFactory factory = new DiskFileItemFactory();

        public Either<FileExtractorError, PluginFile> apply(HttpServletRequest req)
        {
            try
            {
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                @SuppressWarnings("unchecked") List<FileItem> items = upload.parseRequest(req);
                for (FileItem item : items)
                {
                    if (UPLOAD_FILENAME.equals(item.getFieldName()) && !item.isFormField())
                    {
                        File tmp = File.createTempFile("plugindev-", getFileName(item.getName()));
                        tmp.renameTo(new File(tmp.getParentFile(), item.getName()));
                        item.write(tmp);
                        return Either.right(new PluginFile(tmp, item.getName()));
                    }
                }
                return Either.left(new FileExtractorError(FileExtractorError.Type.FILE_NOT_FOUND));
            }
            catch (FileUploadException e)
            {
                return Either.left(new FileExtractorError(FileExtractorError.Type.FILE_UPLOAD, e));
            }
            catch (Exception e)
            {
                return Either.left(new FileExtractorError(FileExtractorError.Type.OTHER, e));
            }
        }
    }

    static class PluginFile
    {
        final File file;
        final String name;

        PluginFile(File file, String name)
        {
            this.file = checkNotNull(file, "file");
            this.name = checkNotNull(name, "name");
        }
    }
}
