package acore.hack.utility.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.Uniform;

public class VenShaderProgram {
   protected final ShaderProgram program;

   public VenShaderProgram(String vertex, String fragment) {
      this.program = new ShaderProgram(
         MinecraftClient.getInstance().getResourceManager(),
         "ariscore:shaders/core/" + vertex,
         "ariscore:shaders/core/" + fragment
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
