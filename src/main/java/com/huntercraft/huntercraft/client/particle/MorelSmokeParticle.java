package com.huntercraft.huntercraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class MorelSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseQuadSize;
    private final float curlSpeed;

    protected MorelSmokeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.91F;
        this.gravity = -0.0025F;
        this.xd = xd * 0.35D + ((this.random.nextDouble() - 0.5D) * 0.018D);
        this.yd = yd * 0.3D + 0.012D + (this.random.nextDouble() * 0.018D);
        this.zd = zd * 0.35D + ((this.random.nextDouble() - 0.5D) * 0.018D);
        this.baseQuadSize = 0.38F + (this.random.nextFloat() * 0.28F);
        this.quadSize = this.baseQuadSize;
        this.lifetime = 52 + this.random.nextInt(34);
        this.hasPhysics = false;
        this.curlSpeed = 0.012F + (this.random.nextFloat() * 0.014F);
        float tone = 0.88F + (this.random.nextFloat() * 0.10F);
        this.rCol = tone;
        this.gCol = tone;
        this.bCol = tone;
        this.alpha = 0.0F;
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
        float fadeIn = Mth.clamp(ageProgress / 0.18F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - ageProgress) / 0.62F, 0.0F, 1.0F);
        this.alpha = 0.92F * fadeIn * fadeOut;
        this.quadSize = this.baseQuadSize * (1.0F + ageProgress * 1.7F);

        double curl = Math.sin((this.age * 0.32D) + this.curlSpeed * 80.0F) * 0.0018D;
        double cross = Math.cos((this.age * 0.27D) + this.curlSpeed * 60.0F) * 0.0018D;
        this.xd += curl;
        this.zd += cross;
        this.yd += 0.00045D;
        this.roll += this.curlSpeed;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new MorelSmokeParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
