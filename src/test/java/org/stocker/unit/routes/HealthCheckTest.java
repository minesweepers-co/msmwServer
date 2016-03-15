package org.stocker.unit.routes;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.stocker.routes.HealthCheck;
import org.vertx.java.core.Handler;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class HealthCheckTest {

    HealthCheck healthCheck = new HealthCheck();

    @Test
    public void testHealthCheckEndpoint(){
        YokeRequest yokeRequest = mock(YokeRequest.class);
        YokeResponse response = mock(YokeResponse.class);
        when(yokeRequest.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        Handler<Object> next = spy(new Handler<Object>() {
            @Override
            public void handle(Object event) {
                // ignore
            }
        });
        healthCheck.handle(yokeRequest, next);
        Mockito.verify(next, times(0)).handle(any());
        Mockito.verify(response, times(1)).setStatusCode(200);
        Mockito.verify(response, times(1)).end();
    }
}
