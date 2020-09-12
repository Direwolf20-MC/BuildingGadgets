package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

@Tainted(reason = "Part of the Template system")
public interface JsonBiDiSerializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}
