package com.atlassian.jira.soap.axis;

import org.apache.axis.client.Stub;

import javax.activation.DataHandler;
import java.io.File;

public interface SoapAttachmentHelper
{
    String[] addFiles(Stub stub,
                                          String[] fileLocations);

    File[] saveFile(String[] attachmentFileNames) throws java.rmi.RemoteException;

    void relayAttachments(Stub stub) throws java.rmi.RemoteException;

    DataHandler[] getDataHandlers();
}
