package com.direwolf20.buildinggadgets.common.network.login;

import net.minecraft.network.PacketBuffer;

import java.util.function.IntSupplier;

public class LoginIndexedMessage implements IntSupplier {
    private int index;

    public LoginIndexedMessage() {
        this.index = 0;
    }

    @Override
    public int getAsInt() {
        return getIndex();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void encode(PacketBuffer buffer) {
    }
}
