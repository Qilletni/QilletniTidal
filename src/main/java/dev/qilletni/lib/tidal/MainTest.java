package dev.qilletni.lib.tidal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tidal.sdk.tidalapi.TidalApi;
import com.tidal.sdk.tidalapi.generated.TidalApiClient;
import com.tidal.sdk.tidalapi.generated.models.*;
import com.tidal.sdk.tidalapi.oauth2.AuthorizationCodeFlow;
import com.tidal.sdk.tidalapi.oauth2.InMemoryTokenStorage;
import com.tidal.sdk.tidalapi.oauth2.OAuth2Config;
import com.tidal.sdk.tidalapi.oauth2.OAuth2TokenManager;
import dev.qilletni.lib.tidal.api.helper.IncludedInnerWrapper;
import dev.qilletni.lib.tidal.api.helper.ModelHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tidal.sdk.tidalapi.generated.TidalApiClient.DEFAULT_BASE_URL;

public class MainTest {

    private static String authCode = null;
    private static String state = null;
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static final String TOKEN_FILE = "tidal_token.properties";
    private static OAuth2TokenManager tokenManager;
    private static OAuth2Config config;

    public static void main(String[] args) throws Exception {

        // Initialize OAuth2
        config = new OAuth2Config.Builder()
                .clientId(System.getenv("TIDAL_CLIENT_ID"))
                .clientSecret(System.getenv("TIDAL_CLIENT_SECRET"))
                .build();

        tokenManager = new OAuth2TokenManager(config, new InMemoryTokenStorage());

        // Try to load existing token
        if (loadToken(tokenManager)) {
            System.out.println("Loaded existing token from file\n");
        } else {
            System.out.println("No existing token found, starting authentication flow...\n");

            // Start local server to handle OAuth callback
            HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
            server.createContext("/callback", new CallbackHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Local server started on http://localhost:8888");

            AuthorizationCodeFlow authFlow = new AuthorizationCodeFlow(config, tokenManager);

            // Get login URL and open in browser
            String loginUrl = authFlow.initializeLogin("http://localhost:8888/callback", "user.read", "playlists.read");
            System.out.println("\nOpening browser for authentication...");
            System.out.println("URL: " + loginUrl + "\n");
//            Desktop.getDesktop().browse(new URI(loginUrl));

            // Wait for callback
            latch.await();
            server.stop(0);

            // Complete authentication
            System.out.println("\nExchanging authorization code for token...");
            authFlow.finalizeLogin(authCode, state, "http://localhost:8888/callback");
            System.out.println("Authentication successful!\n");

            // Save token to file
            saveToken(tokenManager);
        }

        // Use the API
        TidalApiClient tidalApi = getTidalApiClient();
        System.out.println("Fetching user info...\n");

        try {


//            Response<SearchResultsSingleResourceDataDocument> response =
//                    CoroutineHelper.runSuspend(cont ->
//                            tidalApi.createSearchResults().searchResultsIdGet(
//                                    "God Knows",
//                                    "US",
//                                    "include",
//                                    List.of("tracks"),
//                                    cont
//                            )
//                    );
//
//            System.out.println(response.body());
//
//            System.out.println("Response Code: " + response.code());
//            System.out.println("Response Message: " + response.message());
//            System.out.println("Response isSuccessful: " + response.isSuccessful());
//            if (response.isSuccessful()) {
//
//                var included = response.body().getIncluded();
////                var firstIncluded = included.getFirst();
////
////                if (firstIncluded instanceof TracksResourceObject trackResource) {
////                    System.out.printf("Track:  #%s   %s%n", trackResource.getId(), trackResource.getAttributes().getTitle());
////
////                    Response<TracksSingleResourceDataDocument> fullTrackResponse =
////                            CoroutineHelper.runSuspend(cont ->
////                                    tidalApi.createTracks().tracksIdGet(
////                                            trackResource.getId(),
////                                            "US",
////                                            List.of("albums", "artists"),
////                                            cont
////                                    )
////                            );
////
////                    if (!fullTrackResponse.isSuccessful()) {
////                        System.out.println("Not successful!");
////                        return;
////                    }
////
////                    for (var includedInner : fullTrackResponse.body().getIncluded()) {
////                        switch (includedInner) {
////                            case AlbumsResourceObject albumsResource:
////                                System.out.println("Album: " + albumsResource.getAttributes().getTitle());
////                                break;
////                            case ArtistsResourceObject artistsResource:
////                                System.out.println("Artist: " + artistsResource.getAttributes().getName());
////                                break;
////                        }
////                        System.out.println("includedInner = " + includedInner.getClass().getCanonicalName());
////                    }
////
////                }
//
////            System.out.println("included = " + included.getFirst());
//
//                for (IncludedInner includedInner : included) {
//                    if (includedInner instanceof TracksResourceObject trackResource) {
//                        System.out.printf("Track:  #%s   %s%n", trackResource.getId(), trackResource.getAttributes().getTitle());
//                    } else {
//                        System.out.println("Inner is NOT a TracksResourceObject");
//                    }
//
////                if (!includedInner.getType().equals("tracks")) {
////                    System.out.println("Non-track: " +  includedInner.getType());
////                    continue;
////                }
//
////                System.out.println("includedInner = " + includedInner);
//                }
//
////            System.out.println("Response Body: " + response.body());
//            } else if (response.errorBody() != null) {
//                var data = response.body().getData();
//                try {
//                    System.out.println("Error Body: " + response.errorBody().string());
//                } catch (Exception e) {
//                    System.out.println("Could not read error body: " + e.getMessage());
//                }
//            }


            Response<TracksMultiResourceDataDocument> fullTrackResponse =
                    CoroutineHelper.runSuspend(cont ->
                            tidalApi.createTracks().tracksGet(
                                    "US",
                                    null,
                                    List.of("albums", "artists"),
                                    null, // owners
                                    null,
                                    List.of(
                                        "103945728",
                                        "11009232",
                                        "114661564",
                                        "120741760",
                                        "1392950",
                                        "1403517",
                                        "150851"
                                    ), // list of IDs
                                    cont
                            )
                    );

            if (!fullTrackResponse.isSuccessful()) {
                System.out.println("Not successful!");
                return;
            }

            var body = fullTrackResponse.body();

            var includedInnerWrapper = new IncludedInnerWrapper(body.getIncluded());

            for (var trackResource : body.getData()) {
                var attributes = trackResource.getAttributes();
                var title = attributes.getTitle();

                System.out.println(title);

                var albums = ModelHelper.collectIncludeInners(includedInnerWrapper, trackResource.getRelationships().getAlbums().getData(), AlbumsResourceObject.class);
                var artists = ModelHelper.collectIncludeInners(includedInnerWrapper, trackResource.getRelationships().getArtists().getData(), ArtistsResourceObject.class);

                for (var album : albums) {
                    System.out.printf("\tAlbum: %s - %s%n", album.getAttributes().getTitle(), album.getAttributes().getVersion());
                }

                for (var artist : artists) {
                    System.out.printf("\tArtist: %s%n", artist.getAttributes().getName());
                }
            }

        } finally {
            tidalApi.shutdown();
            tokenManager.shutdown();
        }




//        tidalApi.createUsers().usersMeGet().enqueue(new Callback<>() {
//            @Override
//            public void onResponse(Call<UsersSingleResourceDataDocument> call, Response<UsersSingleResourceDataDocument> response) {
//                System.out.println("Response Code: " + response.code());
//                System.out.println("Response Message: " + response.message());
//                System.out.println("Response isSuccessful: " + response.isSuccessful());
//                if (response.isSuccessful()) {
//                    System.out.println("Response Body: " + response.body());
//                } else if (response.errorBody() != null) {
//                    var data = response.body().getData();
//                    try {
//                        System.out.println("Error Body: " + response.errorBody().string());
//                    } catch (Exception e) {
//                        System.out.println("Could not read error body: " + e.getMessage());
//                    }
//                }
//                System.exit(0);
//            }
//
//            @Override
//            public void onFailure(Call<UsersSingleResourceDataDocument> call, Throwable throwable) {
//                throwable.printStackTrace();
//                System.err.println("Failure!");
//                System.exit(1);
//            }
//        });
    }

    private static TidalApiClient getTidalApiClient() throws Exception {
        // Check if token needs refresh
        if (tokenManager.isTokenExpired()) {
            System.out.println("Token expired, refreshing...\n");
            tokenManager.refreshToken();
            saveToken(tokenManager);
            System.out.println("Token refreshed successfully\n");
        }

        return new TidalApiClient(tokenManager, DEFAULT_BASE_URL);
    }

    private static boolean loadToken(OAuth2TokenManager tokenManager) {
        try {
            File tokenFile = new File(TOKEN_FILE);
            if (!tokenFile.exists()) {
                return false;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(tokenFile)) {
                props.load(fis);
            }

            String accessToken = props.getProperty("access_token");
            String refreshToken = props.getProperty("refresh_token");
            String expiresAtStr = props.getProperty("expires_at");

            if (accessToken == null || refreshToken == null || expiresAtStr == null) {
                return false;
            }

            long expiresAt = Long.parseLong(expiresAtStr);

            System.out.println("2 set refreshToken = " + refreshToken);

            // Set the token in the token manager using reflection or direct methods if available
            // Note: This assumes OAuth2TokenManager has methods to set tokens
            // You may need to adjust this based on the actual API
            tokenManager.setCredentials(accessToken, refreshToken, expiresAt);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to load token: " + e.getMessage());
            return false;
        }
    }

    private static void saveToken(OAuth2TokenManager tokenManager) {
        try {
            Properties props = new Properties();

            // Get token information from token manager
            // Note: This assumes OAuth2TokenManager has getter methods
            // You may need to adjust this based on the actual API
            String accessToken = tokenManager.getAccessToken();
            String refreshToken = tokenManager.getRefreshToken();
            long expiresAt = tokenManager.getExpiresAt();

            System.out.println("accessToken = " + accessToken);
            System.out.println("refreshToken = " + refreshToken);
            System.out.println("expiresAt = " + expiresAt);

            props.setProperty("access_token", accessToken);
            props.setProperty("refresh_token", refreshToken);
            props.setProperty("expires_at", String.valueOf(expiresAt));

            try (FileOutputStream fos = new FileOutputStream(TOKEN_FILE)) {
                props.store(fos, "Tidal OAuth2 Token - DO NOT SHARE");
            }

            System.out.println("Token saved to " + TOKEN_FILE + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save token: " + e.getMessage());
        }
    }

    static class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();

            // Parse callback parameters
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair[0].equals("code")) authCode = pair[1];
                if (pair[0].equals("state")) state = pair[1];
            }

            // Send response to browser
            String response = "<html><body><h1>Authentication Successful!</h1><p>You can close this window.</p></body></html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            // Signal main thread
            latch.countDown();
        }
    }

}
