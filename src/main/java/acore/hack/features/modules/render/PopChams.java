package acore.hack.features.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import acore.hack.events.impl.TotemPopEvent;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.ColorSetting;
import acore.hack.utility.math.MathUtility;
import acore.hack.utility.render.Render2DEngine;
import acore.hack.utility.render.Render3DEngine;

public final class PopChams extends Module {
   private final Setting<PopChams.Mode> mode = new Setting<>("Mode", PopChams.Mode.Simple);
   private final Setting<Boolean> secondLayer = new Setting<>("SecondLayer", false);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(-1, true)));
   private final Setting<Integer> ySpeed = new Setting<>("YSpeed", 1, -10, 10);
   private final Setting<Integer> aSpeed = new Setting<>("AlphaSpeed", 35, 1, 100);
   private final Setting<Float> rotSpeed = new Setting<>("RotationSpeed", 0.0F, 0.0F, 6.0F);
   private final CopyOnWriteArrayList<PopChams.Person> popList = new CopyOnWriteArrayList<>();

   public PopChams() {
      super("PopChams", "Highlights totem pops.", Module.Category.RENDER);
   }

   @Override
   public void onUpdate() {
      this.popList.forEach(person -> person.update(this.popList));
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      if (this.mode.is(PopChams.Mode.Simple)) {
         RenderSystem.defaultBlendFunc();
      } else {
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      }
      this.popList.forEach(person -> this.renderEntity(stack, person.player, person.modelPlayer, person.getTexture(), person.getAlpha()));
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   @EventHandler
   private void onTotemPop(@NotNull TotemPopEvent e) {
      if (!e.getEntity().equals(mc.player) && mc.world != null) {
         OtherClientPlayerEntity entity = new OtherClientPlayerEntity(mc.world, new GameProfile(e.getEntity().getUuid(), e.getEntity().getName().getString()));
         entity.copyFrom(e.getEntity());
         entity.setUuid(e.getEntity().getUuid());
         entity.setPosition(e.getEntity().getX(), e.getEntity().getY(), e.getEntity().getZ());
         entity.setYaw(e.getEntity().getYaw());
         entity.setPitch(e.getEntity().getPitch());
         entity.setSneaking(e.getEntity().isSneaking());
         entity.getAbilities().setFlySpeed(e.getEntity().getAbilities().getFlySpeed());
         entity.getAbilities().flying = e.getEntity().getAbilities().flying;
         Identifier texture = ((AbstractClientPlayerEntity)e.getEntity()).getSkinTextures().texture();
         this.popList.add(new PopChams.Person(entity, texture));
      }
   }

   private void renderEntity(@NotNull MatrixStack matrices, @NotNull net.minecraft.entity.LivingEntity entity, @NotNull PlayerEntityModel<PlayerEntity> modelBase, Identifier texture, int alpha) {
      modelBase.leftPants.visible = this.secondLayer.getValue();
      modelBase.rightPants.visible = this.secondLayer.getValue();
      modelBase.leftSleeve.visible = this.secondLayer.getValue();
      modelBase.rightSleeve.visible = this.secondLayer.getValue();
      modelBase.jacket.visible = this.secondLayer.getValue();
      modelBase.leftLeg.visible = this.secondLayer.getValue();
      double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().x;
      double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().y;
      double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().z;
      
      matrices.push();
      matrices.translate((float)x, (float)y, (float)z);
      float yRotYaw = alpha / 255.0F * 360.0F * this.rotSpeed.getValue();
      yRotYaw = yRotYaw == 0.0F ? 0.0F : Render2DEngine.interpolateFloat(yRotYaw, yRotYaw - this.aSpeed.getValue().intValue() / 255.0F * 360.0F * this.rotSpeed.getValue(), Render3DEngine.getTickDelta());
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtility.rad(180.0F - entity.bodyYaw + yRotYaw)));
      prepareScale(matrices);
      modelBase.animateModel((PlayerEntity)entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), Render3DEngine.getTickDelta());
      float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1.0F);
      modelBase.setAngles((PlayerEntity)entity, entity.limbAnimator.getPos(), limbSpeed, entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());
      BufferBuilder buffer;
      if (this.mode.is(PopChams.Mode.Textured)) {
         RenderSystem.setShaderTexture(0, texture);
         RenderSystem.setShader(GameRenderer::getPositionTexProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      } else {
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
      }
      RenderSystem.setShaderColor(this.color.getValue().getGlRed(), this.color.getValue().getGlGreen(), this.color.getValue().getGlBlue(), alpha / 255.0F);
      modelBase.render(matrices, buffer, 10, 0);
      Render2DEngine.endBuilding(buffer);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   private static void prepareScale(@NotNull MatrixStack matrixStack) {
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.scale(1.6F, 1.8F, 1.6F);
      matrixStack.translate(0.0F, -1.501F, 0.0F);
   }

   private enum Mode {
      Simple,
      Textured;
   }

   private class Person {
      private final PlayerEntity player;
      private final PlayerEntityModel<PlayerEntity> modelPlayer;
      private Identifier texture;
      private int alpha;

      public Person(PlayerEntity player, Identifier texture) {
         this.player = player;
         EntityRendererFactory.Context ctx = new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer);
         this.modelPlayer = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false);
         this.modelPlayer.getHead().scale(new Vector3f(-0.3F, -0.3F, -0.3F));
         this.alpha = PopChams.this.color.getValue().getAlpha();
         this.texture = texture;
      }

      public void update(CopyOnWriteArrayList<PopChams.Person> arrayList) {
         if (this.alpha <= 0) {
            arrayList.remove(this);
            this.player.remove(RemovalReason.KILLED);
            this.player.discard();
         } else {
            this.alpha = this.alpha - PopChams.this.aSpeed.getValue();
         }
      }

      public int getAlpha() {
         return MathUtility.clamp(this.alpha, 0, 255);
      }

      public Identifier getTexture() {
         return this.texture;
      }
   }
}
