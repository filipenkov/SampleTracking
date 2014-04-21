package com.atlassian.jira.issue.thumbnail;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.ThumbnailConfiguration;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.util.log.OneShotLogger;
import com.atlassian.jira.util.mime.MimeManager;
import com.google.common.base.Predicate;
import com.sun.media.jai.codec.SeekableStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.RenderedOp;
import java.awt.*;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Some of this code was taken in 4.4 from our friends in Confluence
 */
public class DefaultThumbnailManager implements ThumbnailManager
{
    private static final Logger log = Logger.getLogger(DefaultThumbnailManager.class);
    private static final OneShotLogger jaiMessageLog = new OneShotLogger(log);

    // Passed in from pico container
    private final ThumbnailConfiguration thumbnailConfiguration;
    private final AttachmentManager attachmentManager;
    private final MimeManager mimeManager;

    private final Thumber thumber = new Thumber(MIME_TYPE);
    private final Predicate<Dimensions> rasterBasedRenderingThreshold;


    public DefaultThumbnailManager(ThumbnailConfiguration thumbnailConfiguration, AttachmentManager attachmentManager, MimeManager mimeManager)
    {
        this.thumbnailConfiguration = thumbnailConfiguration;
        this.attachmentManager = attachmentManager;
        this.mimeManager = mimeManager;
        this.rasterBasedRenderingThreshold = new AdaptiveThresholdPredicate();
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, com.opensymphony.user.User user)
            throws Exception
    {
        return getThumbnails(attachments, (User) user);
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, User user) throws Exception
    {
        List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
        for (Attachment attachment : attachments)
        {
            if (isThumbnailable(attachment) && checkToolkit())
            {
                thumbnails.add(doGetThumbnail(attachment));
            }
        }
        return thumbnails;
    }

    @Override
    public Collection<Thumbnail> getThumbnails(GenericValue issue, com.opensymphony.user.User user) throws Exception
    {
        return getThumbnails(attachmentManager.getAttachments(issue), user);
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Issue issue, com.opensymphony.user.User user) throws Exception
    {
        return getThumbnails(attachmentManager.getAttachments(issue), user);
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Issue issue, User user) throws Exception
    {
        return getThumbnails(attachmentManager.getAttachments(issue), user);
    }

