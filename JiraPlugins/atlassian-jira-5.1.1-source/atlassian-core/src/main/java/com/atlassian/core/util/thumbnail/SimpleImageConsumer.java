package com.atlassian.core.util.thumbnail;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class SimpleImageConsumer implements ImageConsumer
{
    private final Object holder;

    private ColorModel colorModel;
    private WritableRaster raster;
    private int width;
    private int height;

    private BufferedImage image;
    private int[] intBuffer;
    private volatile boolean loadComplete;

    public SimpleImageConsumer()
    {
        holder = new Object();
        width = -1;
        height = -1;
        loadComplete = false;
    }

    public void imageComplete(int status)
    {
        synchronized(holder)
        {
            loadComplete = true;
            holder.notify();
        }
    }

    public void setColorModel(ColorModel model)
    {
        colorModel = model;
        createImage();
    }

    /**
     * Notification of the dimensions of the source image.
     *
     * @param w The width of the source image
     * @param h The height of the source image
     */
    public void setDimensions(int w, int h)
    {
        width = w;
        height = h;
        createImage();
    }

    /**
     * Notification of load hints that may be useful. Not used in this
     * implementation.
     *
     * @param flags The hints
     */
    public void setHints(int flags)
    {
    }

    /**
     * Notification of a bunch of pixel values in byte form. Used for
     * 256 color or less images (eg GIF, greyscale etc).
     *
     * @param x The starting x position of the pixels
     * @param y The starting y position of the pixels
     * @param w The number of pixels in the width
     * @param h The number of pixels in the height
     * @param model The color model used with these pixel values
     * @param offset The offset into the source array to copy from
     * @param scansize The number of pixel values between rows
     */
    public void setPixels(int x,
                          int y,
                          int w,
                          int h,
                          ColorModel model,
                          byte[] pixels,
                          int offset,
                          int scansize)
    {
        if((intBuffer == null) || (pixels.length > intBuffer.length))
            intBuffer = new int[pixels.length];

        for(int i = pixels.length; --i >= 0 ; )
            intBuffer[i] = (int)pixels[i] & 0xFF;

        raster.setPixels(x, y, w, h, intBuffer);
    }

    /**
     * Notification of a bunch of pixel values as ints. These will be
     * full 3 or 4 component images.
     *
     * @param x The starting x position of the pixels
     * @param y The starting y position of the pixels
     * @param w The number of pixels in the width
     * @param h The number of pixels in the height
     * @param model The color model used with these pixel values
     * @param offset The offset into the source array to copy from
     * @param scansize The number of pixel values between rows
     */
    public void setPixels(int x,
                          int y,
                          int w,
                          int h,
                          ColorModel model,
                          int[] pixels,
                          int offset,
                          int scansize)
    {
        image.setRGB(x, y, w, h, pixels, offset, scansize);
    }

    /**
     * Notification of the properties of the image to use. Not used in this implementation.
     *
     * @param props The map of properties for this image
     */
    public void setProperties(Hashtable props)
    {
        createImage();
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Fetch the image. This image is not necessarily completely rendered
     * although we do try to guarantee it.
     *
     * Torsten Römer: Changed to public
     *
     * @return The image that has been created for the current input
     */
    public BufferedImage getImage()
    {
        if(!loadComplete)
        {
            synchronized(holder)
            {
                try
                {
                    holder.wait();
                }
                catch(InterruptedException ie)
                {
                }
            }
        }

        return image;
    }

    /**
     * Convenience method used to create the output image based on the data
     * that has been given to us so far. Will not create the image until all
     * the necessary information is given, and once created, will not overwrite
     * the current image.
     *
     * Torsten Römer: Use another constructor of BufferedImage. With the one used
     * here the resulting jpg was extremely blueish.
     */
    private void createImage()
    {
        // meet the preconditions first.
        if((image != null) ||
           (width == -1) ||
           (colorModel == null))
            return;

        boolean hasAlpha = colorModel.hasAlpha() || colorModel.getTransparency() != Transparency.OPAQUE;
        image = new BufferedImage(width, height, hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }
}
