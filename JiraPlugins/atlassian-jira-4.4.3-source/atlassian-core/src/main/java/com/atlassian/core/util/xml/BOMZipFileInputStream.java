package com.atlassian.core.util.xml;

import com.atlassian.core.util.DataUtils;

import java.io.*;
import java.util.zip.ZipInputStream;

/**
 * An input streams that handles Unicode Byte-Order Mark (BOM) marker within a normal file as well as a ZIP file.
 * Distilled and adapted from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206835
 */
public class BOMZipFileInputStream extends InputStream
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public final static byte[] UTF32BEBOMBYTES = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF,};
    public final static byte[] UTF32LEBOMBYTES = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00,};
    public final static byte[] UTF16BEBOMBYTES = new byte[]{(byte) 0xFE, (byte) 0xFF,};
    public final static byte[] UTF16LEBOMBYTES = new byte[]{(byte) 0xFF, (byte) 0xFE,};
    public final static byte[] UTF8BOMBYTES = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF,};
    public final static byte[][] BOMBYTES = new byte[][]{
            UTF32BEBOMBYTES,
            UTF32LEBOMBYTES,
            UTF16BEBOMBYTES,
            UTF16LEBOMBYTES,
            UTF8BOMBYTES,
    };
    public final static int NONE = -1;

    /**
     * No bom sequence is longer than 4 bytes
     */
    public final static int MAXBOMBYTES = 4;

    // ------------------------------------------------------------------------------------------------- Type Properties
    private InputStream daStream;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public BOMZipFileInputStream(String fileName) throws IOException, FileNotFoundException
    {
        int BOMType = getBOMType(fileName);
        int skipBytes = getSkipBytes(BOMType);
        InputStream fIn = getFileInputStream(fileName);
        if (skipBytes > 0)
        {
            fIn.skip(skipBytes);
        }
        daStream = fIn;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public int read() throws IOException
    {
        return daStream.read();
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods

    private InputStream getFileInputStream(String filename) throws IOException
    {
        InputStream is = null;
        FileInputStream fileInputStream = new FileInputStream(filename);
        if (filename != null && filename.trim().endsWith(DataUtils.SUFFIX_ZIP))
        {
            ZipInputStream input = new ZipInputStream(new BufferedInputStream(fileInputStream));
            input.getNextEntry();
            is = input;
        }
        else
        {
            is = new BufferedInputStream(fileInputStream);
        }
        return is;
    }

    private int getBOMType(String _f) throws IOException
    {
        InputStream fileInputStream = getFileInputStream(_f);
        byte[] buff = new byte[MAXBOMBYTES];
        int read = fileInputStream.read(buff);
        int bomType = getBOMType(buff, read);
        fileInputStream.close();
        return bomType;
    }

    private int getSkipBytes(int bomType)
    {
        if (bomType < 0 || bomType >= BOMBYTES.length) return 0;
        return BOMBYTES[bomType].length;
    }

    private int getBOMType(byte[] _bomBytes, int _length)
    {
        for (int i = 0; i < BOMBYTES.length; i++)
        {
            for (int j = 0; j < _length && j < BOMBYTES[i].length; j++)
            {
                if (_bomBytes[j] != BOMBYTES[i][j]) break;
                if (_bomBytes[j] == BOMBYTES[i][j] && j == BOMBYTES[i].length - 1) return i;
            }
        }
        return NONE;
    }
}