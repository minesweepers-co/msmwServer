package org.stocker.exceptions;


import org.vertx.java.core.json.JsonObject;

public class ErrorObjectAdapter {

    // right now , it just takes in status . modify to tie http error codes to be also pulled from ServiceExceptions later .
    public static JsonObject generateErrorObj(ServiceException s){
        return new JsonObject().putString("error", s.getStatus());
    }

}
