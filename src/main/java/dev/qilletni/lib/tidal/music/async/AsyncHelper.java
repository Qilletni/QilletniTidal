package dev.qilletni.lib.tidal.music.async;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

public class AsyncHelper {

    public static <T> CompletableFuture<T> adaptCall(Call<T> call) {
        var completableFuture = new CompletableFuture<T>();

        call.enqueue(new Callback<T>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                completableFuture.complete(response.body());
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                completableFuture.completeExceptionally(throwable);
            }
        });

        return completableFuture;
    }

}
