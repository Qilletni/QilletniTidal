package dev.qilletni.lib.tidal.music.entities;

import dev.qilletni.api.auth.ServiceProvider;
import dev.qilletni.api.music.Album;
import dev.qilletni.api.music.Artist;
import dev.qilletni.lib.tidal.music.provider.TidalServiceProvider;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
public class TidalAlbum implements Album {

    @Id
    private String id;
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn(name="artistOrder")
    private List<TidalArtist> artists;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<TidalTrack> tracks;

    public TidalAlbum() {}

    public TidalAlbum(String id, String name, List<TidalArtist> artists, List<TidalTrack> tracks) {
        this.id = id;
        this.name = name;
        this.artists = artists;
        this.tracks = tracks;
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
    public Artist getArtist() {
        return artists.getFirst();
    }

    @Override
    public List<Artist> getArtists() {
        return artists.stream().map(Artist.class::cast).toList();
    }

    @Override
    public Optional<ServiceProvider> getServiceProvider() {
        return Optional.ofNullable(TidalServiceProvider.getServiceProviderInstance());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TidalAlbum that = (TidalAlbum) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TidalAlbum{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", artists=" + artists +
                ", tracks=" + tracks +
                '}';
    }
}
