package concurrency.simple_tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureTimeoutException {
    /*
        timeout exception
     */
    static void main() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(1000);
            System.out.println("worker");
            return 42;
        });
        /*
        This means:
            “Wait up to 500ms for the result”
            But the task:
                •	Sleeps for 1000ms
                •	So it will NOT finish within 500ms
            After 500ms:
            ➡ TimeoutException is thrown.
         */
        System.out.println("Result: " + future.get(500, TimeUnit.MILLISECONDS));
        /*
            Even after exception the task is still running.
                to prevent leaking
            try {
                future.get(500, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true); // interrupt the thread
            }
         */

        executor.shutdown();
    }
}
