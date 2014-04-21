package com.atlassian.plugins.rest.common.multipart.fileupload;

import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.FileSizeLimitExceededException;
import com.atlassian.plugins.rest.common.multipart.MultipartForm;
import com.atlassian.plugins.rest.common.multipart.MultipartHandler;
import com.google.common.base.Preconditions;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class CommonsFileUploadMultipartHandler implements MultipartHandler
{
    private final ServletFileUpload servletFileUpload;

    public CommonsFileUploadMultipartHandler(long maxFileSize, long maxSize)
    {
        servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
        servletFileUpload.setFileSizeMax(maxFileSize);
        servletFileUpload.setSizeMax(maxSize);
    }

    public FilePart getFilePart(HttpServletRequest request, String field)
    {
        return getForm(request).getFilePart(field);
    }

    private static class CommonsFileUploadMultipartForm implements MultipartForm
    {
        private final Collection<FileItem> fileItems;

        private CommonsFileUploadMultipartForm(final Collection<FileItem> fileItems)
        {
            this.fileItems = fileItems;
        }

        public FilePart getFilePart(String field)
        {
            for (FileItem item : fileItems)
            {
                if (item.getFieldName().equals(field))
                {
                    return new CommonsFileUploadFilePart(item);
                }
            }
            return null;
        }

        public Collection<FilePart> getFileParts(String field)
        {
            Collection<FilePart> fileParts = new ArrayList<FilePart>();
            for (FileItem item : fileItems)
            {
                if (item.getFieldName().equals(field))
                {
                    fileParts.add(new CommonsFileUploadFilePart(item));
                }
            }
            return fileParts;
        }
    }

    public MultipartForm getForm(HttpServletRequest request)
    {
        return getForm(new ServletRequestContext(request));
    }

    @SuppressWarnings ("unchecked")
    public MultipartForm getForm(RequestContext request)
    {
        try
        {
            return new CommonsFileUploadMultipartForm(servletFileUpload.parseRequest(request));
        }
        catch (FileUploadException e)
        {
            if (e instanceof FileUploadBase.FileSizeLimitExceededException || e instanceof FileUploadBase.SizeLimitExceededException)
            {
                throw new FileSizeLimitExceededException(e.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    private static class CommonsFileUploadFilePart implements FilePart
    {
        private final FileItem fileItem;

        CommonsFileUploadFilePart(FileItem fileItem)
        {
            this.fileItem = Preconditions.checkNotNull(fileItem);
        }

        public String getName()
        {
            return fileItem.getName();
        }

        public InputStream getInputStream() throws IOException
        {
            return fileItem.getInputStream();
        }

        public String getContentType()
        {
            return fileItem.getContentType();
        }

        public void write(final File file) throws IOException
        {
            try
            {
                fileItem.write(file);
            }
            catch (Exception e)
            {
                if (e instanceof IOException)
                {
                    throw (IOException) e;
                }
                else
                {
                    // Change to IOException once we don't support Java 1.5 anymore
                    throw new RuntimeException(e);
                }
            }
        }

        public String getValue()
        {
            return fileItem.getString();
        }

        public boolean isFormField()
        {
            return fileItem.isFormField();
        }
    }
}
