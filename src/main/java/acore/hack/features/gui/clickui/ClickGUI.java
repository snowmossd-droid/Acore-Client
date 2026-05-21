package acore.hack.features.gui.clickui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.features.gui.clickui.impl.SearchBar;
import acore.hack.features.gui.clickui.impl.ThemeSelector;
import acore.hack.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static ClickGUI INSTANCE;
    public static List<AbstractCategory> windows = new ArrayList<>();
    public static boolean anyHovered;
    public static String currentDescription = "";
    public static boolean descriptionActive;
    
    private boolean firstOpen = true;
    private float scrollY = 0f;
    private boolean closing = false;
    private float animProgress = 0f;
    
    private final SearchBar searchBar = new SearchBar();
    private final ThemeSelector themeSelector = new ThemeSelector();
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        INSTANCE = this;
    }
    
    public static ClickGUI getInstance() {
        if (INSTANCE == null) INSTANCE = new ClickGUI();
        return INSTANCE;
    }
    
    @Override
    protected void init() {
        setupWindows();
        searchBar.resetState();
        closing = false;
        firstOpen = false;
    }
    
    private void setupWindows() {
        windows.clear();
        Module.Category[] categories = Module.Category.values();
        int panelWidth = 125;
        int panelHeight = 280;
        int panelMargin = 8;
        int totalWidth = categories.length * (panelWidth + panelMargin) - panelMargin;
        int startX = (mc.getWindow().getScaledWidth() - totalWidth) / 2;
        int startY = (mc.getWindow().getScaledHeight() - panelHeight) / 2;
        
        for (int i = 0; i < categories.length; i++) {
            Category window = new Category(
                categories[i],
                ModuleManager.INSTANCE.getModulesByCategory(categories[i]),
                startX + i * (panelWidth + panelMargin),
                startY,
                panelWidth,
                25f
            );
            window.setOpen(true);
            windows.add(window);
        }
        
        windows.forEach(AbstractCategory::init);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        anyHovered = false;
        descriptionActive = false;
        currentDescription = "";
        
        float target = closing ? 0f : 1f;
        animProgress += (target - animProgress) * 0.15f;
        
        if (animProgress < 0.01f && closing) {
            mc.setScreen(null);
            closing = false;
            return;
        }
        
        renderBackground(context, mouseX, mouseY, delta);
        
        int panelWidth = 125;
        int panelHeight = 280;
        int panelMargin = 8;
        Module.Category[] categories = Module.Category.values();
        int totalWidth = categories.length * (panelWidth + panelMargin) - panelMargin;
        int startX = (mc.getWindow().getScaledWidth() - totalWidth) / 2;
        int startY = (mc.getWindow().getScaledHeight() - panelHeight) / 2;
        
        for (int i = 0; i < windows.size(); i++) {
            AbstractCategory w = windows.get(i);
            float targetX = startX + i * (panelWidth + panelMargin);
            float targetY = startY;
            float progress = getDelayedProgress(animProgress, w.getName());
            float offsetY = -90f * (1f - progress);
            w.setX(targetX);
            w.setY(targetY + offsetY);
            
            if (progress > 0f) {
                float alpha = MathHelper.clamp(animProgress * progress, 0f, 1f);
                w.setAlpha(alpha);
                w.render(context, mouseX, mouseY, delta);
            }
        }
        
        renderBottomBar(context, mouseX, mouseY, delta, animProgress);
        renderDescription(context);
        renderHints(context.getMatrices());
        
        if (scrollY != 0f) {
            windows.forEach(w -> w.setModuleOffset(scrollY, mouseX, mouseY));
            scrollY = 0f;
        }
    }
    
    private void renderBottomBar(DrawContext context, int mouseX, int mouseY, float delta, float anim) {
        float panelHeight = 280f;
        float searchWidth = Math.min(180f, mc.getWindow().getScaledWidth() - 40f);
        float searchHeight = 20f;
        float themeButtonSize = 16f;
        float gap = 5f;
        
        float searchX = (mc.getWindow().getScaledWidth() - searchWidth) / 2f;
        float searchY = (mc.getWindow().getScaledHeight() + panelHeight) / 2f + 10f;
        float buttonX = searchX + searchWidth + gap;
        float buttonY = searchY + (searchHeight - themeButtonSize) / 2f;
        
        float bottomOffset = 60f * (1f - anim);
        searchY += bottomOffset;
        buttonY += bottomOffset;
        
        searchBar.setPosition(searchX, searchY, searchWidth, searchHeight);
        searchBar.render(context, mouseX, mouseY, delta);
        
        themeSelector.setLayout(buttonX, buttonY, themeButtonSize);
        themeSelector.render(context, mouseX, mouseY, delta);
    }
    
    private void renderDescription(DrawContext context) {
        if (descriptionActive && !currentDescription.isEmpty() && animProgress >= 0.99f) {
            float paddingX = 8f, paddingY = 4f;
            float textWidth = mc.textRenderer.getWidth(currentDescription);
            float textHeight = mc.textRenderer.fontHeight;
            float descWidth = Math.max(60f, textWidth + paddingX * 2);
            float descHeight = textHeight + paddingY * 2;
            float descY = (mc.getWindow().getScaledHeight() - 280f) / 2f - descHeight - 10f;
            float descX = (mc.getWindow().getScaledWidth() - descWidth) / 2f;
            float textY = descY + (descHeight - textHeight) / 2f + 3f;
            
            Color bg = new Color(25, 25, 28, 200);
            drawRoundedRect(context, descX, descY, descWidth, descHeight, descHeight / 2f, bg);
            context.drawCenteredTextWithShadow(mc.textRenderer, currentDescription, (int)(descX + descWidth / 2), (int)textY, Color.WHITE.getRGB());
        }
    }
    
    private void renderHints(MatrixStack matrices) {
        List<String> hints = List.of(
            "Left Click: Enable/Disable",
            "Right Click: Open settings",
            "Mid Click: Change bind",
            "Ctrl + F: Search modules"
        );
        float lineHeight = mc.textRenderer.fontHeight + 2f;
        float startY = mc.getWindow().getScaledHeight() - 8f - lineHeight * hints.size();
        float startX = 8f;
        
        for (int i = 0; i < hints.size(); i++) {
            drawOutlinedText(matrices, hints.get(i), startX, startY + i * lineHeight);
        }
    }
    
    private void drawOutlinedText(MatrixStack matrices, String text, float x, float y) {
        int outlineColor = new Color(0, 0, 0, 255).getRGB();
        int fillColor = Color.WHITE.getRGB();
        mc.textRenderer.draw(matrices, text, x - 0.45f, y, outlineColor);
        mc.textRenderer.draw(matrices, text, x + 0.45f, y, outlineColor);
        mc.textRenderer.draw(matrices, text, x, y - 0.45f, outlineColor);
        mc.textRenderer.draw(matrices, text, x, y + 0.45f, outlineColor);
        mc.textRenderer.draw(matrices, text, x, y, fillColor);
    }
    
    private void drawRoundedRect(DrawContext context, float x, float y, float w, float h, float r, Color color) {
        context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color.getRGB());
    }
    
    private float getDelayedProgress(float progress, String name) {
        float delay = 0f;
        if (name != null) {
            if (name.equalsIgnoreCase("Combat") || name.equalsIgnoreCase("Movement")) delay = 0.4f;
            else if (name.equalsIgnoreCase("Visual") || name.equalsIgnoreCase("Player")) delay = 0.8f;
        }
        if (delay <= 0f) return MathHelper.clamp(progress, 0f, 1f);
        return progress <= delay ? 0f : MathHelper.clamp((progress - delay) / (1f - delay), 0f, 1f);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchBar.mouseClicked((int)mouseX, (int)mouseY, button);
        themeSelector.mouseClicked((int)mouseX, (int)mouseY, button);
        windows.forEach(w -> w.mouseClicked((int)mouseX, (int)mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        searchBar.mouseReleased((int)mouseX, (int)mouseY, button);
        themeSelector.mouseReleased((int)mouseX, (int)mouseY, button);
        windows.forEach(w -> w.mouseReleased((int)mouseX, (int)mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY += (float)verticalAmount * 15f;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        searchBar.keyTyped(keyCode);
        windows.forEach(w -> w.keyTyped(keyCode));
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closing = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchBar.charTyped(chr, modifiers);
        windows.forEach(w -> w.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }
    
    @Override
    public void close() {
        closing = true;
    }
    
    public static void requestDescription(String description) {
        currentDescription = description;
        descriptionActive = true;
    }
    }
