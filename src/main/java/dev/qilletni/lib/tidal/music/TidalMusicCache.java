package dev.qilletni.lib.tidal.music;

import dev.qilletni.api.exceptions.InvalidURLOrIDException;
import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.api.music.MusicCache;
import dev.qilletni.api.music.MusicFetcher;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.Track;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class TidalMusicCache implements MusicCache {

    private final TidalMusicFetcher tidalMusicFetcher;

    public TidalMusicCache(TidalMusicFetcher tidalMusicFetcher) {
        this.tidalMusicFetcher = tidalMusicFetcher;
    }

    @Override
    public Optional<Track> getTrack(String name, String artist) {
        /*
         Fetch track, return just album with uninstantiated Album/Artist (if necessary)
         lookup album, if doesnt exist, fetch it
         store album if necessary
         store track
         */

        // TODO: This has album and artist stubs
        return tidalMusicFetcher.fetchTrack(name, artist);
    }

    @Override
    public Optional<Track> getTrackById(String id) {
        // TODO: This has album and artist stubs
        return tidalMusicFetcher.fetchTrackById(id);
    }

    @Override
    public List<Track> getTracks(List<MusicFetcher.TrackNameArtist> list) {
        return tidalMusicFetcher.fetchTracks(list);
    }

    @Override
    public List<Track> getTracksById(List<String> list) {
        return tidalMusicFetcher.fetchTracksById(list);
    }

    @Override
    public Optional<Playlist> getPlaylist(String name, String author) {
        return tidalMusicFetcher.fetchPlaylist(name, author);
    }

    @Override
    public Optional<Playlist> getPlaylistById(String id) {
        return tidalMusicFetcher.fetchPlaylistById(id);
    }

    @Override
    public Optional<Album> getAlbum(String name, String artist) {
        // Artist entities are stubs
        return tidalMusicFetcher.fetchAlbum(name, artist);
    }

    @Override
    public Optional<Album> getAlbumById(String id) {
        // Artist entities are stubs
        return tidalMusicFetcher.fetchAlbumById(id);
    }

    @Override
    public List<Track> getAlbumTracks(Album album) {
        // TODO: artists in tracks are stubs
        return tidalMusicFetcher.fetchAlbumTracks(album);
    }

    @Override
    public List<Track> getPlaylistTracks(Playlist playlist) {
        // TODO: These are stubbed tracks
        return tidalMusicFetcher.fetchPlaylistTracks(playlist);
    }

    @Override
    public Optional<Artist> getArtistById(String id) {
        return tidalMusicFetcher.fetchArtistById(id);
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return tidalMusicFetcher.fetchArtistByName(name);
    }

    @Override
    public String getIdFromString(String idOrUrl) {
        // Regular expression to match Tidal track URLs or an ID
        var pattern = Pattern.compile("(^|tidal\\.com/.*?/)(\\d{9}|\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})");
        var matcher = pattern.matcher(idOrUrl);

        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                return matcher.group(2);
            }
        }

        throw new InvalidURLOrIDException(String.format("Invalid URL or ID: \"%s\"", idOrUrl));
    }
}
