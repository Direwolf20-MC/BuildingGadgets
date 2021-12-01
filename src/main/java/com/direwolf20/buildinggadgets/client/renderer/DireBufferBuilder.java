package com.direwolf20.buildinggadgets.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrays;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.BitSet;
import java.util.List;

public class DireBufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer byteBuffer;
    private final List<DireBufferBuilder.DrawState> drawStates = Lists.newArrayList();
    private int drawStateIndex = 0;
    private int renderedBytes = 0;
    private int nextElementBytes = 0;
    private int uploadedBytes = 0;
    private int vertexCount;
    @Nullable
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;
    private int drawMode;
    private VertexFormat vertexFormat;
    private boolean fastFormat;
    private boolean fullFormat;
    private boolean isDrawing;

    public DireBufferBuilder(int bufferSizeIn) {
        this.byteBuffer = MemoryTracker.create(bufferSizeIn * 4); // TODO: might need to remove or change to 6
    }

    protected void growBuffer() {
        this.growBuffer(this.vertexFormat.getVertexSize());
    }

    private void growBuffer(int increaseAmount) {
        if (this.nextElementBytes + increaseAmount > this.byteBuffer.capacity()) {
            int i = this.byteBuffer.capacity();
            int j = i + roundUpPositive(increaseAmount);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
            ByteBuffer bytebuffer = MemoryTracker.create(j);
            this.byteBuffer.position(0);
            bytebuffer.put(this.byteBuffer);
            bytebuffer.rewind();
            this.byteBuffer = bytebuffer;
        }
    }

    private static int roundUpPositive(int xIn) {
        int i = 2097152;
        if (xIn == 0) {
            return i;
        } else {
            if (xIn < 0) {
                i *= -1;
            }

            int j = xIn % i;
            return j == 0 ? xIn : xIn + i - j;
        }
    }

    public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
        this.byteBuffer.clear();
        FloatBuffer floatbuffer = this.byteBuffer.asFloatBuffer();
        int i = this.vertexCount / 4;
        float[] afloat = new float[i];

        for (int j = 0; j < i; ++j) {
            afloat[j] = getDistanceSq(floatbuffer, cameraX, cameraY, cameraZ, this.vertexFormat.getIntegerSize(), this.renderedBytes / 4 + j * this.vertexFormat.getVertexSize());
        }

        int[] aint = new int[i];

        for (int k = 0; k < aint.length; aint[k] = k++) {
            ;
        }

        IntArrays.mergeSort(aint, (p_227830_1_, p_227830_2_) -> {
            return Floats.compare(afloat[p_227830_1_], afloat[p_227830_2_]);
        });
        BitSet bitset = new BitSet();
        FloatBuffer floatbuffer1 = FloatBuffer.allocate(this.vertexFormat.getIntegerSize() * 6);

        for (int l = bitset.nextClearBit(0); l < aint.length; l = bitset.nextClearBit(l + 1)) {
            int i1 = aint[l];
            if (i1 != l) {
                this.limitToVertex(floatbuffer, i1);
                floatbuffer1.clear();
                floatbuffer1.put(floatbuffer);
                int j1 = i1;

                for (int k1 = aint[i1]; j1 != l; k1 = aint[k1]) {
                    this.limitToVertex(floatbuffer, k1);
                    FloatBuffer floatbuffer2 = floatbuffer.slice();
                    this.limitToVertex(floatbuffer, j1);
                    floatbuffer.put(floatbuffer2);
                    bitset.set(j1);
                    j1 = k1;
                }

                this.limitToVertex(floatbuffer, l);
                floatbuffer1.flip();
                floatbuffer.put(floatbuffer1);
            }

            bitset.set(l);
        }
    }

    private void limitToVertex(FloatBuffer floatBufferIn, int indexIn) {
        int i = this.vertexFormat.getIntegerSize() * 4;
        floatBufferIn.limit(this.renderedBytes / 4 + (indexIn + 1) * i);
        floatBufferIn.position(this.renderedBytes / 4 + indexIn * i);
    }

    public DireBufferBuilder.State getVertexState() {
        this.byteBuffer.limit(this.nextElementBytes);
        this.byteBuffer.position(this.renderedBytes);
        ByteBuffer bytebuffer = ByteBuffer.allocate(this.vertexCount * this.vertexFormat.getVertexSize());
        bytebuffer.put(this.byteBuffer);
        this.byteBuffer.clear();
        return new DireBufferBuilder.State(bytebuffer, this.vertexFormat);
    }

    private static float getDistanceSq(FloatBuffer floatBufferIn, float x, float y, float z, int integerSize, int offset) {
        float f = floatBufferIn.get(offset + integerSize * 0 + 0);
        float f1 = floatBufferIn.get(offset + integerSize * 0 + 1);
        float f2 = floatBufferIn.get(offset + integerSize * 0 + 2);
        float f3 = floatBufferIn.get(offset + integerSize * 1 + 0);
        float f4 = floatBufferIn.get(offset + integerSize * 1 + 1);
        float f5 = floatBufferIn.get(offset + integerSize * 1 + 2);
        float f6 = floatBufferIn.get(offset + integerSize * 2 + 0);
        float f7 = floatBufferIn.get(offset + integerSize * 2 + 1);
        float f8 = floatBufferIn.get(offset + integerSize * 2 + 2);
        float f9 = floatBufferIn.get(offset + integerSize * 3 + 0);
        float f10 = floatBufferIn.get(offset + integerSize * 3 + 1);
        float f11 = floatBufferIn.get(offset + integerSize * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - x;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - y;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - z;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    public void setVertexState(DireBufferBuilder.State state) {
        state.stateByteBuffer.clear();
        int i = state.stateByteBuffer.capacity();
        this.growBuffer(i);
        this.byteBuffer.limit(this.byteBuffer.capacity());
        this.byteBuffer.position(this.renderedBytes);
        this.byteBuffer.put(state.stateByteBuffer);
        this.byteBuffer.clear();
        VertexFormat vertexformat = state.stateVertexFormat;
        this.setVertexFormat(vertexformat);
        this.vertexCount = i / vertexformat.getVertexSize();
        this.nextElementBytes = this.renderedBytes + this.vertexCount * vertexformat.getVertexSize();
    }

    public void begin(int glMode, VertexFormat format) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            this.isDrawing = true;
            this.drawMode = glMode;
            this.setVertexFormat(format);
            this.vertexFormatElement = format.getElements().get(0);
            this.vertexFormatIndex = 0;
            this.byteBuffer.clear();
        }
    }

    private void setVertexFormat(VertexFormat vertexFormatIn) {
        if (this.vertexFormat != vertexFormatIn) {
            this.vertexFormat = vertexFormatIn;
            boolean flag = vertexFormatIn == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP; //POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
            boolean flag1 = vertexFormatIn == DefaultVertexFormat.BLOCK;
            this.fastFormat = flag || flag1;
            this.fullFormat = flag;
        }
    }

    public void finishDrawing() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            this.isDrawing = false;
            this.drawStates.add(new DireBufferBuilder.DrawState(this.vertexFormat, this.vertexCount, this.drawMode));
            this.renderedBytes += this.vertexCount * this.vertexFormat.getVertexSize();
            this.vertexCount = 0;
            this.vertexFormatElement = null;
            this.vertexFormatIndex = 0;
        }
    }

    public void putByte(int indexIn, byte byteIn) {
        this.byteBuffer.put(this.nextElementBytes + indexIn, byteIn);
    }

    public void putShort(int indexIn, short shortIn) {
        this.byteBuffer.putShort(this.nextElementBytes + indexIn, shortIn);
    }

    public void putFloat(int indexIn, float floatIn) {
        this.byteBuffer.putFloat(this.nextElementBytes + indexIn, floatIn);
    }

    public void endVertex() {
        if (this.vertexFormatIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        } else {
            ++this.vertexCount;
            this.growBuffer();
        }
    }

    public void nextElement() {
        ImmutableList<VertexFormatElement> immutablelist = this.vertexFormat.getElements();
        this.vertexFormatIndex = (this.vertexFormatIndex + 1) % immutablelist.size();
        this.nextElementBytes += this.vertexFormatElement.getByteSize();
        VertexFormatElement vertexformatelement = immutablelist.get(this.vertexFormatIndex);
        this.vertexFormatElement = vertexformatelement;
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextElement();
        }

        if (this.defaultColorSet && this.vertexFormatElement.getUsage() == VertexFormatElement.Usage.COLOR) {
            BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
        }

    }

    public VertexConsumer color(int red, int green, int blue, int alpha) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else {
            return BufferVertexConsumer.super.color(red, green, blue, alpha);
        }
    }

    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else if (this.fastFormat) {
            this.putFloat(0, x);
            this.putFloat(4, y);
            this.putFloat(8, z);
            this.putByte(12, (byte) ((int) (red * 255.0F)));
            this.putByte(13, (byte) ((int) (green * 255.0F)));
            this.putByte(14, (byte) ((int) (blue * 255.0F)));
            this.putByte(15, (byte) ((int) (alpha * 255.0F)));
            this.putFloat(16, texU);
            this.putFloat(20, texV);
            int i;
            if (this.fullFormat) {
                this.putShort(24, (short) (overlayUV & '\uffff'));
                this.putShort(26, (short) (overlayUV >> 16 & '\uffff'));
                i = 28;
            } else {
                i = 24;
            }

            this.putShort(i + 0, (short) (lightmapUV & '\uffff'));
            this.putShort(i + 2, (short) (lightmapUV >> 16 & '\uffff'));
            this.putByte(i + 4, BufferVertexConsumer.normalIntValue(normalX)); // @mcp: normalIntValue = normalInt
            this.putByte(i + 5, BufferVertexConsumer.normalIntValue(normalY));
            this.putByte(i + 6, BufferVertexConsumer.normalIntValue(normalZ));
            this.nextElementBytes += i + 8;
            this.endVertex();
        } else {
            super.vertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
        }
    }

    public Pair<DireBufferBuilder.DrawState, ByteBuffer> getNextBuffer() {
        DireBufferBuilder.DrawState bufferbuilder$drawstate = this.drawStates.get(this.drawStateIndex++);
        this.byteBuffer.position(this.uploadedBytes);
        this.uploadedBytes += bufferbuilder$drawstate.getVertexCount() * bufferbuilder$drawstate.getFormat().getVertexSize();
        this.byteBuffer.limit(this.uploadedBytes);
        if (this.drawStateIndex == this.drawStates.size() && this.vertexCount == 0) {
            this.reset();
        }

        ByteBuffer bytebuffer = this.byteBuffer.slice();
        this.byteBuffer.clear();
        return Pair.of(bufferbuilder$drawstate, bytebuffer);
    }

    public void reset() {
        if (this.renderedBytes != this.uploadedBytes) {
            LOGGER.warn("Bytes mismatch " + this.renderedBytes + " " + this.uploadedBytes);
        }

        this.discard();
    }

    public void discard() {
        this.renderedBytes = 0;
        this.uploadedBytes = 0;
        this.nextElementBytes = 0;
        this.drawStates.clear();
        this.drawStateIndex = 0;
    }

    public VertexFormatElement currentElement() {
        if (this.vertexFormatElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        } else {
            return this.vertexFormatElement;
        }
    }

    public boolean isDrawing() {
        return this.isDrawing;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class DrawState {
        private final VertexFormat format;
        private final int vertexCount;
        private final int drawMode;

        private DrawState(VertexFormat formatIn, int vertexCountIn, int drawModeIn) {
            this.format = formatIn;
            this.vertexCount = vertexCountIn;
            this.drawMode = drawModeIn;
        }

        public VertexFormat getFormat() {
            return this.format;
        }

        public int getVertexCount() {
            return this.vertexCount;
        }

        public int getDrawMode() {
            return this.drawMode;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        private final ByteBuffer stateByteBuffer;
        private final VertexFormat stateVertexFormat;

        private State(ByteBuffer byteBufferIn, VertexFormat vertexFormatIn) {
            this.stateByteBuffer = byteBufferIn;
            this.stateVertexFormat = vertexFormatIn;
        }
    }

    public void putBulkData(ByteBuffer buffer) {
        growBuffer(buffer.limit() + this.vertexFormat.getVertexSize());
        this.byteBuffer.position(this.vertexCount * this.vertexFormat.getVertexSize());
        this.byteBuffer.put(buffer);
        this.vertexCount += buffer.limit() / this.vertexFormat.getVertexSize();
    }

    // Forge start
    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }
}
