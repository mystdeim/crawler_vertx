import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class DownloadVerticle extends AbstractVerticle {

    public static final String ADDRESS = "download";

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
        set = Collections.synchronizedSet(new HashSet<>());

        eb.<String>consumer(ADDRESS, msg -> {
            try {
                URL url = new URL(msg.body());
                String urlTxt = getCanonicalStr(msg.body());

                if (!set.contains(urlTxt)) {
                    set.add(urlTxt);
                    int id = set.size();
                    out.printf("Downloading: %s [%s] \n", msg.body(), Thread.currentThread());
                    Document doc = Jsoup.connect(msg.body()).get();

                    Elements elements = doc.select("a");
                    for (Element element : elements) {
                        String newUrlTxt = element.absUrl("href");
                        URL newUrl = new URL(newUrlTxt);
                        if (newUrl.getHost().equalsIgnoreCase(url.getHost())) {
                            if (set.size() < limit) {
                                eb.send(ADDRESS, newUrlTxt);
                            }
                        }
                    }

                    JsonObject json = new JsonObject();
                    json.put("id", id);
                    json.put("url", urlTxt);
                    eb.send(Main.ADDRESS_NEW_URL, json);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private final int deep;
    private final int limit;
    private Set<String> set;

    private String getCanonicalStr(String url) throws MalformedURLException {
        URL tmp = new URL(url);
        return tmp.getHost() + tmp.getPath();
    }

}
