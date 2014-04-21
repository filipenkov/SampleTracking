package com.atlassian.gadgets.publisher.internal.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.atlassian.gadgets.publisher.internal.GadgetSpecValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements spec validation through XML schemas.
 */
public final class GadgetSpecValidatorImpl implements GadgetSpecValidator
{
    private final Log log = LogFactory.getLog(GadgetSpecValidatorImpl.class);

    /**
     * Returns true if the specified gadget spec is valid against the XML
     * schema provided at construction time, false otherwise. Null values
     * for {@code spec} will be rejected. If validation fails, an error
     * will be logged at the DEBUG level.
     * @param spec the gadget spec to validate against the schema
     * @return true if the spec validates, false otherwise
     * @throws NullPointerException if {@code spec} is null
     */
    public boolean isValid(final InputStream spec)
    {
        checkNotNull(spec);
        try
        {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(spec));
            return true;
        }
        catch (ParserConfigurationException pce)
        {
            throw new Error("couldn't create XML parser", pce);
        }
        catch (SAXException saxe)
        {
            log.debug("couldn't parse gadget spec", saxe);
        }
        catch (IOException ioe)
        {
            log.debug("couldn't read from spec InputStream", ioe);
        }
        return false;
    }
}
