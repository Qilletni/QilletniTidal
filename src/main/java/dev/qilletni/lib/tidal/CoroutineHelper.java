package dev.qilletni.lib.tidal;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import retrofit2.Response;

import java.util.function.Function;

/**
 * Helper class for calling Kotlin suspend functions from Java code.
 * Uses kotlinx.coroutines.runBlocking to execute suspend functions synchronously.
 */
public class CoroutineHelper {

    /**
     * Executes a suspend function synchronously using runBlocking.
     * <p>
     * Usage example:
     * <pre>
     * var response = CoroutineHelper.runSuspend(cont ->
     *     api.suspendMethod("param1", "param2", cont)
     * );
     * </pre>
     *
     * @param suspendCall A function that takes a Continuation and calls the suspend function
     * @param <T> The return type
     * @return The result of the suspend function
     */
    public static <T> T runSuspend(Function<Continuation<? super T>, Object> suspendCall) throws InterruptedException {
        return BuildersKt.runBlocking(
            EmptyCoroutineContext.INSTANCE,
            (scope, continuation) -> suspendCall.apply(continuation)
        );
    }
}
