package com.atlassian.renderer.wysiwyg;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.tidy.Tidy;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.apache.html.dom.HTMLDocumentImpl;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/**
 * Utility class for using NekoHTML
 * http://people.apache.org/~andyc/neko/doc/html/index.html
 */
public abstract class HtmlParserUtil {

    public DocumentFragment getDocumentFragment() {
            return document;
    }

    public Document getDocument() {
        try
        {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(document);
            return doc;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
   }

    private DocumentFragment document;

    public HtmlParserUtil(String xhtml) throws UnsupportedEncodingException {
        this(new ByteArrayInputStream(xhtml.getBytes("UTF-8")));
    }

    public HtmlParserUtil(InputStream in) {
        DOMFragmentParser parser = new DOMFragmentParser();

            HTMLDocument htmlDocument = new HTMLDocumentImpl();
            document = htmlDocument.createDocumentFragment();
            InputSource inputSource = new InputSource(in);
            try
            {
                parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
                init(parser);
                parser.parse(inputSource, document);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
    }

    protected abstract void init(DOMFragmentParser parser);

    public Node findTag(String name) {
        return findTag(name, document);
    }

    private Node findTag(String name, Node node) {
        if (node.getNodeName().toLowerCase().equals(name.toLowerCase())) {
            return node;
        } else {
            for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
                Node n = findTag(name, node.getChildNodes().item(i));
                if (n != null) {
                    return n;
                }
            }
            return null;
        }
    }

    public String getText(Node node) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
            Node n = node.getChildNodes().item(i);
            if (n.getNodeType() == Node.TEXT_NODE)
            {
                sb.append(n.getNodeValue());
            }
        }
        return sb.toString();
    }
}
