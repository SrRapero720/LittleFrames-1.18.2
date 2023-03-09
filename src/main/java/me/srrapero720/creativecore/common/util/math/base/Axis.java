package me.srrapero720.creativecore.common.util.math.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;

public enum Axis {
    X {
        public double get(double x, double y, double z) {
            return x;
        }

        public float get(float x, float y, float z) {
            return x;
        }

        public int get(int x, int y, int z) {
            return x;
        }

        public int get(BlockPos pos) {
            return pos.getX();
        }

        public int get(ChunkPos pos) {
            return pos.x;
        }

        public <T> T get(T x, T y, T z) {
            return x;
        }

        public void set(BlockPos.MutableBlockPos pos, int value) {
            pos.setX(value);
        }

        public Axis one() {
            return Axis.Y;
        }

        public Axis two() {
            return Axis.Z;
        }

        public Facing facing(boolean positive) {
            return positive ? Facing.EAST : Facing.WEST;
        }

        public Direction.Axis toVanilla() {
            return net.minecraft.core.Direction.Axis.X;
        }

        public Vec3i mirror(Vec3i vec) {
            return new Vec3i(-vec.getX(), vec.getY(), vec.getZ());
        }

        public BlockPos mirror(BlockPos vec) {
            return new BlockPos(-vec.getX(), vec.getY(), vec.getZ());
        }
    },
    Y {
        public double get(double x, double y, double z) {
            return y;
        }

        public float get(float x, float y, float z) {
            return y;
        }

        public int get(int x, int y, int z) {
            return y;
        }

        public int get(BlockPos pos) {
            return pos.getY();
        }

        public int get(ChunkPos pos) {
            throw new UnsupportedOperationException();
        }

        public <T> T get(T x, T y, T z) {
            return y;
        }

        public void set(BlockPos.MutableBlockPos pos, int value) {
            pos.setY(value);
        }

        public Axis one() {
            return Axis.Z;
        }

        public Axis two() {
            return Axis.X;
        }

        public Facing facing(boolean positive) {
            return positive ? Facing.UP : Facing.DOWN;
        }

        public Direction.Axis toVanilla() {
            return net.minecraft.core.Direction.Axis.Y;
        }

        public Vec3i mirror(Vec3i vec) {
            return new Vec3i(vec.getX(), -vec.getY(), vec.getZ());
        }

        public BlockPos mirror(BlockPos vec) {
            return new BlockPos(vec.getX(), -vec.getY(), vec.getZ());
        }
    },
    Z {
        public double get(double x, double y, double z) {
            return z;
        }

        public float get(float x, float y, float z) {
            return z;
        }

        public int get(int x, int y, int z) {
            return z;
        }

        public int get(BlockPos pos) {
            return pos.getZ();
        }

        public int get(ChunkPos pos) {
            return pos.z;
        }

        public <T> T get(T x, T y, T z) {
            return z;
        }

        public void set(BlockPos.MutableBlockPos pos, int value) {
            pos.setZ(value);
        }

        public Axis one() {
            return Axis.X;
        }

        public Axis two() {
            return Axis.Y;
        }

        public Facing facing(boolean positive) {
            return positive ? Facing.SOUTH : Facing.NORTH;
        }

        public Direction.Axis toVanilla() {
            return net.minecraft.core.Direction.Axis.Z;
        }

        public Vec3i mirror(Vec3i vec) {
            return new Vec3i(vec.getX(), vec.getY(), -vec.getZ());
        }

        public BlockPos mirror(BlockPos vec) {
            return new BlockPos(vec.getX(), vec.getY(), -vec.getZ());
        }
    };

    private Axis() {
    }

    public static Axis get(Direction.Axis axis) {
        switch (axis) {
            case X:
                return X;
            case Y:
                return Y;
            case Z:
                return Z;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Axis third(Axis one, Axis two) {
        switch (one) {
            case X:
                if (two == Y) {
                    return Z;
                }

                return Y;
            case Y:
                if (two == X) {
                    return Z;
                }

                return X;
            case Z:
                if (two == Y) {
                    return X;
                }

                return Y;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Axis getMirrorAxis(Mirror mirrorIn) {
        switch (mirrorIn) {
            case FRONT_BACK:
                return X;
            case LEFT_RIGHT:
                return Z;
            default:
                return null;
        }
    }

    public abstract Axis one();

    public abstract Axis two();

    public abstract Facing facing(boolean var1);

    public abstract double get(double var1, double var3, double var5);

    public abstract float get(float var1, float var2, float var3);

    public abstract int get(int var1, int var2, int var3);

    public abstract int get(BlockPos pos);

    public abstract int get(ChunkPos pos);

    public abstract <T> T get(T var1, T var2, T var3);

    public abstract void set(BlockPos.MutableBlockPos pos, int value);

    public abstract Direction.Axis toVanilla();

    public Facing mirror(Facing facing) {
        return facing.axis == this ? facing.opposite() : facing;
    }

    public Direction mirror(Direction facing) {
        return facing.getAxis() == this.toVanilla() ? facing.getOpposite() : facing;
    }

    public abstract Vec3i mirror(Vec3i var1);

    public abstract BlockPos mirror(BlockPos var1);

    public void mirror(Vec3d vec) {
        vec.set(this, -vec.get(this));
    }

    public void mirror(Vec3f vec) {
        vec.set(this, -vec.get(this));
    }
}