package com.atlassian.diff;

import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diffs words in a line of text.
 */
public class WordLevelDiffer
{
    /**
     * words are non-whitespace sequences, ore newlines
     */
    private static final Pattern WORD = Pattern.compile("[\\p{Alnum}_]+|\\S|(\r\n|\r|\n)");

    static class Word {
        final String leading;
        final String word;
        final String trailing;

        Word(String leading, String word, String trailing)
        {
            this.leading = leading;
            this.word = word;
            this.trailing = trailing;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Word word1 = (Word) o;

            if (!word.equals(word1.word)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return word.hashCode();
        }
    }

    /**
     * Returns a list of chunks that contain text and the status of the text - unchanged, added or deleted.
     *
     * @param originalLine the original line
     * @param revisedLine  the revised line
     * @return a list of chunks
     * @throws DifferentiationFailedException
     */
    public static List<DiffChunk> diffLine(String originalLine, String revisedLine) throws DifferentiationFailedException
    {
        Word[] originalWords = tokenize(originalLine);
        Word[] revisedWords = tokenize(revisedLine);
        return diffWords(originalWords, revisedWords);
    }

    static List<DiffChunk> diffWords(Word[] originalContent, Word[] revisedContent) throws
            DifferentiationFailedException
    {
        List<DiffChunk> wordChunks = new ArrayList<DiffChunk>();

        if (originalContent.length == 0 && revisedContent.length == 0)
            return null;

        Revision revision = new Diff(originalContent).diff(revisedContent);

        Chunk previousOriginalChunk = null;

        int numDeltas = revision.size();
        for (int deltaIndex = 0; deltaIndex < numDeltas; deltaIndex++)
        {
            Delta delta = revision.getDelta(deltaIndex);
            Chunk originalChunk = delta.getOriginal();
            Chunk revisedChunk = delta.getRevised();

            // Add any unchanged content in context before the delta
            WordChunk unchangedChunk = getUnchangedWordsBetweenChunks(originalContent, previousOriginalChunk, originalChunk);
            if (unchangedChunk != null)
            {
                wordChunks.add(unchangedChunk);
            }

            List<Word> originalChunkWords = originalChunk.chunk();
            List<Word> revisedChunkWords = revisedChunk.chunk();

                // Add the delta between original and revised
            if (delta instanceof DeleteDelta || delta instanceof ChangeDelta)
            {
                String deletedChunkText = join(originalChunkWords);
                if (StringUtils.isEmpty(deletedChunkText))
                    deletedChunkText = " "; // leading/trailing whitespace

                wordChunks.add(new WordChunk(DiffType.DELETED_WORDS, deletedChunkText));
            }
            if (delta instanceof AddDelta || delta instanceof ChangeDelta)
            {
                String addedChunkText = join(revisedChunkWords);
                if (StringUtils.isEmpty(addedChunkText))
                    addedChunkText = " "; // leading/trailing whitespace

                wordChunks.add(new WordChunk(DiffType.ADDED_WORDS, addedChunkText));
            }

            previousOriginalChunk = originalChunk;
        }

        // Add any trailing unchanged content in context.
        WordChunk unchangedChunk = getUnchangedWordsBetweenChunks(originalContent, previousOriginalChunk, null);
        if (unchangedChunk != null)
        {
            wordChunks.add(unchangedChunk);
        }

        return wordChunks;
    }

    static WordChunk getUnchangedWordsBetweenChunks(Word[] originalContent, Chunk chunkBefore, Chunk chunkAfter)
    {
        int unchangedStart = (chunkBefore != null) ? chunkBefore.last() + 1 : 0;
        int unchangedEnd = (chunkAfter != null) ? chunkAfter.first() : originalContent.length;
        if (unchangedEnd <= unchangedStart)
        {
            return null;
        }

        String chunkText = join(originalContent, unchangedStart, unchangedEnd);
        return new WordChunk(DiffType.UNCHANGED, chunkText);
    }

    static Word[] tokenize(String input)
    {
        List<Word> tokens = new LinkedList<Word>();
        final Matcher m = WORD.matcher(input);
        int last = 0;
        while (m.find(last)) {
            int start = m.start();
            final String leading = input.substring(last, start);
            tokens.add(new Word(leading, m.group(), ""));
            last = m.end();
        }
        if (last < input.length()) {
            tokens.add(new Word("", input.substring(last), ""));
        }
        return tokens.toArray(new Word[tokens.size()]);
    }

    private static String join(Iterable<Word> words)
    {
        StringBuilder b = new StringBuilder();
        for (Word w : words)
        {
            b.append(w.leading);
            b.append(w.word);
            b.append(w.trailing);
        }
        return b.toString();
    }
    private static String join(Word[] words, int start, int end)
    {
        StringBuilder b = new StringBuilder();
        for (int i = start; i < words.length && i < end; i++) {
            Word w = words[i];
            b.append(w.leading);
            b.append(w.word);
            b.append(w.trailing);
        }
        return b.toString();
    }

}