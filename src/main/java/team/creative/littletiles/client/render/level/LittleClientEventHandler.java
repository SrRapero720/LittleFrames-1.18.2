package team.creative.littletiles.client.render.level;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.cache.builder.RenderingThread;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

public class LittleClientEventHandler {

    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    public static int transparencySortingIndex;

    @SubscribeEvent
    public synchronized void levelUnload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide())
            RenderingThread.unload();
    }

    @SubscribeEvent
    public void renderOverlay(RenderBlockOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            PoseStack pose = new PoseStack();
            Player player = event.getPlayer();
            BlockPos blockpos = new BlockPos(player.getEyePosition(TickUtils.getDeltaFrameTime(player.level)));
            BlockEntity blockEntity = player.level.getBlockEntity(blockpos);
            if (blockEntity instanceof BETiles be) {
                AABB bb = player.getBoundingBox();
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                    LittleTile tile = pair.value;
                    if (tile.isMaterial(Material.WATER) && tile.intersectsWith(bb, pair.key)) {

                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.enableTexture();
                        RenderSystem.setShaderTexture(0, RES_UNDERWATER_OVERLAY);

                        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                        float f = player.level.dimensionType().brightness(player.level.getMaxLocalRawBrightness(blockpos));
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(f, f, f, 0.1F);
                        Vector3d color = ColorUtils.toVec(tile.color);
                        RenderSystem.setShaderColor(f * (float) color.x, f * (float) color.y, f * (float) color.z, 0.5F);
                        float f7 = -mc.player.getYRot() / 64.0F;
                        float f8 = mc.player.getXRot() / 64.0F;
                        Matrix4f matrix4f = pose.last().pose();
                        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + f7, 0.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + f7, 0.0F + f8).endVertex();
                        // TODO: Fix this if needed
//                        BufferUploader.drawWithShader(bufferbuilder.end());
                        RenderSystem.disableBlend();

                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

}
