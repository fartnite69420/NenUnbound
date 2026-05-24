package com.huntercraft.huntercraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ElasticReflectParticle extends TextureSheetParticle {
    protected ElasticReflectParticle(ClientLevel level, double x, double y, double z, double red, double green, double blue, double scale, SpriteSet sprites) {
        super(level, x, y, z, (level.random.nextDouble() - 0.5D) * 0.03D, level.random.nextDouble() * 0.025D, (level.random.nextDouble() - 0.5D) * 0.03D);
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 12 + this.random.nextInt(6);
        this.quadSize = (float) (0.22F * Mth.clamp(scale, 0.65D, 1.3D));
        this.rCol = (float) Mth.clamp(red, 0.0D, 1.0D);
        this.gCol = (float) Mth.clamp(green, 0.0D, 1.0D);
        this.bCol = (float) Mth.clamp(blue, 0.0D, 1.0D);
        this.alpha = 0.88F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0.0F, 0.88F * (1.0F - progress));
        this.quadSize *= 0.985F;
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double red, double green, double blue) {
            return new ElasticReflectParticle(level, x, y, z, red, green, blue, 1.0D, this.sprites);
        }
    }
}
