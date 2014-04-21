/**
 * Contains a set of annotations that are used to identify and document a product's API. API elements carry with them a
 * guarantee of binary compatibility with future versions, meaning that clients that are compiled against version X are
 * generally guaranteed to run on version X+1 without needing to be recompiled (the exact guarantee will vary depending
 * on the product's API policy).
 * <p/>
 * Binary compatibility is defined in <a href="http://java.sun.com/docs/books/jls/second_edition/html/binaryComp.doc.html">
 * chapter 13 of the Java Language Specification</a>.
 */
package com.atlassian.annotations;
