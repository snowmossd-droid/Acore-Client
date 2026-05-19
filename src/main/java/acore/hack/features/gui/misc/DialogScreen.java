package acore.hack.features.gui.misc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

public class DialogScreen extends Screen {
    private final String header;
    private final String description;
    private final String yesText;
    private final String noText;
    private final Runnable yesAction;
    private final Runnable noAction;
    
    public DialogScreen(String header, String description, String yesText, String noText, Runnable yesAction, Runnable noAction) {
        super(Text.literal("Dialog"));
        this.header = header;
        this.description = description;
        this.yesText = yesText;
        this.noText = noText;
        this.yesAction = yesAction;
        this.noAction = noAction;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        float mainX = width / 2f - 120f;
        float mainY = height / 2f - 80f;
        float mainW = 240f;
        float mainH = 160f;
        
        context.fill((int)mainX, (int)mainY, (int)(mainX + mainW), (int)(mainY + mainH), new Color(20, 20, 25, 240).getRGB());
        context.drawCenteredTextWithShadow(textRenderer, header, width / 2, (int)(mainY + 20), Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, description, width / 2, (int)(mainY + 50), Color.GRAY.getRGB());
        
        boolean yesHover = mouseX >= mainX + 20 && mouseX <= mainX + 110 && mouseY >= mainY + 100 && mouseY <= mainY + 140;
        boolean noHover = mouseX >= mainX + 130 && mouseX <= mainX + 220 && mouseY >= mainY + 100 && mouseY <= mainY + 140;
        
        context.fill((int)mainX + 20, (int)mainY + 100, (int)mainX + 110, (int)mainY + 140, 
            yesHover ? new Color(60, 60, 70).getRGB() : new Color(40, 40, 50).getRGB());
        context.fill((int)mainX + 130, (int)mainY + 100, (int)mainX + 220, (int)mainY + 140,
            noHover ? new Color(60, 60, 70).getRGB() : new Color(40, 40, 50).getRGB());
        
        context.drawCenteredTextWithShadow(textRenderer, yesText, (int)(mainX + 65), (int)(mainY + 118), Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, noText, (int)(mainX + 175), (int)(mainY + 118), Color.WHITE.getRGB());
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float mainX = width / 2f - 120f;
        float mainY = height / 2f - 80f;
        
        if (mouseX >= mainX + 20 && mouseX <= mainX + 110 && mouseY >= mainY + 100 && mouseY <= mainY + 140) {
            yesAction.run();
            return true;
        }
        if (mouseX >= mainX + 130 && mouseX <= mainX + 220 && mouseY >= mainY + 100 && mouseY <= mainY + 140) {
            noAction.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
                     }
