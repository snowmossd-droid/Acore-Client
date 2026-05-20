package acore.hack.utility.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public interface WindowResizeCallback {
    Event<WindowResizeCallback> EVENT = EventFactory.createArrayBacked(WindowResizeCallback.class,
        listeners -> (client, window) -> {
            for (WindowResizeCallback listener : listeners) {
                listener.onWindowResize(client, window);
            }
        }
    );

    void onWindowResize(MinecraftClient client, net.minecraft.client.util.Window window);
}
