package com.huntercraft.huntercraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SmokyJailSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseQuadSize;
    private final float curlSpeed;

    protected SmokyJailSmokeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.84F;
        this.gravity = -0.001F;
        this.xd = xd * 0.18D + ((this.random.nextDouble() - 0.5D) * 0.01D);
        this.yd = yd * 0.12D + 0.004D + (this.random.nextDouble() * 0.008D);
        this.zd = zd * 0.18D + ((this.random.nextDouble() - 0.5D) * 0.01D);
        this.baseQuadSize = 0.78F + (this.random.nextFloat() * 0.48F);
        this.quadSize = this.baseQuadSize;
        this.lifetime = 92 + this.random.nextInt(42);
        this.hasPhysics = false;
        this.curlSpeed = 0.006F + (this.random.nextFloat() * 0.009F);
        float tone = 0.82F + (this.random.nextFloat() * 0.14F);
        this.rCol = tone;
        this.gCol = tone;
        this.bCol = tone;
        this.alpha = 1.0F;
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2.0F);
        this.oRoll = this.roll;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        float ageProgress = (float) this.age / this.lifetime;
        float fadeIn = Mth.clamp(ageProgress / 0.08F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - ageProgress) / 0.22F, 0.0F, 1.0F);
        this.alpha = Mth.clamp(1.35F * fadeIn * fadeOut, 0.0F, 1.0F);
        this.quadSize = this.baseQuadSize * (1.0F + ageProgress * 0.85F);

        double curl = Math.sin((this.age * 0.2D) + this.curlSpeed * 80.0F) * 0.0008D;
        double cross = Math.cos((this.age * 0.18D) + this.curlSpeed * 60.0F) * 0.0008D;
        this.xd += curl;
        this.zd += cross;
        this.yd += 0.00012D;
        this.roll += this.curlSpeed;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new SmokyJailSmokeParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
