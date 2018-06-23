package com.direwolf20.buildinggadgets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderHelper {
        public static void drawBeam(Vector S, Vector E, Vector P, float width) {
            Vector PS = Sub(S, P);
            Vector SE = Sub(E, S);

            Vector normal = Cross(PS, SE);
            normal = normal.normalize();

            Vector half = Mul(normal, width);
            Vector p1 = Add(S, half);
            Vector p2 = Sub(S, half);
            Vector p3 = Add(E, half);
            Vector p4 = Sub(E, half);

            drawQuad(Tessellator.getInstance(), p1, p3, p4, p2);
        }

        public static void renderBillboardQuadBright(double scale) {
            int brightness = 240;
            int b1 = brightness >> 16 & 65535;
            int b2 = brightness & 65535;
            GlStateManager.pushMatrix();
            RenderHelper.rotateToPlayer();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
            buffer.pos(-scale, -scale, 0.0D).tex(0.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(-scale, scale, 0.0D).tex(0.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(scale, scale, 0.0D).tex(1.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(scale, -scale, 0.0D).tex(1.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        public static void rotateToPlayer() {
            GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        }

        private static void drawQuad(Tessellator tessellator, Vector p1, Vector p2, Vector p3, Vector p4) {
            int brightness = 240;
            int b1 = brightness >> 16 & 65535;
            int b2 = brightness & 65535;

            BufferBuilder buffer = tessellator.getBuffer();
            buffer.pos(p1.getX(), p1.getY(), p1.getZ()).tex(0.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(p2.getX(), p2.getY(), p2.getZ()).tex(1.0D, 0.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(p3.getX(), p3.getY(), p3.getZ()).tex(1.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
            buffer.pos(p4.getX(), p4.getY(), p4.getZ()).tex(0.0D, 1.0D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        }

        public static class Vector {
            public final float x;
            public final float y;
            public final float z;

            public Vector(float x, float y, float z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            public float getX() {
                return x;
            }

            public float getY() {
                return y;
            }

            public float getZ() {
                return z;
            }

            public float norm() {
                return (float) Math.sqrt(x * x + y * y + z * z);
            }

            public Vector normalize() {
                float n = norm();
                return new Vector(x / n, y / n, z / n);
            }
        }

        private static Vector Cross(Vector a, Vector b) {
            float x = a.y * b.z - a.z * b.y;
            float y = a.z * b.x - a.x * b.z;
            float z = a.x * b.y - a.y * b.x;
            return new Vector(x, y, z);
        }

        private static Vector Sub(Vector a, Vector b) {
            return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
        }

        private static Vector Add(Vector a, Vector b) {
            return new Vector(a.x + b.x, a.y + b.y, a.z + b.z);
        }

        private static Vector Mul(Vector a, float f) {
            return new Vector(a.x * f, a.y * f, a.z * f);
        }

        public static void renderHighLightedBlocksOutline(BufferBuilder buffer, float mx, float my, float mz, float r, float g, float b, float a) {
            buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();

            buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();

            buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();

            buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
            buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
        }


    }
