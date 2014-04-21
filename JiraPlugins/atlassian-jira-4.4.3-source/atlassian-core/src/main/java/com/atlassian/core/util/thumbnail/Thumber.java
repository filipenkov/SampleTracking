package com.atlassian.core.util.thumbnail;

import com.atlassian.core.util.ImageInfo;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;

/**
 * A class to create and retrieve thumbnail of images.
 */
public class Thumber
{
    private static final Logger log = Logger.getLogger(Thumber.class);
    private final ImageInfo imageInfo = new ImageInfo();

    // According to RFC 2045, mime types are not case sensitive. You can't just use contains. You MUST use equalsIgnoreCase.
    public static final List<String> THUMBNAIL_MIME_TYPES = Collections.unmodifiableList(Arrays.asList(ImageIO.getReaderMIMETypes()));
    public static final List<String> THUMBNAIL_FORMATS = Collections.unmodifiableList(Arrays.asList(ImageIO.getReaderFormatNames()));
    private Thumbnail.MimeType mimeType;

    /**
     * Legacy compatible behaviour, all thumnails generated will be of type
     * {@link com.atlassian.core.util.thumbnail.Thumbnail.MimeType#JPG} which does not support transparency.
     */
    public Thumber()
    {
        this(Thumbnail.MimeType.JPG);
    }

    /**
     * Thumbnails will be generated of the given type and, if the type permits it (PNG), preserve transparency.
     * @param mimeType the type of all thumbnails generated and retrieved.
     */
    public Thumber(Thumbnail.MimeType mimeType)
    {
        if (mimeType == null)
        {
            throw new IllegalArgumentException("mimeType cannot be null");
        }
        this.mimeType = mimeType;
    }

    // According to RFC 2045, mime types are not case sensitive. You can't just use contains. You MUST use equalsIgnoreCase.
    public static List<String> getThumbnailMimeTypes()
    {
        return THUMBNAIL_MIME_TYPES;
    }

    public static List<String> getThumbnailFormats()
    {
        return THUMBNAIL_FORMATS;
    }

    private float encodingQuality = 0.80f; // default to 0.80f, seems reasonable enough, and still provides good result.

    /**
     * @return True if the AWT default toolkit exists, false (with an error logged) if it does not.
     */
    public boolean checkToolkit()
    {
        try
        {
            Toolkit.getDefaultToolkit();
        }
        catch (Throwable e)
        {
            log.error("Unable to acquire AWT default toolkit - thumbnails will not be displayed. Check DISPLAY variable or use setting -Djava.awt.headless=true.", e);
            return false;
        }
        return true;
    }


    /**
     * Retrieves an existing thumbnail, or creates a new one.
     *
     * @param originalFile The file which is being thumbnailed.
     * @param thumbnailFile The location of the existing thumbnail (if it exists), or the location to create a new
     * thumbnail.
     * @param maxWidth The max width of the thumbnail.
     * @param maxHeight The max height of the thumbnail.
     */
    public Thumbnail retrieveOrCreateThumbNail(File originalFile, File thumbnailFile, int maxWidth, int maxHeight, long thumbnailId)
            throws MalformedURLException
    {
        FileInputStream originalFileStream = null;
        try
        {
            originalFileStream = new FileInputStream(originalFile);
            return retrieveOrCreateThumbNail(originalFileStream, originalFile.getName(), thumbnailFile, maxWidth, maxHeight, thumbnailId);
        }
        catch (FileNotFoundException e)
        {
            log.error("Unable to create thumbnail: file not found: " + originalFile.getAbsolutePath());
        }
        finally
        {
            IOUtils.closeQuietly(originalFileStream);
        }

        return null;
    }

    private void storeImageAsPng(BufferedImage image, File file) throws FileNotFoundException
    {
        if (image == null)
        {
            log.warn("Can't store a null scaledImage.");
            return;
        }
        try
        {
            ImageIO.write(image, "png", file);
        }
        catch (IOException e)
        {
            log.error("Error encoding the thumbnail image", e);
        }
    }

