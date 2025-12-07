package dev.qilletni.lib.tidal.api.helper;

import com.tidal.sdk.tidalapi.generated.models.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModelHelper {

    public static <T extends IncludedInner> List<T> collectIncludeInners(IncludedInnerWrapper includedInnerWrapper, @Nullable List<ResourceIdentifier> resourceIdentifiers, Class<T> expectedIncludedInner) {
        if (resourceIdentifiers == null) {
            return Collections.emptyList();
        }

        return resourceIdentifiers
                .stream().map(ResourceIdentifier::getId)
                .map(id -> includedInnerWrapper.getInner(id, expectedIncludedInner))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public static String getIncludedInnerId(IncludedInner includedInner) {
        return switch (includedInner) {
            case AlbumsResourceObject obj -> obj.getId();
            case AppreciationsResourceObject obj -> obj.getId();
            case ArtistBiographiesResourceObject obj -> obj.getId();
            case ArtistClaimsResourceObject obj -> obj.getId();
            case ArtistRolesResourceObject obj -> obj.getId();
            case ArtistsResourceObject obj -> obj.getId();
            case ArtworksResourceObject obj -> obj.getId();
            case GenresResourceObject obj -> obj.getId();
            case LyricsResourceObject obj -> obj.getId();
            case PlaylistsResourceObject obj -> obj.getId();
            case PlayQueuesResourceObject obj -> obj.getId();
            case ProvidersResourceObject obj -> obj.getId();
            case SearchResultsResourceObject obj -> obj.getId();
            case SearchSuggestionsResourceObject obj -> obj.getId();
            case SharesResourceObject obj -> obj.getId();
            case TrackFilesResourceObject obj -> obj.getId();
            case TrackManifestsResourceObject obj -> obj.getId();
            case TrackSourceFilesResourceObject obj -> obj.getId();
            case TracksResourceObject obj -> obj.getId();
            case TrackStatisticsResourceObject obj -> obj.getId();
            case UserCollectionFoldersResourceObject obj -> obj.getId();
            case UserCollectionsResourceObject obj -> obj.getId();
            case UserEntitlementsResourceObject obj -> obj.getId();
            case UserRecommendationsResourceObject obj -> obj.getId();
            case UserReportsResourceObject obj -> obj.getId();
            case UsersResourceObject obj -> obj.getId();
            case VideosResourceObject obj -> obj.getId();
        };
    }

}
