package com.direwolf20.buildinggadgets.common.config.utils;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class allows for converting
 */
public class FieldSerializer {
    private static Set<ITypeSerializer> serializers = new HashSet<>();
    private static Map<String, FieldMapper<?, ?>> mappingAdapters = new HashMap<>();

    private static void addSerializer(ITypeSerializer serializer) {
        serializers.add(serializer);
    }

    private static void addMapper(String id, FieldMapper<?, ?> mapper) {
        Preconditions.checkArgument(!mappingAdapters.containsKey(id),"Cannot overwrite already registered adapter!");
        mappingAdapters.put(id, mapper);
    }

    private static FieldWrapper wrapperFor(@Nonnull Field field, @Nullable Object instance, @Nonnull String mapper) {
        return new FieldWrapper(field, mappingAdapters.getOrDefault(mapper, FieldMapper.id(field.getType())), instance);
    }

    private static FieldWrapper wrapperFor(Field field, String mapper) {
        return wrapperFor(field, null, mapper);
    }

    @Nullable
    public static NBTBase parseFieldValue(Field field, String mapperId) {
        return parseFieldValue(field, null, mapperId);
    }

    @Nullable
    public static NBTBase parseFieldValue(@Nonnull Field field, @Nullable Object instance, @Nonnull String mapperId) {
        field.setAccessible(true);
        FieldWrapper wrapper = wrapperFor(field, instance, mapperId);
        NBTBase tag = null;
        for (ITypeSerializer serializer: serializers) {
            try {
                tag = serializer.serializeValue(wrapper);
                if (tag != null)
                    return tag;
            } catch (IllegalAccessException e) {
                BuildingGadgets.logger.error("Failed to serialize Field " + field.getName() + "! Retrying with different serializer, if possible.", e);
            }
        }
        return tag;
    }

    public static void applyValue(NBTBase nbt, Field field, String mapperId){
        applyValue(nbt, field, null, mapperId);
    }

    public static void applyValue(@Nonnull NBTBase nbt, @Nonnull Field field, @Nullable Object instance, @Nonnull String mapperId){
        field.setAccessible(true);
        FieldWrapper wrapper = wrapperFor(field, instance, mapperId);
        for (ITypeSerializer serializer : serializers) {
            try {
                if (serializer.applyValue(nbt, wrapper))
                    return;
            } catch (IllegalAccessException e) {
                BuildingGadgets.logger.error("Failed to apply Field value to " + field.getName() + "! Retrying with different serializer, if possible.", e);
            }
        }
    }

    private static abstract class PrimitiveSerializer implements ITypeSerializer{
        private final Class<?> primitiveClass;
        private final Class<?> boxedClass;

        public PrimitiveSerializer(Class<?> primitiveClass, Class<?> boxedClass) {
            this.primitiveClass = primitiveClass;
            this.boxedClass = boxedClass;
        }

        public Class<?> getPrimitiveClass() {
            return primitiveClass;
        }

        public Class<?> getBoxedClass() {
            return boxedClass;
        }

        protected boolean isAcceptableClass(Class<?> clazz) {
            return getPrimitiveClass().equals(clazz) || getBoxedClass().equals(clazz);
        }

        /**
         * @param field The Field to serialize
         * @return Some kind of NBTValue representing this field, which can later be parsed by this TypeSerializer
         * @throws IllegalAccessException if The Field could not be accessed by the given {@link FieldWrapper}
         * @implSpec This Method should return null, if it is incapable of parsing the given Field
         */
        @Nullable
        @Override
        public NBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
            if (field.getMappedType().equals(getPrimitiveClass())) {
                return serializePrimitiveVal(field);
            } else if (field.getMappedType().equals(getBoxedClass())) {
                return serializeBoxedVal(field);
            }
            return null;
        }

        /**
         * @param tag   The Tag's value to apply
         * @param field The field to apply the value to
         * @return whether or not this ITypeSerializer was capable of applying the given value
         * @throws IllegalAccessException if The Field could not be accessed by the given {@link FieldWrapper}
         */
        @Override
        public boolean applyValue(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
            return (getPrimitiveClass().equals(field.getMappedType()) && applyPrimitiveVal(tag, field)) ||
                    (getBoxedClass().equals(field.getMappedType()) && applyBoxedVal(tag, field));
        }

        protected abstract NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException;

        protected abstract NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException;

        protected abstract boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException;

        protected abstract boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException;

