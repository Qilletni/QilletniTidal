package dev.qilletni.lib.tidal.music;

import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.api.music.MusicCache;
import dev.qilletni.api.music.MusicFetcher;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.Track;

import java.util.List;
import java.util.Optional;

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
        return tidalMusicFetcher.fetchTrack(name, artist);
    }

    @Override
    public Optional<Track> getTrackById(String id) {
        return tidalMusicFetcher.fetchTrackById(id);
    }

    @Override
    public List<Track> getTracks(List<MusicFetcher.TrackNameArtist> list) {
        return List.of();
    }

    @Override
    public List<Track> getTracksById(List<String> list) {
        return List.of();
    }

    @Override
    public Optional<Playlist> getPlaylist(String s, String s1) {
        return Optional.empty();
    }

    @Override
    public Optional<Playlist> getPlaylistById(String s) {
        return Optional.empty();
    }

    @Override
    public Optional<Album> getAlbum(String s, String s1) {
        return Optional.empty();
    }

    @Override
    public Optional<Album> getAlbumById(String s) {
        return Optional.empty();
    }

    @Override
    public List<Track> getAlbumTracks(Album album) {
        return List.of();
    }

    @Override
    public List<Track> getPlaylistTracks(Playlist playlist) {
        return List.of();
    }

    @Override
    public Optional<Artist> getArtistById(String s) {
        return Optional.empty();
    }

    @Override
    public Optional<Artist> getArtistByName(String s) {
        return Optional.empty();
    }

    @Override
    public String getIdFromString(String s) {
        return "";
    }
}
