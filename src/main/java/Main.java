import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.WorkerExecutor;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class Main {

    public static final String WORKERPOOL_NAME = "download-pool";

    public static final String ADDRESS_NEW_URL = "new_url";

    // -Dvertx.disableFileCaching=true
    public static void main(String[] args) {

        VertxOptions options = new VertxOptions();
        options.getAddressResolverOptions().setOptResourceEnabled(true);

        System.getProperties().entrySet().stream()
                .filter(p -> p.getKey().toString().startsWith("vertx"))
                .forEach(p -> out.printf("%s=%s\n", p.getKey(), p.getValue()));


        Vertx vertx = Vertx.vertx(options);

        int poolSize = 10;
        long maxExecuteTime = 120_000;
        WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKERPOOL_NAME, poolSize, maxExecuteTime);

        vertx.deployVerticle(new FrontendVerticle());
    }

}
