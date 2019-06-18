package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import javax.annotation.Nullable;
import java.io.*;

public final class SerializationHelper {

    private SerializationHelper() {
    }

    @Nullable
    public static byte[] serialize(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            BuildingGadgets.LOG.error("Exception when serializing object {}", object, e);
            return null;
        }
    }

    @Nullable
    public static <T> T deserialize(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            ObjectInput in = new ObjectInputStream(bis);
            @SuppressWarnings("unchecked") // Casting to erased type (Object)
                    T t = (T) in.readObject();
            return t;
        } catch (IOException | ClassNotFoundException e) {
            BuildingGadgets.LOG.error("Exception when deserializing byte array {}", bytes, e);
            return null;
        }
    }

}
