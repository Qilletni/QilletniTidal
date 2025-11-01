package dev.qilletni.lib.tidal.api.oauth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tidal.sdk.tidalapi.generated.TidalApiClient;
import com.tidal.sdk.tidalapi.generated.models.SearchResultsSingleResourceDataDocument;
import com.tidal.sdk.tidalapi.generated.models.UsersResourceObject;
import com.tidal.sdk.tidalapi.generated.models.UsersSingleResourceDataDocument;
import com.tidal.sdk.tidalapi.oauth2.AuthorizationCodeFlow;
import com.tidal.sdk.tidalapi.oauth2.OAuth2Config;
import com.tidal.sdk.tidalapi.oauth2.OAuth2TokenManager;
import dev.qilletni.api.lib.persistence.PackageConfig;
import dev.qilletni.lib.tidal.CoroutineHelper;
import dev.qilletni.lib.tidal.api.TidalAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.tidal.sdk.tidalapi.generated.TidalApiClient.DEFAULT_BASE_URL;

/**
 * OAuth2 Authorization Code Flow implementation for Tidal API.
 *
 * <p>This class handles the complete OAuth flow including:
 * <ul>
 *   <li>Token caching and persistence via {@link PackageConfig}</li>
 *   <li>Automatic token refresh before expiration</li>
 *   <li>Browser-based user authentication with local callback server</li>
 *   <li>User profile fetching and caching</li>
 * </ul>
 *
 * <p>Improvements over similar implementations:
 * <ul>
 *   <li>Uses {@link ScheduledExecutorService} for robust token refresh scheduling</li>
 *   <li>Proper resource cleanup with stored references to servers and executors</li>
 *   <li>Configurable constants for ports, timeouts, and buffer times</li>
 *   <li>Thread-safe API client access with synchronized blocks</li>
 *   <li>Comprehensive error handling with specific exception types</li>
 *   <li>Graceful degradation when desktop browsing is unavailable</li>
 *   <li>Timeout handling for OAuth callbacks to prevent indefinite waiting</li>
 * </ul>
 */
