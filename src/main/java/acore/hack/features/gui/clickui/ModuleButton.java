package acore.hack.features.gui.clickui;

import acore.hack.features.gui.clickui.impl.*;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends AbstractButton {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public final Module module;
    private boolean open;
    private boolean hovered;
    private float animation;
    private final List<AbstractElement> elements = new ArrayList<>();
    private float contentX, contentY, contentWidth, contentHeight;
    private boolean binding = false;
    private boolean holdbind = false;
    private float categoryAnimation = 0f;
    
    public ModuleButton(Module module) {
        this.module = module;
        
        for (Setting<?> setting : module.getSettings()) {
            if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                elements.add(new BooleanElement(setting));
            } else if (setting.getValue() instanceof ColorSetting) {
                elements.add(new ColorPickerElement(setting));
            } else if (setting.getValue() instanceof BooleanSettingGroup) {
                elements.add(new BooleanParentElement((Setting<BooleanSettingGroup>) setting));
            } else if (setting.isNumberSetting() && setting.hasRestriction()) {
                elements.add(new SliderElement(setting));
            } else if (setting.isEnumSetting()) {
                elements.add(new ModeElement(setting));
            } else if (setting.getValue() instanceof Bind) {
                elements.add(new BindElement(setting));
            } else if (setting.getValue() instanceof String) {
                elements.add(new StringElement(setting));
            }
        }
    }
    
    @Override
    public void init() {
        elements.forEach(AbstractElement::init);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean withinContent = isHovered(mouseX, mouseY, contentX, contentY, contentWidth, contentHeight);
        hovered = withinContent && isHovered(mouseX, mouseY, x, y, width, height);
        
        animation += ((module.isEnabled() ? 1f : 0f) - animation) * 0.125f;
        categoryAnimation += (getElementsHeight() - categoryAnimation) * 0.05f;
        
        if (hovered) {
            ClickGUI.requestDescription(module.getDescription());
        }
        
        float textY = y + (height - mc.textRenderer.fontHeight) / 2f + 3f;
        
        Color bgColor = module.isEnabled() 
            ? new Color(70, 70, 200, (int)(80 * animation))
            : new Color(40, 40, 50, 80);
        drawRoundedRect(context.getMatrices(), x + 4f, y + 1f, width - 8f, height - 2f, 4f, bgColor);
        
        if (!binding) {
            String bindText = getBindText();
            if (!bindText.equals("None")) {
                context.drawTextWithShadow(mc.textRenderer, bindText, 
                    (int)(x + width - 11f - mc.textRenderer.getStringWidth(bindText)), 
                    (int)textY, module.isEnabled() ? Color.CYAN.getRGB() : Color.GRAY.getRGB());
            }
        } else {
            String bindMsg = holdbind ? Formatting.GRAY + "Hold" : "Toggle";
            context.drawTextWithShadow(mc.textRenderer, bindMsg,
                (int)(x + width - 11f - mc.textRenderer.getStringWidth(bindMsg)),
                (int)textY, Color.WHITE.getRGB());
            context.drawTextWithShadow(mc.textRenderer, "Press key...",
                (int)(x + 6f), (int)textY, Color.WHITE.getRGB());
        }
        
        if (!binding) {
            context.drawTextWithShadow(mc.textRenderer, module.getName(),
                (int)(x + 6f), (int)textY, module.isEnabled() ? Color.CYAN.getRGB() : Color.WHITE.getRGB());
        }
        
        if (isOpen()) {
            float panelHeight = height + getElementsHeight();
            float guideClipLeft = x + 4f;
            float guideClipRight = x + width - 4f;
            float guideClipBottom = y + panelHeight;
            
            drawRoundedRect(context.getMatrices(), x + 4f, y + 1f, width - 8f, panelHeight - 2f, 4f, new Color(30, 30, 40, 180));
            
            float offsetY = 0f;
            for (AbstractElement element : elements) {
                if (element.isVisible()) {
                    element.setOffsetY(offsetY);
                    element.setX(x);
                    element.setY(y + height);
                    element.setWidth(width);
                    element.setHeight(13f);
                    
                    if (element instanceof ModeElement combobox) {
                        element.setHeight(combobox.getExpandedHeight());
                    } else if (element instanceof ColorPickerElement picker) {
                        element.setHeight(picker.getHeight());
                    } else if (element instanceof SliderElement) {
                        element.setHeight(18f);
                    }
                    
                    element.render(context, mouseX, mouseY, delta);
                    offsetY += element.getHeight();
                }
            }
        }
    }
    
    private String getBindText() {
        if (module.getBind() == null || module.getBind().getKey() == -1) return "None";
        String keyName = GLFW.glfwGetKeyName(module.getBind().getKey(), 0);
        return keyName == null ? "Key" : keyName.toUpperCase();
    }
    
    private void drawRoundedRect(MatrixStack matrices, float x, float y, float w, float h, float r, Color color) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.pop();
    }
    
    private boolean isHovered(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    public void setContentArea(float x, float y, float width, float height) {
        this.contentX = x;
        this.contentY = y;
        this.contentWidth = width;
        this.contentHeight = height;
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (binding) {
            if (button == 0 || button == 1 || button == 2) {
                module.setBind(button, true, holdbind);
                binding = false;
            }
            return;
        }
        
        if (hovered) {
            if (button == 0 && module.isToggleable()) {
                module.toggle();
            } else if (button == 1 && !elements.isEmpty()) {
                setOpen(!isOpen());
            } else if (button == 2) {
                binding = true;
                holdbind = false;
            }
        }
        
        if (isOpen()) {
            elements.forEach(e -> {
                if (e.isVisible()) e.mouseClicked(mouseX, mouseY, button);
            });
        }
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (isOpen()) {
            elements.forEach(e -> e.mouseReleased(mouseX, mouseY, button));
        }
    }
    
    @Override
    public void keyTyped(int keyCode) {
        if (binding) {
            if (keyCode != 256 && keyCode != 261) {
                module.setBind(keyCode, false, holdbind);
            } else {
                module.setBind(-1, false, holdbind);
            }
            binding = false;
        }
        
        if (isOpen()) {
            elements.forEach(e -> e.keyTyped(keyCode));
        }
    }
    
    @Override
    public void charTyped(char key, int keyCode) {
        if (isOpen()) {
            elements.forEach(e -> e.charTyped(key, keyCode));
        }
    }
    
    @Override
    public void onGuiClosed() {
        elements.forEach(AbstractElement::onClose);
    }
    
    @Override
    public void tick() {
        elements.forEach(AbstractElement::getSetting);
    }
    
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    
    public float getElementsHeight() { return categoryAnimation; }
    public float getTargetElementsHeight() {
        float target = 0f;
        for (AbstractElement e : elements) {
            if (e.isVisible()) target += e.getHeight();
        }
        return target;
    }
                        }
