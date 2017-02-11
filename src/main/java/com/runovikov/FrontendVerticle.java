package com.runovikov;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import static java.lang.System.out;

/**
 * @author romanovi
 * @since 2/7/17.
 */
public class FrontendVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        final EventBus eb = getVertx().eventBus();
        final Router router = Router.router(vertx);

        SockJSHandlerOptions sockJSHandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(10_000);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJSHandlerOptions);
        PermittedOptions inboundPermitted = new PermittedOptions()
                .setAddress("start");
        PermittedOptions outboundPermitted = new PermittedOptions()
                .setAddress("new_url");
        BridgeOptions options = new BridgeOptions();
        sockJSHandler.bridge(options
                .addInboundPermitted(inboundPermitted)
                .addOutboundPermitted(outboundPermitted));
        router.route("/eventbus/*").handler(sockJSHandler);



        eb.consumer("start", msg -> {

            JsonObject object = new JsonObject(msg.body().toString());
            JsonObject sObj = new JsonObject();
            sObj.put("deep", 0);
            sObj.put("url", object.getString("url"));

            Runnable firstMsg = () -> {
                eb.send("download", sObj);
                out.println(msg.body());
            };

            if (null != downloadVId) {
                vertx.undeploy(downloadVId, event -> {
                    out.printf("Undeploy: %s \n", downloadVId);
                    deployDownload(firstMsg, object);
                });
            } else {
                deployDownload(firstMsg, object);
            }
        });

        StaticHandler staticHandler = StaticHandler.create("assets");
        staticHandler.setCachingEnabled(false);
        router.route("/*").handler(staticHandler);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);
    }

    private volatile String downloadVId = null;

    private void deployDownload(Runnable task, JsonObject object) {
        DeploymentOptions options = new DeploymentOptions().setWorkerPoolName(Main.WORKERPOOL_NAME);
        DownloadVerticle downloadVerticle = new DownloadVerticle(
                object.getInteger("deep"), object.getInteger("limit"), object.getInteger("speed"));
        vertx.deployVerticle(downloadVerticle, options, event -> {
            if (event.succeeded()) {
                downloadVId = event.result();
                out.println("Download verticle was deployed");
                task.run();
            } else {
                event.cause().printStackTrace();
            }
        });
    }
}
