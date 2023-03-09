package me.srrapero720.creativecore.client.render.box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;

import com.mojang.math.Vector3d;
import me.srrapero720.creativecore.client.render.face.FRenderBoxFace;
import team.creative.creativecore.client.render.model.CreativeBakedQuad;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import java.util.Random;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.geo.NormalPlane;
import team.creative.creativecore.common.util.math.geo.Ray2d;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;

@OnlyIn(value = Dist.CLIENT)
public class FRenderBox extends RenderBox {
    
    private static final VectorFan DOWN = new VectorFanSimple(new Vec3f[] { new Vec3f(0, 0, 1), new Vec3f(0, 0, 0), new Vec3f(1, 0, 0), new Vec3f(1, 0, 1) });
    private static final VectorFan UP = new VectorFanSimple(new Vec3f[] { new Vec3f(0, 1, 0), new Vec3f(0, 1, 1), new Vec3f(1, 1, 1), new Vec3f(1, 1, 0) });
    private static final VectorFan NORTH = new VectorFanSimple(new Vec3f[] { new Vec3f(1, 1, 0), new Vec3f(1, 0, 0), new Vec3f(0, 0, 0), new Vec3f(0, 1, 0) });
    private static final VectorFan SOUTH = new VectorFanSimple(new Vec3f[] { new Vec3f(0, 1, 1), new Vec3f(0, 0, 1), new Vec3f(1, 0, 1), new Vec3f(1, 1, 1) });
    private static final VectorFan WEST = new VectorFanSimple(new Vec3f[] { new Vec3f(0, 1, 0), new Vec3f(0, 0, 0), new Vec3f(0, 0, 1), new Vec3f(0, 1, 1) });
    private static final VectorFan EAST = new VectorFanSimple(new Vec3f[] { new Vec3f(1, 1, 1), new Vec3f(1, 0, 1), new Vec3f(1, 0, 0), new Vec3f(1, 1, 0) });
    
    public BlockState state;
    public int color = -1;
    
    public boolean keepVU = false;
    public boolean allowOverlap = false;
    public boolean doesNeedQuadUpdate = true;
    public boolean needsResorting = false;
    public boolean emissive = false;
    
    private FRenderBoxFace renderEast = FRenderBoxFace.RENDER;
    private FRenderBoxFace renderWest = FRenderBoxFace.RENDER;
    private FRenderBoxFace renderUp = FRenderBoxFace.RENDER;
    private FRenderBoxFace renderDown = FRenderBoxFace.RENDER;
    private FRenderBoxFace renderSouth = FRenderBoxFace.RENDER;
    private FRenderBoxFace renderNorth = FRenderBoxFace.RENDER;
    
    private Object quadEast = null;
    private Object quadWest = null;
    private Object quadUp = null;
    private Object quadDown = null;
    private Object quadSouth = null;
    private Object quadNorth = null;
    
    public Object customData;
    
    public FRenderBox(AlignedBox cube) {
        super(cube);
    }
    
    public FRenderBox(AlignedBox cube, FRenderBox box) {
        super(cube);
        this.state = box.state;
        this.color = box.color;
        this.renderEast = box.renderEast;
        this.renderWest = box.renderWest;
        this.renderUp = box.renderUp;
        this.renderDown = box.renderDown;
        this.renderSouth = box.renderSouth;
        this.renderNorth = box.renderNorth;
    }
    
    public FRenderBox(AlignedBox cube, BlockState state) {
        super(cube);
        this.state = state;
    }
    
    public FRenderBox(AlignedBox cube, Block block) {
        this(cube, block.defaultBlockState());
    }
    
