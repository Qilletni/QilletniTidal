package dev.qilletni.lib.tidal.music.strategies.search;

import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import dev.qilletni.api.music.strategies.search.SearchResolveStrategy;
import dev.qilletni.lib.tidal.music.strategies.search.TidalSearchResolveStrategyFactory.SearchResolveResult;

public class TidalDefaultSearchResolveStrategy implements SearchResolveStrategy<SearchResultsSingleResourceDataDocument, SearchResolveResult> {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public SearchResolveResult resolveTrack(SearchResultsSingleResourceDataDocument searchResults, String title, String artist) {
        var tracks = searchResults.getData().getRelationships().getTracks().getData();
        return new SearchResolveResult(tracks.getFirst());
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public Class<SearchResultsSingleResourceDataDocument> getTrackType() {
        return SearchResultsSingleResourceDataDocument.class;
    }
}