    // All thumbnail images are stored in JPEG format on disk.
    // With CORE-101 fixed this method may be called with a null image
    public void storeImage(BufferedImage scaledImage, File file) throws FileNotFoundException
    {
        if (scaledImage == null)
        {
            log.warn("Can't store a null scaledImage.");
            return;
        }
        FileImageOutputStream fout = null;
        try
        {
            fout = new FileImageOutputStream((file));

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(encodingQuality);
            writer.setOutput(fout);
            writer.write(null, new IIOImage(scaledImage, null, null), param);
        }
        catch (IOException e)
        {
            log.error("Error encoding the thumbnail image", e);
        }
        finally
        {
            try
            {
                if (fout != null)
                {
                    fout.close();
                }
            }
            catch (IOException e)
            {
                //
            }
        }
    }

    private BufferedImage scaleImageFastAndGood(BufferedImage imageToScale, WidthHeightHelper newDimensions)
    {
        if (newDimensions.width > imageToScale.getWidth() || newDimensions.height > imageToScale.getHeight())
        {
            return getScaledInstance(imageToScale, newDimensions.getWidth(), newDimensions.getHeight(),
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC, false);
        }
        else
        {
            return getScaledInstance(imageToScale, newDimensions.getWidth(), newDimensions.getHeight(),
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        }
    }


    /**
     * Convenience method that returns a scaled instance of the provided {@code BufferedImage}.
     *
     * Borrowed from http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     * Warning: this algorith enters endless loop, when target size is bigger than image size and higherQuality is true
     *
     * @param image the original image to be scaled
     * @param targetWidth the desired width of the scaled instance, in pixels
     * @param targetHeight the desired height of the scaled instance, in pixels
     * @param hint one of the rendering hints that corresponds to {@code RenderingHints.KEY_INTERPOLATION} (e.g. {@code
     * RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR}, {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR}, {@code
     * RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step scaling technique that provides higher quality
     * than the usual one-step technique (only useful in downscaling cases, where {@code targetWidth} or {@code
     * targetHeight} is smaller than the original dimensions, and generally only when the {@code BILINEAR} hint is
     * specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    private BufferedImage getScaledInstance(BufferedImage image,
            int targetWidth,
            int targetHeight,
            Object hint,
            boolean higherQuality)
    {
        int type = (image.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        int w, h;
        if (higherQuality)
        {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = image.getWidth();
            h = image.getHeight();
        }
        else
        {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do
        {
            if (higherQuality && w > targetWidth)
            {
                w /= 2;
                if (w < targetWidth)
                {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight)
            {
                h /= 2;
                if (h < targetHeight)
                {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.drawImage(image, 0, 0, w, h, null);
            g2.dispose();

            image = tmp;
        }
        while (w != targetWidth || h != targetHeight);

        return image;
    }

    /**
     * This method should take BufferedImage argument, but takes just Image for backward compatibility (so that the
     * client code can stay intact). Normally anyway a BufferedImage instance will be provided and the image will be
     * directly processed without transforming it to BufferedImage first.
     *
     * @param imageToScale image to scale (BufferedImage is welcome, other image types will be transformed to
     * BufferedImage first)
     * @param newDimensions desired max. dimensions
     * @return scaled image
     */
    public BufferedImage scaleImage(Image imageToScale, WidthHeightHelper newDimensions)
    {
        if (imageToScale instanceof BufferedImage)
        {
            return scaleImageFastAndGood((BufferedImage) imageToScale, newDimensions);
        }
        return scaleImageFastAndGood(Pictures.toBufferedImage(imageToScale), newDimensions);
    }


    /**
     * This method provides extremely slow way to scale your image. There are generally much better alternatives (order
     * of magnitude faster with similar quality).
     * Consider using {@link Thumber#getScaledInstance(java.awt.image.BufferedImage, int, int, Object, boolean)} instead
     *
     * @param imageToScale input image
     * @param newDimensions desired max. dimensions (the ratio will be kept)
     * @return scaled image
     */
    @Deprecated
    public BufferedImage scaleImageOld(Image imageToScale, WidthHeightHelper newDimensions)
    {


        // If the original image is an instance of BufferedImage, we need to make sure
        // that it is an sRGB image. If it is not, we need to convert it before scaling it
        // as we run into these issue otherwise:
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4886071
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4705399

        if (imageToScale instanceof BufferedImage)
        {
            BufferedImage bufferedImage = (BufferedImage) imageToScale;
            if (!bufferedImage.getColorModel().getColorSpace().isCS_sRGB())
            {
                BufferedImage sRGBImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics g = sRGBImage.getGraphics();
                g.drawImage(bufferedImage, 0, 0, null);
                g.dispose();
                imageToScale = sRGBImage;
            }
        }

        AreaAveragingScaleFilter scaleFilter =
                new AreaAveragingScaleFilter(newDimensions.getWidth(), newDimensions.getHeight());
        ImageProducer producer = new FilteredImageSource(imageToScale.getSource(),
                scaleFilter);

        SimpleImageConsumer generator = new SimpleImageConsumer();
        producer.startProduction(generator);
        BufferedImage scaled = generator.getImage();

        // CORE-101 getImage may return null
        if (scaled == null)
        {
            log.warn("Unabled to create scaled image.");
        }
        else
        {
            scaled.flush();
        }

        return scaled;
    }

    /**
     * Need to pass filename in as we cannot get the filename from the stream that is passed in
     *
     * @return a Thumbnail instance or null if an error occured
     */
    public Thumbnail retrieveOrCreateThumbNail(InputStream originalFileStream, String fileName, File thumbnailFile, int maxWidth, int maxHeight, long thumbnailId)
            throws MalformedURLException
    {
        Thumbnail thumbnail = null;
        try
        {
            thumbnail = getThumbnail(thumbnailFile, fileName, thumbnailId);
        }
        catch (IOException e)
        {
            log.error("Unable to get thumbnail image for id " + thumbnailId, e);
            return null;
        }

        if (thumbnail == null)
        {
            try
            {
                thumbnail = createThumbnail(originalFileStream, thumbnailFile, maxWidth, maxHeight, thumbnailId, fileName);
            }
            catch (IIOException e)
            {
                log.info("Unable to create thumbnail image for id " + thumbnailId, e);
                return null;
            }
            catch (IOException e)
            {
                log.error("Unable to create thumbnail image for id " + thumbnailId, e);
                return null;
            }
        }

        return thumbnail;
    }

    // PRIVATE METHODS -------------------------------------------------------------------------------------------
    private BufferedImage scaleImage(Image originalImage, int maxWidth, int maxHeight)
    {
        return scaleImage(originalImage, determineScaleSize(maxWidth, maxHeight, originalImage));
    }

    private WidthHeightHelper determineScaleSize(int maxWidth, int maxHeight, Image image)
    {
        return determineScaleSize(maxWidth, maxHeight, image.getWidth(null), image.getHeight(null));
    }

    private Thumbnail createThumbnail(InputStream originalFile, File thumbnailFile, int maxWidth, int maxHeight, long thumbId, String fileName)
            throws IOException, FileNotFoundException
    {
        // Load original image.
        final Image originalImage = getImage(originalFile);
        // Create scaled buffered image from original image.
        final BufferedImage scaledImage = scaleImage(originalImage, maxWidth, maxHeight);

        final int height = scaledImage.getHeight();
        final int width = scaledImage.getWidth();

        if (mimeType == Thumbnail.MimeType.PNG)
        {
            storeImageAsPng(scaledImage, thumbnailFile);
        }
        else
        {
            storeImage(scaledImage, thumbnailFile);
        }

        return new Thumbnail(height, width, fileName, thumbId, mimeType);
    }


    private Thumbnail getThumbnail(File thumbnailFile, String filename, long thumbId) throws IOException
    {
        if (thumbnailFile.exists())
        {
            final Image thumbImage = getImage(thumbnailFile);
            return new Thumbnail(thumbImage.getHeight(null), thumbImage.getWidth(null), filename, thumbId, mimeType);
        }
        return null;
    }

    /**
     * @return An Image object or null if there was no suitable ImageReader for the given data
     */
    public Image getImage(File file) throws IOException
    {
        return ImageIO.read(file);
    }

    /**
     * @return An Image object or null if there was no suitable ImageReader for the given data
     */
    public Image getImage(InputStream is) throws IOException
    {
        return ImageIO.read(is);
    }

    /**
     * Set the default encoding quality used by the thumber to encode jpegs.
     */
    public void setEncodingQuality(float f)
    {
        if (f > 1.0f || f < 0.0f)
        {
            throw new IllegalArgumentException("Invalid quality setting '" + f + "', value must be between 0 and 1. ");
        }
        encodingQuality = f;
    }

    public WidthHeightHelper determineScaleSize(int maxWidth, int maxHeight, int imageWidth, int imageHeight)
    {
        if (maxHeight > imageHeight && maxWidth > imageWidth)
        {
            return new Thumber.WidthHeightHelper(imageWidth, imageHeight);
        }
        // Determine scale size.
        // Retain original image proportions with scaled image.
        double thumbRatio = (double) maxWidth / (double) maxHeight;

        double imageRatio = (double) imageWidth / (double) imageHeight;

        if (thumbRatio < imageRatio)
        {
            return new Thumber.WidthHeightHelper(maxWidth, (int) Math.max(1, maxWidth / imageRatio));
        }
        else
        {
            return new Thumber.WidthHeightHelper((int) Math.max(1, maxHeight * imageRatio), maxHeight);
        }
    }

    public boolean isFileSupportedImage(File file)
    {
        try
        {
            return isFileSupportedImage(new FileInputStream(file));
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
    }

    public boolean isFileSupportedImage(InputStream inputStream)
    {
        try
        {
            imageInfo.setInput(inputStream);
            imageInfo.check();
            for (String format : THUMBNAIL_FORMATS)
            {
                if (format.equalsIgnoreCase(imageInfo.getFormatName()))
                {
                    return true;
                }
            }
            return false;
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (Exception e)
            {
                log.error(e, e);
            }
        }
    }

    public static class WidthHeightHelper
    {
        private int width;
        private int height;

        public WidthHeightHelper(int width, int height)
        {
            this.width = width;
            this.height = height;
        }

        public int getWidth()
        {
            return width;
        }

        public void setWidth(int width)
        {
            this.width = width;
        }

        public int getHeight()
        {
            return height;
        }

        public void setHeight(int height)
        {
            this.height = height;
        }
    }

    /**
     * Code based on http://www.dreamincode.net/code/snippet1076.htm Looks like public domain
     * The change: I don't use screen-compatible image creation - I assume JIRA runs in headless mode anyway
     */
    static class Pictures
    {
        public static BufferedImage toBufferedImage(Image image)
        {
            if (image instanceof BufferedImage) {return (BufferedImage) image;}

            // This code ensures that all the pixels in the image are loaded
            image = new ImageIcon(image).getImage();

            // Determine if the image has transparent pixels
            boolean hasAlpha = hasAlpha(image);

            // Create a buffered image using the default color model
            int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);

            // Copy image to buffered image
            Graphics g = bimage.createGraphics();

            // Paint the image onto the buffered image
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bimage;
        }

        public static boolean hasAlpha(Image image)
        {
            // If buffered image, the color model is readily available
            if (image instanceof BufferedImage)
            {
                return ((BufferedImage) image).getColorModel().hasAlpha();
            }

            // Use a pixel grabber to retrieve the image's color model;
            // grabbing a single pixel is usually sufficient
            PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
            try
            {
                pg.grabPixels();
            }
            catch (InterruptedException ignored)
            {
            }

            // Get the image's color model
            return pg.getColorModel().hasAlpha();
        }
    }

}