    @Override
    public boolean isThumbnailable(Attachment attachment) throws DataAccessException
    {
        String mimeType = mimeManager.getSuggestedMimeType(attachment.getFilename());

        // Check that file is of a valid image mime type
        // Note - all thumbnails are saved in JPEG format on disk.
        File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
        if (thumber.isFileSupportedImage(attachmentFile))
        {
            for (String thumbnailMimeType : Thumber.THUMBNAIL_MIME_TYPES)
            {
                if (thumbnailMimeType.equalsIgnoreCase(mimeType))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the Thumbnail that corresponds to an Attachment, or null if the given attachment is not an image.
     *
     * @param attachment an Attachment
     * @return returns a Thumbnail, or null
     */
    @Override
    public Thumbnail getThumbnail(Attachment attachment)
    {
        if (!isThumbnailable(attachment))
        {
            return null;
        }

        try
        {
            return doGetThumbnail(attachment);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Error getting thumbnail for: " + attachment, e);
        }
    }

    @Override
    public boolean checkToolkit(com.opensymphony.user.User user)
    {
        return thumber.checkToolkit();
    }

    @Override
    public boolean checkToolkit()
    {
        return thumber.checkToolkit();
    }

    private Thumbnail doGetThumbnail(final Attachment attachment) throws MalformedURLException
    {
        if (!isThumbnailable(attachment))
        {
            throw new IllegalArgumentException("Unable to create thumbnail image of attachment with id:" + attachment.getId());
        }

        final File originalFile = AttachmentUtils.getAttachmentFile(attachment);
        final File thumbnailFile = AttachmentUtils.getThumbnailFile(attachment);

        final int maxWidth = thumbnailConfiguration.getMaxWidth();
        final int maxHeight = thumbnailConfiguration.getMaxHeight();


        if (thumbnailFile.exists())
        {
            log.debug("Thumbnail file '" + thumbnailFile + "' already exists. Returning existing thumbnail.");
            try
            {
                final BufferedImage image = ImageIO.read(thumbnailFile);
                if (image != null)
                {
                    return new Thumbnail(image.getHeight(), image.getWidth(), thumbnailFile.getName(), attachment.getId(), Thumbnail.MimeType.PNG);
                }
                log.warn("Unable to read image data from  existing thumbnail file '" + thumbnailFile + "'.  Deleting this thumbnail");
                FileUtils.deleteQuietly(thumbnailFile);
            }
            catch (IOException ioe)
            {
                log.warn("Unable to render existing thumbnail file '" + thumbnailFile + "' because of " + ioe.getLocalizedMessage());
                return new BrokenThumbnail(attachment.getId());
            }
        }

        // check that the thumbnail file can be created, since ImageIO will get NullPointerExceptions if we don't
        // see also http://bugs.sun.com/view_bug.do?bug_id=5034864
        try
        {
            FileUtils.touch(thumbnailFile);
            FileUtils.deleteQuietly(thumbnailFile);
        }
        catch (IOException ioe)
        {
            log.warn("Unable to write to thumbnail file '" + thumbnailFile + "' because of " + ioe.getLocalizedMessage());
            return new BrokenThumbnail(attachment.getId());
        }

        // if they dopnt fit within a sensible image size, we use a streamable thumbnail.  The image quality is
        // not as good but the memory impact is limited
        final Dimensions originalImageDimensions = imageDimensions(attachment);
        if (!rasterBasedRenderingThreshold.apply(originalImageDimensions))
        {
            log.debug("Image dimensions exceed the threshold for raster based image manipulation. Using stream based renderer.");
            try
            {
                return generateWithStreamRenderer(attachment, thumbnailFile, maxWidth, maxHeight);
            }
            catch (CMMException cme)
            {
                // We don't use the JAI based fallback renderer if the image is too large.
                log.warn("Attachment image is very large and contains color information that JIRA cant handle : '" + originalFile + "'");
                return new BrokenThumbnail(attachment.getId());
            }
        }

        // Render using the raster based renderers...
        try
        {
            return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
            {
                @Override
                public Thumbnail withInputStream(InputStream is) throws MalformedURLException
                {
                    return fixup(thumber.retrieveOrCreateThumbNail(is, attachment.getFilename(), thumbnailFile, maxWidth, maxHeight, attachment.getId()), thumbnailFile);
                }


            });
        }
        catch (final CMMException ce)
        {
            // The JAI based renderer is only used for images that have embedded color profile information that causes the Thumber to fail.
            // We still want to use Thumber in general as it's faster.
            log.debug("Failed to create thumbnail, delegating to JAI based thumbnail renderer: CMMException " + ce.getLocalizedMessage()); // No need to dump the stack trace, we expect this to happen.
            return generateWithInMemoryJAIRenderer(attachment, thumbnailFile, maxWidth, maxHeight);
        }
    }

    private Thumbnail fixup(@Nullable Thumbnail thumbnail, File thumbnailFile)
    {
        if (thumbnail == null)
        {
            return null;
        }

        // the core thumber code has a behaviour that causes it to name the thumbnail differently depending on whether the file exists or not
        // so we ensure its always the file name we expect
        return new Thumbnail(thumbnail.getHeight(), thumbnail.getWidth(), thumbnailFile.getName(), thumbnail.getAttachmentId(), thumbnail.getMimeType());
    }

    /**
     * Generate the thumbnail for the given {@link Attachment} using the raster based (in memory) renderer.
     *
     * @param attachment The attachment containing the image data
     * @param thumbnailFile The file to use for the thumbnail
     * @param maxWidth Maximum width of the thumbnail
     * @param maxHeight Maximum height of the thumbnail
     * @return Thumbnail that does not exceed the given maxWidth or maxHeight
     */
    private Thumbnail generateWithInMemoryJAIRenderer(final Attachment attachment,
            final File thumbnailFile,
            final int maxWidth,
            final int maxHeight)
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
        {
            @Override
            public Thumbnail withInputStream(InputStream is) throws IOException
            {
                jaiMessageLog.warn("The first time we call the JAI library it may fail to find the native library implementation.  The following output is harmless but unpreventable and hence this precending log message.");
                Dimensions d = new JAIImageRenderer().renderThumbnail(is, thumbnailFile, maxWidth, maxHeight);
                return new Thumbnail(d.getHeight(), d.getWidth(), thumbnailFile.getName(), attachment.getId(), Thumbnail.MimeType.PNG);
            }
        });
    }

