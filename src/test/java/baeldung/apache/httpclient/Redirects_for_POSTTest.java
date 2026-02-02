package baeldung.apache.httpclient;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class Redirects_for_POSTTest {

    //region Baeldung original

    @Test
    @Disabled
    void givenRedirectingPOST_whenUsingDefaultRedirectStrategy_thenRedirected() throws IOException {

        final HttpPost request = new HttpPost("http://t.co/I5YYd9tddw");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy())
                .build()) {
            httpClient.execute(request, response -> {
                assertThat(response.getCode()).isEqualTo(200);
                return response;
            });
        }
    }

    @Test
    @Disabled
    void givenRedirectingPOST_whenConsumingUrlWhichRedirectsWithPOST_thenRedirected() throws IOException {
        HttpPost request = new HttpPost("http://t.co/I5YYd9tddw");
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

    @Test
    @Disabled
    void givenRedirectingGet_whenRedirectHandlingDisabled_thenRedirectResponseReturned() throws IOException {
        var request = new HttpGet("http://t.co/I5YYd9tddw");
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

    @Test
    void follows_redirects_even_for_post() throws Exception {
        var request = new HttpPost("https://t.co/I5YYd9tddw");
        request.addHeader("User-Agent", "Mozilla/5.0");

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()) {

            httpClient.execute(request, response -> {
                // may still fail for t.co specifically (edge/bot protection),
                // but this is the correct knob for POST redirect behavior
                assertThat(response.getCode()).isEqualTo(200);
                return null;
            });
        }
    }

    //endregion

}