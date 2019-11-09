package chessgames;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.PathMatcher;
import java.nio.file.FileSystems;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main Application Class for Chessgames.
 *
 * @author Aled Davies
 */
public class ChessGames {
  /**
   * Parse the result line.
   *
   * @param line - A String containing the line. Cannot be null.
   * @return Optional.empty() if the line is null, or does not fileMatches the expected pattern.
   *     Optional.of(MatchResult) otherwise.
   */
  static Optional<MatchResult> parseLine(String line) {
    if (line != null && line.startsWith("[Result")) {
      int index = line.indexOf("-");

      if (index != -1) {
        switch (line.charAt(index - 1)) {
          case '0':
            return Optional.of(MatchResult.BLACK_WINS);
          case '1':
            return Optional.of(MatchResult.WHITE_WINS);
          case '2':
            return Optional.of(MatchResult.DRAW);
          default:
            return Optional.empty(); // Ignore malformed lines or matches in progress
        }
      }
    }
    return Optional.empty();
  }

  /** @return A Stream of Lines from the file at supplied Path. */
  static Stream<String> parseFile(Path filePath) {
    checkNotNull("Path to file cannot be null", filePath);

    try {
      return Files.lines(filePath, StandardCharsets.ISO_8859_1);

    } catch (IOException e) {
      throw new RuntimeException(String.format("Error reading file: %s", filePath), e);
    }
  }

  private static final PathMatcher pathMatcher =
      FileSystems.getDefault().getPathMatcher("glob:*.pgn");

  static boolean fileMatches(Path path, BasicFileAttributes attributes) {
    return path != null && path.getFileName() != null && pathMatcher.matches(path.getFileName());
  }

  private static void checkNotNull(String message, Object object) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  /**
   * Main application method.
   *
   * @param basePath - A reference to the base directory we need to start from.
   * @throws IOException - An IOException is thrown if an error occurs while processing the files.
   */
  public void run(Path basePath) throws IOException {
    // Validate arguments
    checkNotNull("Base directory path cannot be null", basePath);

    // Locate all the files we need to consider
    // Process each file. As each file is independent of each other we can
    // parseFile each file in parallel and collate the results.
    Map<MatchResult, Long> results =
        Files.find(basePath, Integer.MAX_VALUE, ChessGames::fileMatches)
            .collect(Collectors.toList()) // Collect all files that fileMatches the pattern
            .parallelStream() // Process in Parallel
            .flatMap(ChessGames::parseFile) // Process the File into lines
            .filter(l -> l.startsWith("[Result")) // Only consider lines that start with "[Result"
            .map(ChessGames::parseLine) // Parse the line
            .filter(Optional::isPresent) // If a result is present unpack
            .map(Optional::get)
            .collect(
                Collectors.groupingBy(
                    Function.identity(), // Group the files by Match results
                    Collectors.counting()));

    // Extract the results from the map for printing
    long blackWins = results.getOrDefault(MatchResult.BLACK_WINS, 0L);
    long whiteWins = results.getOrDefault(MatchResult.WHITE_WINS, 0L);
    long draws = results.getOrDefault(MatchResult.DRAW, 0L);

    // Print the results
    System.out.printf("%d %d %d %d%n", blackWins + whiteWins + draws, whiteWins, blackWins, draws);
  }

  /**
   * Main method.
   *
   * @param args - Program arguments.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("usage: chessgames <BASE DIRECTORY PATH>");
      return;
    }

    Path basePath = Paths.get(args[0]);

    if (basePath == null) {
      System.out.printf("chessgames: ERROR - Cannot find path: '%s'%n", args[0]);
      return;
    }

    if (Files.isDirectory(basePath)) {
      try {
        ChessGames chessGames = new ChessGames();
        chessGames.run(basePath);

      } catch (IOException e) {
        System.out.printf("chessgames: ERROR - '%s'%n", e.getMessage());
      }
    } else {
      System.out.printf("chessgames: ERROR - '%s' is not a directory%n", args[0]);
    }
  }
}
