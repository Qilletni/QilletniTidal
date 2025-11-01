package dev.qilletni.lib.tidal.music.entities;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.User;
import dev.qilletni.lib.tidal.music.provider.TidalServiceProvider;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

@Entity
public class TidalUser implements User {

    @Id
    private String id;
    private String name;

    public TidalUser() {}

    public TidalUser(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<ServiceProvider> getServiceProvider() {
        return Optional.ofNullable(TidalServiceProvider.getServiceProviderInstance());
    }

    @Override
    public String toString() {
        return "TidalUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
