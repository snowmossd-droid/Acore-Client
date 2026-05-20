package acore.hack.utility.render.shaders.satin.impl;

import acore.hack.utility.render.shaders.satin.api.managed.*;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import java.util.function.Consumer;

public class ReloadableShaderEffectManager implements ShaderEffectManager {
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();
    
    @Override
    public ManagedShaderEffect manage(Identifier id) {
        return null;
    }
    
    @Override
    public ManagedShaderEffect manage(Identifier id, Consumer<ManagedShaderEffect> consumer) {
        return null;
    }
    
    @Override
    public ManagedCoreShader manageCoreShader(Identifier id) {
        return null;
    }
    
    @Override
    public ManagedCoreShader manageCoreShader(Identifier id, VertexFormat format) {
        return null;
    }
    
    @Override
    public ManagedCoreShader manageCoreShader(Identifier id, VertexFormat format, Consumer<ManagedCoreShader> consumer) {
        return null;
    }
}
