package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.util.Stack;

/**
 * This Filter does some final filtering on the Tokens returned by the standard Lucene tokenizers in order to create the
 * exact tokens required for JIRA.
 *
 * <p/>
 * Currently, the StandardTokenizer takes anything of the 'alpha.alpha.alpha' form, and keeps it all together, because
 * it htinks it may be a server hostname (like "www.atlassian.com").
 * This is useful, however it prevents searches on the words between the dots.
 * An example is searching for 'NullPointerException' when 'java.lang.NullPointerException' has
 * been indexed.
 * This filter tokenizes the individual words, as well as the full phrase, allowing searching to
 * be done on either. (JRA-6397)
 * <p/>
 * <b>In addition</b>, a comma separated list of numbers (eg "123,456,789") is not tokenized at the commas.
 * This prevents searching on just "123".
 * This filter tokenizes the individual numbers, as well as the full phrase, allowing searching to
 * be done on either. (JRA-7774)
 */
public class SubtokenFilter extends TokenFilter
{
    // Some standard token types as defined in StandardTokenizer
    private static final String TOKEN_TYPE_HOST = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST];
    private static final String TOKEN_TYPE_NUM = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.NUM];
    // The "default" token type

    private static final String TOKEN_TYPE_EXCEPTION = "EXCEPTION";

    private Stack<Token> subtokenStack;

    public SubtokenFilter(TokenStream tokenStream)
    {
        super(tokenStream);
        this.subtokenStack = new Stack<Token>();
    }

    public Token next() throws IOException
    {
        if (!subtokenStack.isEmpty())
        {
            return subtokenStack.pop();
        }

        Token token = input.next();
        if (token == null)
        {
            return null;
        }

        // The standard Tokenizer thinks that 'Exceptions' are like 'hosts'
        // Some language analyzers show this token as type "word" (the default type).
        // http://lucene.apache.org/java/2_3_2/api/org/apache/lucene/analysis/Token.html#type()
        if (TOKEN_TYPE_HOST.equals(token.type()) || Token.DEFAULT_TYPE.equals(token.type()))
        {
            addSubtokensToStack(token, '.', TOKEN_TYPE_EXCEPTION);
        }
        //Comma separated alphanum are not separated correctly (JRA-7774)
        else if (TOKEN_TYPE_NUM.equals(token.type()))
        {
            addSubtokensToStack(token, ',', TOKEN_TYPE_NUM);
        }
        return token;

    }

    private void addSubtokensToStack(Token parentToken, char separatorChar, String newTokenType)
    {
        char[] termBuffer = parentToken.termBuffer();
        if (termBuffer == null)
        {
            // Reading the source code, this cannot happen in Lucene v2.3.2, however the previous implementation of this
            // filter had a null check, so I will make it work the same.
            return;
        }
        int termLength = parentToken.termLength();
        int offset = 0;
        // We iterate over the array, trying to find the separatorChar ('.' or ',')
        for (int index = 0; index <= termLength; index++)
        {
            // Note that we actually iterate past the last character in the array. At this point index == termLength.
            // We must check for this condition first to stop ArrayIndexOutOfBoundsException.
            // Being at the end of the array is a subtoken border just like the separator character ('.'), except we don't want to
            // add a duplicate token if no separator was already found. Hence we also check for offset > 0.
            if ((index < termLength && termBuffer[index] == separatorChar)
                 || (index == termLength && offset > 0))
            {
                int subtokenLength = index - offset;
                // Check that this is not an "empty" subtoken
                if (subtokenLength > 0)
                {
                    Token subtoken = new Token(parentToken.startOffset(), parentToken.endOffset(), newTokenType);
                    subtoken.setTermBuffer(parentToken.termBuffer(), offset, subtokenLength);
                    // Fixes JRA-20241 - when we create aliases for a token we need to mark them as in the same position
                    // otherwise things like PhraseQueries will not work correctly.
                    subtoken.setPositionIncrement(0);
                    subtokenStack.push(subtoken);
                }
                offset = index + 1;
            }
        }
    }
}
