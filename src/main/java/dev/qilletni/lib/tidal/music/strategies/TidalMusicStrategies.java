package dev.qilletni.lib.tidal.music.strategies;

import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import dev.qilletni.api.music.strategies.MusicStrategies;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategy;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategyFactory;
import dev.qilletni.lib.tidal.music.strategies.search.TidalDefaultSearchResolveStrategy;
import dev.qilletni.lib.tidal.music.strategies.search.TidalSearchResolveStrategyFactory;
import dev.qilletni.lib.tidal.music.strategies.search.TidalSearchResolveStrategyFactory.SearchResolveResult;

import java.util.Optional;

public class TidalMusicStrategies implements MusicStrategies<SearchResultsSingleResourceDataDocument, SearchResolveResult> {

    private final SearchResolveStrategyProvider<SearchResultsSingleResourceDataDocument, SearchResolveResult> searchResolveStrategyProvider = new SearchResolveStrategyProvider<>() {
        private static final TidalSearchResolveStrategyFactory TIDAL_SEARCH_RESOLVE_STRATEGY_FACTORY = new TidalSearchResolveStrategyFactory();

        private final Object fetchResolveStrategyLock = new Object();
        private SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult> fetchResolveStrategy = new TidalDefaultSearchResolveStrategy();

        @Override
        public SearchResolveStrategyFactory<SearchResultsSingleResourceDataDocument, SearchResolveResult> getSearchResolveStrategyFactory() {
            return TIDAL_SEARCH_RESOLVE_STRATEGY_FACTORY;
        }

        @Override
        public boolean setSearchResolveStrategy(String name) {
            return TIDAL_SEARCH_RESOLVE_STRATEGY_FACTORY.getStrategy(name).map(strategy -> {
                synchronized (fetchResolveStrategyLock) {
                    return fetchResolveStrategy = strategy;
                }
            }).isPresent();
        }

        @Override
        public SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult> getCurrentSearchResolveStrategy() {
            synchronized (fetchResolveStrategyLock) {
                return fetchResolveStrategy;
            }
        }
    };

    @Override
    public Optional<SearchResolveStrategyProvider<SearchResultsSingleResourceDataDocument, SearchResolveResult>> getSearchResolveStrategyProvider() {
        return Optional.of(searchResolveStrategyProvider);
    }
}

