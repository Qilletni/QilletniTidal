package dev.qilletni.lib.tidal.music.entities.stubs;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.lib.tidal.music.entities.TidalAlbum;

import java.util.List;
import java.util.Optional;

public class TidalAlbumStub extends TidalAlbum {

    private final String id;

    public TidalAlbumStub(String id) {
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
    public Optional<ServiceProvider> getServiceProvider() {
        throw new UnsupportedOperationException("getServiceProvider() unsupported in a stub");
    }
}
