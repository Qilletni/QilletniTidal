package dev.qilletni.lib.tidal.music.entities;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.User;

import java.util.Optional;

public class TidalPlaylist implements Playlist {

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public User getCreator() {
        return null;
    }

    @Override
    public int getTrackCount() {
        return 0;
    }

    @Override
    public Optional<ServiceProvider> getServiceProvider() {
        return Optional.empty();
    }
}
