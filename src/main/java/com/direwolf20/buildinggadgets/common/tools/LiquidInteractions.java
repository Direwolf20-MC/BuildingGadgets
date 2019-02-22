package com.direwolf20.buildinggadgets.common.tools;

public class LiquidInteractions {
    public enum GadgetGeneric {
        IGNORE, REPLACE_TOP;

        public static byte getID(LiquidInteractions.GadgetGeneric mode) {
            switch (mode) {
                case IGNORE:
                    return (byte) 0;
                case REPLACE_TOP:
                    return (byte) 1;
                default:
                    return (byte) 0;
            }
        }
        public static LiquidInteractions.GadgetGeneric idToEnum(byte id) {
            switch (id) {
                case 0:
                    return IGNORE;
                case 1:
                    return REPLACE_TOP;
                default:
                    return IGNORE;
            }
        }
    }
    public enum GadgetBuilding {
        IGNORE, REPLACE_TOP, PLACE_ABOVE;

        public static byte getID(LiquidInteractions.GadgetBuilding mode) {
            switch(mode) {
                case IGNORE:
                    return (byte)0;
                case REPLACE_TOP:
                    return (byte)1;
                 case PLACE_ABOVE:
                     return (byte)2;
                 default:
                     return (byte)0;
            }
        };
        public static LiquidInteractions.GadgetBuilding idToEnum(byte id) {
            switch(id) {
                case 0:
                    return IGNORE;
                case 1:
                    return REPLACE_TOP;
                case 2:
                    return PLACE_ABOVE;
                default:
                    return IGNORE;
            }
        }
    }
}
