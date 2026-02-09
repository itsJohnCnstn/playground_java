package baeldung.apache.httpclient;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class Multipart_Upload {
    void usingAddPart(String urlString) throws IOException {
        //Given
        File file = new File(urlString);
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody stringBody1 = new StringBody("This is message 1", ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody2 = new StringBody("This is message 2", ContentType.MULTIPART_FORM_DATA);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);

        //When
        builder.addPart("file", fileBody);
        builder.addPart("message1", stringBody1);
        builder.addPart("message2", stringBody2);

        //Then
        HttpEntity entity = builder.build();
        HttpPost request = new HttpPost(urlString);
        request.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            httpClient.execute(request, _ -> {
                // do something there
                return null;
            });
        }
    }

    void usingAddBinaryBodyAndAddTextBody(String urlString) throws IOException {
        //Given
        File file = new File(urlString);
        String message = "This is message 1";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);

        //When
        builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, "TEXT_FILE_NAME");
        builder.addTextBody("text", message, ContentType.DEFAULT_BINARY);

        //Then
        HttpEntity entity = builder.build();
        HttpPost request = new HttpPost(urlString);
        request.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            httpClient.execute(request, _ -> {
                // do something there
                return null;
            });
        }
    }

    void usingAByteArrayAndText(String urlString) throws IOException {
        //Given
        String message = "This is message 1";
        byte[] bytes = "binary code".getBytes();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);

        //When
        builder.addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, "TEXT_FILENAME");
        builder.addTextBody("text", message, ContentType.TEXT_PLAIN);

        //Then
        HttpEntity entity = builder.build();
        HttpPost request = new HttpPost(urlString);
        request.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            httpClient.execute(request, _ -> {
                // do something there
                return null;
            });
        }
    }

    void zipFileImageFileAndText(String urlString) throws IOException {
        //Given
        URL zipUrl = Thread.currentThread().getContextClassLoader().getResource("resources/" + "ZIP_FILENAME");
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource("resources/" + "IMAGE_FILENAME");
        FileInputStream zipFileInputStream = new FileInputStream(zipUrl.getPath());
        File image = new File(imageUrl.getPath());
        String message = "This is message 1";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);

        //When
        builder.addBinaryBody("upstream", zipFileInputStream, ContentType.create("application/zip"), "TEXT_ZIP_FILENAME");
        builder.addBinaryBody("file", image, ContentType.DEFAULT_BINARY, "TEXT_IMAGE_FILENAME");
        builder.addTextBody("text", message, ContentType.DEFAULT_BINARY);

        //Then
        HttpEntity entity = builder.build();
        HttpPost request = new HttpPost(urlString);
        request.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            httpClient.execute(request, _ -> {
                // do something there
                return null;
            });
        }
    }

}
