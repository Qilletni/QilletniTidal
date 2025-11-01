package dev.qilletni.lib.tidal.music;

import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.api.music.MusicTypeConverter;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.Track;
import dev.qilletni.api.music.User;

import java.util.List;
import java.util.Optional;

public class TidalMusicTypeConverter implements MusicTypeConverter {

    @Override
    public Optional<Track> convertTrack(List<Track> list) {
        return Optional.empty();
    }

    @Override
    public Optional<Album> convertAlbum(List<Album> list) {
        return Optional.empty();
    }

    @Override
    public Optional<Artist> convertArtist(List<Artist> list) {
        return Optional.empty();
    }

    @Override
    public Optional<Playlist> convertPlaylist(List<Playlist> list) {
        return Optional.empty();
    }

    @Override
    public Optional<User> convertUser(List<User> list) {
        return Optional.empty();
    }
}
