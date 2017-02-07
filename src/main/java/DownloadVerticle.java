import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

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

        final EventBus eb = getVertx().eventBus();
        eb.consumer("download", msg -> {
           out.printf("Download: %s \n", msg.body());
        });

    }

    private final int deep;
    private final int limit;

}
