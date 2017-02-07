import io.vertx.core.Vertx;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class Main {

    // -Dvertx.disableFileCaching=true
    public static void main(String[] args) {

        System.getProperties().entrySet().stream()
                .filter(p -> p.getKey().toString().startsWith("vertx"))
                .forEach(p -> out.printf("%s=%s\n", p.getKey(), p.getValue()));

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FrontendVerticle());
    }

}
