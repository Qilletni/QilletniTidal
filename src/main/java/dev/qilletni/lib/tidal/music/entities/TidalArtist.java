package dev.qilletni.lib.tidal.music.entities;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Artist;
import dev.qilletni.lib.tidal.music.provider.TidalServiceProvider;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;
import java.util.Optional;

@Entity
public class TidalArtist implements Artist {

    @Id
    private String id;
    private String name;

    public TidalArtist() {}

    public TidalArtist(String id, String name) {
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TidalArtist that = (TidalArtist) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TidalArtist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
