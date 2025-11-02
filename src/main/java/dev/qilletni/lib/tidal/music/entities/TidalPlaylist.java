package dev.qilletni.lib.tidal.music.entities;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Playlist;
import dev.qilletni.api.music.User;

import java.util.Optional;

public class TidalPlaylist implements Playlist {

    private String id;
    private String title;
    private User creator;
    private int trackCount;

    public TidalPlaylist() {}

    public TidalPlaylist(String id, String title, User creator, int trackCount) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.trackCount = trackCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public User getCreator() {
        return creator;
    }

    @Override
    public int getTrackCount() {
        return trackCount;
    }

    @Override
    public Optional<ServiceProvider> getServiceProvider() {
        return Optional.empty();
    }
}
