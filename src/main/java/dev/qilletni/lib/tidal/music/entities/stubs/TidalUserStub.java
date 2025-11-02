package dev.qilletni.lib.tidal.music.entities.stubs;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.lib.tidal.music.entities.TidalUser;

import java.util.Optional;

public class TidalUserStub extends TidalUser {

    private final String id;

    public TidalUserStub(String id) {
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
