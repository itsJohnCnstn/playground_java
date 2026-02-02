package baeldung.apache.httpclient.timeout;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to configure and use Apache HttpClient 5 with
 * connection and socket timeouts.
 *
 * <p>
 * This example is intentionally simple and suitable for learning:
 * <ul>
 *   <li>Uses {@link BasicHttpClientConnectionManager} (single connection)</li>
 *   <li>Configures connect + socket timeouts via {@link ConnectionConfig}</li>
 *   <li>Uses a {@code ResponseHandler} lambda</li>
 *   <li>Consumes the response entity correctly</li>
 * </ul>
 *
 * <p>
 * Based on:
 * <a href="https://www.baeldung.com/httpclient-timeout">Baeldung: Apache HttpClient Timeout</a>
 */
public class Apache_HttpClient_Timeout {

    /**
     * Java entry point.
     *
     * <p>
     * Declares {@code throws IOException} so we can see real failures
     * (timeouts, connection errors, HTTP errors) without wrapping them.
     */
    static void main() throws IOException {
        HttpResult result = new Apache_HttpClient_Timeout().request("https://www.github.com");
        System.out.println(
                "Finished: status=" + result.status() +
                        ", bodyLength=" + result.body().length()
        );
    }

    /**
     * Manages HTTP connections.
     *
     * <p>
     * {@link BasicHttpClientConnectionManager} is:
     * <ul>
     *   <li>Single-connection</li>
     *   <li>Thread-unsafe</li>
     *   <li>Good for demos, tests, and learning</li>
     * </ul>
     *
     * <p>
     * For real applications, {@code PoolingHttpClientConnectionManager}
     * is usually preferred.
     */
    private final BasicHttpClientConnectionManager connectionManager;

    /**
     * Creates a client configuration with explicit timeouts.
     *
     * <p>
     * Timeouts explained:
     * <ul>
     *   <li><b>Connect timeout</b> – (http.connection.timeout) –
     *      the time to establish the connection with the remote host</li>
     *   <li><b>Socket timeout</b> – (http.socket.timeout) – the time waiting for data –
     *      after establishing the connection; maximum time of inactivity between two data packets</li>
     * </ul>
     *
     * <p>
     * If either timeout is exceeded, an {@link IOException} is thrown.
     */
    public Apache_HttpClient_Timeout() {
        int timeout = 5;
        ConnectionConfig config = ConnectionConfig.custom()
                .setConnectTimeout(timeout * 1000, TimeUnit.MILLISECONDS)
                .setSocketTimeout(timeout * 1000, TimeUnit.MILLISECONDS)
                .build();
        this.connectionManager = new BasicHttpClientConnectionManager();
        this.connectionManager.setConnectionConfig(config);
    }

    /**
     * Simple immutable result holder.
     *
     * <p>
     * Using a {@code record} keeps the example concise and explicit.
     * It avoids returning raw {@code ClassicHttpResponse}, whose
     * lifecycle is managed by HttpClient.
     */
    record HttpResult(int status, String body) {
    }

    /**
     * Executes an HTTP GET request and returns the response status + body.
     *
     * @param url the URL to request
     * @return HTTP status and response body
     * @throws IOException if the request fails or returns a 4xx/5xx status
     */
    private HttpResult request(String url) throws IOException {
        HttpGet request = new HttpGet(url);


        int hardTimeout = 5_000;

        /*
            While setting timeouts on establishing the HTTP connection and not receiving data is very useful,
                sometimes we need to set a hard timeout for the entire request.
            For example, the download of a potentially large file fits into this category.
                In this case, the connection may be successfully established,
                and data may be consistently coming through,
                but we still need to ensure that the operation doesn’t go over some specific time threshold.
            HttpClient doesn’t have any configuration that allows us to set an overall timeout for a request;
                it does, however, provide abort functionality for requests,
                so we can leverage that mechanism to implement a simple timeout mechanism:

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    request.abort();
                }
            };
            new Timer(true).schedule(timerTask, hardTimeout);
         */

        //Refactored / modern-style version:
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            scheduler.schedule(
                    () -> request.abort(),
                    hardTimeout,
                    TimeUnit.MILLISECONDS);
        }

        /*
         * CloseableHttpClient is created per call here for simplicity.
         *
         * In real applications, the client is usually long-lived
         * and reused across requests.
         */
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build()) {

            /*
             * execute(request, handler):
             *
             * - HttpClient opens the connection
             * - Passes ClassicHttpResponse to the handler
             * - Closes/releases the response automatically
             *
             * IMPORTANT:
             * The response entity MUST be consumed inside the handler.
             */
            return httpClient.execute(request, response -> {
                int status = response.getCode();
                String body = response.getEntity() == null
                        ? ""
                        : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println(status + " " + response.getReasonPhrase());

                if (status >= 400) throw new IOException("Request failed: " + status);
                return new HttpResult(status, body);
            });
        }

    }
}
