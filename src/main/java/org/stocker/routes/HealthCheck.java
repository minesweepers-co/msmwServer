package org.stocker.routes;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public class HealthCheck extends Middleware{

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        request.response().setStatusCode(200).end();
    }
}
