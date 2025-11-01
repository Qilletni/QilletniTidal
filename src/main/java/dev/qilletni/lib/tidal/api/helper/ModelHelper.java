package dev.qilletni.lib.tidal.api.helper;

import com.tidal.sdk.tidalapi.generated.models.AlbumsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.AppreciationsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtistBiographiesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtistClaimsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtistRolesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtistsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtworksResourceObject;
import com.tidal.sdk.tidalapi.generated.models.GenresResourceObject;
import com.tidal.sdk.tidalapi.generated.models.IncludedInner;
import com.tidal.sdk.tidalapi.generated.models.LyricsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.PlayQueuesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.PlaylistsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ProvidersResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ResourceIdentifier;
import com.tidal.sdk.tidalapi.generated.models.SearchResultsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.SearchSuggestionsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.SharesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TrackFilesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TrackManifestsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TrackSourceFilesResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TrackStatisticsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.TracksResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UserCollectionsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UserEntitlementsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UserRecommendationsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UserReportsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UsersResourceObject;
import com.tidal.sdk.tidalapi.generated.models.VideosResourceObject;
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
            case UserCollectionsResourceObject obj -> obj.getId();
            case UserEntitlementsResourceObject obj -> obj.getId();
            case UserRecommendationsResourceObject obj -> obj.getId();
            case UserReportsResourceObject obj -> obj.getId();
            case UsersResourceObject obj -> obj.getId();
            case VideosResourceObject obj -> obj.getId();
        };
    }

}
