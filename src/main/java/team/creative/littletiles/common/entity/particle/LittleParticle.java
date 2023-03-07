package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.mixin.client.render.ParticleEngineAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleParticle extends TextureSheetParticle {
    
    private static final Minecraft mc = Minecraft.getInstance();

    public ParticleSettings settings;
    public SpriteSet sprites;
    private float scaleDeviation;
    
    public LittleParticle(ClientLevel level, Vec3d pos, Vec3d speed, ParticleSettings settings) {
        super(level, pos.x, pos.y, pos.z);
        this.xd = speed.x * (Math.random() * 0.1 + 0.95);
        this.yd = speed.y * (Math.random() * 0.1 + 0.95);
        this.zd = speed.z * (Math.random() * 0.1 + 0.95);
        this.lifetime = (int) (settings.lifetime + settings.lifetimeDeviation * Math.random());
        this.gravity = settings.gravity;
        this.alpha = ColorUtils.alphaF(settings.color);
        this.rCol = ColorUtils.redF(settings.color);
        this.gCol = ColorUtils.greenF(settings.color);
        this.bCol = ColorUtils.blueF(settings.color);
        if (settings.randomColor) {
            this.rCol *= Math.random();
            this.gCol *= Math.random();
            this.bCol *= Math.random();
        }
        this.settings = settings;
        this.sprites = ((ParticleEngineAccessor) mc.particleEngine).getSpriteSets().get(settings.texture.particleTexture);
        this.scaleDeviation = (float) (Math.random() * settings.sizeDeviation);
        settings.texture.init(this);
        this.setSize(0.2F * settings.startSize, 0.2F * settings.startSize);
    }
    
    @Override
    public void tick() {
        settings.texture.tick(this);
        this.scaleDeviation = scaleDeviation + getAge() / (float) getMaxAge() * (settings.endSize - settings.startSize) + settings.startSize;
        super.tick();
    }
    
    public int getAge() {
        return age;
    }
    
    public int getMaxAge() {
        return lifetime;
    }
    
    public void setSpriteFirst(SpriteSet set) {
        this.setSprite(set.get(0, this.lifetime));
    }
    
    public void setSpriteFromAgeReverse(SpriteSet set) {
        if (!this.removed)
            this.setSprite(set.get(this.lifetime - this.age, this.lifetime));
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return settings.texture.type;
    }

    public static class ParticleSettings {

        public float gravity = 0;
        public int color = ColorUtils.rgba(20, 20, 20, 255);
        public int lifetime = 40;
        public int lifetimeDeviation = 5;
        public float startSize = 0.4F;
        public float endSize = 0.5F;
        public float sizeDeviation = 0.04F;
        public LittleParticleTexture texture = LittleParticleTexture.dust_fade_out;
        public boolean randomColor = false;

        public ParticleSettings() {

        }

        public ParticleSettings(float gravity, int color, int lifetime, int lifetimeDeviation, float startSize, float endSize, float sizeDeviation, LittleParticleTexture texture, boolean randomColor) {
            this.gravity = gravity;
            this.color = color;
            this.lifetime = lifetime;
            this.lifetimeDeviation = lifetimeDeviation;
            this.startSize = startSize;
            this.endSize = endSize;
            this.sizeDeviation = sizeDeviation;
            this.texture = texture;
            this.randomColor = randomColor;
        }

        public ParticleSettings(CompoundTag nbt) {
            gravity = nbt.getFloat("gravity");
            color = nbt.getInt("color");
            lifetime = nbt.getInt("lifetime");
            lifetimeDeviation = nbt.getInt("lifetimeDeviation");
            startSize = nbt.getFloat("startSize");
            endSize = nbt.getFloat("endSize");
            sizeDeviation = nbt.getFloat("sizeDeviation");
            randomColor = nbt.getBoolean("randomColor");
            texture = LittleParticleTexture.get(nbt.getString("texture"));
        }

        public void write(CompoundTag nbt) {
            nbt.putFloat("gravity", gravity);
            nbt.putInt("color", color);
            nbt.putInt("lifetime", lifetime);
            nbt.putInt("lifetimeDeviation", lifetimeDeviation);
            nbt.putFloat("startSize", startSize);
            nbt.putFloat("endSize", endSize);
            nbt.putFloat("sizeDeviation", sizeDeviation);
            nbt.putString("texture", texture.name());
            nbt.putBoolean("randomColor", randomColor);
        }

        public ParticleSettings copy() {
            return new ParticleSettings(gravity, color, lifetime, lifetimeDeviation, startSize, endSize, sizeDeviation, texture, randomColor);
        }
    }
}
