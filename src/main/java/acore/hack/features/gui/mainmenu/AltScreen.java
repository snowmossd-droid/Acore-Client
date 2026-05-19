package acore.hack.features.gui.mainmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

public class AltScreen extends Screen {
    public AltScreen() {
        super(Text.literal("Alt Manager"));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        int panelW = (int)(width * 0.25f);
        int panelH = (int)(height * 0.5f);
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, new Color(20, 20, 25, 230).getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Alt Manager", width / 2, panelY + 20, Color.WHITE.getRGB());
        
        // Simple alt list placeholder
        context.drawTextWithShadow(textRenderer, "No alts added", panelX + 10, panelY + 50, Color.GRAY.getRGB());
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
