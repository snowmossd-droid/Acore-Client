package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.FriendManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.combat.Aura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.*;

/**
 * CatLean-style ClickGUI
 *
 * Layout (matching screenshots):
 *   ┌─────────────────────────────────────────────────────┐
 *   │  [UI] [Windows] [Hud] [Theme] [Search]   (top bar)  │
 *   ├──────────┬──────────┬────────────┬───────────────────┤
 *   │  Pvp     │ Movement │   Visual   │    Utility        │
 *   │ [Attack] │ [Basic]  │[Cosmetic]  │ [World]           │
 *   │ [Legit]  │ [Rage]   │ [Esp]      │ [Equip]           │
 *   │ [Protect]│          │            │ [Player][Misc]    │
 *   │  items…  │  items…  │  items…    │   items…          │
 *   └──────────┴──────────┴────────────┴───────────────────┘
 *
 *   When a module is right-clicked (or clicked in PVP Attack area),
 *   a settings panel slides in over that column.
 */
public class ClickGUI extends Screen {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ──────────────────────────────────────────────────────────────
    // Colour palette  (0xAARRGGBB)
    // ──────────────────────────────────────────────────────────────
    private static final int COL_TOP_BAR      = 0xCC1A1A2E;
    private static final int COL_TOP_BTN_HOV  = 0xFF252540;
    private static final int COL_TOP_BTN_SEL  = 0xFF3A3A5A;

    private static final int COL_PANEL_BG     = 0xCC1C1C30;   // semi-transparent dark
    private static final int COL_PANEL_HEADER = 0xDD202035;
    private static final int COL_TAB_SEL      = 0xFF2A2A45;
    private static final int COL_TAB_HOV      = 0xFF232338;

    private static final int COL_ITEM_HOV     = 0xFF252540;
    private static final int COL_ITEM_ACTIVE  = 0xFF2E2E50;

    private static final int COL_ACCENT       = 0xFF4ECDC4;   // teal – same as CatLean toggle cyan
    private static final int COL_ACCENT_DIM   = 0xFF2A7A76;

    private static final int COL_TOGGLE_ON    = 0xFF4ECDC4;
    private static final int COL_TOGGLE_OFF   = 0xFF3A3A5A;

    private static final int COL_SLIDER_BG    = 0xFF2A2A45;
    private static final int COL_SLIDER_FILL  = 0xFF4ECDC4;
    private static final int COL_SLIDER_KNOB  = 0xFFFFFFFF;

    private static final int COL_BORDER       = 0xFF2E2E4E;
    private static final int COL_TEXT         = 0xFFFFFFFF;
    private static final int COL_TEXT_DIM     = 0xFFAAAAAA;
    private static final int COL_TEXT_HINT    = 0xFF555577;
    private static final int COL_TEXT_HEADER  = 0xFF4ECDC4;

    private static final int COL_SETTINGS_BG  = 0xDD1A1A2E;
    private static final int COL_DROPDOWN_BG  = 0xFF1E1E35;
    private static final int COL_DROPDOWN_HOV = 0xFF2A2A45;

    // ──────────────────────────────────────────────────────────────
    // Layout constants
    // ──────────────────────────────────────────────────────────────
    private static final int TOP_H      = 24;   // top navigation bar height
    private static final int PANEL_W    = 185;  // width of each column panel
    private static final int PANEL_GAP  = 6;    // gap between panels
    private static final int PANEL_TOP  = TOP_H + 8;
    private static final int HEADER_H   = 22;   // panel title row
    private static final int TAB_H      = 18;   // sub-tab row
    private static final int ITEM_H     = 22;   // module list row height
    private static final int PADDING    = 8;

    private static final int SETTING_PANEL_W  = 200;
    private static final int SETTING_ITEM_H   = 26;

    // ──────────────────────────────────────────────────────────────
    // Top-bar tabs
    // ──────────────────────────────────────────────────────────────
    private static final String[] TOP_TABS = {"UI", "Windows", "Hud", "Theme", "Search"};
    private int selectedTopTab = 0;

