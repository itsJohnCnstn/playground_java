package baeldung.apache.httpclient;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates HTTP redirect handling for POST requests with Apache HttpClient 5.x.
 *
 * <h2>Source</h2>
 * Based on: <a href="https://www.baeldung.com/httpclient-redirect-on-http-post">
 * Baeldung - HttpClient Redirect on HTTP POST</a>
 *
 * <h2>What Are HTTP Redirects?</h2>
 * A redirect is a server response instructing the client to request a different URL.
 * The server responds with a 3xx status code and a {@code Location} header containing the new URL.
 *
 * <pre>
 * ┌──────────┐   POST /short-url    ┌──────────┐
 * │  Client  │ ──────────────────>  │  Server  │
 * └──────────┘                      └──────────┘
 *       │                                 │
 *       │   301 Moved Permanently         │
 *       │   Location: /new-url            │
 *       │ <────────────────────────────── │
 *       │                                 │
 *       │   GET /new-url (or POST)        │
 *       │ ──────────────────────────────> │
 * </pre>
 *
 * <h3>Common Redirect Status Codes</h3>
 * <ul>
 *   <li><b>301 Moved Permanently</b> - Resource permanently moved; browsers cache this</li>
 *   <li><b>302 Found</b> - Temporary redirect (historically ambiguous for POST)</li>
 *   <li><b>303 See Other</b> - Always redirect with GET (safe after POST)</li>
 *   <li><b>307 Temporary Redirect</b> - Preserve original method (POST stays POST)</li>
 *   <li><b>308 Permanent Redirect</b> - Like 301 but preserves method</li>
 * </ul>
 *
 * <h2>Why POST Redirects Were Historically Prohibited (RFC 2616)</h2>
 * <p>
 * The HTTP specification (RFC 2616, Section 10.3) states:
 * <blockquote>
 * "If the 301 status code is received in response to a request other than GET or HEAD,
 * the user agent MUST NOT automatically redirect the request unless it can be confirmed
 * by the user, since this might change the conditions under which the request was issued."
 * </blockquote>
 *
 * <h3>Reasons for this restriction:</h3>
 * <ul>
 *   <li><b>Safety concern</b>: POST modifies server state (creates/updates data)</li>
 *   <li><b>Duplicate submissions</b>: Auto-following could charge credit cards twice,
 *       submit forms multiple times, or create duplicate records</li>
 *   <li><b>User confirmation required</b>: User should decide whether to resubmit</li>
 *   <li><b>GET is safe/idempotent</b>: No side effects, safe to repeat automatically</li>
 * </ul>
 *
 * <h2>HttpClient 5.x vs 4.x Behavior</h2>
 * <ul>
 *   <li><b>HttpClient 4.x</b>: POST redirects NOT followed by default (strict RFC compliance)</li>
 *   <li><b>HttpClient 5.x</b>: POST redirects ARE followed by default (more practical approach)</li>
 * </ul>
 *
 * <h2>Why Baeldung Examples Return 520 Error</h2>
 * <p>
 * The original Baeldung examples use {@code http://t.co/...} which returns HTTP 520.
 * <ul>
 *   <li><b>Root cause</b>: Using HTTP instead of HTTPS</li>
 *   <li><b>Status 520</b> = Cloudflare error when origin server returns unexpected response</li>
 *   <li><b>Solution</b>: Use {@code https://t.co/...} instead of {@code http://t.co/...}</li>
 * </ul>
 * The {@code t.co} URL shortener (Twitter/X) enforces HTTPS and rejects plain HTTP requests.
 *
 * <h2>Key Concepts Explained</h2>
 *
 * <h3>LaxRedirectStrategy</h3>
 * <p>
 * {@link LaxRedirectStrategy} explicitly follows redirects for ALL HTTP methods
 * (GET, POST, PUT, DELETE, etc.), relaxing strict RFC compliance. In HttpClient 5.x,
 * this is largely the default behavior, but using it explicitly documents your intent.
 *
 * <h3>Why ResponseHandler Must Return a Value</h3>
 * <pre>{@code
 * // CORRECT: HttpClientResponseHandler<T> requires returning T
 * httpClient.execute(request, response -> {
 *     assertThat(response.getCode()).isEqualTo(200);
 *     return response;  // Must return something (or null)
 * });
 *
 * // WRONG: Won't compile - lambda must accept response parameter and return value
 * httpClient.execute(request, () -> {
 *     // This signature doesn't match HttpClientResponseHandler
 * });
 * }</pre>
 * <p>
 * The {@code execute(request, handler)} method takes {@code HttpClientResponseHandler<T>}
 * which is a functional interface: {@code T handleResponse(ClassicHttpResponse response)}.
 * This design ensures the response is properly consumed and closed (resource management).
 *
 * <h3>Why Methods Throw IOException</h3>
 * <p>
 * Network operations can fail in many ways:
 * <ul>
 *   <li>DNS resolution failure</li>
 *   <li>Connection timeout</li>
 *   <li>Socket errors</li>
 *   <li>Server unavailable</li>
 *   <li>SSL/TLS handshake failure</li>
 * </ul>
 * Java's checked exception design forces explicit handling of these failure modes.
 *
 * <h3>User-Agent Header</h3>
 * <pre>{@code
 * request.addHeader("User-Agent", "Mozilla/5.0");
 * }</pre>
 * <p>
 * The User-Agent header identifies the client to the server. Many servers and CDNs
 * (like Cloudflare) block requests without a User-Agent as a basic bot protection measure.
 * {@code "Mozilla/5.0"} is a minimal valid User-Agent that passes most checks.
 *
 * @see DefaultRedirectStrategy
 * @see LaxRedirectStrategy
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3">RFC 2616 Section 10.3</a>
 */
class Redirects_for_POSTTest {

    //region Baeldung original

    /**
     * POST redirect with explicit {@link DefaultRedirectStrategy}.
     *
     * <p><b>Demonstrates:</b> In HttpClient 5.x, {@link DefaultRedirectStrategy} follows
     * redirects for POST requests (unlike HttpClient 4.x which required {@link LaxRedirectStrategy}).
     *
     * <p><b>Fix from original Baeldung:</b> Changed {@code http://} to {@code https://}
     * because t.co enforces HTTPS and returns 520 for plain HTTP requests.
     *
     * @throws IOException if network error occurs (DNS, timeout, connection refused, etc.)
     */
    @Test
    void givenRedirectingPOST_whenUsingDefaultRedirectStrategy_thenRedirected() throws IOException {

        final HttpPost request = new HttpPost("https://t.co/I5YYd9tddw");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy())
                .build()) {
            httpClient.execute(request, response -> {
                assertThat(response.getCode()).isEqualTo(200);
                return response;
            });
        }
    }

    /**
     * POST redirect with default HttpClient settings (no explicit strategy).
     *
     * <p><b>Demonstrates:</b> In HttpClient 5.x, even without explicitly setting a redirect
     * strategy, POST redirects are followed automatically. This is different from HttpClient 4.x
     * where POST redirects were NOT followed by default (strict RFC 2616 compliance).
     *
     * <p><b>Key insight:</b> No {@link LaxRedirectStrategy} needed in HttpClient 5.x -
     * POST redirects work out of the box.
     *
     * @throws IOException if network error occurs (DNS, timeout, connection refused, etc.)
     */
    @Test
    void givenRedirectingPOST_whenConsumingUrlWhichRedirectsWithPOST_thenRedirected() throws IOException {
        HttpPost request = new HttpPost("https://t.co/I5YYd9tddw");
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .build()) {
            httpClient.execute(request, response -> {
                assertThat(response.getCode()).isEqualTo(200);
                return response;
            });
        }
    }

    //endregion

    //region Fixed

    /**
     * Demonstrates how to capture the redirect response instead of following it.
     *
     * <p><b>Demonstrates:</b>
     * <ul>
     *   <li>{@code disableRedirectHandling()} - prevents automatic redirect following</li>
     *   <li>How to inspect the raw redirect response (3xx status code)</li>
     *   <li>How to read the {@code Location} header containing the redirect target URL</li>
     * </ul>
     *
     * <p><b>Use case:</b> When you need to know WHERE the redirect points to without
     * actually following it (e.g., URL validation, link checking, analytics).
     *
     * @throws IOException if network error occurs (DNS, timeout, connection refused, etc.)
     */
    @Test
    void givenRedirectingGet_whenRedirectHandlingDisabled_thenRedirectResponseReturned() throws IOException {
        var request = new HttpGet("https://t.co/I5YYd9tddw");
        request.addHeader("User-Agent", "Mozilla/5.0");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling() // assert redirect, don't follow
                .build()) {

            httpClient.execute(request, response -> {
                assertThat(response.getCode()).isIn(301, 302, 303, 307, 308);
                assertThat(response.getFirstHeader("Location")).isNotNull();
                return response;
            });
        }
    }

    /**
     * Minimal working example: POST request follows redirects with default settings.
     *
     * <p><b>Key takeaway:</b> In HttpClient 5.x, POST redirects work out of the box.
     * No special configuration needed - just use HTTPS.
     *
     * <p><b>What's NOT needed:</b>
     * <ul>
     *   <li>No {@link LaxRedirectStrategy} - default handles POST redirects</li>
     *   <li>No User-Agent header - t.co accepts requests without it over HTTPS</li>
     *   <li>No special headers - just the URL and default client</li>
     * </ul>
     *
     * @throws IOException if network error occurs (DNS, timeout, connection refused, etc.)
     */
    @Test
    void follows_redirects_even_for_post() throws IOException {
        var request = new HttpPost("https://t.co/I5YYd9tddw");

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .build()) {

            httpClient.execute(request, response -> {
                assertThat(response.getCode()).isEqualTo(200);
                return null;
            });
        }
    }

    //endregion

}