        @Override
        public String toString() {
            return "PrimitiveSerializer{" +
                    "primitiveClass=" + primitiveClass.getSimpleName() +
                    ", boxedClass=" + boxedClass.getSimpleName() +
                    '}';
        }
    }

    private static class StringSerializer implements ITypeSerializer {
        @Nullable
        @Override
        public NBTBase serializeValue(FieldWrapper field) throws IllegalAccessException{
            if (!field.getMappedType().equals(String.class))
                return null;
            String val = field.get(String.class);
            return new NBTTagString(val);
        }

        @Override
        public boolean applyValue(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
            if (!field.getMappedType().equals(String.class) || !(tag instanceof NBTTagString))
                return false;
            field.set(((NBTTagString) tag).getString(), String.class);
            return true;
        }

        @Override
        public String toString() {
            return "StringSerializer{}";
        }
    }

    private static class StringArraySerializer implements ITypeSerializer {
        @Nullable
        @Override
        public NBTBase serializeValue(FieldWrapper field) throws IllegalAccessException{
            if (!field.getMappedType().equals(String[].class))
                return null;
            String[] val = field.get(String[].class);
            return NBTTool.createStringList(val);
        }

        @Override
        public boolean applyValue(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
            if (!field.getMappedType().equals(String[].class) || !(tag instanceof NBTTagList))
                return false;
            field.set(NBTTool.readStringList((NBTTagList) tag), String[].class);
            return true;
        }

        @Override
        public String toString() {
            return "StringArraySerializer{}";
        }
    }

    public static void init() {
        clear();
        addSerializer(new PrimitiveSerializer(boolean.class,Boolean.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByte((byte) (field.get(boolean.class) ? 0 : 1));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByte((byte) (field.get(Boolean.class) ? 0 : 1));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByte)) return false;
                field.set(((NBTTagByte) tag).getByte() == 0, boolean.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByte)) return false;
                field.set(((NBTTagByte) tag).getByte() == 0, Boolean.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(byte.class, Byte.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByte(field.get(byte.class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByte(field.get(Byte.class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByte)) return false;
                field.set(((NBTTagByte) tag).getByte(), byte.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByte)) return false;
                field.set(((NBTTagByte) tag).getByte(), Byte.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(short.class, Short.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagShort(field.get(short.class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagShort(field.get(Short.class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagShort)) return false;
                field.set(((NBTTagShort) tag).getShort(), short.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagShort)) return false;
                field.set(((NBTTagShort) tag).getShort(), Short.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(char.class, Character.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagShort((short) ((char) field.get(char.class)));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagShort((short) ((char) field.get(Character.class)));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagShort)) return false;
                field.set((char) ((NBTTagShort) tag).getShort(), char.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagShort)) return false;
                field.set((char) ((NBTTagShort) tag).getShort(), Character.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(int.class, Integer.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagInt(field.get(int.class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagInt(field.get(Integer.class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagInt)) return false;
                field.set(((NBTTagInt) tag).getInt(), int.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagInt)) return false;
                field.set(((NBTTagInt) tag).getInt(), Integer.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(float.class, Float.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagFloat(field.get(float.class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagFloat(field.get(Float.class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagFloat)) return false;
                field.set(((NBTTagFloat) tag).getFloat(), float.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagFloat)) return false;
                field.set(((NBTTagFloat) tag).getFloat(), Float.class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(double.class, Double.class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagDouble(field.get(double.class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagDouble(field.get(Double.class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagDouble)) return false;
                field.set(((NBTTagDouble) tag).getDouble(), double.class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagDouble)) return false;
                field.set(((NBTTagDouble) tag).getDouble(), Double.class);
                return true;
            }
        });
        addSerializer(new StringSerializer());
        //Arrays
        addSerializer(new PrimitiveSerializer(boolean[].class,Boolean[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createBooleanList(field.get(boolean[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createBooleanList(field.get(Boolean[].class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByteArray)) return false;
                field.set(NBTTool.readBooleanList((NBTTagByteArray) tag), boolean[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByteArray)) return false;
                field.set(NBTTool.readBBooleanList((NBTTagByteArray) tag), Boolean[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(byte[].class, Byte[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByteArray(field.get(byte[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagByteArray(ArrayUtils.asPrimitive(field.get(Byte[].class)));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByteArray)) return false;
                field.set(((NBTTagByteArray) tag).getByteArray(), byte[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagByteArray)) return false;
                field.set(ArrayUtils.asBoxed(((NBTTagByteArray) tag).getByteArray()), Byte[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(short[].class, Short[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createShortList(field.get(short[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createShortList(field.get(Short[].class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readShortList((NBTTagList) tag), short[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readBShortList((NBTTagList) tag), Short[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(char[].class, Character[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagString(String.valueOf(field.get(char[].class)));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagString(String.valueOf(ArrayUtils.asPrimitive(field.get(Character[].class))));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagString)) return false;
                field.set(((NBTTagString) tag).getString().toCharArray(), char[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagString)) return false;
                field.set(ArrayUtils.asBoxed(((NBTTagString) tag).getString().toCharArray()), Character[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(int[].class, Integer[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagIntArray(field.get(int[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return new NBTTagIntArray(ArrayUtils.asPrimitive(field.get(Integer[].class)));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagIntArray)) return false;
                field.set(((NBTTagIntArray) tag).getIntArray(), int[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagIntArray)) return false;
                field.set(ArrayUtils.asBoxed(((NBTTagIntArray) tag).getIntArray()), Integer[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(float[].class, Float[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createFloatList(field.get(float[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createFloatList(field.get(Float[].class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readFloatList((NBTTagList) tag), float[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readBFloatList((NBTTagList) tag), Float[].class);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(double[].class, Double[].class) {
            @Override
            protected NBTBase serializePrimitiveVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createDoubleList(field.get(double[].class));
            }

            @Override
            protected NBTBase serializeBoxedVal(FieldWrapper field) throws IllegalAccessException {
                return NBTTool.createDoubleList(field.get(Double[].class));
            }

            @Override
            protected boolean applyPrimitiveVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readDoubleList((NBTTagList) tag), double[].class);
                return true;
            }

            @Override
            protected boolean applyBoxedVal(NBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!(tag instanceof NBTTagList)) return false;
                field.set(NBTTool.readBDoubleList((NBTTagList) tag), Double[].class);
                return true;
            }
        });
        addSerializer(new StringArraySerializer());
        //addMapper(FieldMapper.BLOCK_LIST_MAPPER_ID, FieldMapper.BLOCK_LIST_MAPPER);
        addMapper(FieldMapper.PATTERN_LIST_MAPPER_ID, FieldMapper.PATTERN_LIST_MAPPER);
    }

    private static void clear() {
        serializers.clear();
        mappingAdapters.clear();
    }

    static {
        init();
    }
}