    // ──────────────────────────────────────────────────────────────
    // Column definitions
    // ──────────────────────────────────────────────────────────────
    private static final ColumnDef[] COLUMNS = {
        new ColumnDef("Pvp",      new String[]{"Attack","Legit","Protect"}, new String[][]{
            {"Aura","Auto Base Place","Auto Crystal","Auto Dripstone","Auto Mine","Auto Netherite Scrap","Auto Sweet Berries","Auto Web","Breach Swap","Criticals","Hole Filler","Mace Killer","More Knockback","Trap"},
            {"Legit Aura","No Hitbox","Trigger Bot"},
            {"Anti Surround","Surround","Hole Filler"}
        }),
        new ColumnDef("Movement", new String[]{"Basic","Rage"}, new String[][]{
            {"Anti Knock Back","Avoid","Elytra Recast","Gui Move","Hole Anchor","Infinite Elytra","Legit Strafe","Move Fix","No Push","Sprint","Wind Hop"},
            {"Bhop","Jesus","Longjump","No Fall","Step","Tp Mine","Velocity"}
        }),
        new ColumnDef("Visual",   new String[]{"Cosmetic","Esp"}, new String[][]{
            {"Aspect","Crystal Chams","Damage Particles","Damage Tint","Dismemberment","Free Look","Full Bright","Hand Chams","Hit Fx","No Camera Clip","Particles","Pop Chams","Swing Animations","Throw Petard","Totem Animation","Trajectories","View Model","World Renderer"},
            {"ESP","Cave Finder","Chams","Nametag","Tracers","Waypoints"}
        }),
        new ColumnDef("Utility",  new String[]{"World","Equip","Player","Misc"}, new String[][]{
            {"Auto Bone Meal","Auto Land","Auto Shear","Auto Sign","Fake Player","Free Cam","Ft Helper","No Render","Nuker","Pearl Chaser","Soil Ripper","Tps Sync","Zoom"},
            {"Auto Armor","Auto Drop","Auto Tool","Chest Stealer","Inv Manager"},
            {"Anti AFK","Auto Eat","Auto Fish","Auto Respawn","Blink","No Rotate","Packet Fly"},
            {"Chat Log","Command List","Server Spoof","Timer"}
        })
    };

    // per-column selected sub-tab index
    private int[] colSubTab = {0, 0, 0, 0};

    // enabled modules set (in a real mod this would come from ModuleManager)
    private final Set<String> enabledModules = new HashSet<>();

    // ──────────────────────────────────────────────────────────────
    // Settings panel state
    // ──────────────────────────────────────────────────────────────
    private String openSettingsModule = null;  // null = no panel open
    private int    settingsPanelCol   = -1;    // which column the panel belongs to
    private int    settingsPanelX     = 0;
    private int    settingsPanelY     = 0;
    private List<SettingItem> openSettings = new ArrayList<>();

    // dragging settings panel
    private boolean draggingSettings = false;
    private int dragOffX, dragOffY;

    // slider dragging
    private int  draggingSliderIdx = -1;
    private int  draggingSliderX0  = 0;
    private int  draggingSliderW   = 0;

    // dropdown open
    private int  openDropdownIdx   = -1;

    // search
    private boolean searchOpen = false;
    private String  searchText = "";

    // keybind binding
    private String  bindingModuleName = null;
    private boolean isBinding         = false;

    // ──────────────────────────────────────────────────────────────
    // Animation
    // ──────────────────────────────────────────────────────────────
    private float animTick = 0f;

    // ──────────────────────────────────────────────────────────────
    public ClickGUI() {
        super(Text.literal("CatLean GUI"));
    }

    // ═══════════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        animTick += delta;

        // dim background slightly
        ctx.fill(0, 0, width, height, 0x55000000);

        // top bar
        renderTopBar(ctx, mx, my);

        // column panels
        int totalW = COLUMNS.length * PANEL_W + (COLUMNS.length - 1) * PANEL_GAP;
        int startX = (width - totalW) / 2;

        for (int c = 0; c < COLUMNS.length; c++) {
            int panelX = startX + c * (PANEL_W + PANEL_GAP);
            renderColumn(ctx, mx, my, c, panelX, PANEL_TOP);
        }

        // settings panel (floating)
        if (openSettingsModule != null) {
            renderSettingsPanel(ctx, mx, my);
        }

        // keybind overlay
        if (isBinding && bindingModuleName != null) {
            renderKeybindOverlay(ctx);
        }

        // search overlay
        if (searchOpen) {
            renderSearchOverlay(ctx, mx, my);
        }

