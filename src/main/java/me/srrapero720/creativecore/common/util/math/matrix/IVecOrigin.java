//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.srrapero720.creativecore.common.util.math.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import me.srrapero720.creativecore.common.util.math.base.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.creativecore.common.util.math.matrix.Matrix3;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public interface IVecOrigin {
    double offX();

    double offY();

    double offZ();

    double rotX();

    double rotY();

    double rotZ();

    double offXLast();

    double offYLast();

    double offZLast();

    double rotXLast();

    double rotYLast();

    double rotZLast();

    boolean isRotated();

    void offX(double var1);

    void offY(double var1);

    void offZ(double var1);

    void off(double var1, double var3, double var5);

    void rotX(double var1);

    void rotY(double var1);

    void rotZ(double var1);

    void rot(double var1, double var3, double var5);

    Vec3d center();

    void setCenter(Vec3d var1);

    Matrix3 rotation();

    Matrix3 rotationInv();

    Vec3d translation();

    void tick();

    IVecOrigin getParent();

    default double translationCombined(Axis axis) {
        return this.translation().get(axis);
    }

    default void onlyRotateWithoutCenter(Vec3d vec) {
        this.rotation().transform(vec);
    }

    default BlockPos transformPointToWorld(BlockPos pos) {
        Vec3d vec = new Vec3d(pos);
        this.transformPointToWorld(vec);
        return vec.toBlockPos();
    }

    default BlockPos transformPointToFakeWorld(BlockPos pos) {
        Vec3d vec = new Vec3d(pos);
        this.transformPointToFakeWorld(vec);
        return vec.toBlockPos();
    }

    default void transformPointToWorld(Vec3d vec) {
        vec.sub(this.center());
        this.rotation().transform(vec);
        vec.add(this.center());
        vec.add(this.translation());
    }

    default void transformPointToFakeWorld(Vec3d vec) {
        vec.sub(this.translation());
        vec.sub(this.center());
        this.rotationInv().transform(vec);
        vec.add(this.center());
    }

    default Vector3d transformPointToWorld(Vector3d vec) {
        Vec3d real = new Vec3d(vec);
        this.transformPointToWorld(real);
        return new Vector3d(real.x, real.y, real.z);
    }

    default Vector3d transformPointToFakeWorld(Vector3d vec) {
        Vec3d real = new Vec3d(vec);
        this.transformPointToFakeWorld(real);
        return new Vector3d(real.x, real.y, real.z);
    }

    default Vec3 transformPointToWorld(Vec3 vec) {
        Vec3d real = new Vec3d(vec);
        this.transformPointToWorld(real);
        return new Vec3(real.x, real.y, real.z);
    }

    default Vec3 transformPointToFakeWorld(Vec3 vec) {
        Vec3d real = new Vec3d(vec);
        this.transformPointToFakeWorld(real);
        return new Vec3(real.x, real.y, real.z);
    }

    default AABB getAxisAlignedBox(AABB box) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -1.7976931348623157E308;
        double maxY = -1.7976931348623157E308;
        double maxZ = -1.7976931348623157E308;

        for(int i = 0; i < BoxCorner.values().length; ++i) {
            Vec3d vec = BoxCorner.values()[i].get(box);
            this.transformPointToWorld(vec);
            minX = Math.min(minX, vec.x);
            minY = Math.min(minY, vec.y);
            minZ = Math.min(minZ, vec.z);
            maxX = Math.max(maxX, vec.x);
            maxY = Math.max(maxY, vec.y);
            maxZ = Math.max(maxZ, vec.z);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    default OBB getOrientatedBox(AABB box) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -1.7976931348623157E308;
        double maxY = -1.7976931348623157E308;
        double maxZ = -1.7976931348623157E308;

        for(int i = 0; i < BoxCorner.values().length; ++i) {
            Vec3d vec = BoxCorner.values()[i].get(box);
            this.transformPointToFakeWorld(vec);
            minX = Math.min(minX, vec.x);
            minY = Math.min(minY, vec.y);
            minZ = Math.min(minZ, vec.z);
            maxX = Math.max(maxX, vec.x);
            maxY = Math.max(maxY, vec.y);
            maxZ = Math.max(maxZ, vec.z);
        }

        return new OBB(this, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @OnlyIn(Dist.CLIENT)
    default void setupRenderingInternal(PoseStack matrixStack, Entity entity, float partialTicks) {
        double rotX = this.rotXLast() + (this.rotX() - this.rotXLast()) * (double)partialTicks;
        double rotY = this.rotYLast() + (this.rotY() - this.rotYLast()) * (double)partialTicks;
        double rotZ = this.rotZLast() + (this.rotZ() - this.rotZLast()) * (double)partialTicks;
        double offX = this.offXLast() + (this.offX() - this.offXLast()) * (double)partialTicks;
        double offY = this.offYLast() + (this.offY() - this.offYLast()) * (double)partialTicks;
        double offZ = this.offZLast() + (this.offZ() - this.offZLast()) * (double)partialTicks;
        Vec3d rotationCenter = this.center();
        matrixStack.translate(offX, offY, offZ);
        matrixStack.translate(rotationCenter.x, rotationCenter.y, rotationCenter.z);
        GL11.glRotated(rotX, 1.0, 0.0, 0.0);
        GL11.glRotated(rotY, 0.0, 1.0, 0.0);
        GL11.glRotated(rotZ, 0.0, 0.0, 1.0);
        matrixStack.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
    }

    @OnlyIn(Dist.CLIENT)
    default void setupRendering(PoseStack matrixStack, Entity entity, float partialTicks) {
        this.setupRenderingInternal(matrixStack, entity, partialTicks);
    }

    default boolean hasChanged() {
        return this.offXLast() != this.offX() || this.offYLast() != this.offY() || this.offZLast() != this.offZ() || this.rotXLast() != this.rotX() || this.rotYLast() != this.rotY() || this.rotZLast() != this.rotZ();
    }

    public default AABB getAABB(AABB box) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        for (int i = 0; i < BoxCorner.values().length; i++) {
            Vec3d vec = BoxCorner.values()[i].get(box);

            transformPointToWorld(vec);

            minX = Math.min(minX, vec.x);
            minY = Math.min(minY, vec.y);
            minZ = Math.min(minZ, vec.z);
            maxX = Math.max(maxX, vec.x);
            maxY = Math.max(maxY, vec.y);
            maxZ = Math.max(maxZ, vec.z);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
