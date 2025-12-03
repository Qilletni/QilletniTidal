package dev.qilletni.lib.tidal.music.strategies.search;

import com.tidal.sdk.tidalapi.generated.models.ResourceIdentifier;
import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import com.tidal.sdk.tidalapi.generated.models.TracksResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TracksSingleResourceDataDocument;
import dev.qilletni.api.music.MusicCache;
import dev.qilletni.api.music.Track;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategy;
import dev.qilletni.lib.tidal.music.strategies.search.TidalSearchResolveStrategyFactory.SearchResolveResult;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Cache name and resolved track
public class TidalFuzzySearchResolveStrategy implements SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidalFuzzySearchResolveStrategy.class);

    private static final double TITLE_WEIGHT = 0.7;
    private static final double ARTIST_WEIGHT = 0.3;
    private static final double MINIMUM_SCORE_THRESHOLD = 0.6;
    private static final double METADATA_PENALTY = 0.85;
    private static final double METADATA_BLEND_CORE = 0.7;
    private static final double METADATA_BLEND_META = 0.3;

    private final MusicCache musicCache;
    private final JaroWinklerSimilarity jaroWinkler;

    public TidalFuzzySearchResolveStrategy(MusicCache musicCache) {
        this.musicCache = musicCache;
        this.jaroWinkler = new JaroWinklerSimilarity();
    }

    @Override
    public String getName() {
        return "fuzzy";
    }

    @Override
    public SearchResolveResult resolveTrack(SearchResultsSingleResourceDataDocument searchResults, String title, String artist) {
        var trackIds = searchResults.getData().getRelationships().getTracks().getData();
        var tracks = musicCache.getTracksById(trackIds.stream().map(ResourceIdentifier::getId).toList());

        LOGGER.debug("Fuzzy matching query: title='{}', artist='{}'", title, artist);

        Track bestMatch = null;
        double bestScore = MINIMUM_SCORE_THRESHOLD;

        for (var track : tracks) {
            double titleScore = calculateTitleScore(title, track.getName());
            double artistScore = calculateArtistScore(artist, track);
            double score = calculateMatchScore(title, artist, track);

            LOGGER.debug("Track: '{}' by '{}' | Title Score: {}, Artist Score: {}, Final Score: {}",
                    track.getName(),
                    track.getArtists().isEmpty() ? "Unknown" : track.getArtists().get(0).getName(),
                    String.format("%.3f", titleScore),
                    String.format("%.3f", artistScore),
                    String.format("%.3f", score));

            if (score > bestScore) {
                bestScore = score;
                bestMatch = track;
            }
        }

        if (bestMatch != null) {
            LOGGER.debug("Best match: '{}' by '{}' with score {}",
                    bestMatch.getName(),
                    bestMatch.getArtists().isEmpty() ? "Unknown" : bestMatch.getArtists().get(0).getName(),
                    String.format("%.3f", bestScore));
        } else {
            LOGGER.debug("No match found above threshold {}", MINIMUM_SCORE_THRESHOLD);
        }

        return bestMatch != null ? new SearchResolveResult(bestMatch) : null;
    }

    /**
     * Extracts the core title by removing parentheticals, brackets, and trailing metadata.
     */
    private String extractCoreTitle(String title) {
        String core = title
                .replaceAll("\\s*[(\\[][^)\\]]*[)\\]]\\s*", " ")  // Remove (Remix), [Live], etc.
                .replaceAll("\\s*-\\s*.*$", "")                         // Remove " - Remastered" type suffixes
                .trim();

        return normalizeSimple(core);
    }

    /**
     * Normalizes a string by converting to lowercase, removing punctuation and "The" prefix.
     */
    private String normalizeSimple(String s) {
        return s.toLowerCase()
                .replaceFirst("^the\\s+", "")           // Remove "The" prefix
                .replaceAll("[^a-z0-9\\s]", " ")        // Remove punctuation, keep spaces
                .replaceAll("\\s+", " ")                // Collapse multiple spaces
                .trim();
    }

    /**
     * Normalizes artist names.
     */
    private String normalizeArtist(String artist) {
        return artist.toLowerCase()
                .replaceFirst("^the\\s+", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Checks if a title has metadata (content in parentheses or brackets).
     */
    private boolean hasMetadata(String title) {
        return title.matches(".*[(\\[].*[)\\]].*");
    }

    /**
     * Extracts metadata content from parentheses and brackets.
     */
    private String extractMetadata(String title) {
        Pattern pattern = Pattern.compile("[(\\[]([^)\\]]+)[)\\]]");
        Matcher matcher = pattern.matcher(title);

        StringBuilder metadata = new StringBuilder();
        while (matcher.find()) {
            if (!metadata.isEmpty()) metadata.append(" ");
            metadata.append(matcher.group(1));
        }

        return metadata.toString();
    }

    /**
     * Calculates title score with asymmetric metadata handling.
     * If the query specifies metadata (e.g., "Song (Remix)"), tracks without matching metadata are penalized.
     */
    private double calculateTitleScore(String queryTitle, String trackTitle) {
        String queryCore = extractCoreTitle(queryTitle);
        String trackCore = extractCoreTitle(trackTitle);

        // Base score on core title match
        double coreScore = jaroWinkler.apply(queryCore, trackCore);

        // If core doesn't match well, bail early
        if (coreScore < 0.7) {
            return coreScore;
        }

        // Check if query has metadata (parentheticals/brackets)
        boolean queryHasMetadata = hasMetadata(queryTitle);

        if (!queryHasMetadata) {
            // User didn't specify version → accept any version
            // "Song" query matches "Song", "Song (Remix)", "Song [Live]" equally
            return coreScore;
        }

        // User specified a version → compare metadata
        String queryMetadata = extractMetadata(queryTitle);
        String trackMetadata = extractMetadata(trackTitle);

        if (trackMetadata.isEmpty()) {
            // Query: "Song (Remix)", Track: "Song" (original)
            // Penalize: user wanted a specific version
            return coreScore * METADATA_PENALTY;
        }

        // Both have metadata → compare them
        double metadataScore = jaroWinkler.apply(
                normalizeSimple(queryMetadata),
                normalizeSimple(trackMetadata)
        );

        // Blend core score with metadata match
        return coreScore * METADATA_BLEND_CORE + metadataScore * METADATA_BLEND_META;
    }

    /**
     * Calculates the best artist match score by comparing against all artists in the track.
     */
    private double calculateArtistScore(String queryArtist, Track track) {
        return track.getArtists().stream()
                .mapToDouble(artist -> jaroWinkler.apply(
                        normalizeArtist(queryArtist),
                        normalizeArtist(artist.getName())
                ))
                .max()
                .orElse(0.0);
    }

    /**
     * Calculates the final match score using weighted harmonic mean.
     * Both title and artist must have reasonable scores for a high final score.
     */
    private double calculateMatchScore(String queryTitle, String queryArtist, Track track) {
        double titleScore = calculateTitleScore(queryTitle, track.getName());
        double artistScore = calculateArtistScore(queryArtist, track);

        // Avoid division by zero
        if (titleScore == 0 || artistScore == 0) {
            return 0.0;
        }

        // Harmonic mean: both dimensions must be good
        return 1.0 / ((TITLE_WEIGHT / titleScore) + (ARTIST_WEIGHT / artistScore));
    }

    @Override
    public Class<SearchResultsSingleResourceDataDocument> getTrackType() {
        return SearchResultsSingleResourceDataDocument.class;
    }
}

