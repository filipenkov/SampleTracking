package com.atlassian.core.util.thumbnail;

import com.atlassian.core.util.ImageInfo;
import junit.framework.TestCase;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This tests vary on the level of abstraction tested.
 * The reason is that I did not want to change public interface (method visibility) of Thumber class (in order to
 * keep clients compatible).
 * Thus sometimes higher level methods are tested and the test are not really unit -> e.g. testing
 * {@link com.atlassian.core.util.thumbnail.Thumber#retrieveOrCreateThumbNail(java.io.InputStream, String, java.io.File, int, int, long)}
 */
public class ThumberTest extends TestCase
{
    private static final int MAX_WIDTH = 30;
    private static final int MAX_HEIGHT = 40;
    private final File tempFile;
    private static final String TEST_FILE_NAME = "mypicture.png";

    public ThumberTest() throws IOException
    {
        tempFile = File.createTempFile("atlassian-core-thumbnail", "test");
    }

    @Override
    protected void setUp() throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        tempFile.delete(); // this file must not exist, other wise thumber won't try to scale the image
    }

    @Override
    protected void tearDown() throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        tempFile.delete();
    }

    public void testRetrieveOrCreateThumbnailTransparentPng() throws IOException
    {
        Thumber thumber = new Thumber(Thumbnail.MimeType.PNG);
        testRetrieveOrCreateThumbnail(thumber, "/transparent-png.png", Transparency.TRANSLUCENT, ImageInfo.FORMAT_PNG);
    }

    public void testRetrieveOrCreateThumbnailOpaqueGif() throws IOException
    {
        Thumber thumber = new Thumber();
        testRetrieveOrCreateThumbnail(thumber, "/opaque-gif.gif", Transparency.OPAQUE, ImageInfo.FORMAT_JPEG);
    }


    public void testRetrieveOrCreateThumbnailOpaquePng() throws IOException
    {
        Thumber thumber = new Thumber();
        testRetrieveOrCreateThumbnail(thumber, "/opaque-png.png", Transparency.OPAQUE, ImageInfo.FORMAT_JPEG);
    }

    public void testRetrieveOrCreateThumbnailJpg() throws IOException
    {
        Thumber thumber = new Thumber();
        testRetrieveOrCreateThumbnail(thumber, "/test-jpg.jpg", Transparency.OPAQUE, ImageInfo.FORMAT_JPEG);
    }

    public void testRetrieveOrCreateThumbnailTransparentGif() throws IOException
    {
        Thumber thumber = new Thumber(Thumbnail.MimeType.PNG);
        testRetrieveOrCreateThumbnail(thumber, "/transparent-gif.gif", Transparency.TRANSLUCENT, ImageInfo.FORMAT_PNG);
    }


    public void testScaleImageForNonBufferedImage()
    {
        // this should normally produce ToolkitImage
        final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/opaque-gif.gif"));
        final BufferedImage thumbnail = new Thumber().scaleImage(image,
                new Thumber.WidthHeightHelper(MAX_WIDTH, MAX_HEIGHT));

        // scaleImage ignores aspect ratio -> always produces desired with & height
        assertEquals(MAX_WIDTH, thumbnail.getWidth());
        assertEquals(MAX_HEIGHT, thumbnail.getHeight());
        assertEquals(Transparency.OPAQUE, thumbnail.getTransparency());
    }

    public void testScaleImageForNonBufferedImageTransparentGif()
    {
        // this should normally produce ToolkitImage
        final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/transparent-gif.gif"));
        final BufferedImage thumbnail = new Thumber().scaleImage(image,
                new Thumber.WidthHeightHelper(MAX_WIDTH, MAX_HEIGHT));

        // scaleImage ignores aspect ratio -> always produces desired with & height
        assertEquals(MAX_WIDTH, thumbnail.getWidth());
        assertEquals(MAX_HEIGHT, thumbnail.getHeight());
        assertEquals(Transparency.TRANSLUCENT, thumbnail.getTransparency());
    }

    public void testUpscalingShouldReturnOriginalSize() throws IOException
    {
        final String resourceName = "/opaque-png.png";
        final BufferedImage image = ImageIO.read(getClass().getResourceAsStream(resourceName));
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Thumbnail thumbnail = new Thumber().retrieveOrCreateThumbNail(
                getClass().getResourceAsStream(resourceName), TEST_FILE_NAME,
                tempFile, width * 3, height * 3, 1);

        assertThumbnail(thumbnail, Transparency.OPAQUE, ImageInfo.FORMAT_JPEG, width, height);
    }

    public void testScaleImageUpscaling() throws IOException
    {
        final String resourceName = "/opaque-png.png";
        final BufferedImage image = ImageIO.read(getClass().getResourceAsStream(resourceName));
        final int width = image.getWidth();
        final int height = image.getHeight();
        assertScaleImage(image, width * 3, height * 3);
        assertScaleImage(image, width - 10, height + 100);
        assertScaleImage(image, width + 100, height - 20);
    }

    public void testScaleImageWithInvalidParams() throws IOException
    {
        final String resourceName = "/opaque-png.png";
        final BufferedImage image = ImageIO.read(getClass().getResourceAsStream(resourceName));
        try
        {
            assertScaleImage(image, -30, -20);
            fail(IllegalArgumentException.class.getName() + " expected");
        }
        catch (IllegalArgumentException ignore)
        {
            // this is expected
        }
    }

    /**
     * Testing extremely wide and tall images clamp minimum width and height to 1,
     * as per http://jira.atlassian.com/browse/JRA-20369
     */
    public void testDetermineScaleSize() throws Exception {
        Thumber t = new Thumber();
        Thumber.WidthHeightHelper helper = t.determineScaleSize(20, 20, 1, 600);
        assertEquals(1, helper.getWidth());
        assertEquals(20, helper.getHeight());

        helper = t.determineScaleSize(200, 200, 600, 1);
        assertEquals(200, helper.getWidth());
        assertEquals(1, helper.getHeight());
    }

    private void assertScaleImage(final BufferedImage image, int aWidth, int aHeight)
    {
        final BufferedImage thumbnail = new Thumber().scaleImage(image, new Thumber.WidthHeightHelper(aWidth,
                aHeight));
        assertEquals(aWidth, thumbnail.getWidth());
        assertEquals(aHeight, thumbnail.getHeight());
        assertEquals(image.getTransparency(), thumbnail.getTransparency());
    }

    private void testRetrieveOrCreateThumbnail(Thumber thumber, String imageResourceName, int expectedTransparency, int expectedFormat) throws IOException
    {
        final Thumbnail thumbnail = thumber.retrieveOrCreateThumbNail(
                getClass().getResourceAsStream(imageResourceName), TEST_FILE_NAME,
                tempFile, MAX_WIDTH, MAX_HEIGHT, 1);

        final BufferedImage image = ImageIO.read(getClass().getResourceAsStream(imageResourceName));

        assertThumbnail(thumbnail, expectedTransparency, expectedFormat,
                MAX_WIDTH, MAX_WIDTH * image.getHeight() / image.getWidth());
    }

    private void assertThumbnail(final Thumbnail thumbnail, final int expectedTransparency, final int expectedFormat,
            int expectedWidth, int expectedHeight) throws IOException
    {
        assertEquals(expectedWidth, thumbnail.getWidth());
        assertEquals(expectedHeight, thumbnail.getHeight());
        assertEquals(TEST_FILE_NAME, thumbnail.getFilename());

        final BufferedImage thumbnailImage = ImageIO.read(tempFile);
        assertEquals(expectedWidth, thumbnailImage.getWidth());
        assertEquals(expectedHeight, thumbnailImage.getHeight());
        assertEquals(expectedTransparency, thumbnailImage.getTransparency());
        assertImageType(tempFile, expectedFormat);
    }


    private void assertImageType(final File tempFile, int format) throws IOException
    {
        final ImageInfo imageInfo = new ImageInfo();
        final FileInputStream fis = new FileInputStream(tempFile);
        imageInfo.setInput(fis);
        assertTrue(imageInfo.check());
        assertEquals(format, imageInfo.getFormat());
        fis.close();
    }
}
