package com.runovikov;

import com.google.common.util.concurrent.RateLimiter;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class DownloadVerticle extends AbstractVerticle {

    public DownloadVerticle(int deep, int limit, int speed) {
        this.deep = deep;
        this.limit = limit;
        rateLimiter = RateLimiter.create(speed);
    }

    @Override
    public void start() throws Exception {

        final EventBus eb = getVertx().eventBus();
        set = Collections.synchronizedSet(new HashSet<>());

        eb.<JsonObject>consumer(Main.ADDRESS_DOWNLOAD, msg -> {
            int currentDeep = msg.body().getInteger("deep");
            String urlTxt = getCanonicalStr(msg.body().getString("url"));
            String host = getHost(msg.body().getString("url"));

            if (!set.contains(urlTxt) && set.size() < limit && currentDeep <= deep) {

                set.add(urlTxt);
                int id = set.size();

                // blocking
                getVertx().<JsonObject>executeBlocking(future -> {
                    out.printf("Downloading: %s [%s] \n", msg.body(), Thread.currentThread());
                    Document doc = null;
                    JsonObject json = new JsonObject();
                    try {
                        rateLimiter.acquire();
                        long ms = System.currentTimeMillis();
                        doc = Jsoup.connect(msg.body().getString("url")).get();
                        json.put("time", System.currentTimeMillis() - ms);
                    } catch (IOException e) {
                        future.fail(e);
                        e.printStackTrace();
                    }

                    json.put("url", urlTxt);
                    json.put("id", id);
                    json.put("size", readableFileSize(doc.toString().length()));
                    json.put("status", 200); // because parse finished successfully

                    Elements elements = doc.select("a");
                    for (Element element : elements) {
                        String newUrlTxt = element.absUrl("href");
                        try {
                            URL newUrl = new URL(newUrlTxt);
                            if (newUrl.getHost().equalsIgnoreCase(host)) {
                                eb.send(Main.ADDRESS_DOWNLOAD, new JsonObject()
                                        .put("url", newUrlTxt)
                                        .put("deep", currentDeep + 1));
                            }
                        } catch (Exception e) {
                            err.printf("Bad link: '%s' \n", newUrlTxt);
                        }
                    }
                    future.complete(json);
                }, false, res -> {
                    out.printf("Downloaded: %s [%s] \n", msg.body(), Thread.currentThread());
                    eb.publish(Main.ADDRESS_NEW_URL, res.result());
                });
            }
        });

    }

    private final int deep;
    private final int limit;
    private Set<String> set;
    private RateLimiter rateLimiter;

    private String getCanonicalStr(String url) {
        try {
            URL tmp = new URL(url.replaceAll("\\/+$", ""));
            return tmp.getHost() + tmp.getPath();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    public static void main(String[] args) {

        VertxOptions options = new VertxOptions().setWorkerPoolSize(20);
        options.getAddressResolverOptions().setOptResourceEnabled(true);
        Vertx vertx = Vertx.vertx(options);

        vertx.deployVerticle(new DownloadVerticle(2, 10, 10), event -> {
            vertx.eventBus().publish(Main.ADDRESS_DOWNLOAD, new JsonObject()
                    .put("url","http://megapl.ru")
                    .put("deep", 0)
            );
        });
    }

}