        super.render(ctx, mx, my, delta);
    }

    // ──────────────────────────────────────────────────────────────
    // Top navigation bar
    // ──────────────────────────────────────────────────────────────
    private void renderTopBar(DrawContext ctx, int mx, int my) {
        ctx.fill(0, 0, width, TOP_H, COL_TOP_BAR);
        ctx.fill(0, TOP_H, width, TOP_H + 1, COL_BORDER);

        int totalW = COLUMNS.length * PANEL_W + (COLUMNS.length - 1) * PANEL_GAP;
        int centerX = (width - totalW) / 2;

        // compute tabs centred above panels
        int tabW = 60, tabH = 18, tabY = (TOP_H - tabH) / 2;
        int tabsTotal = TOP_TABS.length * tabW + (TOP_TABS.length - 1) * 4;
        int tabStartX = (width - tabsTotal) / 2;

        for (int i = 0; i < TOP_TABS.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            boolean sel = i == selectedTopTab;
            boolean hov = mx >= tx && mx < tx + tabW && my >= tabY && my < tabY + tabH;

            int bg = sel ? COL_TOP_BTN_SEL : (hov ? COL_TOP_BTN_HOV : 0);
            if (bg != 0) ctx.fill(tx, tabY, tx + tabW, tabY + tabH, bg);
            if (sel) {
                ctx.fill(tx + 2, tabY + tabH - 2, tx + tabW - 2, tabY + tabH, COL_ACCENT);
            }

            int textColor = sel ? COL_ACCENT : (hov ? COL_TEXT : COL_TEXT_DIM);
            int strW = textRenderer.getWidth(TOP_TABS[i]);
            ctx.drawText(textRenderer, TOP_TABS[i], tx + (tabW - strW) / 2, tabY + 5, textColor, false);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // One column panel
    // ──────────────────────────────────────────────────────────────
    private void renderColumn(DrawContext ctx, int mx, int my, int col, int px, int py) {
        ColumnDef cd = COLUMNS[col];

        // max visible rows to determine panel height
        String[] items = cd.items[colSubTab[col]];
        int contentH = items.length * ITEM_H + 6;
        int panelH = HEADER_H + TAB_H + contentH;
        int panelBottom = py + panelH;

        // panel background + border
        ctx.fill(px, py, px + PANEL_W, panelBottom, COL_PANEL_BG);
        drawBorder(ctx, px, py, PANEL_W, panelH, COL_BORDER);

        // header row
        ctx.fill(px, py, px + PANEL_W, py + HEADER_H, COL_PANEL_HEADER);
        int titleW = textRenderer.getWidth(cd.name);
        ctx.drawText(textRenderer, cd.name, px + (PANEL_W - titleW) / 2, py + 7, COL_TEXT_HEADER, false);

        // sub-tabs row
        int tabY = py + HEADER_H;
        ctx.fill(px, tabY, px + PANEL_W, tabY + TAB_H, COL_PANEL_HEADER);
        ctx.fill(px, tabY + TAB_H - 1, px + PANEL_W, tabY + TAB_H, COL_BORDER);

        int nTabs = cd.tabs.length;
        int tw = PANEL_W / nTabs;
        for (int t = 0; t < nTabs; t++) {
            int tx = px + t * tw;
            int tRight = (t == nTabs - 1) ? px + PANEL_W : tx + tw;
            boolean sel = t == colSubTab[col];
            boolean hov = mx >= tx && mx < tRight && my >= tabY && my < tabY + TAB_H;

            int bg = sel ? COL_TAB_SEL : (hov ? COL_TAB_HOV : 0);
            if (bg != 0) ctx.fill(tx, tabY, tRight, tabY + TAB_H, bg);
            if (sel) ctx.fill(tx + 1, tabY + TAB_H - 2, tRight - 1, tabY + TAB_H - 1, COL_ACCENT);

            int strW = textRenderer.getWidth(cd.tabs[t]);
            int tabColor = sel ? COL_TEXT : (hov ? COL_TEXT_DIM : COL_TEXT_HINT);
            ctx.drawText(textRenderer, cd.tabs[t], tx + (tRight - tx - strW) / 2, tabY + 5, tabColor, false);
        }

        // module items
        int iy = tabY + TAB_H + 3;
        for (String item : items) {
            boolean enabled = enabledModules.contains(item);
            boolean hov = mx >= px && mx < px + PANEL_W && my >= iy && my < iy + ITEM_H;
            boolean isOpen = item.equals(openSettingsModule);

            int bg = isOpen ? COL_ITEM_ACTIVE : (hov ? COL_ITEM_HOV : 0);
            if (bg != 0) ctx.fill(px, iy, px + PANEL_W, iy + ITEM_H, bg);

            // enabled indicator bar (left edge)
            if (enabled) ctx.fill(px, iy + 2, px + 3, iy + ITEM_H - 2, COL_ACCENT);

            // item name
            int txtColor = enabled ? COL_ACCENT : (hov ? COL_TEXT : COL_TEXT_DIM);
            ctx.drawText(textRenderer, item, px + PADDING, iy + 7, txtColor, false);

            // settings arrow (right side, shown on hover or if open)
            if (hov || isOpen) {
                ctx.drawText(textRenderer, "···", px + PANEL_W - 20, iy + 7, COL_TEXT_HINT, false);
            }

            iy += ITEM_H;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Settings panel (floating, draggable)
    // ──────────────────────────────────────────────────────────────
    private void renderSettingsPanel(DrawContext ctx, int mx, int my) {
        int pw = SETTING_PANEL_W;
        int totalH = 26 + openSettings.size() * SETTING_ITEM_H + 8;
        int px = settingsPanelX, py = settingsPanelY;

        // clamp to screen
        px = Math.max(0, Math.min(width - pw, px));
        py = Math.max(TOP_H, Math.min(height - totalH, py));
        settingsPanelX = px; settingsPanelY = py;

        // shadow
        ctx.fill(px + 4, py + 4, px + pw + 4, py + totalH + 4, 0x44000000);

        // background
        ctx.fill(px, py, px + pw, py + totalH, COL_SETTINGS_BG);
        drawBorder(ctx, px, py, pw, totalH, COL_ACCENT_DIM);

        // header
        ctx.fill(px, py, px + pw, py + 22, 0xFF181828);
        ctx.fill(px, py, px + 3, py + 22, COL_ACCENT);
        ctx.drawText(textRenderer, openSettingsModule, px + 8, py + 7, COL_ACCENT, false);
        // close button
        boolean closeHov = mx >= px + pw - 16 && mx < px + pw - 4 && my >= py + 4 && my < py + 18;
        ctx.drawText(textRenderer, "✕", px + pw - 14, py + 7, closeHov ? COL_TEXT : COL_TEXT_DIM, false);

        // divider
        ctx.fill(px, py + 22, px + pw, py + 23, COL_BORDER);

        // settings items
        int iy = py + 26;
        for (int i = 0; i < openSettings.size(); i++) {
            SettingItem s = openSettings.get(i);
            boolean hov = mx >= px && mx < px + pw && my >= iy && my < iy + SETTING_ITEM_H;
            if (hov) ctx.fill(px, iy, px + pw, iy + SETTING_ITEM_H, 0xFF222238);

            ctx.drawText(textRenderer, s.name, px + 8, iy + 8, COL_TEXT_DIM, false);

            switch (s.type) {
                case TOGGLE: {
                    boolean val = (Boolean) s.value;
                    int tgW = 28, tgH = 12;
                    int tgX = px + pw - tgW - 8, tgY = iy + (SETTING_ITEM_H - tgH) / 2;
                    ctx.fill(tgX, tgY, tgX + tgW, tgY + tgH, val ? COL_TOGGLE_ON : COL_TOGGLE_OFF);
                    // knob
                    int knobX = val ? tgX + tgW - tgH : tgX;
                    ctx.fill(knobX + 1, tgY + 1, knobX + tgH - 1, tgY + tgH - 1, COL_SLIDER_KNOB);
                    break;
                }
                case SLIDER:
                case SLIDER_INT: {
                    float val = ((Number) s.value).floatValue();
                    float pct = Math.max(0, Math.min(1, (val - s.min) / (s.max - s.min)));
                    int slW = 70, slH = 4;
                    int slX = px + pw - slW - 8, slY = iy + (SETTING_ITEM_H - slH) / 2;
                    ctx.fill(slX, slY, slX + slW, slY + slH, COL_SLIDER_BG);
                    ctx.fill(slX, slY, slX + (int)(slW * pct), slY + slH, COL_SLIDER_FILL);
                    // knob circle approximation (2px square)
                    int kx = slX + (int)(slW * pct) - 2;
                    ctx.fill(kx, slY - 2, kx + 4, slY + slH + 2, COL_SLIDER_KNOB);
                    // value label
                    String vStr = s.type == SettingType.SLIDER_INT
                        ? String.valueOf((int) val)
                        : String.format("%.1f", val);
                    ctx.drawText(textRenderer, vStr, slX - textRenderer.getWidth(vStr) - 4, iy + 8, COL_TEXT_DIM, false);
                    // store coords for drag
                    if (i == draggingSliderIdx) { draggingSliderX0 = slX; draggingSliderW = slW; }
                    break;
                }
                case MODE: {
                    String val = (String) s.value;
                    int btnW = Math.min(textRenderer.getWidth(val) + 14, 80);
                    int btnX = px + pw - btnW - 8, btnY = iy + 5;
                    ctx.fill(btnX, btnY, btnX + btnW, btnY + 14, COL_TOGGLE_OFF);
                    drawBorder(ctx, btnX, btnY, btnW, 14, COL_BORDER);
                    ctx.drawText(textRenderer, val, btnX + 4, btnY + 3, COL_TEXT_DIM, false);
                    // dropdown chevron
                    ctx.drawText(textRenderer, "▾", btnX + btnW - 10, btnY + 3, COL_TEXT_HINT, false);

                    // open dropdown?
                    if (i == openDropdownIdx) {
                        String[] opts = (String[]) s.extra;
                        int dY = btnY + 14;
                        ctx.fill(btnX, dY, btnX + btnW, dY + opts.length * 12 + 4, COL_DROPDOWN_BG);
                        drawBorder(ctx, btnX, dY, btnW, opts.length * 12 + 4, COL_BORDER);
                        for (int o = 0; o < opts.length; o++) {
                            boolean oh = mx >= btnX && mx < btnX + btnW && my >= dY + 2 + o * 12 && my < dY + 2 + o * 12 + 12;
                            if (oh) ctx.fill(btnX, dY + 2 + o * 12, btnX + btnW, dY + 2 + o * 12 + 12, COL_DROPDOWN_HOV);
                            boolean sel = opts[o].equals(val);
                            ctx.drawText(textRenderer, opts[o], btnX + 4, dY + 4 + o * 12, sel ? COL_ACCENT : COL_TEXT_DIM, false);
                        }
                    }
                    break;
                }
                default: break;
            }
            iy += SETTING_ITEM_H;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Keybind overlay
    // ──────────────────────────────────────────────────────────────
    private void renderKeybindOverlay(DrawContext ctx) {
        ctx.fill(0, 0, width, height, 0xCC000000);
        int bw = 260, bh = 80;
        int bx = (width - bw) / 2, by = (height - bh) / 2;
        ctx.fill(bx, by, bx + bw, by + bh, COL_SETTINGS_BG);
        drawBorder(ctx, bx, by, bw, bh, COL_ACCENT);
        ctx.fill(bx, by, bx + bw, by + 22, 0xFF181828);
        ctx.drawText(textRenderer, "§lSet Keybind", bx + 12, by + 7, COL_ACCENT, false);
        ctx.drawText(textRenderer, "Module: " + bindingModuleName, bx + 12, by + 30, COL_TEXT, false);
        ctx.drawText(textRenderer, "Press any key…", bx + 12, by + 46, COL_TEXT_DIM, false);
        ctx.drawText(textRenderer, "ESC to cancel", bx + 12, by + 62, COL_TEXT_HINT, false);
    }

    // ──────────────────────────────────────────────────────────────
    // Search overlay
    // ──────────────────────────────────────────────────────────────
    private void renderSearchOverlay(DrawContext ctx, int mx, int my) {
        ctx.fill(0, 0, width, height, 0xBB000000);
        int bw = 300, inputY = TOP_H + 20;
        int bx = (width - bw) / 2;
        ctx.fill(bx, inputY, bx + bw, inputY + 22, COL_SETTINGS_BG);
        drawBorder(ctx, bx, inputY, bw, 22, COL_ACCENT);
        ctx.drawText(textRenderer,
            searchText.isEmpty() ? "§7Search modules…" : searchText + "█",
            bx + 8, inputY + 7, searchText.isEmpty() ? COL_TEXT_HINT : COL_TEXT, false);

        if (!searchText.isEmpty()) {
            int ry = inputY + 28;
            for (ColumnDef cd : COLUMNS) {
                for (String[] group : cd.items) {
                    for (String item : group) {
                        if (item.toLowerCase().contains(searchText.toLowerCase())) {
                            ctx.fill(bx, ry, bx + bw, ry + 18, COL_ITEM_HOV);
                            ctx.drawText(textRenderer, item, bx + 8, ry + 5, COL_TEXT, false);
                            ry += 18;
                            if (ry > height - 20) break;
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MOUSE EVENTS
    // ═══════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        // ── keybind/search overlays consume all input ──
        if (isBinding) return true;
        if (searchOpen) {
            if (button == 1) { searchOpen = false; searchText = ""; }
            return true;
        }

        // ── top-bar tabs ──
        int nTabs = TOP_TABS.length;
        int tabW = 60, tabH = 18, tabY = (TOP_H - tabH) / 2;
        int tabsTotal = nTabs * tabW + (nTabs - 1) * 4;
        int tabStartX = (width - tabsTotal) / 2;
        if (my >= tabY && my < tabY + tabH) {
            for (int i = 0; i < nTabs; i++) {
                int tx = tabStartX + i * (tabW + 4);
                if (mx >= tx && mx < tx + tabW) {
                    if (TOP_TABS[i].equals("Search")) {
                        searchOpen = true;
                        searchText = "";
                    } else {
                        selectedTopTab = i;
                        SoundManager.playClickSound();
                    }
                    return true;
                }
            }
        }

        // ── close/drag settings panel ──
        if (openSettingsModule != null) {
            int pw = SETTING_PANEL_W;
            int totalH = 26 + openSettings.size() * SETTING_ITEM_H + 8;
            int px = settingsPanelX, py = settingsPanelY;

            // close button
            if (mx >= px + pw - 16 && mx < px + pw - 4 && my >= py + 4 && my < py + 18) {
                openSettingsModule = null;
                openSettings.clear();
                openDropdownIdx = -1;
                SoundManager.playClickSound();
                return true;
            }

            // begin drag from header
            if (mx >= px && mx < px + pw && my >= py && my < py + 22) {
                draggingSettings = true;
                dragOffX = mx - px; dragOffY = my - py;
                return true;
            }

            // settings item interactions
            if (mx >= px && mx < px + pw && my >= py + 26 && my < py + 26 + openSettings.size() * SETTING_ITEM_H) {
                int idx = (my - (py + 26)) / SETTING_ITEM_H;
                if (idx >= 0 && idx < openSettings.size()) {
                    SettingItem s = openSettings.get(idx);
                    handleSettingClick(s, idx, mx, my, px, py, button);
                    return true;
                }
            }

            // click outside panel closes it
            if (mx < px || mx > px + pw || my < py || my > py + totalH) {
                if (button == 1) { openSettingsModule = null; openSettings.clear(); openDropdownIdx = -1; return true; }
            }
        }

        // ── column panels ──
        int totalW = COLUMNS.length * PANEL_W + (COLUMNS.length - 1) * PANEL_GAP;
        int startX = (width - totalW) / 2;

        for (int c = 0; c < COLUMNS.length; c++) {
            int panelX = startX + c * (PANEL_W + PANEL_GAP);
            ColumnDef cd = COLUMNS[c];

            // sub-tab click
            int tabRowY = PANEL_TOP + HEADER_H;
            if (my >= tabRowY && my < tabRowY + TAB_H && mx >= panelX && mx < panelX + PANEL_W) {
                int nT = cd.tabs.length;
                int tw = PANEL_W / nT;
                int t = (mx - panelX) / tw;
                if (t >= 0 && t < nT) {
                    colSubTab[c] = t;
                    SoundManager.playClickSound();
                    return true;
                }
            }

            // module item click
            String[] items = cd.items[colSubTab[c]];
            int iy = PANEL_TOP + HEADER_H + TAB_H + 3;
            for (int i = 0; i < items.length; i++) {
                if (mx >= panelX && mx < panelX + PANEL_W && my >= iy && my < iy + ITEM_H) {
                    String mod = items[i];
                    if (button == 0) {
                        // left click = toggle enable
                        if (enabledModules.contains(mod)) enabledModules.remove(mod);
                        else enabledModules.add(mod);
                        SoundManager.playClickSound();
                    } else if (button == 1) {
                        // right click = open settings
                        openSettingsFor(mod, panelX + PANEL_W + 4, iy);
                        SoundManager.playClickSound();
                    }
                    return true;
                }
                iy += ITEM_H;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        int mx = (int) mouseX, my = (int) mouseY;

        if (draggingSettings) {
            settingsPanelX = mx - dragOffX;
            settingsPanelY = my - dragOffY;
            return true;
        }

        if (draggingSliderIdx >= 0 && draggingSliderIdx < openSettings.size()) {
            SettingItem s = openSettings.get(draggingSliderIdx);
            float pct = Math.max(0, Math.min(1, (float)(mx - draggingSliderX0) / draggingSliderW));
            float val = s.min + pct * (s.max - s.min);
            s.value = (s.type == SettingType.SLIDER_INT) ? (int) val : val;
            applySettingToModule(openSettingsModule, s);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSettings = false;
        draggingSliderIdx = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ═══════════════════════════════════════════════════════════════
    // KEY EVENTS
    // ═══════════════════════════════════════════════════════════════
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // keybind overlay
        if (isBinding && bindingModuleName != null) {
            if (keyCode == 256) { isBinding = false; bindingModuleName = null; }
            else {
                Module mod = ModuleManager.getModule(bindingModuleName);
                if (mod != null) {
                    mod.setKeybind(keyCodeName(keyCode));
                    mod.setKeyCode(keyCode);
                }
                isBinding = false; bindingModuleName = null;
                SoundManager.playClickSound();
            }
            return true;
        }

        // search overlay
        if (searchOpen) {
            if (keyCode == 256) { searchOpen = false; searchText = ""; return true; }
            if (keyCode == 257 || keyCode == 335) { searchOpen = false; return true; }
            if (keyCode == 259 && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
            return true;
        }

        // ESC closes GUI
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchOpen && (Character.isLetterOrDigit(chr) || chr == ' ')) {
            searchText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // ═══════════════════════════════════════════════════════════════
    // SETTINGS PANEL HELPERS
    // ═══════════════════════════════════════════════════════════════
    private void openSettingsFor(String moduleName, int preferredX, int preferredY) {
        openSettingsModule = moduleName;
        settingsPanelX = Math.min(preferredX, width - SETTING_PANEL_W - 4);
        settingsPanelY = Math.max(TOP_H + 4, Math.min(preferredY, height - 200));
        openDropdownIdx = -1;
        openSettings = buildSettingsFor(moduleName);
    }

    /** Returns a generic or module-specific settings list. */
    private List<SettingItem> buildSettingsFor(String modName) {
        List<SettingItem> list = new ArrayList<>();

        // Try to get real Aura settings
        if (modName.equals("Aura")) {
            Aura aura = (Aura) ModuleManager.getModule("Aura");
            if (aura != null) {
                list.add(new SettingItem("Range",        aura.range,        1f, 6f,  SettingType.SLIDER));
                list.add(new SettingItem("FOV",          (float)aura.fov,   1f, 180f,SettingType.SLIDER_INT));
                list.add(new SettingItem("Through Walls",aura.throughWalls,         SettingType.TOGGLE));
                list.add(new SettingItem("Rotation Mode",aura.rotationMode.name(), new String[]{"NONE","LEGIT","NORMAL"}, SettingType.MODE));
                list.add(new SettingItem("Aim Mode",     aura.aimMode.name(),       new String[]{"HEAD","BODY","LEGS"},   SettingType.MODE));
                list.add(new SettingItem("Attack Chance",(float)100,        0f, 100f,SettingType.SLIDER_INT));
                list.add(new SettingItem("Smooth Rotation",aura.smoothRotation,     SettingType.TOGGLE));
                list.add(new SettingItem("Shield Breaker",aura.shieldBreaker,       SettingType.TOGGLE));
                list.add(new SettingItem("Auto Weapon",  aura.autoWeapon,           SettingType.TOGGLE));
                list.add(new SettingItem("Only Weapon",  aura.onlyWeapon,           SettingType.TOGGLE));
                list.add(new SettingItem("Pause While Eating",aura.pauseWhileEating,SettingType.TOGGLE));
                list.add(new SettingItem("Pause In Inventory",aura.pauseInInventory,SettingType.TOGGLE));
                list.add(new SettingItem("Disable On Death",true,                   SettingType.TOGGLE));
                list.add(new SettingItem("Sprint",       "Normal", new String[]{"Normal","None","Always"}, SettingType.MODE));
                list.add(new SettingItem("Esp",          "Comets", new String[]{"None","Comets","Box"},    SettingType.MODE));
                list.add(new SettingItem("Sort",         aura.sort.name(),          new String[]{"Distance","LowestHealth","HighestHealth"}, SettingType.MODE));
                return list;
            }
        }

        // Fallback generic settings
        list.add(new SettingItem("Enabled",   true, SettingType.TOGGLE));
        list.add(new SettingItem("Range",     3.0f, 1f, 6f,  SettingType.SLIDER));
        list.add(new SettingItem("Speed",     1.0f, 0f, 5f,  SettingType.SLIDER));
        list.add(new SettingItem("Mode",      "Default", new String[]{"Default","Silent","Strict"}, SettingType.MODE));
        list.add(new SettingItem("Ignore Walls", false, SettingType.TOGGLE));
        return list;
    }

    private void handleSettingClick(SettingItem s, int idx, int mx, int my,
                                     int px, int py, int button) {
        int iy = py + 26 + idx * SETTING_ITEM_H;

        switch (s.type) {
            case TOGGLE: {
                s.value = !(Boolean) s.value;
                applySettingToModule(openSettingsModule, s);
                SoundManager.playClickSound();
                break;
            }
            case SLIDER:
            case SLIDER_INT: {
                int slW = 70, slX = px + SETTING_PANEL_W - slW - 8;
                if (mx >= slX - 4 && mx <= slX + slW + 4) {
                    draggingSliderIdx = idx;
                    draggingSliderX0  = slX;
                    draggingSliderW   = slW;
                    float pct = Math.max(0, Math.min(1, (float)(mx - slX) / slW));
                    float val = s.min + pct * (s.max - s.min);
                    s.value = (s.type == SettingType.SLIDER_INT) ? (int) val : val;
                    applySettingToModule(openSettingsModule, s);
                    SoundManager.playClickSound();
                }
                break;
            }
            case MODE: {
                if (openDropdownIdx == idx) {
                    // check dropdown options
                    String[] opts = (String[]) s.extra;
                    int btnW = Math.min(textRenderer.getWidth((String)s.value) + 14, 80);
                    int btnX = px + SETTING_PANEL_W - btnW - 8, btnY = iy + 5;
                    int dY = btnY + 14;
                    for (int o = 0; o < opts.length; o++) {
                        if (mx >= btnX && mx < btnX + btnW && my >= dY + 2 + o * 12 && my < dY + 2 + o * 12 + 12) {
                            s.value = opts[o];
                            applySettingToModule(openSettingsModule, s);
                            SoundManager.playClickSound();
                        }
                    }
                    openDropdownIdx = -1;
                } else {
                    openDropdownIdx = idx;
                }
                break;
            }
            default: break;
        }
    }

    /** Apply a setting value back to the actual module (Aura-specific + generic). */
    private void applySettingToModule(String modName, SettingItem s) {
        if (modName == null) return;
        Module mod = ModuleManager.getModule(modName);
        if (mod == null) return;

        if (mod instanceof Aura) {
            Aura a = (Aura) mod;
            switch (s.name) {
                case "Range":              a.range           = (float)    ((Number) s.value).floatValue(); break;
                case "FOV":                a.fov             = (int)      ((Number) s.value).floatValue(); break;
                case "Through Walls":      a.throughWalls    = (boolean)  s.value; break;
                case "Smooth Rotation":    a.smoothRotation  = (boolean)  s.value; break;
                case "Shield Breaker":     a.shieldBreaker   = (boolean)  s.value; break;
                case "Auto Weapon":        a.autoWeapon      = (boolean)  s.value; break;
                case "Only Weapon":        a.onlyWeapon      = (boolean)  s.value; break;
                case "Pause While Eating": a.pauseWhileEating= (boolean)  s.value; break;
                case "Pause In Inventory": a.pauseInInventory= (boolean)  s.value; break;
                case "Rotation Mode": {
                    String v = (String) s.value;
                    if (v.equals("NONE"))  a.rotationMode = Aura.RotationMode.NONE;
                    else if (v.equals("LEGIT"))  a.rotationMode = Aura.RotationMode.LEGIT;
                    else a.rotationMode = Aura.RotationMode.NORMAL;
                    break;
                }
                case "Aim Mode": {
                    String v = (String) s.value;
                    if (v.equals("HEAD")) a.aimMode = Aura.AimMode.HEAD;
                    else if (v.equals("LEGS")) a.aimMode = Aura.AimMode.LEGS;
                    else a.aimMode = Aura.AimMode.BODY;
                    break;
                }
                case "Sort": {
                    String v = (String) s.value;
                    if (v.equals("LowestHealth"))  a.sort = Aura.SortMode.LowestHealth;
                    else if (v.equals("HighestHealth")) a.sort = Aura.SortMode.HighestHealth;
                    else a.sort = Aura.SortMode.LowestDistance;
                    break;
                }
                default: break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DRAW HELPERS
    // ═══════════════════════════════════════════════════════════════
    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,         y,         x + w,     y + 1,     color);  // top
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color);  // bottom
        ctx.fill(x,         y,         x + 1,     y + h,     color);  // left
        ctx.fill(x + w - 1, y,         x + w,     y + h,     color);  // right
    }

    private String keyCodeName(int keyCode) {
        if (keyCode >= 65 && keyCode <= 90)  return String.valueOf((char) keyCode);
        if (keyCode >= 48 && keyCode <= 57)  return String.valueOf((char) keyCode);
        switch (keyCode) {
            case 32: return "SPACE"; case 256: return "ESC"; case 257: return "ENTER";
            default: return "KEY_" + keyCode;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN OVERRIDES
    // ═══════════════════════════════════════════════════════════════
    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        super.close();
        ConfigManager.saveAllConfigs();
    }

    // ═══════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════
    private static class ColumnDef {
        final String   name;
        final String[] tabs;
        final String[][] items;
        ColumnDef(String name, String[] tabs, String[][] items) {
            this.name = name; this.tabs = tabs; this.items = items;
        }
    }

    private enum SettingType { TOGGLE, SLIDER, SLIDER_INT, MODE }

    private static class SettingItem {
        String name;
        Object value;
        float min, max;
        SettingType type;
        Object extra;   // String[] for MODE options

        SettingItem(String n, Object v, SettingType t)                     { name=n; value=v; type=t; }
        SettingItem(String n, Object v, float min, float max, SettingType t){ this(n,v,t); this.min=min; this.max=max; }
        SettingItem(String n, Object v, String[] opts, SettingType t)      { this(n,v,t); this.extra=opts; }
    }
}
