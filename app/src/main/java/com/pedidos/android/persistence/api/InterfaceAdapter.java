package com.pedidos.android.persistence.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private Type desiredType; //type of T

    /// implementation type
    public InterfaceAdapter(Type genericType){
        desiredType = genericType;
    }

    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
        return context.serialize(object);
    }

    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(elem, desiredType);
    }
}
