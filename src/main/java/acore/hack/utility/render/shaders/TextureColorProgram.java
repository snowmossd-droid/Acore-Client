package acore.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import acore.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import acore.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;

public class TextureColorProgram {
   public static final ManagedCoreShader TEXTURE_COLOR_PROGRAM = ShaderEffectManager.getInstance().manageCoreShader(Identifier.of("ariscore", "texture_color"), VertexFormats.POSITION_TEXTURE_COLOR);

   public TextureColorProgram() {
      RenderSystem.setShader(TEXTURE_COLOR_PROGRAM::getProgram);
   }
}
