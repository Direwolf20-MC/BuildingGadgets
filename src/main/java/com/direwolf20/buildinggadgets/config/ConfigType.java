package com.direwolf20.buildinggadgets.config;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.tools.NBTTools;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

enum ConfigType {
    BOOLEAN(0) {
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof Boolean) {
                boolean val = (boolean) value;
                res.setByte(InGameConfig.KEY_VALUE,(byte)(val?0:1));
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagByte) {
                f.setBoolean(null,((NBTTagByte) nbt).getByte()==0);
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagByte!");
            }
        }
    },
    BOOLEAN_ARR(1){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof boolean[]) {
                boolean[] val = (boolean[]) value;
                byte[] resVal = new byte[val.length];
                for (int i = 0; i < val.length; ++i) {
                    resVal[i] = (byte)(val[i]?0:1);
                }
                res.setByteArray(InGameConfig.KEY_VALUE,resVal);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagByteArray) {
                byte[] ar = ((NBTTagByteArray) nbt).getByteArray();
                boolean[] val = new boolean[ar.length];
                for (int i=0; i<ar.length; ++i) {
                    val[i] = ar[i] == 0;
                }
                f.set(null,val);
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagByteArray!");
            }
        }
    },
    INT(2){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if (value instanceof Integer) {
                int val = (int)value;
                res.setInteger(InGameConfig.KEY_VALUE,val);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagInt) {
                f.setInt(null,((NBTTagInt) nbt).getInt());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagInt!");
            }
        }
    },
    INT_ARR(3){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof int[]) {
                int[] val = (int[]) value;
                res.setIntArray(InGameConfig.KEY_VALUE,val);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagIntArray) {
                f.set(null,((NBTTagIntArray) nbt).getIntArray());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagIntArray!");
            }
        }
    },
    DOUBLE(4){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof Double) {
                double val = (double)value;
                res.setDouble(InGameConfig.KEY_VALUE,val);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagDouble) {
                f.setDouble(null,((NBTTagDouble) nbt).getDouble());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagDouble!");
            }
        }
    },
    DOUBLE_ARR(5){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof double[]) {
                double[] val = (double[]) value;
                res.setTag(InGameConfig.KEY_VALUE,NBTTools.createDoubleList(val));
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagList) {
                f.set(null,NBTTools.readDoubleList((NBTTagList) nbt));
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagList!");
            }
        }
    },
    CHAR(6){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof Character) {
                char val = (char)value;
                res.setShort(InGameConfig.KEY_VALUE,(short) val);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagShort) {
                f.setChar(null,(char) ((NBTTagShort) nbt).getShort());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagShort!");
            }
        }
    },
    CHAR_ARR(7){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof char[]) {
                res.setTag(InGameConfig.KEY_VALUE,new NBTTagString(String.valueOf((char[]) value)));
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagString) {
                f.set(null,((NBTTagString) nbt).getString().toCharArray());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagString!");
            }
        }
    },
    STRING(8){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof String) {
                String val = (String)value;
                res.setString(InGameConfig.KEY_VALUE,val);
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagString) {
                f.set(null,((NBTTagString) nbt).getString());
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagString!");
            }
        }
    },
    STRING_ARR(9){
        NBTTagCompound getTag(Object value) {
            NBTTagCompound res = super.getTag(value);
            if(value instanceof String[]) {
                String[] val = (String[]) value;
                res.setTag(InGameConfig.KEY_VALUE,NBTTools.createStringList(val));
            }
            return res;
        }

        void applyValue(Field f, NBTBase nbt) throws IllegalAccessException {
            if (nbt instanceof NBTTagList) {
                f.set(null,NBTTools.readStringList((NBTTagList) nbt));
            }
            else {
                BuildingGadgets.logger.warn("Unexpected "+nbt.getClass().getName()+" found which was expected to be an NBTTagList!");
            }
        }
    };
    private static Map<Byte,ConfigType> typeById = new HashMap<>();
    private byte id;
    ConfigType(int id) {
        this.id = (byte)id;
    }

    public byte getId() {
        return id;
    }

    NBTTagCompound getTag(Object value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte(InGameConfig.KEY_TYPE,id);
        return tag;
    }

    abstract void applyValue(Field f, NBTBase nbt) throws IllegalAccessException;

    @Nullable
    static NBTTagCompound getValueTag(Object value) {
        if(value instanceof Boolean) {
            return ConfigType.BOOLEAN.getTag(value);
        }
        else if(value instanceof boolean[]) {
            return ConfigType.BOOLEAN_ARR.getTag(value);
        }
        else if (value instanceof Integer) {
            return ConfigType.INT.getTag(value);
        }
        else if(value instanceof int[]) {
            return ConfigType.INT_ARR.getTag(value);
        }
        else if(value instanceof Double) {
            return ConfigType.DOUBLE.getTag(value);
        }
        else if(value instanceof double[]) {
            return ConfigType.DOUBLE_ARR.getTag(value);
        }
        else if(value instanceof Character) {
            return ConfigType.CHAR.getTag(value);
        }
        else if(value instanceof char[]) {
            return ConfigType.CHAR_ARR.getTag(value);
        }
        else if(value instanceof String) {
            return ConfigType.STRING.getTag(value);
        }
        else if(value instanceof String[]) {
            return ConfigType.STRING_ARR.getTag(value);
        } else {
            return null;
        }
    }

    static void handleValueTag(Field f, NBTTagCompound compound) {
        if (!compound.hasKey(InGameConfig.KEY_TYPE) || !compound.hasKey(InGameConfig.KEY_VALUE)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an inproperly initialised Value-NBTTagCompound!");
            return;
        }
        NBTBase value = compound.getTag(InGameConfig.KEY_VALUE);
        byte id = compound.getByte(InGameConfig.KEY_TYPE);
        ConfigType type = typeById.get(id);
        if (type==null) {
            BuildingGadgets.logger.warn("Tried to read synchronisation with an unkown type!");
            return;
        }
        try {
            f.setAccessible(true);
            type.applyValue(f,value);
        } catch (IllegalAccessException e) {
            BuildingGadgets.logger.error("Failed to apply Synchronised value to field "+f.getName()+"!");
        }
    }

    static {
        for (ConfigType t: values()) {
            assert !typeById.containsKey(t.getId()):"ID's have to be unique!";
            typeById.put(t.getId(),t);
        }
    }
}
