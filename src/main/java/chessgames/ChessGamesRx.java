package chessgames;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Main Application Class for ChessGames.
 *
 * @author Aled Davies
 */
public class ChessGamesRx {

  private static void checkNotNull(String message, Object object) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  private static Observable<Path> findFiles(Path basePath) {
    checkNotNull("Base Path cannot be null", basePath);

    return Observable.create(observableEmitter -> {
      try (Stream<Path> paths = Files.find(basePath, Integer.MAX_VALUE, ChessGames::fileMatches)) {
        paths.forEach(observableEmitter::onNext);
        observableEmitter.onComplete();
      }
    });
  }

  private static Observable<MatchResult> parseFile(Path filePath) {
    checkNotNull("Path to file cannot be null", filePath);

    return Observable.create(observableEmitter -> {
      try (Stream<String> lines = Files.lines(filePath, StandardCharsets.ISO_8859_1)) {
        lines.filter(l -> l.startsWith("[Result")) // Only consider lines that start with "[Result"
            .map(ChessGames::parseLine) // Parse the line
            .filter(Optional::isPresent) // Ignore lines that could not be parsed
            .map(Optional::get) // If a result is present unpack
            .forEach(observableEmitter::onNext);
        observableEmitter.onComplete();
      }
    });
  }

  /**
   * Main application method.
   *
   * @param basePath - A reference to the base directory we need to start from.
   * @throws IOException - An IOException is thrown if an error occurs while processing the files.
   */
  private void run(Path basePath) throws IOException {
    // Validate arguments
    checkNotNull("Base directory path cannot be null", basePath);

    // Process each file. As each file is independent of each other we can
    // parseFile each file in parallel and collate the results.
    Map<MatchResult, Long> results = findFiles(basePath)
        .subscribeOn(Schedulers.io())
        .flatMap(
            path -> parseFile(path)
                    .subscribeOn(Schedulers.computation())
                    .groupBy(val -> val)
                    .flatMapSingle(gr -> gr.count().map(count -> newMapEntry(gr.getKey(), count))))
            .collect(
                HashMap<MatchResult, Long>::new,
                (r, s) -> r.put(s.getKey(), r.getOrDefault(s.getKey(), 0L) + s.getValue()))
            .blockingGet();

    // Extract the results from the map for printing
    long blackWins = results.getOrDefault(MatchResult.BLACK_WINS, 0L);
    long whiteWins = results.getOrDefault(MatchResult.WHITE_WINS, 0L);
    long draws = results.getOrDefault(MatchResult.DRAW, 0L);

    // Print the results
    System.out.printf("%d %d %d %d%n", blackWins + whiteWins + draws, whiteWins, blackWins, draws);
  }

  private <K, V> Map.Entry<K, V> newMapEntry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
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

    if (basePath.toFile().exists()) {
      try {
        ChessGamesRx chessGames = new ChessGamesRx();
        chessGames.run(basePath);

      } catch (IOException e) {
        System.out.printf("chessgames: ERROR - '%s'%n", e.getMessage());
      }
    } else {
      System.out.printf("chessgames: ERROR - '%s' is not a directory%n", args[0]);
    }
  }
}
