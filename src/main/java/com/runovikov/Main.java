package com.runovikov;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class Main {

    public static final String WORKERPOOL_NAME = "download-pool";

    public static final String ADDRESS_DOWNLOAD = "download";
    public static final String ADDRESS_NEW_URL = "new_url";

    // -Dvertx.disableFileCaching=true
    public static void main(String[] args) {

        VertxOptions options = new VertxOptions();
        options.getAddressResolverOptions().setOptResourceEnabled(true);

        System.getProperties().entrySet().stream()
                .filter(p -> p.getKey().toString().startsWith("vertx"))
                .forEach(p -> out.printf("%s=%s\n", p.getKey(), p.getValue()));


        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new FrontendVerticle());
    }
}
