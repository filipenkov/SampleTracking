package com.atlassian.diff;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;

import java.util.List;

public class TestWordLevelDiffer extends ListeningTestCase
{
    @Test
    public void testDiffLineAdditions() throws DifferentiationFailedException
    {
        // Additions at ends.
        String originalLine = "a b";
        String revisedLine = "1 2 a b 3 4";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.ADDED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.UNCHANGED, "a b");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " 3 4");

        // Additions inside only
        revisedLine = "a 1 2 b";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "a");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, " 1 2");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " b");
    }

    @Test
    public void testDiffLineDeletions() throws DifferentiationFailedException
    {
        // Deletions from ends.
        String originalLine = "1 2 a b 3 4";
        String revisedLine = "a b";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.DELETED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.UNCHANGED, " a b");
        assertChunk(chunks.get(2), DiffType.DELETED_WORDS, " 3 4");

        // Deletions inside only
        originalLine = "a 1 2 b";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "a");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " 1 2");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " b");
    }

    @Test
    public void testDiffLineChanges() throws DifferentiationFailedException
    {
        // Changes at ends.
        String originalLine = "1 2 a b c d 3 4";
        String revisedLine = "5 6 a b c d 7 8";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(5, chunks.size());

        assertChunk(chunks.get(0), DiffType.DELETED_WORDS, "1 2");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, "5 6");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " a b c d");
        assertChunk(chunks.get(3), DiffType.DELETED_WORDS, " 3 4");
        assertChunk(chunks.get(4), DiffType.ADDED_WORDS, " 7 8");

        // Changes inside only
        originalLine = "5 6 e f g h 7 8";

        chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "5 6");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " e f g h");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " a b c d");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " 7 8");
    }

    @Test
    public void testWordsOfContext() throws DifferentiationFailedException
    {
        WordLevelDiffer.Word[] originalContent = WordLevelDiffer.tokenize("The quick black fox jumped on the lazy dog.");
        WordLevelDiffer.Word[] revisedContent = WordLevelDiffer.tokenize("The quick brown fox jumped over the lazy dog.");

        Delta delta;
        Chunk chunkBefore = null, chunkAfter;
        String wordsOfContext;

        Revision revision = new Diff(originalContent).diff(revisedContent);

        delta = revision.getDelta(0);
        chunkBefore = null;
        chunkAfter = delta.getOriginal();
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals("The quick", wordsOfContext);

        delta = revision.getDelta(1);
        chunkBefore = chunkAfter;
        chunkAfter = delta.getOriginal();
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals(" fox jumped", wordsOfContext);

        chunkBefore = chunkAfter;
        chunkAfter = null;
        wordsOfContext = WordLevelDiffer.getUnchangedWordsBetweenChunks(originalContent, chunkBefore, chunkAfter).getText();
        assertEquals(" the lazy dog.", wordsOfContext);
    }

    @Test
    public void testSpaceAdditions() throws Exception
    {
        String originalLine = "The quick brown foxjumped over the lazy dog.";
        String revisedLine  = "The quick brown fox jumped over the lazy dog.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(4, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "The quick brown");
        assertChunk(chunks.get(1), DiffType.DELETED_WORDS, " foxjumped");
        assertChunk(chunks.get(2), DiffType.ADDED_WORDS, " fox jumped");
        assertChunk(chunks.get(3), DiffType.UNCHANGED, " over the lazy dog.");
    }
    @Test
    public void testChangeOfSpace() throws Exception
    {
        String originalLine = "The quick brown fox  jumped over the lazy dog.";
        String revisedLine  = "The quick brown fox jumped over the lazy dog.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(1, chunks.size());

        final WordChunk chunk0 = (WordChunk)chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals(originalLine, chunk0.getText());
    }

    // "numbers" and "numbers," should show up as a char change, not a word replacement.
    @Test
    public void testWordAndCharacterAdditions() throws Exception
    {
        String originalLine = "Update the port numbers for the test runner.";
        String revisedLine  = "Update the port numbers, dbnames and cluster.names for the test runner.";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        assertChunk(chunks.get(0), DiffType.UNCHANGED, "Update the port numbers");
        assertChunk(chunks.get(1), DiffType.ADDED_WORDS, ", dbnames and cluster.names");
        assertChunk(chunks.get(2), DiffType.UNCHANGED, " for the test runner.");
    }

    @Test
    public void testChangeInNewlines() throws Exception
    {
        String originalLine = "project = 'ABC'\nAND assignee is not empty";
        String revisedLine  = "project = 'ABC' AND assignee is empty";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(5, chunks.size());

        final DiffChunk chunk0 = chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals("project = 'ABC'", chunk0.getText());

        final DiffChunk chunk1 = chunks.get(1);
        assertEquals(DiffType.DELETED_WORDS, chunk1.getType());
        assertEquals("\n", chunk1.getText());

        final DiffChunk chunk2 = chunks.get(2);
        assertEquals(DiffType.UNCHANGED, chunk2.getType());
        assertEquals("AND assignee is", chunk2.getText());

        final DiffChunk chunk3 = chunks.get(3);
        assertEquals(DiffType.DELETED_WORDS, chunk3.getType());
        assertEquals(" not", chunk3.getText());

        final DiffChunk chunk4 = chunks.get(4);
        assertEquals(DiffType.UNCHANGED, chunk4.getType());
        assertEquals(" empty", chunk4.getText());
    }
    
    @Test
    public void testModifiedEndOfLone() throws Exception
    {
        String originalLine = "1st\n2nd\n3rd";
        String revisedLine  = "1st\n2nd foo\n3rd";

        List<DiffChunk> chunks = WordLevelDiffer.diffLine(originalLine, revisedLine);
        assertEquals(3, chunks.size());

        final DiffChunk chunk0 = chunks.get(0);
        assertEquals(DiffType.UNCHANGED, chunk0.getType());
        assertEquals("1st\n2nd", chunk0.getText());

        final DiffChunk chunk1 = chunks.get(1);
        assertEquals(DiffType.ADDED_WORDS, chunk1.getType());
        assertEquals(" foo", chunk1.getText());

        final DiffChunk chunk2 = chunks.get(2);
        assertEquals(DiffType.UNCHANGED, chunk2.getType());
        assertEquals("\n3rd", chunk2.getText());
    }

    private void assertChunk(DiffChunk actualChunk, DiffType expectedType, String expectedText)
    {
        assertEquals(expectedType, actualChunk.getType());
        assertEquals(expectedText, actualChunk.getText());
    }
}
