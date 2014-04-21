package com.atlassian.core.util.bean;

/**
 * @deprecated Use {@link com.atlassian.core.util.ImageInfo} instead (since 20/08/2008)
 */
public class ImageInfo extends com.atlassian.core.util.ImageInfo {
    
    public static final int FORMAT_JPEG = com.atlassian.core.util.ImageInfo.FORMAT_JPEG;

    public static final int FORMAT_GIF = com.atlassian.core.util.ImageInfo.FORMAT_JPEG;

    public static final int FORMAT_PNG = com.atlassian.core.util.ImageInfo.FORMAT_PNG;
 
    public static final int FORMAT_BMP = com.atlassian.core.util.ImageInfo.FORMAT_BMP;

    public static final int FORMAT_PCX = com.atlassian.core.util.ImageInfo.FORMAT_PCX;

    public static final int FORMAT_IFF = com.atlassian.core.util.ImageInfo.FORMAT_IFF;

    public static final int FORMAT_RAS = com.atlassian.core.util.ImageInfo.FORMAT_RAS;

    public static final int FORMAT_PBM = com.atlassian.core.util.ImageInfo.FORMAT_PBM;

    public static final int FORMAT_PGM = com.atlassian.core.util.ImageInfo.FORMAT_PGM;

    public static final int FORMAT_PPM = com.atlassian.core.util.ImageInfo.FORMAT_PPM;

    public static final int FORMAT_PSD = com.atlassian.core.util.ImageInfo.FORMAT_PSD;

    public static final int FORMAT_SWF = com.atlassian.core.util.ImageInfo.FORMAT_SWF;
    
    public boolean isValidImage()
    {
        return super.check();
    }
    
}
