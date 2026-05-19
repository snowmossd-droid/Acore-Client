package acore.hack.features.gui.mainmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen extends Screen {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final List<MainMenuButton> buttons = new ArrayList<>();
    
    public MainMenuScreen() {
        super(Text.literal("AcoreHack Main Menu"));
    }
    
    @Override
    protected void init() {
        rebuildButtons();
    }
    
    private void rebuildButtons() {
        buttons.clear();
        float totalWidth = 124f;
        float startX = width / 2f - totalWidth / 2f;
        float buttonY = height - 21.6f - 8f;
        
        buttons.add(new MainMenuButton(startX, buttonY, 21.6f, 21.6f, 8f, "Singleplayer", 
            MainMenuButton.IconType.SINGLEPLAYER, () -> {}));
        buttons.add(new MainMenuButton(startX + 25.6f, buttonY, 21.6f, 21.6f, 8f, "Multiplayer",
            MainMenuButton.IconType.MULTIPLAYER, () -> {}));
        buttons.add(new MainMenuButton(startX + 51.2f, buttonY, 21.6f, 21.6f, 8f, "Alt",
            MainMenuButton.IconType.ALT, () -> {}));
        buttons.add(new MainMenuButton(startX + 76.8f, buttonY, 21.6f, 21.6f, 8f, "Settings",
            MainMenuButton.IconType.SETTING, () -> {}));
        buttons.add(new MainMenuButton(startX + 102.4f, buttonY, 21.6f, 21.6f, 8f, "Leave",
            MainMenuButton.IconType.LEAVE, () -> MinecraftClient.getInstance().stop()));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        String timeText = LocalTime.now().format(TIME_FORMATTER);
        context.drawCenteredTextWithShadow(textRenderer, timeText, width / 2, (int)(height * 0.11f), Color.WHITE.getRGB());
        
        float groupWidth = 136f;
        float groupHeight = 31.6f;
        float groupX = width / 2f - groupWidth / 2f;
        float groupY = height - 21.6f - 8f - 5f;
        context.fill((int)groupX, (int)groupY, (int)(groupX + groupWidth), (int)(groupY + groupHeight), new Color(20, 20, 25, 200).getRGB());
        
        buttons.forEach(button -> button.render(context, mouseX, mouseY));
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (MainMenuButton btn : buttons) {
            if (btn.onClick((int)mouseX, (int)mouseY)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
          }
