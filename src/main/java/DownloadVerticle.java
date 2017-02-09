import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class DownloadVerticle extends AbstractVerticle {

    public DownloadVerticle(int deep, int limit) {
        this.deep = deep;
        this.limit = limit;
    }

    @Override
    public void start() throws Exception {

        HttpClientOptions options = new HttpClientOptions().
                setProtocolVersion(HttpVersion.HTTP_1_1).
                setSsl(true).
                setTrustAll(true);

        final EventBus eb = getVertx().eventBus();
        final HttpClient client = vertx.createHttpClient(options);

        eb.<String>consumer("download", msg -> {
            out.printf("Download: %s [%s] \n", msg.body(), Thread.currentThread());
            try {
                URL url = new URL(msg.body());
//                int port = url.getProtocol().startsWith("https") ? 443 : 80;
                int port = 443;
                client.getNow(port, url.getHost(), url.getPath(), response -> {
                    out.printf("Received response with status code %d from %s://%s/%s:%d \n",
                            response.statusCode(), url.getProtocol(), url.getHost(), url.getPath(), port);
                    if (response.statusCode() == 301) {
                        String newPath = response.headers().get("Location");
                        eb.send("download", newPath);
                    } else if (response.statusCode() == 200) {
                        response.bodyHandler(buffer -> {
                            // Now all the body has been read
                            System.out.println("Total response body length is " + buffer.length());
//                            System.out.println(buffer.getString(0, buffer.length()));
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private final int deep;
    private final int limit;

}
