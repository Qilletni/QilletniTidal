package dev.qilletni.lib.tidal.api;

import com.tidal.sdk.tidalapi.generated.TidalApiClient;
import com.tidal.sdk.tidalapi.generated.models.UsersResourceObject;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TidalAuthorizer {

    /**
     * Creates an authorized {@link TidalApiClient}, with an automatic refresh loop (if applicable).
     *
     * @return The future of the created {@link TidalApiClient}
     */
    CompletableFuture<TidalApiClient> authorizeTidal();

    /**
     * Shuts down the authorizer. This should clean up any async tasks currently running.
     */
    void shutdown();

    /**
     * Gets the current {@link TidalApiClient} after authorization.
     *
     * @return The current {@link TidalApiClient}
     */
    TidalApiClient getTidalApi();

    /**
     * Gets the current user profile that has been authenticated with. If no user is associated with the
     * authentication, an empty optional is returned.
     *
     * @return The current user
     */
    Optional<UsersResourceObject> getCurrentUser();
}
