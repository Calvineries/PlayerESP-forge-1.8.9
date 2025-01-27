package com.github.calvineries.playeresp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

// For forge handshake ^-^
@Mod(modid = "hitboxes", name = "PlayerESP", version = "1.0.0", useMetadata = true)

public class PlayerESP {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final KeyBinding toggleHitboxKey = new KeyBinding("Toggle Hitbox", Keyboard.KEY_H, "PlayerESP");
    private final KeyBinding toggleNameKey = new KeyBinding("Toggle Name", Keyboard.KEY_N, "PlayerESP");

    private boolean showHitbox = true;
    private boolean showName = true;

    public PlayerESP() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(toggleHitboxKey);
        ClientRegistry.registerKeyBinding(toggleNameKey);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (toggleHitboxKey.isPressed()) {
            showHitbox = !showHitbox;
        }

        if (toggleNameKey.isPressed()) {
            showName = !showName;
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                if (showName) {
                    renderPlayerName((EntityPlayer) entity, event.partialTicks);
                }
                if (showHitbox) {
                    renderHitbox((EntityPlayer) entity, event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
        if (event.entity instanceof EntityPlayer && event.entity != mc.thePlayer) {
            event.setCanceled(true);
        }
    }

    private void renderPlayerName(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY + player.height + 0.5;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        String displayName = player.getDisplayName().getFormattedText();

        double distance = player.getDistanceToEntity(mc.thePlayer);
        float maxDistance = 50;
        float scale = 0.02F + (float) Math.min(Math.min(distance, maxDistance) / 300, 0.8);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.enableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        int color = 0xFFFFFF;
        mc.fontRendererObj.drawString(displayName, -mc.fontRendererObj.getStringWidth(displayName) / 2, 0, color, false);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    private void renderHitbox(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        float[] color = getColorFromName(player.getDisplayName().getFormattedText());
        GlStateManager.color(color[0], color[1], color[2], 1.0F);

        GL11.glLineWidth(3.0F);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(-player.width / 2, 0, -player.width / 2);
        GL11.glVertex3d(-player.width / 2, 0, player.width / 2);
        GL11.glVertex3d(player.width / 2, 0, player.width / 2);
        GL11.glVertex3d(player.width / 2, 0, -player.width / 2);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(-player.width / 2, player.height, -player.width / 2);
        GL11.glVertex3d(-player.width / 2, player.height, player.width / 2);
        GL11.glVertex3d(player.width / 2, player.height, player.width / 2);
        GL11.glVertex3d(player.width / 2, player.height, -player.width / 2);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(-player.width / 2, 0, -player.width / 2);
        GL11.glVertex3d(-player.width / 2, player.height, -player.width / 2);

        GL11.glVertex3d(-player.width / 2, 0, player.width / 2);
        GL11.glVertex3d(-player.width / 2, player.height, player.width / 2);

        GL11.glVertex3d(player.width / 2, 0, player.width / 2);
        GL11.glVertex3d(player.width / 2, player.height, player.width / 2);

        GL11.glVertex3d(player.width / 2, 0, -player.width / 2);
        GL11.glVertex3d(player.width / 2, player.height, -player.width / 2);
        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    private float[] getColorFromName(String formattedName) {
        for (EnumChatFormatting format : EnumChatFormatting.values()) {
            if (formattedName.contains(format.toString())) {
                return getColorFromEnum(format);
            }
        }
        return new float[]{1.0F, 0.0F, 0.0F};
    }

    private float[] getColorFromEnum(EnumChatFormatting format) {
        switch (format) {
            case BLACK: return new float[]{0.0F, 0.0F, 0.0F};
            case DARK_BLUE: return new float[]{0.0F, 0.0F, 0.5F};
            case DARK_GREEN: return new float[]{0.0F, 0.5F, 0.0F};
            case DARK_AQUA: return new float[]{0.0F, 0.5F, 0.5F};
            case DARK_RED: return new float[]{0.5F, 0.0F, 0.0F};
            case DARK_PURPLE: return new float[]{0.5F, 0.0F, 0.5F};
            case GOLD: return new float[]{1.0F, 0.5F, 0.0F};
            case GRAY: return new float[]{0.5F, 0.5F, 0.5F};
            case DARK_GRAY: return new float[]{0.25F, 0.25F, 0.25F};
            case BLUE: return new float[]{0.3F, 0.3F, 1.0F};
            case GREEN: return new float[]{0.3F, 1.0F, 0.3F};
            case AQUA: return new float[]{0.3F, 1.0F, 1.0F};
            case RED: return new float[]{1.0F, 0.3F, 0.3F};
            case LIGHT_PURPLE: return new float[]{1.0F, 0.3F, 1.0F};
            case YELLOW: return new float[]{1.0F, 1.0F, 0.3F};
            case WHITE: default: return new float[]{1.0F, 1.0F, 1.0F};
        }
    }
}
