package dev.qilletni.lib.tidal.music.entities.stubs;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.lib.tidal.music.entities.TidalTrack;

import java.util.List;
import java.util.Optional;

public class TidalTrackStub extends TidalTrack {

    private final String id;

    public TidalTrackStub(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("getName() unsupported in a stub");
    }

    @Override
    public Artist getArtist() {
        throw new UnsupportedOperationException("getArtist() unsupported in a stub");
    }

    @Override
    public List<Artist> getArtists() {
        throw new UnsupportedOperationException("getArtists() unsupported in a stub");
    }

    @Override
    public Album getAlbum() {
        throw new UnsupportedOperationException("getAlbum() unsupported in a stub");
    }

    @Override
    public int getDuration() {
        throw new UnsupportedOperationException("getDuration() unsupported in a stub");
    }

    @Override
    public Optional<ServiceProvider> getServiceProvider() {
        throw new UnsupportedOperationException("getServiceProvider() unsupported in a stub");
    }
}
