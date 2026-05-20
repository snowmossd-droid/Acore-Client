package acore.hack.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.Uniform;
import net.minecraft.util.Identifier;

public class ShaderProgram {
   protected final ShaderProgram program;

   public ShaderProgram(String vertex, String fragment) {
      this.program = new ShaderProgram(
         RenderSystem.getResourceFactory(), 
         Identifier.of("ariscore", "shaders/core/" + vertex + ".vsh"), 
         Identifier.of("ariscore", "shaders/core/" + fragment + ".fsh")
      );
   }

   public void use() {
      this.program.bind();
   }

   public Uniform getUniform(String name) {
      return this.program.getUniform(name);
   }

   public void setUniform(String name, float value) {
      Uniform uniform = this.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   public void setUniform(String name, int value) {
      Uniform uniform = this.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   public void setUniform(String name, float v1, float v2) {
      Uniform uniform = this.getUniform(name);
      if (uniform != null) {
         uniform.set(v1, v2);
      }
   }

   public void setUniform(String name, float v1, float v2, float v3) {
      Uniform uniform = this.getUniform(name);
      if (uniform != null) {
         uniform.set(v1, v2, v3);
      }
   }

   public void setUniform(String name, float v1, float v2, float v3, float v4) {
      Uniform uniform = this.getUniform(name);
      if (uniform != null) {
         uniform.set(v1, v2, v3, v4);
      }
   }
}
