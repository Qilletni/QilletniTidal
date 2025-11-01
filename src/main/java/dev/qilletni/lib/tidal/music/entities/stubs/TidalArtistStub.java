package dev.qilletni.lib.tidal.music.entities.stubs;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Artist;
import dev.qilletni.lib.tidal.music.entities.TidalArtist;

import java.util.Optional;

/**
 * A stub that holds an {@link dev.qilletni.lib.tidal.music.entities.TidalArtist} but just stores an ID.
 */
public class TidalArtistStub extends TidalArtist {

    private final String id;

    public TidalArtistStub(String id) {
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
    public Optional<ServiceProvider> getServiceProvider() {
        throw new UnsupportedOperationException("getServiceProvider() unsupported in a stub");
    }
}
