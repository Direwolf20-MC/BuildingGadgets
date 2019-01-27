package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.utils.BoxedArray;
import com.direwolf20.buildinggadgets.common.tools.NBTTool;
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
    private static Map<String, FieldMapper<?,?>> mappingAdapters = new HashMap<>();

    private static void addSerializer(ITypeSerializer serializer) {
        serializers.add(serializer);
    }

    private static void addMapper(String id, FieldMapper<?,?> mapper) {
        Preconditions.checkArgument(!mappingAdapters.containsKey(id),"Cannot overwrite already registered adapter!");
        mappingAdapters.put(id,mapper);
    }

    private static FieldWrapper wrapperFor(@Nonnull Field field, @Nullable Object instance,@Nonnull String mapper) {
        return new FieldWrapper(field,mappingAdapters.getOrDefault(mapper, FieldMapper.GENERIC_IDENTITY_MAPPER), instance);
    }

    private static FieldWrapper wrapperFor(Field field, String mapper) {
        return wrapperFor(field,null,mapper);
    }

    @Nullable
    public static INBTBase parseFieldValue(Field field, String mapperId) {
        return parseFieldValue(field,null,mapperId);
    }

    @Nullable
    public static INBTBase parseFieldValue(@Nonnull Field field, @Nullable Object instance, @Nonnull String mapperId) {
        field.setAccessible(true);
        FieldWrapper wrapper = wrapperFor(field,instance,mapperId);
        INBTBase tag = null;
        for (ITypeSerializer serializer: serializers) {
            try {
                tag = serializer.serializeValue(wrapper);
                if (tag!=null)
                    return tag;
            } catch (IllegalAccessException e) {
                BuildingGadgets.logger.error("Failed to serialize Field "+field.getName()+"! Retrying with different serializer, if possible.",e);
            }
        }
        return tag;
    }

    public static void applyValue(INBTBase nbt, Field field, String mapperId) {
        applyValue(nbt,field,null,mapperId);
    }

    public static void applyValue(@Nonnull INBTBase nbt, @Nonnull Field field, @Nullable Object instance, @Nonnull String mapperId) {
        field.setAccessible(true);
        FieldWrapper wrapper = wrapperFor(field,instance,mapperId);
        for (ITypeSerializer serializer: serializers) {
            try {
                if (serializer.applyValue(nbt,wrapper))
                    return;
            } catch (IllegalAccessException e) {
                BuildingGadgets.logger.error("Failed to apply Field value to "+field.getName()+"! Retrying with different serializer, if possible.",e);
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
        public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
            if(!field.getType().equals(String.class))
                return null;
            String val = (String)field.get();
            return new NBTTagString(val);
        }

        @Override
        public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
            if(!field.getType().equals(String.class) || !(tag instanceof NBTTagString))
                return false;
            field.set(((NBTTagString) tag).getString());
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
        public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
            if(!field.getType().equals(String[].class))
                return null;
            String[] val = (String[])field.get();
            return NBTTool.createStringList(val);
        }

        @Override
        public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
            if(!field.getType().equals(String[].class) || !(tag instanceof NBTTagList))
                return false;
            field.set(NBTTool.readStringList((NBTTagList) tag));
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
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if(!isAcceptableClass(field.getType()))
                    return null;
                byte val = (byte) (((boolean)field.get())?0:1);
                return new NBTTagByte(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagByte))
                    return false;
                field.set(((NBTTagByte) tag).getByte()==0);
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(byte.class,Byte.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if(!isAcceptableClass(field.getType()))
                    return null;
                byte val = (byte) field.get();
                return new NBTTagByte(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagByte))
                    return false;
                field.set(((NBTTagByte) tag).getByte());
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(short.class,Short.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                short val = (short)field.get();
                return new NBTTagShort(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagShort))
                    return false;
                field.set(((NBTTagShort) tag).getShort());
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(char.class,Character.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                char val = (char)field.get();
                return new NBTTagShort((short)val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagShort))
                    return false;
                field.set((char)((NBTTagShort) tag).getShort());
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(int.class,Integer.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                int val = (int)field.get();
                return new NBTTagInt(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagInt))
                    return false;
                field.set(((NBTTagInt) tag).getInt());
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(float.class,Float.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                float val = (float)field.get();
                return new NBTTagFloat(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagFloat))
                    return false;
                field.set(((NBTTagFloat) tag).getFloat());
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(double.class,Double.class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                double val = (double)field.get();
                return new NBTTagDouble(val);
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagDouble))
                    return false;
                field.set(((NBTTagDouble) tag).getDouble());
                return true;
            }
        });
        addSerializer(new StringSerializer());
        //Arrays
        addSerializer(new PrimitiveSerializer(boolean[].class,Boolean[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if(!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return NBTTool.createBooleanList((boolean[]) field.get());
                }
                else {
                    return NBTTool.createBooleanList((Boolean[]) field.get());
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagByteArray))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(NBTTool.readBooleanList((NBTTagByteArray)tag));
                }
                else {
                    field.set(NBTTool.readBBooleanList((NBTTagByteArray)tag));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(byte[].class,Byte[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if(!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return new NBTTagByteArray((byte[]) field.get());
                }
                else {
                    return new NBTTagByteArray(BoxedArray.asPrimitive((Byte[]) field.get()));
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagByteArray))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(((NBTTagByteArray)tag).getByteArray());
                }
                else {
                    field.set(BoxedArray.asBoxed(((NBTTagByteArray)tag).getByteArray()));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(short[].class,Short[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return NBTTool.createShortList((short[]) field.get());
                }
                else {
                    return NBTTool.createShortList((Short[]) field.get());
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagList))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(NBTTool.readShortList((NBTTagList) tag));
                }
                else {
                    field.set(NBTTool.readBShortList((NBTTagList)tag));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(char[].class,Character[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return new NBTTagString(String.valueOf((char[])field.get()));
                }
                else {
                    return new NBTTagString(String.valueOf(BoxedArray.asPrimitive((Character[])field.get())));
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagString))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(((NBTTagString)tag).getString().toCharArray());
                }
                else {
                    field.set(BoxedArray.asBoxed(((NBTTagString)tag).getString().toCharArray()));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(int[].class,Integer[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return new NBTTagIntArray((int[])field.get());
                }
                else {
                    return new NBTTagIntArray(BoxedArray.asPrimitive((Integer[]) field.get()));
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagIntArray))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(((NBTTagIntArray) tag).getIntArray());
                }
                else {
                    field.set(BoxedArray.asBoxed(((NBTTagIntArray) tag).getIntArray()));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(float[].class,Float[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return NBTTool.createFloatList((float[]) field.get());
                }
                else {
                    return NBTTool.createFloatList((Float[]) field.get());
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagList))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(NBTTool.readFloatList((NBTTagList) tag));
                }
                else {
                    field.set(NBTTool.readBFloatList((NBTTagList)tag));
                }
                return true;
            }
        });
        addSerializer(new PrimitiveSerializer(double[].class,Double[].class) {
            @Nullable
            @Override
            public INBTBase serializeValue(FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()))
                    return null;
                if (getPrimitiveClass().equals(field.getType())) {
                    return NBTTool.createDoubleList((double[]) field.get());
                }
                else {
                    return NBTTool.createDoubleList((Double[]) field.get());
                }
            }

            @Override
            public boolean applyValue(INBTBase tag, FieldWrapper field) throws IllegalAccessException {
                if (!isAcceptableClass(field.getType()) || !(tag instanceof NBTTagList))
                    return false;
                if (getPrimitiveClass().equals(field.getType())) {
                    field.set(NBTTool.readDoubleList((NBTTagList) tag));
                }
                else {
                    field.set(NBTTool.readBDoubleList((NBTTagList)tag));
                }
                return true;
            }
        });
        addSerializer(new StringArraySerializer());
        addMapper(FieldMapper.BLOCK_LIST_MAPPER_ID, FieldMapper.BLOCK_LIST_MAPPER);
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
