package dev.qilletni.lib.tidal.api;

import com.tidal.sdk.tidalapi.generated.TidalApiClient;

public class TidalApiSingleton {

    private static TidalApiClient tidalApiClient;

    public static TidalApiClient getTidalApi() {
        return tidalApiClient;
    }

    public static void setTidalApi(TidalApiClient tidalApi) {
        tidalApiClient = tidalApi;
    }
}
