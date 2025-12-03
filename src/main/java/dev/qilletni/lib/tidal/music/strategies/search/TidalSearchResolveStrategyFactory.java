package dev.qilletni.lib.tidal.music.strategies.search;

import com.tidal.sdk.tidalapi.generated.models.ResourceIdentifier;
import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import dev.qilletni.api.music.Track;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategy;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TidalSearchResolveStrategyFactory implements SearchResolveStrategyFactory<SearchResultsSingleResourceDataDocument, TidalSearchResolveStrategyFactory.SearchResolveResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidalSearchResolveStrategyFactory.class);

    private final Map<String, SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult>> strategies = new HashMap<>();

    @Override
    public void registerStrategy(SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult> strategy) {
        if (strategies.containsKey(strategy.getName())) {
            LOGGER.error("Strategy with name \"{}\" already registered", strategy.getName());
            return;
        }

        strategies.put(strategy.getName(), strategy);
    }

    @Override
    public Optional<SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult>> getStrategy(String name) {
        return Optional.ofNullable(strategies.get(name));
    }

    public record SearchResolveResult(Track track, ResourceIdentifier resourceIdentifier) {
        public SearchResolveResult(ResourceIdentifier resourceIdentifier) {
            this(null, resourceIdentifier);
        }

        public SearchResolveResult(Track track) {
            this(track, null);
        }

        public Optional<Track> processToTrack(Function<ResourceIdentifier, Optional<Track>> trackFunction) {
            if (track != null) {
                return Optional.of(track);
            }

            if (resourceIdentifier == null) {
                return Optional.empty();
            }

            return trackFunction.apply(resourceIdentifier);
        }
    }

}
