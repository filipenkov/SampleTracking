package com.atlassian.core.spool;

import com.mockobjects.dynamic.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TestSpools extends AbstractSpoolTest
{
    public void testByteArraySpool() throws Exception
    {
        Spool byteArraySpool = new ByteArraySpool();
        byte[] data = getTestData(1024);
        verifySpool(byteArraySpool, data);
    }

    public void testBufferedFileSpool() throws Exception
    {
        Spool bufferedArraySpool = new BufferedFileSpool();
        byte[] data = getTestData(1024 * 10);
        verifySpool(bufferedArraySpool, data);
    }

    public void testSmartSpoolImmediateOverflow() throws Exception
    {
        Mock mockImmediateOverflowSpool = new Mock(Spool.class);
        Mock mockThresholdingSpool = new Mock(ThresholdingSpool.class);

        Spool immediateOverflowSpool = (Spool) mockImmediateOverflowSpool.proxy();
        ThresholdingSpool thresholdingSpool = (ThresholdingSpool) mockThresholdingSpool.proxy();

        SmartSpool smartSpool = new SmartSpool();
        smartSpool.setOverThresholdSpool(immediateOverflowSpool);
        smartSpool.setThresholdingSpool(thresholdingSpool);

        byte[] data = getTestData(2049); // One byte more than we are prepared to spool in RAM
        InputStream dataStream = new ByteArrayInputStream(data);

        mockThresholdingSpool.expectAndReturn("getThresholdBytes", new Integer(2048));
        mockImmediateOverflowSpool.expectAndReturn("spool", dataStream, new ByteArrayInputStream(data));
        mockThresholdingSpool.expectNotCalled("spool");

        verifySpool(smartSpool, dataStream);

        mockThresholdingSpool.verify();
        mockImmediateOverflowSpool.verify();
    }

    public void testSmartSpoolNoImmediateOverflow() throws Exception
    {
        Mock mockImmediateOverflowSpool = new Mock(Spool.class);
        Mock mockThresholdingSpool = new Mock(ThresholdingSpool.class);

        Spool immediateOverflowSpool = (Spool) mockImmediateOverflowSpool.proxy();
        ThresholdingSpool thresholdingSpool = (ThresholdingSpool) mockThresholdingSpool.proxy();

        SmartSpool smartSpool = new SmartSpool();
        smartSpool.setOverThresholdSpool(immediateOverflowSpool);
        smartSpool.setThresholdingSpool(thresholdingSpool);

        byte[] data = getTestData(2047); // One byte under RAM maximum
        InputStream dataStream = new ByteArrayInputStream(data);

        mockThresholdingSpool.expectAndReturn("getThresholdBytes", new Integer(2048));
        mockThresholdingSpool.expectAndReturn("spool", dataStream, new ByteArrayInputStream(data));
        mockImmediateOverflowSpool.expectNotCalled("spool");

        verifySpool(smartSpool, dataStream);

        mockThresholdingSpool.verify();
        mockImmediateOverflowSpool.verify();
    }

}
