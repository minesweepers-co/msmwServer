package org.stocker;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import com.google.common.base.Strings;
import com.mongodb.*;
import org.stocker.nseData.NseDataRow;
import org.stocker.routes.UpdateRecordsRoute;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.URISyntaxException;
import java.net.UnknownHostException;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class PingVerticle extends Verticle {

  public void start() {
      DB mongoDB = null;
      try {
          Mongo mongo = new Mongo("localhost", 27017);
          mongoDB = mongo.getDB("trial1");
      } catch (UnknownHostException e) {
          e.printStackTrace();
      }

      final DBCollection stockCollection = mongoDB.getCollection("stocks");

      RouteMatcher matcher = new RouteMatcher();


      matcher.get("/stock/:stock", new Handler<HttpServerRequest>() {
          @Override
          public void handle(HttpServerRequest httpServerRequest) {
              String stock = httpServerRequest.params().get("stock");
              if(Strings.isNullOrEmpty(stock)){
                  httpServerRequest.response().setStatusCode(400).end();
              }

              BasicDBObject searchQuery = new BasicDBObject();
              searchQuery.put("SYMBOL", stock);

              DBCursor cursor = stockCollection.find(searchQuery);

              if (cursor.hasNext()) {
                  JsonObject jsonObject = new JsonObject();
                  DBObject object = cursor.next();
                  jsonObject.putString(NseDataRow.LOW.toString(), object.get("LOW").toString());
                  jsonObject.putString(NseDataRow.HIGH.toString(), object.get("HIGH").toString());
                  jsonObject.putString(NseDataRow.CLOSE.toString(), object.get("CLOSE").toString());
                  httpServerRequest.response().end(jsonObject.toString());
              }
          }
      });

      vertx.createHttpServer().requestHandler(matcher).listen(4080);


      container.logger().info("PingVerticle started : 4080");

  }
}


