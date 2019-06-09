package com.direwolf20.buildinggadgets.common.util.helpers;

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
        } catch (IOException ignored) {
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
        } catch (IOException | ClassNotFoundException ignored) {
            return null;
        }
    }

}
