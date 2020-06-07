package com.direwolf20.buildinggadgets.common.util.tools;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface JsonBiDiSerializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}