    public FRenderBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, BlockState state) {
        super(minX, minY, minZ, maxX, maxY, maxZ, state);
        this.state = state;
    }
    
    public FRenderBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Block block) {
        this(minX, minY, minZ, maxX, maxY, maxZ, block.defaultBlockState());
    }
    
    public FRenderBox setColor(int color) {
        this.color = color;
        return this;
    }
    
    public FRenderBox setKeepUV(boolean keep) {
        this.keepVU = keep;
        return this;
    }
    
    public void setQuad(Facing facing, List<BakedQuad> quads) {
        Object quad = quads == null || quads.isEmpty() ? null : quads.size() == 1 ? quads.get(0) : quads;
        switch (facing) {
            case DOWN:
                quadDown = quad;
                break;
            case EAST:
                quadEast = quad;
                break;
            case NORTH:
                quadNorth = quad;
                break;
            case SOUTH:
                quadSouth = quad;
                break;
            case UP:
                quadUp = quad;
                break;
            case WEST:
                quadWest = quad;
                break;
        }
    }
    
    public Object getQuad(Facing facing) {
        switch (facing) {
            case DOWN:
                return quadDown;
            case EAST:
                return quadEast;
            case NORTH:
                return quadNorth;
            case SOUTH:
                return quadSouth;
            case UP:
                return quadUp;
            case WEST:
                return quadWest;
        }
        return null;
    }
    
    public int countQuads() {
        int quads = 0;
        if (quadUp != null)
            quads += quadUp instanceof List ? ((List) quadUp).size() : 1;
        if (quadDown != null)
            quads += quadDown instanceof List ? ((List) quadDown).size() : 1;
        if (quadEast != null)
            quads += quadEast instanceof List ? ((List) quadEast).size() : 1;
        if (quadWest != null)
            quads += quadWest instanceof List ? ((List) quadWest).size() : 1;
        if (quadSouth != null)
            quads += quadSouth instanceof List ? ((List) quadSouth).size() : 1;
        if (quadNorth != null)
            quads += quadNorth instanceof List ? ((List) quadNorth).size() : 1;
        return quads;
    }
    
    public void setFace(Facing facing, FRenderBoxFace face) {
        switch (facing) {
            case DOWN:
                renderDown = face;
                break;
            case EAST:
                renderEast = face;
                break;
            case NORTH:
                renderNorth = face;
                break;
            case SOUTH:
                renderSouth = face;
                break;
            case UP:
                renderUp = face;
                break;
            case WEST:
                renderWest = face;
                break;
        }
    }
    
    public FRenderBoxFace getFace(Facing facing) {
        return switch (facing) {
            case EAST -> renderEast;
            case WEST -> renderWest;
            case UP -> renderUp;
            case DOWN -> renderDown;
            case SOUTH -> renderSouth;
            case NORTH -> renderNorth;
        };
    }
    
    public boolean shouldRenderFace(Facing facing) {
        return switch (facing) {
            case EAST -> renderEast.shouldRender();
            case WEST -> renderWest.shouldRender();
            case UP -> renderUp.shouldRender();
            case DOWN -> renderDown.shouldRender();
            case SOUTH -> renderSouth.shouldRender();
            case NORTH -> renderNorth.shouldRender();
        };
    }
    
    public boolean intersectsWithFace(Facing facing, RenderInformationHolder holder, BlockPos offset) {
        switch (facing.axis) {
            case X:
                return holder.maxY > this.minY - offset.getY() && holder.minY < this.maxY - offset.getY() && holder.maxZ > this.minZ - offset
                        .getZ() && holder.minZ < this.maxZ - offset.getZ();
            case Y:
                return holder.maxX > this.minX - offset.getX() && holder.minX < this.maxX - offset.getX() && holder.maxZ > this.minZ - offset
                        .getZ() && holder.minZ < this.maxZ - offset.getZ();
            case Z:
                return holder.maxX > this.minX - offset.getX() && holder.minX < this.maxX - offset.getX() && holder.maxY > this.minY - offset
                        .getY() && holder.minY < this.maxY - offset.getY();
        }
        return false;
    }
    
    protected Object getRenderQuads(Facing facing) {
        if (getFace(facing).deleteQuadCache())
            return getFace(facing).getCachedFans();
        switch (facing) {
            case DOWN:
                return DOWN;
            case EAST:
                return EAST;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case UP:
                return UP;
            case WEST:
                return WEST;
        }
        return null;
    }
    
    protected float getOffsetX() {
        return minX;
    }
    
    protected float getOffsetY() {
        return minY;
    }
    
    protected float getOffsetZ() {
        return minZ;
    }
    
    protected float getOverallScale(Facing facing) {
        return getFace(facing).getScale();
    }
    
    protected float getScaleX() {
        return maxX - minX;
    }
    
    protected float getScaleY() {
        return maxY - minY;
    }
    
    protected float getScaleZ() {
        return maxZ - minZ;
    }
    
    protected boolean scaleAndOffsetQuads(Facing facing) {
        return true;
    }
    
    protected boolean onlyScaleOnceNoOffset(Facing facing) {
        return getFace(facing).hasCachedFans();
    }
    
    public void deleteQuadCache() {
        doesNeedQuadUpdate = true;
        quadEast = null;
        quadWest = null;
        quadUp = null;
        quadDown = null;
        quadSouth = null;
        quadNorth = null;
    }
    
    protected boolean previewScalingAndOffset() {
        return true;
    }
    
    public float getPreviewOffX() {
        return minX;
    }
    
    public float getPreviewOffY() {
        return minY;
    }
    
    public float getPreviewOffZ() {
        return minZ;
    }
    
    public float getPreviewScaleX() {
        return maxX - minX;
    }
    
    public float getPreviewScaleY() {
        return maxY - minY;
    }
    
    public float getPreviewScaleZ() {
        return maxZ - minZ;
    }
    
    public void renderPreview(PoseStack pose, BufferBuilder builder, int alpha) {
        int red = ColorUtils.red(color);
        int green = ColorUtils.green(color);
        int blue = ColorUtils.blue(color);
        
        if (previewScalingAndOffset()) {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderPreview(pose.last()
                                .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderPreview(pose.last()
                            .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha);
            }
        } else {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderPreview(pose.last().pose(), builder, red, green, blue, alpha);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderPreview(pose.last().pose(), builder, red, green, blue, alpha);
            }
        }
    }
    
    public void renderLines(PoseStack pose, BufferBuilder builder, int alpha) {
        int red = ColorUtils.red(color);
        int green = ColorUtils.green(color);
        int blue = ColorUtils.blue(color);
        
        if (red == 1 && green == 1 && blue == 1)
            red = green = blue = 0;
        
        if (previewScalingAndOffset()) {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderLines(pose.last()
                                .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderLines(pose.last()
                            .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha);
            }
        } else {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderLines(pose.last().pose(), builder, red, green, blue, alpha);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderLines(pose.last().pose(), builder, red, green, blue, alpha);
            }
        }
    }
    
    public void renderLines(PoseStack pose, BufferBuilder builder, int alpha, Vector3d center, double grow) {
        int red = ColorUtils.red(color);
        int green = ColorUtils.green(color);
        int blue = ColorUtils.blue(color);
        
        if (red == 1 && green == 1 && blue == 1)
            red = green = blue = 0;
        
        if (previewScalingAndOffset()) {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderLines(pose.last()
                                .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha, center, grow);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderLines(pose.last()
                            .pose(), builder, getPreviewOffX(), getPreviewOffY(), getPreviewOffZ(), getPreviewScaleX(), getPreviewScaleY(), getPreviewScaleZ(), red, green, blue, alpha, center, grow);
            }
        } else {
            for (int i = 0; i < Facing.values().length; i++) {
                Object renderQuads = getRenderQuads(Facing.values()[i]);
                if (renderQuads instanceof List)
                    for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                        ((List<VectorFan>) renderQuads).get(j).renderLines(pose.last().pose(), builder, red, green, blue, alpha, center, grow);
                else if (renderQuads instanceof VectorFan)
                    ((VectorFan) renderQuads).renderLines(pose.last().pose(), builder, red, green, blue, alpha, center, grow);
            }
        }
    }
    
    public boolean isTranslucent() {
        if (ColorUtils.isTransparent(color))
            return true;
        return !state.getMaterial().isSolidBlocking() || !state.getMaterial().isSolid();
    }
    
    protected List<BakedQuad> getBakedQuad(LevelAccessor level, BakedModel blockModel, BlockState state, Facing facing, BlockPos pos, RenderType layer, Random rand) {
        return OptifineHelper.getBakedQuad(blockModel.getQuads(state, facing.toVanilla(), rand), level, state, facing, pos, layer, rand);
    }
    
    public List<BakedQuad> getBakedQuad(LevelAccessor level, @Nullable BlockPos pos, BlockPos offset, BlockState state, BakedModel blockModel, Facing facing, RenderType layer, Random rand, boolean overrideTint, int defaultColor) {
        if (pos != null)
            rand.setSeed(state.getSeed(pos));
        
        List<BakedQuad> blockQuads = getBakedQuad(level, blockModel, state, facing, pos, layer, rand);
        
        if (blockQuads.isEmpty())
            return Collections.emptyList();
        RenderInformationHolder holder = new RenderBox.RenderInformationHolder(DefaultVertexFormat.BLOCK, facing, this.color != -1 ? this.color : defaultColor);
        holder.offset = offset;
        
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < blockQuads.size(); i++) {
            
            holder.setQuad(blockQuads.get(i), overrideTint, defaultColor);
            if (!needsResorting && OptifineHelper.isEmissive(holder.quad.getSprite()))
                needsResorting = true;
            
            int[] data = holder.quad.getVertices();
            
            int index = 0;
            int uvIndex = index + holder.uvOffset / 4;
            float tempMinX = Float.intBitsToFloat(data[index]);
            float tempMinY = Float.intBitsToFloat(data[index + 1]);
            float tempMinZ = Float.intBitsToFloat(data[index + 2]);
            
            float tempU = Float.intBitsToFloat(data[uvIndex]);
            
            holder.uvInverted = false;
            
            index = 1 * holder.format.getIntegerSize();
            uvIndex = index + holder.uvOffset / 4;
            if (tempMinX != Float.intBitsToFloat(data[index])) {
                if (tempU != Float.intBitsToFloat(data[uvIndex]))
                    holder.uvInverted = Axis.X != facing.getUAxis();
                else
                    holder.uvInverted = Axis.X != facing.getVAxis();
            } else if (tempMinY != Float.intBitsToFloat(data[index + 1])) {
                if (tempU != Float.intBitsToFloat(data[uvIndex]))
                    holder.uvInverted = Axis.Y != facing.getUAxis();
                else
                    holder.uvInverted = Axis.Y != facing.getVAxis();
            } else {
                if (tempU != Float.intBitsToFloat(data[uvIndex]))
                    holder.uvInverted = Axis.Z != facing.getUAxis();
                else
                    holder.uvInverted = Axis.Z != facing.getVAxis();
            }
            
            index = 2 * holder.format.getIntegerSize();
            float tempMaxX = Float.intBitsToFloat(data[index]);
            float tempMaxY = Float.intBitsToFloat(data[index + 1]);
            float tempMaxZ = Float.intBitsToFloat(data[index + 2]);
            
            holder.setBounds(tempMinX, tempMinY, tempMinZ, tempMaxX, tempMaxY, tempMaxZ);
            
            // Check if it is intersecting, otherwise there is no need to render it
            if (!intersectsWithFace(facing, holder, offset))
                continue;
            
            uvIndex = holder.uvOffset / 4;
            float u1 = Float.intBitsToFloat(data[uvIndex]);
            float v1 = Float.intBitsToFloat(data[uvIndex + 1]);
            uvIndex = 2 * holder.format.getIntegerSize() + holder.uvOffset / 4;
            float u2 = Float.intBitsToFloat(data[uvIndex]);
            float v2 = Float.intBitsToFloat(data[uvIndex + 1]);
            
            if (holder.uvInverted) {
                holder.sizeU = facing.getV(tempMinX, tempMinY, tempMinZ) < facing.getV(tempMaxX, tempMaxY, tempMaxZ) ? u2 - u1 : u1 - u2;
                holder.sizeV = facing.getU(tempMinX, tempMinY, tempMinZ) < facing.getU(tempMaxX, tempMaxY, tempMaxZ) ? v2 - v1 : v1 - v2;
            } else {
                holder.sizeU = facing.getU(tempMinX, tempMinY, tempMinZ) < facing.getU(tempMaxX, tempMaxY, tempMaxZ) ? u2 - u1 : u1 - u2;
                holder.sizeV = facing.getV(tempMinX, tempMinY, tempMinZ) < facing.getV(tempMaxX, tempMaxY, tempMaxZ) ? v2 - v1 : v1 - v2;
            }
            
            Object renderQuads = getRenderQuads(holder.facing);
            if (renderQuads instanceof List)
                for (int j = 0; j < ((List<VectorFan>) renderQuads).size(); j++)
                    ((List<VectorFan>) renderQuads).get(j).generate(holder, quads);
            else if (renderQuads instanceof VectorFan)
                ((VectorFan) renderQuads).generate(holder, quads);
        }
        
        for (BakedQuad quad : quads)
            if (quad instanceof CreativeBakedQuad c)
                c.updateAlpha();
        return quads;
        
    }
    
    private static int uvOffset(VertexFormat format) {
        int offset = 0;
        for (int i = 0; i < format.getElements().size(); i++) {
            if (format.getElements().get(i).getUsage() == Usage.UV)
                return offset;
            offset += format.getElements().get(i).getByteSize();
        }
        return -1;
    }
    
    public class RenderInformationHolder {
        
        public final Facing facing;
        public final int color;
        public final VertexFormat format;
        public final int uvOffset;
        public BlockPos offset;
        public boolean shouldOverrideColor;
        
        public BakedQuad quad;
        
        public NormalPlane normal;
        public Ray2d ray = new Ray2d(Axis.X, Axis.Y, 0, 0, 0, 0);
        
        public final boolean scaleAndOffset;
        
        public final float offsetX;
        public final float offsetY;
        public final float offsetZ;
        
        public final float scaleX;
        public final float scaleY;
        public final float scaleZ;
        
        public float minX;
        public float minY;
        public float minZ;
        public float maxX;
        public float maxY;
        public float maxZ;
        
        public float sizeX;
        public float sizeY;
        public float sizeZ;
        
        public boolean uvInverted;
        public float sizeU;
        public float sizeV;
        
        public RenderInformationHolder(VertexFormat format, Facing facing, int color) {
            this.color = color;
            this.format = format;
            this.facing = facing;
            this.uvOffset = uvOffset(format);
            
            FRenderBox box = getBox();
            scaleAndOffset = box.scaleAndOffsetQuads(facing);
            if (scaleAndOffset) {
                if (box.onlyScaleOnceNoOffset(facing)) {
                    this.offsetX = this.offsetY = this.offsetZ = 0;
                    this.scaleX = this.scaleY = this.scaleZ = box.getOverallScale(facing);
                } else {
                    this.offsetX = box.getOffsetX();
                    this.offsetY = box.getOffsetY();
                    this.offsetZ = box.getOffsetZ();
                    this.scaleX = box.getScaleX();
                    this.scaleY = box.getScaleY();
                    this.scaleZ = box.getScaleZ();
                }
                
            } else {
                this.offsetX = this.offsetY = this.offsetZ = 0;
                this.scaleX = this.scaleY = this.scaleZ = 0;
            }
        }
        
        public void setQuad(BakedQuad quad, boolean overrideTint, int defaultColor) {
            this.quad = quad;
            this.shouldOverrideColor = overrideTint && (defaultColor == -1 || quad.isTinted()) && color != -1;
        }
        
        public void setBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
            
            this.sizeX = this.maxX - this.minX;
            this.sizeY = this.maxY - this.minY;
            this.sizeZ = this.maxZ - this.minZ;
        }
        
        public FRenderBox getBox() {
            return FRenderBox.this;
        }
        
        public boolean hasBounds() {
            switch (facing.axis) {
                case X:
                    return minY != 0 || maxY != 1 || minZ != 0 || maxZ != 1;
                case Y:
                    return minX != 0 || maxX != 1 || minZ != 0 || maxZ != 1;
                case Z:
                    return minX != 0 || maxX != 1 || minY != 0 || maxY != 1;
            }
            return false;
        }
    }
    
    private static class VectorFanSimple extends VectorFan {
        
        public VectorFanSimple(Vec3f[] coords) {
            super(coords);
            
        }
        
        @Override
        @Environment(EnvType.CLIENT)
        @OnlyIn(Dist.CLIENT)
        public void generate(RenderInformationHolder holder, List<BakedQuad> quads) {
            var ri = new RenderBox.RenderInformationHolder(holder.format, holder.format, holder.color)
            int index = 0;
            while (index < coords.length - 3) {
                generate(holder, coords[0], coords[index + 1], coords[index + 2], coords[index + 3], quads);
                index += 2;
            }
            if (index < coords.length - 2)
                generate(holder, coords[0], coords[index + 1], coords[index + 2], coords[index + 2], quads);
        }
        
        @Override
        protected boolean doMinMaxLate() {
            return true;
        }
        
    }
    
}