public class TidalOAuthAuthorizer implements TidalAuthorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidalOAuthAuthorizer.class);

    // Configuration constants
    private static final String PERSIST_ACCESS_TOKEN = "accessToken";
    private static final String PERSIST_REFRESH_TOKEN = "refreshToken";
    private static final String PERSIST_EXPIRES = "tokenExpiresAt";
    private static final int CALLBACK_PORT = 8888;
    private static final String CALLBACK_PATH = "/callback";
    private static final int TOKEN_REFRESH_BUFFER_MINUTES = 5;
    private static final int CALLBACK_TIMEOUT_SECONDS = 300; // 5 minutes

    private final PackageConfig packageConfig;
    private final OAuth2Config oAuth2Config;
    private final OAuth2TokenManager tokenManager;
    private final AuthorizationCodeFlow authFlow;
    private final ExecutorService executorService;
    private final ScheduledExecutorService refreshScheduler;

    private TidalApiClient tidalApiClient;
    private UsersResourceObject currentUser;
    private HttpServer callbackServer;
    private ScheduledFuture<?> refreshTask;

    /**
     * Creates a new TidalOAuthAuthorizer with the specified configuration and credentials.
     *
     * @param packageConfig The package configuration for persistent token storage
     * @param clientId The Tidal API client ID
     * @param clientSecret The Tidal API client secret
     */
    public TidalOAuthAuthorizer(PackageConfig packageConfig, String clientId, String clientSecret) {
        this.packageConfig = packageConfig;
        this.executorService = Executors.newCachedThreadPool();
        this.refreshScheduler = Executors.newSingleThreadScheduledExecutor();

        this.oAuth2Config = new OAuth2Config.Builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        this.tokenManager = new OAuth2TokenManager(oAuth2Config);
        this.authFlow = new AuthorizationCodeFlow(oAuth2Config, tokenManager);
    }

    /**
     * Creates a {@link TidalOAuthAuthorizer} using environment variables for credentials.
     *
     * <p>Required environment variables:
     * <ul>
     *   <li>{@code TIDAL_CLIENT_ID} - The Tidal API client ID</li>
     *   <li>{@code TIDAL_CLIENT_SECRET} - The Tidal API client secret</li>
     * </ul>
     *
     * @param packageConfig The package configuration for persistent storage
     * @return The created authorizer
     * @throws IllegalStateException if the required environment variables are not set
     */
    public static TidalOAuthAuthorizer createFromEnvironment(PackageConfig packageConfig) {
        String clientId = System.getenv("TIDAL_CLIENT_ID");
        String clientSecret = System.getenv("TIDAL_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("TIDAL_CLIENT_ID and TIDAL_CLIENT_SECRET environment variables must be set");
        }

        return new TidalOAuthAuthorizer(packageConfig, clientId, clientSecret);
    }

    /**
     * Authorizes the Tidal API client with OAuth2.
     *
     * <p>This method will:
     * <ol>
     *   <li>Attempt to load and use cached credentials from {@link PackageConfig}</li>
     *   <li>If cached credentials are invalid or missing, initiate manual OAuth flow</li>
     *   <li>Create the {@link TidalApiClient} with valid credentials</li>
     *   <li>Schedule automatic token refresh</li>
     *   <li>Fetch and cache the current user profile</li>
     * </ol>
     *
     * @return A future that completes with the authorized {@link TidalApiClient}
     */
    @Override
    public CompletableFuture<TidalApiClient> authorizeTidal() {
        var completableFuture = new CompletableFuture<TidalApiClient>();

        if (packageConfig.get(PERSIST_ACCESS_TOKEN).isEmpty() || packageConfig.get(PERSIST_REFRESH_TOKEN).isEmpty() || packageConfig.get(PERSIST_EXPIRES).isEmpty()) {
            LOGGER.debug("No cached token found, starting manual authentication");
            performManualAuth(completableFuture);
        }

        LOGGER.debug("Found cached token, attempting to use it");

        try {
            tokenManager.setCredentials(packageConfig.getOrThrow(PERSIST_ACCESS_TOKEN), packageConfig.getOrThrow(PERSIST_REFRESH_TOKEN), Long.parseLong(packageConfig.getOrThrow(PERSIST_EXPIRES)));

            // Check if token needs immediate refresh
            if (tokenManager.isTokenExpired()) {
                LOGGER.debug("Cached token is expired, refreshing");
                tokenManager.refreshToken();
                saveTokenToCache();
            }

            createApiClient();
            scheduleTokenRefresh();
            fetchCurrentUser()
                    .thenRun(() -> completableFuture.complete(tidalApiClient))
                    .exceptionally(throwable -> {
                        LOGGER.error("Failed to fetch current user with cached token", throwable);
                        completableFuture.completeExceptionally(throwable);
                        return null;
                    });

        } catch (Exception e) {
            LOGGER.error("Failed to use cached token, starting fresh authentication", e);
            packageConfig.remove(PERSIST_ACCESS_TOKEN);
            packageConfig.remove(PERSIST_REFRESH_TOKEN);
            packageConfig.remove(PERSIST_EXPIRES);
            packageConfig.saveConfig();
            performManualAuth(completableFuture);
        }

        return completableFuture;
    }

    /**
     * Shuts down the authorizer and cleans up all resources.
     *
     * <p>This method will:
     * <ul>
     *   <li>Cancel any scheduled token refresh tasks</li>
     *   <li>Shut down executor services gracefully</li>
     *   <li>Stop the OAuth callback server if running</li>
     * </ul>
     */
    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down TidalOAuthAuthorizer");

        // Cancel refresh task
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel(true);
        }

        // Shutdown executors
        shutdownExecutor(refreshScheduler, "RefreshScheduler");
        shutdownExecutor(executorService, "ExecutorService");

        // Stop callback server
        if (callbackServer != null) {
            callbackServer.stop(0);
            callbackServer = null;
        }

        tokenManager.shutdown();
    }

    /**
     * Gets the authorized Tidal API client.
     *
     * @return The authorized {@link TidalApiClient}
     * @throws IllegalStateException if the client has not been initialized yet
     */
    @Override
    public TidalApiClient getTidalApi() {
        if (tidalApiClient == null) {
            throw new IllegalStateException("TidalApiClient not initialized. Call authorizeTidal() first.");
        }
        return tidalApiClient;
    }

    /**
     * Gets the current authenticated user's profile.
     *
     * @return An {@link Optional} containing the user profile, or empty if not yet fetched
     */
    @Override
    public Optional<UsersResourceObject> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Performs manual OAuth authentication flow.
     */
    private void performManualAuth(CompletableFuture<TidalApiClient> completableFuture) {
        try {
            getCodeFromUser()
                .thenCompose(this::finalizeAuthentication)
                .thenRun(this::createApiClient)
                .thenRun(this::scheduleTokenRefresh)
                .thenCompose(v -> fetchCurrentUser())
                .thenRun(() -> completableFuture.complete(tidalApiClient))
                .exceptionally(throwable -> {
                    LOGGER.error("Manual authentication failed", throwable);
                    completableFuture.completeExceptionally(throwable);
                    return null;
                });
        } catch (Exception e) {
            completableFuture.completeExceptionally(e);
        }
    }

    /**
     * Initiates the OAuth flow and retrieves the authorization code from the user.
     *
     * <p>This method will:
     * <ol>
     *   <li>Start a local HTTP server to receive the OAuth callback</li>
     *   <li>Generate the authorization URL with required scopes</li>
     *   <li>Attempt to open the URL in the user's default browser</li>
     *   <li>Wait for the callback with a timeout of {@link #CALLBACK_TIMEOUT_SECONDS}</li>
     * </ol>
     *
     * <p>Requested scopes:
     * <ul>
     *   <li>user.read - Read user profile information</li>
     *   <li>playlists.read - Read user playlists</li>
     *   <li>playlists.write - Modify user playlists</li>
     *   <li>user.library.read - Read user library</li>
     *   <li>user.library.write - Modify user library</li>
     * </ul>
     *
     * @return A future containing the authorization code and state
     * @throws IOException if the callback server cannot be started
     */
    private CompletableFuture<AuthCodeResult> getCodeFromUser() throws IOException {
        String redirectUri = getRedirectUri();
        String scopes = String.join(" ",
            "user.read",
            "playlists.read",
            "playlists.write",
            "user.library.read",
            "user.library.write"
        );

        var codeFuture = new CompletableFuture<AuthCodeResult>();

        // Start callback server
        startCallbackServer(codeFuture);

        // Initialize login and get authorization URL
        CompletableFuture.runAsync(() -> {
            try {
                String loginUrl = authFlow.initializeLogin(redirectUri, scopes);
                LOGGER.info("Authorization URL: {}", loginUrl);
                LOGGER.info("Please visit this URL to authorize the application");

                // Try to open browser
//                tryOpenBrowser(loginUrl);
            } catch (Exception e) {
                codeFuture.completeExceptionally(e);
            }
        }, executorService);

        // Add timeout
        CompletableFuture.delayedExecutor(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .execute(() -> {
                if (!codeFuture.isDone()) {
                    codeFuture.completeExceptionally(
                        new TimeoutException("OAuth callback timed out after " + CALLBACK_TIMEOUT_SECONDS + " seconds")
                    );
                    if (callbackServer != null) {
                        callbackServer.stop(0);
                    }
                }
            });

        return codeFuture;
    }

    /**
     * Starts the HTTP server to handle OAuth callback.
     */
    private void startCallbackServer(CompletableFuture<AuthCodeResult> codeFuture) throws IOException {
        callbackServer = HttpServer.create(new InetSocketAddress(CALLBACK_PORT), 0);
        callbackServer.createContext(CALLBACK_PATH, new OAuthCallbackHandler(codeFuture));
        callbackServer.setExecutor(executorService);
        callbackServer.start();
        LOGGER.debug("Callback server started on port {}", CALLBACK_PORT);
    }

    /**
     * Attempts to open the authorization URL in the default browser.
     *
     * <p>If the desktop browsing is not supported or fails, a warning is logged
     * and the user must manually open the URL.
     */
    private void tryOpenBrowser(String loginUrl) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(loginUrl));
                LOGGER.debug("Opened authorization URL in browser");
            } else {
                LOGGER.warn("Desktop browsing not supported. Please manually open the URL above.");
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to open browser automatically: {}. Please manually open the URL above.", e.getMessage());
        }
    }

    /**
     * Finalizes the OAuth flow with the authorization code.
     *
     * <p>This method exchanges the authorization code for access and refresh tokens,
     * then saves them to persistent storage.
     */
    private CompletableFuture<Void> finalizeAuthentication(AuthCodeResult authCodeResult) {
        return CompletableFuture.runAsync(() -> {
            try {
                String redirectUri = getRedirectUri();
                authFlow.finalizeLogin(authCodeResult.code, authCodeResult.state, redirectUri);
                saveTokenToCache();
                LOGGER.debug("Authentication finalized successfully");
            } catch (Exception e) {
                throw new CompletionException("Failed to finalize authentication", e);
            } finally {
                // Stop the callback server
                if (callbackServer != null) {
                    callbackServer.stop(0);
                    callbackServer = null;
                }
            }
        }, executorService);
    }

    /**
     * Creates the TidalApiClient with the current token manager.
     *
     * <p>This method is thread-safe and synchronizes access to the client field.
     */
    private void createApiClient() {
        synchronized (this) {
            tidalApiClient = new TidalApiClient(tokenManager, DEFAULT_BASE_URL);
        }
        LOGGER.debug("TidalApiClient created successfully");
    }

    /**
     * Fetches the current user's profile from the Tidal API.
     *
     * <p>The user profile is cached in {@link #currentUser} for subsequent access
     * via {@link #getCurrentUser()}.
     */
    private CompletableFuture<Void> fetchCurrentUser() {
        return CompletableFuture.runAsync(() -> {
            try {
                TidalApiClient client;
                synchronized (this) {
                    client = tidalApiClient;
                }

                Response<UsersSingleResourceDataDocument> userResponse =
                        CoroutineHelper.runSuspend(cont -> client.createUsers().usersMeGet(cont));

                if (userResponse.isSuccessful() && userResponse.body() != null) {
                    currentUser = userResponse.body().getData();
                    LOGGER.debug("Current user: {}", currentUser.getId());
                } else {
                    LOGGER.warn("Failed to fetch current user - no data returned");
                }
            } catch (Exception e) {
                throw new CompletionException("Failed to fetch current user", e);
            }
        }, executorService);
    }

    /**
     * Schedules automatic token refresh before expiration.
     *
     * <p>The refresh is scheduled to occur {@link #TOKEN_REFRESH_BUFFER_MINUTES} minutes
     * before the token expires. If the refresh fails, it will be retried after 1 minute.
     *
     * <p>After a successful refresh, this method reschedules itself to create a continuous
     * refresh loop.
     */
    private void scheduleTokenRefresh() {
        long expiresAt = tokenManager.getExpiresAt();
        long currentTime = System.currentTimeMillis() / 1000;
        long expiresIn = expiresAt - currentTime;

        // Schedule refresh with buffer time
        long refreshDelay = Math.max(0, expiresIn - (TOKEN_REFRESH_BUFFER_MINUTES * 60));

        LOGGER.debug("Token expires in {}s, scheduling refresh in {}s", expiresIn, refreshDelay);

        refreshTask = refreshScheduler.schedule(() -> {
            try {
                LOGGER.debug("Refreshing token");
                tokenManager.refreshToken();
                saveTokenToCache();

                // Schedule next refresh
                scheduleTokenRefresh();
            } catch (Exception e) {
                LOGGER.error("Failed to refresh token", e);
                // Retry after 1 minute on failure
                refreshTask = refreshScheduler.schedule(this::scheduleTokenRefresh, 1, TimeUnit.MINUTES);
            }
        }, refreshDelay, TimeUnit.SECONDS);
    }

    /**
     * Loads token data from the cache string.
     *
     * <p>The cached token data is in the format: {@code accessToken|refreshToken|expiresAt}
     *
     * @param cachedData The cached token data string
     * @throws IllegalArgumentException if the cached data format is invalid
     */
    private void loadTokenFromCache(String cachedData) {
        String[] parts = cachedData.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid cached token format");
        }

        String accessToken = parts[0];
        String refreshToken = parts[1];
        long expiresAt = Long.parseLong(parts[2]);

        System.out.println("set refreshToken = " + refreshToken);

        tokenManager.setCredentials(accessToken, refreshToken, expiresAt);
        LOGGER.debug("Token loaded from cache");
    }

    /**
     * Saves the current token to the cache.
     *
     * <p>The token data is saved in the format: {@code accessToken|refreshToken|expiresAt}
     */
    private void saveTokenToCache() {
        try {
            packageConfig.set(PERSIST_ACCESS_TOKEN, tokenManager.getAccessToken());
            packageConfig.set(PERSIST_REFRESH_TOKEN, tokenManager.getRefreshToken());
            packageConfig.set(PERSIST_EXPIRES, String.valueOf(tokenManager.getExpiresAt()));

            packageConfig.saveConfig();
            LOGGER.debug("Token saved to cache");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the redirect URI for OAuth callback.
     */
    private String getRedirectUri() {
        return String.format("http://localhost:%d%s", CALLBACK_PORT, CALLBACK_PATH);
    }

    /**
     * Shuts down an executor service gracefully.
     *
     * <p>Attempts a graceful shutdown with a 5-second timeout, then forces shutdown if necessary.
     */
    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("{} did not terminate gracefully, forcing shutdown", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("{} shutdown interrupted, forcing shutdown", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * HTTP handler for OAuth callback.
     *
     * <p>Handles the redirect from Tidal's authorization server, extracts the authorization
     * code and state, and completes the authentication flow.
     */
    private class OAuthCallbackHandler implements HttpHandler {
        private final CompletableFuture<AuthCodeResult> codeFuture;

        public OAuthCallbackHandler(CompletableFuture<AuthCodeResult> codeFuture) {
            this.codeFuture = codeFuture;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();

            if (query == null) {
                sendErrorResponse(exchange, "No query parameters received");
                codeFuture.completeExceptionally(new IllegalStateException("No query parameters in OAuth callback"));
                return;
            }

            String code = null;
            String state = null;
            String error = null;

            // Parse query parameters
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    switch (pair[0]) {
                        case "code" -> code = pair[1];
                        case "state" -> state = pair[1];
                        case "error" -> error = pair[1];
                    }
                }
            }

            if (error != null) {
                sendErrorResponse(exchange, "OAuth error: " + error);
                codeFuture.completeExceptionally(new IllegalStateException("OAuth error: " + error));
                return;
            }

            if (code == null || state == null) {
                sendErrorResponse(exchange, "Missing code or state parameter");
                codeFuture.completeExceptionally(new IllegalStateException("Missing code or state in OAuth callback"));
                return;
            }

            sendSuccessResponse(exchange);
            codeFuture.complete(new AuthCodeResult(code, state));
        }

        private void sendSuccessResponse(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Authentication Successful</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                        h1 { color: #4CAF50; }
                    </style>
                </head>
                <body>
                    <h1>Authentication Successful!</h1>
                    <p>You have successfully authenticated with Tidal.</p>
                    <p>You may close this window and return to the application.</p>
                </body>
                </html>
                """;

            sendResponse(exchange, 200, response);
        }

        private void sendErrorResponse(HttpExchange exchange, String errorMessage) throws IOException {
            String response = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Authentication Failed</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                        h1 { color: #f44336; }
                    </style>
                </head>
                <body>
                    <h1>Authentication Failed</h1>
                    <p>%s</p>
                    <p>Please try again or contact support.</p>
                </body>
                </html>
                """, errorMessage);

            sendResponse(exchange, 400, response);
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Record to hold authorization code and state from OAuth callback.
     */
    private record AuthCodeResult(String code, String state) {}
}