    /**
     * Generate the thumbnail for the given {@link Attachment} using the stream based renderer.
     *
     * @param attachment The attachment containing the image data
     * @param thumbnailFile The file to use for the thumbnail
     * @param maxWidth Maximum width of the thumbnail
     * @param maxHeight Maximum height of the thumbnail
     * @return Thumbnail that does not exceed the given maxWidth or maxHeight
     */
    private Thumbnail generateWithStreamRenderer(final Attachment attachment,
            final File thumbnailFile,
            final int maxWidth,
            final int maxHeight)
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
        {
            @Override
            public Thumbnail withInputStream(InputStream is) throws IOException
            {
                Dimensions d = new StreamingImageRenderer().renderThumbnail(is, thumbnailFile, maxWidth, maxHeight);
                return new Thumbnail(d.getHeight(), d.getWidth(), thumbnailFile.getName(), attachment.getId(), Thumbnail.MimeType.PNG);
            }
        });
    }

    /**
     * Determine the dimensions (width/height) of the original image.
     *
     * @param attachment the attachment in play
     * @return true if we attempt to render the image, false otherwise.
     */
    private Dimensions imageDimensions(final Attachment attachment)
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Dimensions>()
        {
            @Override
            public Dimensions withInputStream(final InputStream is) throws IOException
            {
                return new ImageDimensionsHelper().dimensionsForImage(ImageIO.createImageInputStream(is));
            }
        });
    }

    /**
     * Call the {@link InputStreamConsumer} with the attachment data input stream ensuring that the input stream gets
     * closed properly afterwards.
     *
     * @param attachment The attachment containing the image data
     * @param sc The InputStreamConsumer that consumes the attachment data {@link InputStream}
     * @return what the sc returns
     */
    private <T> T withStreamConsumer(final Attachment attachment, final InputStreamConsumer<T> sc)
    {
        final File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
        InputStream inputStream = null;
        try
        {
            inputStream = new BufferedInputStream(new FileInputStream(attachmentFile));
            return sc.withInputStream(inputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Image renderer based on JAI (Java Advanced Imaging (JAI) API). Used as a fallback renderer if the original image
     * contains colour profile information that causes a CMMException (CONF-21418).
     */
    private class JAIImageRenderer
    {
        public Dimensions renderThumbnail(final InputStream inputStream, final File thumbnailFile, int maxWidth, int maxHeight)
                throws IOException
        {
            OutputStream thumbnailOutputStream = null;
            try
            {
                thumbnailOutputStream = new BufferedOutputStream(new FileOutputStream(thumbnailFile));
                return scale(inputStream, thumbnailOutputStream, maxWidth, maxHeight);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtils.closeQuietly(thumbnailOutputStream);
            }
        }

        private Dimensions scale(InputStream inputStream, OutputStream thumbnail, int maxWidth, int maxHeight)
        {
            RenderedImage image = loadImage(inputStream);
            final Thumber.WidthHeightHelper wh = thumber.determineScaleSize(maxWidth, maxHeight, image.getWidth(), image.getHeight());
            double scale = (wh.getWidth() / (double) image.getWidth());
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(scale); // x scale factor
            pb.add(scale); // y scale factor
            pb.add(0.0F); // x translate
            pb.add(0.0F); // y translate
            pb.add(image);
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image = JAI.create("SubsampleAverage", pb, qualityHints);
            JAI.create("encode", image, thumbnail, "PNG");
            return new Dimensions(image.getWidth(), image.getHeight());
        }

        /**
         * Load the image using the given {@code inputStream}.
         * <p/>
         * This method doesn't close the InputStream!
         *
         * @param inputStream Original image
         * @return RenderedImage
         */
        private RenderedImage loadImage(final InputStream inputStream)
        {
            SeekableStream s = SeekableStream.wrapInputStream(inputStream, true);
            RenderedOp img = JAI.create("stream", s);
            ((OpImage) img.getRendering()).setTileCache(null); // We don't want to cache image tiles in memory.
            return img;
        }
    }

    /**
     * Image renderer using a streaming approach. Doesn't yield high quality thumbnails but doesn't need to rasterize
     * the whole image at once.
     */
    private static class StreamingImageRenderer
    {
        public Dimensions renderThumbnail(final InputStream inputStream, final File thumbnailFile, int maxWidth, int maxHeight)
                throws IOException
        {
            ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
            BufferedImage bi = scaleDown(iis, maxWidth, maxHeight);
            ImageIO.write(bi, "png", thumbnailFile);
            return new Dimensions(bi.getWidth(), bi.getHeight());
        }

        private BufferedImage scaleDown(ImageInputStream inputStream, int maxWidth, int maxHeight) throws IOException
        {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext())
            {
                throw new IOException("There is not ImageReader availble for the given ImageInputStream");
            }
            // Use the first reader
            ImageReader reader = readers.next();
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(inputStream);

            int ratio = maintainAspectRatio(new Dimension(reader.getWidth(0), reader.getHeight(0)), new Dimension(maxWidth, maxHeight));
            param.setSourceSubsampling(ratio, ratio, 0, 0);

            return reader.read(0, param);
        }

        private int maintainAspectRatio(Dimension original, Dimension target)
        {
            if (original.getWidth() > target.getWidth())
            {
                return (int) Math.round(original.getWidth() / target.getWidth());
            }
            else if (original.getHeight() > target.getHeight())
            {
                return (int) Math.round(original.getHeight() / target.getHeight());
            }
            return 1;
        }
    }

    /**
     * Helper class that uses an {@link ImageReader} to determine the width and height of an image.
     * <p/>
     * Doesn't rasterize the whole image and works well even with very large images (e.g. 15,000 x 15,000 px).
     */
    private static class ImageDimensionsHelper
    {
        public Dimensions dimensionsForImage(final ImageInputStream inputStream) throws IOException
        {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext())
            {
                throw new IOException("There is not ImageReader availble for the given ImageInputStream");
            }
            // Use the first one available
            ImageReader reader = readers.next();
            reader.setInput(inputStream);
            return new Dimensions(reader.getWidth(0), reader.getHeight(0));
        }
    }

}

