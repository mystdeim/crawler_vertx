import io.vertx.core.Vertx;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FrontendVerticle());
    }

}
