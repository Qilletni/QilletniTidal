package dev.qilletni.lib.tidal.music;

import com.tidal.sdk.tidalapi.generated.TidalApiClient;
import com.tidal.sdk.tidalapi.generated.models.AlbumsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.ArtistsResourceObject;
import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import com.tidal.sdk.tidalapi.generated.models.TracksSingleResourceDataDocument;
import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.api.music.MusicFetcher;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.Track;
import dev.qilletni.lib.tidal.CoroutineHelper;
import dev.qilletni.lib.tidal.api.helper.IncludedInnerWrapper;
import dev.qilletni.lib.tidal.api.helper.ModelHelper;
import dev.qilletni.lib.tidal.music.entities.TidalAlbum;
import dev.qilletni.lib.tidal.music.entities.TidalArtist;
import dev.qilletni.lib.tidal.music.entities.TidalTrack;
import dev.qilletni.lib.tidal.music.entities.stubs.TidalAlbumStub;
import dev.qilletni.lib.tidal.music.entities.stubs.TidalArtistStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.util.List;
import java.util.Optional;

public class TidalMusicFetcher implements MusicFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidalMusicFetcher.class);

    private final String countryCode;
    private final TidalApiClient tidalApi;

    public TidalMusicFetcher(String countryCode, TidalApiClient tidalApi) {
        this.countryCode = countryCode;
        this.tidalApi = tidalApi;
    }

    private Optional<String> getErrorResponse(Response<?> response) {
        try (var errorBody = response.errorBody()) {
            if (errorBody != null) {
                return Optional.of(errorBody.string());
            }
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    private String getFormatedErrorResponse(Response<?> response) {
        return getErrorResponse(response).map("\n%s"::formatted).orElse("");
    }

    @Override
    public Optional<Track> fetchTrack(String name, String artist) {
        LOGGER.debug("fetchTrack({}, {})", name, artist);

        try {
            Response<SearchResultsSingleResourceDataDocument> response =
                    CoroutineHelper.runSuspend(cont ->
                            tidalApi.createSearchResults().searchResultsIdGet(
                                    "%s %s".formatted(name, artist),
                                    countryCode,
                                    "include",
                                    List.of("tracks"),
                                    cont
                            ));

            if (!response.isSuccessful() || response.body() == null || response.body().getData().getRelationships() == null || response.body().getData().getRelationships().getTracks().getData() == null) {
                LOGGER.error("Failed to fetch track: {}", getFormatedErrorResponse(response));
                return Optional.empty();
            }

            var data = response.body().getData().getRelationships().getTracks().getData().getFirst();

            Response<TracksSingleResourceDataDocument> singleTrackResponse =
                    CoroutineHelper.runSuspend(cont ->
                            tidalApi.createTracks().tracksIdGet(
                                    data.getId(),
                                    countryCode,
                                    List.of("albums", "artists"),
                                    cont
                            )
                    );

            if (!singleTrackResponse.isSuccessful()) {
                LOGGER.error("Failed to fetch track info: {}", getFormatedErrorResponse(singleTrackResponse));
                return Optional.empty();
            }

            return createTrackEntity(singleTrackResponse.body());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Track> fetchTrackById(String id) {
        LOGGER.debug("fetchTrack({})", id);

        try {
            Response<TracksSingleResourceDataDocument> singleTrackResponse =
                    CoroutineHelper.runSuspend(cont ->
                            tidalApi.createTracks().tracksIdGet(
                                    id,
                                    countryCode,
                                    List.of("albums", "artists"),
                                    cont
                            ));

            if (!singleTrackResponse.isSuccessful() || singleTrackResponse.body() == null || singleTrackResponse.body().getData().getRelationships() == null) {
                LOGGER.error("Failed to fetch track by ID: {}", getFormatedErrorResponse(singleTrackResponse));
                return Optional.empty();
            }

            return createTrackEntity(singleTrackResponse.body());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Track> fetchTracks(List<TrackNameArtist> list) {
        throw new RuntimeException("fetchTracks(List<TrackNameArtist>) not supported!");
    }

    @Override
    public List<Track> fetchTracksById(List<String> list) {
        throw new RuntimeException("fetchTracksById(List<String>) not supported!");
    }

    @Override
    public Optional<Playlist> fetchPlaylist(String name, String author) {
        LOGGER.debug("fetchPlaylist({}, {})", name, author);
        return Optional.empty();
    }

    @Override
    public Optional<Playlist> fetchPlaylistById(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<Album> fetchAlbum(String name, String artist) {
        return Optional.empty();
    }

    @Override
    public Optional<Album> fetchAlbumById(String id) {
        return Optional.empty();
    }

    @Override
    public List<Track> fetchAlbumTracks(Album album) {
        return List.of();
    }

    @Override
    public List<Track> fetchPlaylistTracks(Playlist playlist) {
        return List.of();
    }

    @Override
    public Optional<Artist> fetchArtistById(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<Artist> fetchArtistByName(String name) {
        return Optional.empty();
    }

    private Optional<Track> createTrackEntity(TracksSingleResourceDataDocument track) {
        var trackData = track.getData();

        var includedInnerWrapper = new IncludedInnerWrapper(track.getIncluded());

        var artists = ModelHelper.collectIncludeInners(includedInnerWrapper, trackData.getRelationships().getArtists().getData(), ArtistsResourceObject.class);
        var albums = ModelHelper.collectIncludeInners(includedInnerWrapper, trackData.getRelationships().getAlbums().getData(), AlbumsResourceObject.class);

        return Optional.of(new TidalTrack(trackData.getId(),
                trackData.getAttributes().getTitle(),
                artists.stream().map(this::createArtistEntityStub).toList(),
                createAlbumEntityStub(albums.getFirst()),
                DurationConverter.parseDurationToSeconds(trackData.getAttributes().getDuration())));
    }

    private TidalArtist createArtistEntityStub(ArtistsResourceObject artist) {
        return new TidalArtistStub(artist.getId());
    }

    private TidalAlbum createAlbumEntityStub(AlbumsResourceObject album) {
        return new TidalAlbumStub(album.getId());
    }
}
