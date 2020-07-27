package com.direwolf20.buildinggadgets.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.vector.Matrix4f;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class DireVertexBuffer implements AutoCloseable {

    private int glBufferId;
    private final VertexFormat vertexFormat;
    private int count;

    public DireVertexBuffer(VertexFormat vertexFormatIn) {
        this.vertexFormat = vertexFormatIn;
        RenderSystem.glGenBuffers((p_227876_1_) -> {
            this.glBufferId = p_227876_1_;
        });
    }

    public void bindBuffer() {
        RenderSystem.glBindBuffer(34962, () -> this.glBufferId);
    }

    public void upload(DireBufferBuilder bufferIn) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                this.uploadRaw(bufferIn);
            });
        } else {
            this.uploadRaw(bufferIn);
        }

    }

    public CompletableFuture<Void> uploadLater(DireBufferBuilder bufferIn) {
        if (!RenderSystem.isOnRenderThread()) {
            return CompletableFuture.runAsync(() -> {
                this.uploadRaw(bufferIn);
            }, (p_227877_0_) -> {
                RenderSystem.recordRenderCall(p_227877_0_::run);
            });
        } else {
            this.uploadRaw(bufferIn);
            return CompletableFuture.completedFuture((Void) null);
        }
    }

    private void uploadRaw(DireBufferBuilder bufferIn) {
        Pair<DireBufferBuilder.DrawState, ByteBuffer> pair = bufferIn.getNextBuffer();
        if (this.glBufferId != -1) {
            ByteBuffer bytebuffer = pair.getSecond();
            this.count = bytebuffer.remaining() / this.vertexFormat.getSize();
            this.bindBuffer();
            RenderSystem.glBufferData(34962, bytebuffer, 35044);
            unbindBuffer();
        }
    }

    public void draw(Matrix4f matrixIn, int modeIn) {
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(matrixIn);
        RenderSystem.drawArrays(modeIn, 0, this.count);
        RenderSystem.popMatrix();
    }

    public static void unbindBuffer() {
        RenderSystem.glBindBuffer(34962, () -> 0);
    }

    public void close() {
        if (this.glBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.glBufferId);
            this.glBufferId = -1;
        }

    }
}