package chessgames;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 * Unit Test for ChessGames.
 *
 * @author Aled Davies
 */
public class ChessGamesTest {
  @Test
  public void testParseLineWithNull() {
    assertFalse("Null line should be empty", ChessGames.parseLine(null).isPresent());
  }

  @Test
  public void testParseLineWithEmptyString() {
    assertFalse("Empty line should be empty", ChessGames.parseLine(null).isPresent());
  }

  @Test
  public void testParseLineWithMalformedString() {
    assertFalse("Malformed line should be empty", ChessGames.parseLine("Result").isPresent());
  }

  @Test
  public void testParseLineWithBlackWinning() {
    assertEquals(
        "Black should win",
        Optional.of(MatchResult.BLACK_WINS),
        ChessGames.parseLine("[Result \"0-1\"]"));
  }

  @Test
  public void testParseLineWithWhiteWinning() {
    assertEquals(
        "White should win",
        Optional.of(MatchResult.WHITE_WINS),
        ChessGames.parseLine("[Result \"1-0\"]"));
  }

  @Test
  public void testParseLineWithDraw() {
    assertEquals(
        "Should be a draw",
        Optional.of(MatchResult.DRAW),
        ChessGames.parseLine("[Result \"1/2-1/2\"]"));
  }

  @Test
  public void testParseLineWithMatchInProgress() {
    assertEquals(
        "Match should be in progress should be empty",
        Optional.empty(),
        ChessGames.parseLine("[Result \"-\"]"));
  }

  @Test(expected = NullPointerException.class)
  public void testParseFileWillNull() {
    ChessGames.parseFile(null);
  }

  @Test
  public void testFileMatchesWithNulls() {
    assertFalse("null null should not match", ChessGames.fileMatches(null, null));
  }

  @Test
  public void testFileMatchesWithNullPath() {
    BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    assertFalse("<path> null should not match", ChessGames.fileMatches(null, attributes));
    verifyZeroInteractions(attributes);
  }

  @Test
  public void testFileMatchesWithNullFileName() {
    Path path = mock(Path.class);
    when(path.getFileName()).thenReturn(null);
    BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    assertFalse("<path> null should not match", ChessGames.fileMatches(path, attributes));

    verifyZeroInteractions(attributes);
    verify(path, times(1)).getFileName();
  }

  @Test
  public void testFileMatchesWithFileName() {
    Path filePath = mock(Path.class);

    Path path = mock(Path.class);
    when(path.getFileName()).thenReturn(filePath);
    BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    assertFalse("<path> mock filePath should not match", ChessGames.fileMatches(path, attributes));

    verifyZeroInteractions(attributes);
    verifyZeroInteractions(filePath);
    verify(path, times(2)).getFileName();
  }
}
