package com.huntercraft.huntercraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public final class ElasticAuraRenderUtil {
    private ElasticAuraRenderUtil() {
    }

    public static void renderLine(PoseStack poseStack, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float red, float green, float blue, float alpha) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float length = (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
        float nx = length <= 0.0001F ? 0.0F : dx / length;
        float ny = length <= 0.0001F ? 1.0F : dy / length;
        float nz = length <= 0.0001F ? 0.0F : dz / length;
        int r = (int) (red * 255.0F);
        int g = (int) (green * 255.0F);
        int b = (int) (blue * 255.0F);
        int a = (int) (alpha * 255.0F);

        consumer.vertex(pose, x0, y0, z0).color(r, g, b, a).normal(normal, nx, ny, nz).endVertex();
        consumer.vertex(pose, x1, y1, z1).color(r, g, b, a).normal(normal, nx, ny, nz).endVertex();
    }
}
