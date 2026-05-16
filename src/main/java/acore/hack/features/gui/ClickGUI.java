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
 * ClickGUI Style 1 – giống ảnh gốc
 * Đặc điểm:
 *   - Viền bo tròn (radius 6px giả lập bằng fill nhiều lớp)
 *   - Panel nhỏ gọn, các cột ngang
 *   - Keybind: click module → nhấn F/D/X/Z/C/T (hoặc bất kỳ phím nào)
 *   - Scroll chuột lên/xuống để cuộn danh sách module và settings
 *   - Nút ▲▼ để cuộn module list
 *   - Settings panel có scroll
 */
public class ClickGUI extends Screen {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ─── Màu sắc ────────────────────────────────────────────────────
    private static final int C_BG          = 0xCC151520;
    private static final int C_HEADER      = 0xDD1A1A2C;
    private static final int C_HEADER2     = 0xDD202035;
    private static final int C_TAB_SEL     = 0xFF26263D;
    private static final int C_TAB_HOV     = 0xFF1E1E30;
    private static final int C_ITEM_HOV    = 0xFF1F1F32;
    private static final int C_ITEM_ACT    = 0xFF252540;
    private static final int C_ACCENT      = 0xFF4ECDC4;
    private static final int C_ACCENT2     = 0xFF3BBAB2;
    private static final int C_BORDER      = 0xFF2A2A42;
    private static final int C_BORDER_ACC  = 0xFF3A3A5E;
    private static final int C_TEXT        = 0xFFEEEEEE;
    private static final int C_TEXT_DIM    = 0xFF888899;
    private static final int C_TEXT_HINT   = 0xFF444466;
    private static final int C_TOG_ON      = 0xFF4ECDC4;
    private static final int C_TOG_OFF     = 0xFF2E2E48;
    private static final int C_SL_BG       = 0xFF252540;
    private static final int C_SL_FILL     = 0xFF4ECDC4;
    private static final int C_DROP_BG     = 0xFF1C1C2E;
    private static final int C_DROP_HOV    = 0xFF252540;
    private static final int C_SET_BG      = 0xEE141422;
    private static final int C_TOP         = 0xDD111120;

    // ─── Layout ─────────────────────────────────────────────────────
    private static final int TOP_H      = 22;
    private static final int PANEL_W    = 130;   // nhỏ hơn ảnh gốc cho gọn
    private static final int PANEL_GAP  = 5;
    private static final int PANEL_TOP  = TOP_H + 6;
    private static final int HDR_H      = 20;
    private static final int TAB_H      = 16;
    private static final int ITEM_H     = 18;    // nhỏ như ảnh gốc
    private static final int PAD        = 6;
    private static final int RADIUS     = 6;
    private static final int SET_W      = 185;
    private static final int SET_ITEM_H = 22;
    private static final int SCROLL_BTN = 14;    // nút ▲▼

    // ─── Cột module ─────────────────────────────────────────────────
    private static final ColDef[] COLS = {
        new ColDef("Combat",   new String[]{"AimBot","AntiBan","AntiBot","Aura","AutoBuff","AutoCart","AutoGApple","AutoTrap","ChangeLock","CritAura","Delay","Mode","SwapBack"},
                               new String[]{"CrossBow","CloseBind"}),
        new ColDef("Movement", new String[]{"AirStack","AntiWeb","AutoSprint","AutoWalk","Blink","Elytra+","ElytraBoost","ElytraMotion","Flight","FreeLook","OutMove","MoveFix"},
                               new String[]{"LCtrl","M","I","H"}),
        new ColDef("Render",   new String[]{"Animations","Arrows","AspectRatio","BlockESP","ClientAnimation","Crosshair","ESP","FreeCam","FullBright","HUD","Hat","HitParticles"},
                               new String[]{"U"}),
        new ColDef("Player",   new String[]{"AntiAim","AntiAttack","AutoArmor","AutoEat","AutoRespawn","AutoTool","ClickAction","DurabilityAlert","FlytraHelper","FlytraReplace","HotbarReplenish","InventoryCleaner"},
                               new String[]{}),
        new ColDef("Misc",     new String[]{"AntiAFK","AntiCrash","AntiServerIP","AutoAuth","AutoDungeon","AutoTPaccept","ChestStealer","ClickOut","ClientSettings","ClientSound","ClientSpoof","DebugRecord"},
                               new String[]{})
    };

    // ─── State ──────────────────────────────────────────────────────
    private final Set<String>     enabled     = new HashSet<>();
    private final Map<String,String> keybinds = new HashMap<>();
    // scroll offset mỗi cột (số item bị ẩn phía trên)
    private final int[]           scrollOff   = new int[COLS.length];

    private String openMod      = null;
    private int    openModCol   = -1;
    private int    setX = 0, setY = 0;
    private List<SetItem> settings   = new ArrayList<>();
    private int    setScrollOff = 0;   // scroll settings panel

    private boolean dragSet = false;
    private int dragOX, dragOY;
    private int dragSlider = -1;
    private int dragSlX0, dragSlW;
    private int dropIdx = -1;

    // keybind
    private String bindMod  = null;
    private boolean binding = false;

    // search
    private boolean searchOpen = false;
    private String  searchText = "";

    // top tab
    private static final String[] TOP = {"Combat","Movement","Render","Player","Misc","Search"};
    private int topSel = 0;

    // ─── Visible rows (bao nhiêu item hiện trong 1 cột) ─────────────
    private static final int MAX_VISIBLE = 12;

    // ════════════════════════════════════════════════════════════════
    public ClickGUI() {
        super(Text.literal("ClickGUI"));
    }

    // ════════════════════════════════════════════════════════════════
    // RENDER
    // ════════════════════════════════════════════════════════════════
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // backdrop
        ctx.fill(0, 0, width, height, 0x66000000);

        renderTopBar(ctx, mx, my);

        int totalW = COLS.length * PANEL_W + (COLS.length - 1) * PANEL_GAP;
        int sx = (width - totalW) / 2;

        for (int c = 0; c < COLS.length; c++) {
            renderCol(ctx, mx, my, c, sx + c * (PANEL_W + PANEL_GAP), PANEL_TOP);
        }

        if (openMod != null)    renderSettings(ctx, mx, my);
        if (binding)            renderBindOverlay(ctx);
        if (searchOpen)         renderSearch(ctx, mx, my);

        super.render(ctx, mx, my, delta);
    }

    // ─── Top bar ────────────────────────────────────────────────────
    private void renderTopBar(DrawContext ctx, int mx, int my) {
        fillRounded(ctx, 0, 0, width, TOP_H, C_TOP, 0);
        ctx.fill(0, TOP_H, width, TOP_H + 1, C_BORDER);

        int tabW = 64, gap = 3;
        int total = TOP.length * tabW + (TOP.length - 1) * gap;
        int tx0 = (width - total) / 2;

        for (int i = 0; i < TOP.length; i++) {
            int tx = tx0 + i * (tabW + gap);
            int ty = 3;
            boolean sel = i == topSel;
            boolean hov = mx >= tx && mx < tx + tabW && my >= ty && my < ty + TOP_H - 6;
            int bg = sel ? C_TAB_SEL : (hov ? C_TAB_HOV : 0);
            if (bg != 0) fillRounded(ctx, tx, ty, tx + tabW, ty + TOP_H - 6, bg, 3);
            if (sel) ctx.fill(tx + 4, TOP_H - 4, tx + tabW - 4, TOP_H - 2, C_ACCENT);
            int tc = sel ? C_ACCENT : (hov ? C_TEXT : C_TEXT_DIM);
            int sw = textRenderer.getWidth(TOP[i]);
            ctx.drawText(textRenderer, TOP[i], tx + (tabW - sw) / 2, ty + 4, tc, false);
        }
    }

    // ─── Một cột panel ──────────────────────────────────────────────
    private void renderCol(DrawContext ctx, int mx, int my, int col, int px, int py) {
        ColDef cd = COLS[col];
        String[] all = cd.items;
        int visCount = Math.min(MAX_VISIBLE, all.length - scrollOff[col]);
        int contentH = visCount * ITEM_H + SCROLL_BTN * 2 + 4;
        int panelH   = HDR_H + contentH;

        // nền panel bo tròn
        fillRounded(ctx, px, py, px + PANEL_W, py + panelH, C_BG, RADIUS);
        drawRoundedBorder(ctx, px, py, PANEL_W, panelH, C_BORDER, RADIUS);

        // header
        fillRounded(ctx, px, py, px + PANEL_W, py + HDR_H, C_HEADER, RADIUS);
        ctx.fill(px, py + HDR_H - 4, px + PANEL_W, py + HDR_H, C_HEADER);
        int sw = textRenderer.getWidth(cd.name);
        ctx.drawText(textRenderer, cd.name, px + (PANEL_W - sw) / 2, py + 6, C_ACCENT, false);

        // nút scroll ▲ trên
        int upBtnY = py + HDR_H;
        boolean upHov = mx >= px && mx < px + PANEL_W && my >= upBtnY && my < upBtnY + SCROLL_BTN;
        ctx.fill(px, upBtnY, px + PANEL_W, upBtnY + SCROLL_BTN,
                 scrollOff[col] > 0 ? (upHov ? C_ITEM_HOV : C_HEADER2) : C_HEADER);
        int arrowColor = scrollOff[col] > 0 ? C_ACCENT : C_TEXT_HINT;
        int uaw = textRenderer.getWidth("▲");
        ctx.drawText(textRenderer, "▲", px + (PANEL_W - uaw) / 2, upBtnY + 3, arrowColor, false);

        // module items
        int iy = upBtnY + SCROLL_BTN + 2;
        for (int i = scrollOff[col]; i < Math.min(all.length, scrollOff[col] + MAX_VISIBLE); i++) {
            String mod = all[i];
            boolean en   = enabled.contains(mod);
            boolean hov  = mx >= px && mx < px + PANEL_W && my >= iy && my < iy + ITEM_H;
            boolean open = mod.equals(openMod);

            int bg = open ? C_ITEM_ACT : (hov ? C_ITEM_HOV : 0);
            if (bg != 0) ctx.fill(px, iy, px + PANEL_W, iy + ITEM_H, bg);

            // enabled bar bên trái
            if (en) {
                ctx.fill(px + 1, iy + 2, px + 3, iy + ITEM_H - 2, C_ACCENT);
            }

            // tên module
            int tc = en ? C_ACCENT : (hov ? C_TEXT : C_TEXT_DIM);
            ctx.drawText(textRenderer, mod, px + PAD, iy + 5, tc, false);

            // keybind label
            String kb = keybinds.get(mod);
            if (kb != null) {
                int kw = textRenderer.getWidth("[" + kb + "]");
                ctx.drawText(textRenderer, "[" + kb + "]", px + PANEL_W - kw - 3, iy + 5, C_TEXT_HINT, false);
            } else if (hov || open) {
                int dw = textRenderer.getWidth("···");
                ctx.drawText(textRenderer, "···", px + PANEL_W - dw - 3, iy + 5, C_TEXT_HINT, false);
            }

            iy += ITEM_H;
        }

        // nút scroll ▼ dưới
        int dnBtnY = iy + 2;
        boolean dnHov = mx >= px && mx < px + PANEL_W && my >= dnBtnY && my < dnBtnY + SCROLL_BTN;
        boolean canDown = scrollOff[col] + MAX_VISIBLE < all.length;
        ctx.fill(px, dnBtnY, px + PANEL_W, dnBtnY + SCROLL_BTN,
                 canDown ? (dnHov ? C_ITEM_HOV : C_HEADER2) : C_HEADER);
        int dac = canDown ? C_ACCENT : C_TEXT_HINT;
        int daw = textRenderer.getWidth("▼");
        ctx.drawText(textRenderer, "▼", px + (PANEL_W - daw) / 2, dnBtnY + 3, dac, false);
    }

    // ─── Settings panel ─────────────────────────────────────────────
    private void renderSettings(DrawContext ctx, int mx, int my) {
        int pw  = SET_W;
        int visItems = Math.min(settings.size() - setScrollOff, 9);
        int totalH = 24 + visItems * SET_ITEM_H + SCROLL_BTN * 2 + 6;

        int px = Math.max(1, Math.min(width - pw - 1, setX));
        int py = Math.max(TOP_H + 1, Math.min(height - totalH - 1, setY));
        setX = px; setY = py;

        // shadow
        ctx.fill(px + 3, py + 3, px + pw + 3, py + totalH + 3, 0x55000000);

        fillRounded(ctx, px, py, px + pw, py + totalH, C_SET_BG, RADIUS);
        drawRoundedBorder(ctx, px, py, pw, totalH, C_ACCENT, RADIUS);

        // header drag area
        fillRounded(ctx, px, py, px + pw, py + 22, C_HEADER, RADIUS);
        ctx.fill(px, py + 16, px + pw, py + 22, C_HEADER);
        ctx.fill(px, py + 22, px + pw, py + 23, C_BORDER);
        ctx.fill(px + 1, py, px + 3, py + 22, C_ACCENT);
        ctx.drawText(textRenderer, openMod, px + 7, py + 7, C_ACCENT, false);

        // nút đóng
        boolean ch = mx >= px + pw - 14 && mx < px + pw - 3 && my >= py + 4 && my < py + 18;
        ctx.drawText(textRenderer, "✕", px + pw - 13, py + 7, ch ? C_TEXT : C_TEXT_DIM, false);

        // nút scroll ▲
        int sby0 = py + 24;
        boolean suH = mx >= px && mx < px + pw && my >= sby0 && my < sby0 + SCROLL_BTN;
        ctx.fill(px, sby0, px + pw, sby0 + SCROLL_BTN, setScrollOff > 0 ? (suH ? C_DROP_HOV : C_HEADER2) : C_HEADER);
        int uaw = textRenderer.getWidth("▲");
        ctx.drawText(textRenderer, "▲", px + (pw - uaw) / 2, sby0 + 3, setScrollOff > 0 ? C_ACCENT : C_TEXT_HINT, false);

        // items
        int iy = sby0 + SCROLL_BTN + 2;
        for (int i = setScrollOff; i < Math.min(settings.size(), setScrollOff + 9); i++) {
            SetItem s = settings.get(i);
            boolean hov = mx >= px && mx < px + pw && my >= iy && my < iy + SET_ITEM_H;
            if (hov) ctx.fill(px, iy, px + pw, iy + SET_ITEM_H, 0xFF1A1A2C);

            ctx.drawText(textRenderer, s.name, px + 7, iy + 7, C_TEXT_DIM, false);
            renderSetCtrl(ctx, mx, my, s, i, px, py, iy, pw);

            iy += SET_ITEM_H;
        }

        // nút scroll ▼
        int sdy = iy + 2;
        boolean sdH = mx >= px && mx < px + pw && my >= sdy && my < sdy + SCROLL_BTN;
        boolean canSD = setScrollOff + 9 < settings.size();
        ctx.fill(px, sdy, px + pw, sdy + SCROLL_BTN, canSD ? (sdH ? C_DROP_HOV : C_HEADER2) : C_HEADER);
        int daw = textRenderer.getWidth("▼");
        ctx.drawText(textRenderer, "▼", px + (pw - daw) / 2, sdy + 3, canSD ? C_ACCENT : C_TEXT_HINT, false);
    }

    private void renderSetCtrl(DrawContext ctx, int mx, int my,
                                SetItem s, int idx, int px, int py, int iy, int pw) {
        switch (s.type) {
            case TOGGLE: {
                boolean v = (Boolean) s.value;
                int tw = 26, th = 12;
                int tx = px + pw - tw - 7, ty = iy + (SET_ITEM_H - th) / 2;
                // pill shape bo tròn
                fillRounded(ctx, tx, ty, tx + tw, ty + th, v ? C_TOG_ON : C_TOG_OFF, th / 2);
                int kx = v ? tx + tw - th + 1 : tx + 1;
                fillRounded(ctx, kx, ty + 1, kx + th - 2, ty + th - 1, 0xFFFFFFFF, (th - 2) / 2);
                break;
            }
            case SLIDER:
            case SLIDER_INT: {
                float val = ((Number) s.value).floatValue();
                float pct = Math.max(0, Math.min(1, (val - s.min) / (s.max - s.min)));
                int slW = 65, slH = 4;
                int slX = px + pw - slW - 7, slY = iy + (SET_ITEM_H - slH) / 2;
                fillRounded(ctx, slX, slY, slX + slW, slY + slH, C_SL_BG, 2);
                if (pct > 0) fillRounded(ctx, slX, slY, slX + (int)(slW * pct), slY + slH, C_SL_FILL, 2);
                int kx = slX + (int)(slW * pct) - 3;
                fillRounded(ctx, kx, slY - 3, kx + 6, slY + slH + 3, 0xFFFFFFFF, 3);
                String vs = s.type == SetType.SLIDER_INT ? String.valueOf((int) val) : String.format("%.1f", val);
                int vw = textRenderer.getWidth(vs);
                ctx.drawText(textRenderer, vs, slX - vw - 4, iy + 7, C_TEXT_DIM, false);
                break;
            }
            case MODE: {
                String v = (String) s.value;
                int bw = Math.min(textRenderer.getWidth(v) + 14, 75);
                int bx = px + pw - bw - 7, by = iy + 5;
                fillRounded(ctx, bx, by, bx + bw, by + 13, C_TOG_OFF, 3);
                drawRoundedBorder(ctx, bx, by, bw, 13, C_BORDER, 3);
                ctx.drawText(textRenderer, v, bx + 4, by + 3, C_TEXT_DIM, false);
                ctx.drawText(textRenderer, "▾", bx + bw - 9, by + 3, C_TEXT_HINT, false);
                if (idx == dropIdx) {
                    String[] opts = (String[]) s.extra;
                    int dY = by + 13;
                    int dH = opts.length * 12 + 4;
                    fillRounded(ctx, bx, dY, bx + bw, dY + dH, C_DROP_BG, 3);
                    drawRoundedBorder(ctx, bx, dY, bw, dH, C_BORDER, 3);
                    for (int o = 0; o < opts.length; o++) {
                        boolean oh = mx >= bx && mx < bx + bw && my >= dY + 2 + o * 12 && my < dY + 2 + o * 12 + 12;
                        if (oh) ctx.fill(bx + 1, dY + 2 + o * 12, bx + bw - 1, dY + 2 + o * 12 + 12, C_DROP_HOV);
                        boolean sel = opts[o].equals(v);
                        ctx.drawText(textRenderer, opts[o], bx + 4, dY + 4 + o * 12, sel ? C_ACCENT : C_TEXT_DIM, false);
                    }
                }
                break;
            }
        }
    }

    // ─── Keybind overlay ────────────────────────────────────────────
    private void renderBindOverlay(DrawContext ctx) {
        ctx.fill(0, 0, width, height, 0xBB000000);
        int bw = 260, bh = 90;
        int bx = (width - bw) / 2, by = (height - bh) / 2;
        fillRounded(ctx, bx, by, bx + bw, by + bh, C_SET_BG, RADIUS);
        drawRoundedBorder(ctx, bx, by, bw, bh, C_ACCENT, RADIUS);
        ctx.fill(bx + 1, by, bx + 3, by + bh, C_ACCENT);
        ctx.drawText(textRenderer, "§lSet Keybind", bx + 10, by + 8, C_ACCENT, false);
        ctx.drawText(textRenderer, "Module: §f" + bindMod, bx + 10, by + 26, C_TEXT_DIM, false);
        ctx.drawText(textRenderer, "Nhấn phím bất kỳ (F, D, X, Z, C, T...)", bx + 10, by + 44, C_TEXT, false);
        ctx.drawText(textRenderer, "ESC để huỷ", bx + 10, by + 62, C_TEXT_HINT, false);
        ctx.drawText(textRenderer, "DELETE để xoá keybind", bx + 10, by + 74, C_TEXT_HINT, false);
    }

    // ─── Search overlay ─────────────────────────────────────────────
    private void renderSearch(DrawContext ctx, int mx, int my) {
        ctx.fill(0, 0, width, height, 0xBB000000);
        int bw = 300;
        int bx = (width - bw) / 2, iy = TOP_H + 15;
        fillRounded(ctx, bx, iy, bx + bw, iy + 22, C_SET_BG, RADIUS);
        drawRoundedBorder(ctx, bx, iy, bw, 22, C_ACCENT, RADIUS);
        ctx.drawText(textRenderer,
            searchText.isEmpty() ? "§7Tìm module..." : searchText + "█",
            bx + 8, iy + 7, searchText.isEmpty() ? C_TEXT_HINT : C_TEXT, false);

        if (!searchText.isEmpty()) {
            int ry = iy + 28;
            for (ColDef cd : COLS) {
                for (String item : cd.items) {
                    if (item.toLowerCase().contains(searchText.toLowerCase())) {
                        boolean en = enabled.contains(item);
                        ctx.fill(bx, ry, bx + bw, ry + 18, C_ITEM_HOV);
                        if (en) ctx.fill(bx + 1, ry + 2, bx + 3, ry + 16, C_ACCENT);
                        ctx.drawText(textRenderer, item, bx + 8, ry + 5, en ? C_ACCENT : C_TEXT, false);
                        ry += 18;
                        if (ry > height - 20) break;
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // MOUSE EVENTS
    // ════════════════════════════════════════════════════════════════
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        if (binding) return true;
        if (searchOpen) {
            if (button == 1) { searchOpen = false; searchText = ""; }
            return true;
        }

        // top bar
        {
            int tabW = 64, gap = 3;
            int total = TOP.length * tabW + (TOP.length - 1) * gap;
            int tx0 = (width - total) / 2;
            for (int i = 0; i < TOP.length; i++) {
                int tx = tx0 + i * (tabW + gap);
                if (mx >= tx && mx < tx + tabW && my >= 3 && my < TOP_H - 3) {
                    if (TOP[i].equals("Search")) { searchOpen = true; searchText = ""; }
                    else { topSel = i; SoundManager.playClickSound(); }
                    return true;
                }
            }
        }

        // settings panel
        if (openMod != null) {
            int pw = SET_W;
            int visItems = Math.min(settings.size() - setScrollOff, 9);
            int totalH = 24 + visItems * SET_ITEM_H + SCROLL_BTN * 2 + 6;
            int px = setX, py = setY;

            // đóng
            if (mx >= px + pw - 14 && mx < px + pw - 3 && my >= py + 4 && my < py + 18) {
                openMod = null; settings.clear(); dropIdx = -1; SoundManager.playClickSound(); return true;
            }
            // drag header
            if (mx >= px && mx < px + pw && my >= py && my < py + 22) {
                dragSet = true; dragOX = mx - px; dragOY = my - py; return true;
            }
            // scroll ▲
            int sby0 = py + 24;
            if (mx >= px && mx < px + pw && my >= sby0 && my < sby0 + SCROLL_BTN) {
                if (setScrollOff > 0) { setScrollOff--; SoundManager.playClickSound(); } return true;
            }
            // scroll ▼
            int sdy = py + 24 + SCROLL_BTN + 2 + Math.min(settings.size() - setScrollOff, 9) * SET_ITEM_H + 2;
            if (mx >= px && mx < px + pw && my >= sdy && my < sdy + SCROLL_BTN) {
                if (setScrollOff + 9 < settings.size()) { setScrollOff++; SoundManager.playClickSound(); } return true;
            }
            // items
            int iy = py + 24 + SCROLL_BTN + 2;
            for (int i = setScrollOff; i < Math.min(settings.size(), setScrollOff + 9); i++) {
                if (mx >= px && mx < px + pw && my >= iy && my < iy + SET_ITEM_H) {
                    clickSetItem(settings.get(i), i, mx, my, px, py, iy, pw, button);
                    return true;
                }
                iy += SET_ITEM_H;
            }
            // click ngoài đóng
            if (mx < px || mx > px + pw || my < py || my > py + totalH) {
                if (button == 1) { openMod = null; settings.clear(); dropIdx = -1; return true; }
            }
        }

        // columns
        int totalW = COLS.length * PANEL_W + (COLS.length - 1) * PANEL_GAP;
        int sx = (width - totalW) / 2;

        for (int c = 0; c < COLS.length; c++) {
            int px = sx + c * (PANEL_W + PANEL_GAP);
            ColDef cd = COLS[c];
            String[] all = cd.items;
            int upBtnY = PANEL_TOP + HDR_H;
            int dnBtnY = upBtnY + SCROLL_BTN + 2 + Math.min(MAX_VISIBLE, all.length - scrollOff[c]) * ITEM_H + 2;

            // ▲
            if (mx >= px && mx < px + PANEL_W && my >= upBtnY && my < upBtnY + SCROLL_BTN) {
                if (scrollOff[c] > 0) { scrollOff[c]--; SoundManager.playClickSound(); } return true;
            }
            // ▼
            if (mx >= px && mx < px + PANEL_W && my >= dnBtnY && my < dnBtnY + SCROLL_BTN) {
                if (scrollOff[c] + MAX_VISIBLE < all.length) { scrollOff[c]++; SoundManager.playClickSound(); } return true;
            }
            // module items
            int iy = upBtnY + SCROLL_BTN + 2;
            for (int i = scrollOff[c]; i < Math.min(all.length, scrollOff[c] + MAX_VISIBLE); i++) {
                if (mx >= px && mx < px + PANEL_W && my >= iy && my < iy + ITEM_H) {
                    String mod = all[i];
                    if (button == 0) {
                        // left click: toggle on/off
                        if (enabled.contains(mod)) enabled.remove(mod);
                        else enabled.add(mod);
                        SoundManager.playClickSound();
                    } else if (button == 1) {
                        // right click: mở settings
                        openSettingsFor(mod, px + PANEL_W + 4, iy);
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
        if (dragSet) { setX = mx - dragOX; setY = my - dragOY; return true; }
        if (dragSlider >= 0 && dragSlider < settings.size()) {
            SetItem s = settings.get(dragSlider);
            float pct = Math.max(0, Math.min(1, (float)(mx - dragSlX0) / dragSlW));
            float v = s.min + pct * (s.max - s.min);
            s.value = s.type == SetType.SLIDER_INT ? (int) v : v;
            applyToMod(openMod, s);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragSet = false; dragSlider = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        int mx = (int) mouseX, my = (int) mouseY;
        int delta = vAmt > 0 ? -1 : 1;

        // cuộn settings panel
        if (openMod != null) {
            int pw = SET_W;
            int visItems = Math.min(settings.size() - setScrollOff, 9);
            int totalH = 24 + visItems * SET_ITEM_H + SCROLL_BTN * 2 + 6;
            if (mx >= setX && mx < setX + pw && my >= setY && my < setY + totalH) {
                setScrollOff = Math.max(0, Math.min(Math.max(0, settings.size() - 9), setScrollOff + delta));
                return true;
            }
        }

        // cuộn cột module
        int totalW = COLS.length * PANEL_W + (COLS.length - 1) * PANEL_GAP;
        int sx = (width - totalW) / 2;
        for (int c = 0; c < COLS.length; c++) {
            int px = sx + c * (PANEL_W + PANEL_GAP);
            int colH = HDR_H + SCROLL_BTN * 2 + Math.min(MAX_VISIBLE, COLS[c].items.length) * ITEM_H + 4;
            if (mx >= px && mx < px + PANEL_W && my >= PANEL_TOP && my < PANEL_TOP + colH) {
                scrollOff[c] = Math.max(0, Math.min(Math.max(0, COLS[c].items.length - MAX_VISIBLE), scrollOff[c] + delta));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    // ════════════════════════════════════════════════════════════════
    // KEY EVENTS – keybind hỗ trợ F/D/X/Z/C/T và mọi phím khác
    // ════════════════════════════════════════════════════════════════
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding && bindMod != null) {
            if (keyCode == 256) { binding = false; bindMod = null; return true; } // ESC huỷ
            if (keyCode == 261) { keybinds.remove(bindMod); binding = false; bindMod = null; SoundManager.playClickSound(); return true; } // DELETE xoá
            String keyName = keyCodeToName(keyCode);
            keybinds.put(bindMod, keyName);
            Module mod = ModuleManager.getModule(bindMod);
            if (mod != null) { mod.setKeybind(keyName); mod.setKeyCode(keyCode); }
            binding = false; bindMod = null;
            SoundManager.playClickSound();
            return true;
        }
        if (searchOpen) {
            if (keyCode == 256) { searchOpen = false; searchText = ""; return true; }
            if (keyCode == 259 && !searchText.isEmpty()) { searchText = searchText.substring(0, searchText.length() - 1); return true; }
            return true;
        }
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchOpen && (Character.isLetterOrDigit(chr) || chr == ' ')) { searchText += chr; return true; }
        return super.charTyped(chr, modifiers);
    }

    // ════════════════════════════════════════════════════════════════
    // SETTINGS HELPERS
    // ════════════════════════════════════════════════════════════════
    private void openSettingsFor(String mod, int prefX, int prefY) {
        openMod = mod;
        setX = Math.min(prefX, width - SET_W - 4);
        setY = Math.max(TOP_H + 4, Math.min(prefY, height - 250));
        dropIdx = -1; setScrollOff = 0;
        settings = buildSettings(mod);
    }

    private List<SetItem> buildSettings(String name) {
        List<SetItem> list = new ArrayList<>();
        if (name.equals("Aura")) {
            Aura a = (Aura) ModuleManager.getModule("Aura");
            if (a != null) {
                list.add(new SetItem("Range",          a.range,             1f, 6f,  SetType.SLIDER));
                list.add(new SetItem("FOV",            (float) a.fov,       1f, 180f,SetType.SLIDER_INT));
                list.add(new SetItem("Through Walls",  a.throughWalls,           SetType.TOGGLE));
                list.add(new SetItem("Smooth Rotation",a.smoothRotation,         SetType.TOGGLE));
                list.add(new SetItem("Shield Breaker", a.shieldBreaker,          SetType.TOGGLE));
                list.add(new SetItem("Auto Weapon",    a.autoWeapon,             SetType.TOGGLE));
                list.add(new SetItem("Only Weapon",    a.onlyWeapon,             SetType.TOGGLE));
                list.add(new SetItem("Rotation Mode",  a.rotationMode.name(),    new String[]{"NONE","LEGIT","NORMAL"}, SetType.MODE));
                list.add(new SetItem("Aim Mode",       a.aimMode.name(),         new String[]{"HEAD","BODY","LEGS"},   SetType.MODE));
                list.add(new SetItem("Sort",           a.sort.name(),            new String[]{"Distance","LowestHealth","HighestHealth"}, SetType.MODE));
                list.add(new SetItem("Pause Eating",   a.pauseWhileEating,       SetType.TOGGLE));
                list.add(new SetItem("Pause Inventory",a.pauseInInventory,       SetType.TOGGLE));
                list.add(new SetItem("Attack Chance",  100f, 0f, 100f,           SetType.SLIDER_INT));
                list.add(new SetItem("Esp Mode",       "Comets", new String[]{"None","Comets","Box"}, SetType.MODE));
                return list;
            }
        }
        // generic fallback
        list.add(new SetItem("Enabled",    true,              SetType.TOGGLE));
        list.add(new SetItem("Range",      3.0f, 1f, 6f,     SetType.SLIDER));
        list.add(new SetItem("Speed",      1.0f, 0f, 5f,     SetType.SLIDER));
        list.add(new SetItem("Mode",       "Default", new String[]{"Default","Silent","Strict"}, SetType.MODE));
        list.add(new SetItem("No Rotate",  false,             SetType.TOGGLE));
        list.add(new SetItem("Ignore Walls",false,            SetType.TOGGLE));
        list.add(new SetItem("Delay",      5f, 0f, 20f,      SetType.SLIDER_INT));
        return list;
    }

    private void clickSetItem(SetItem s, int idx, int mx, int my,
                               int px, int py, int iy, int pw, int button) {
        switch (s.type) {
            case TOGGLE: {
                s.value = !(Boolean) s.value;
                applyToMod(openMod, s);
                SoundManager.playClickSound();
                break;
            }
            case SLIDER: case SLIDER_INT: {
                int slW = 65, slX = px + pw - slW - 7;
                if (mx >= slX - 4 && mx <= slX + slW + 4) {
                    dragSlider = idx; dragSlX0 = slX; dragSlW = slW;
                    float pct = Math.max(0, Math.min(1, (float)(mx - slX) / slW));
                    float v = s.min + pct * (s.max - s.min);
                    s.value = s.type == SetType.SLIDER_INT ? (int) v : v;
                    applyToMod(openMod, s);
                    SoundManager.playClickSound();
                }
                break;
            }
            case MODE: {
                if (dropIdx == idx) {
                    String[] opts = (String[]) s.extra;
                    String sv = (String) s.value;
                    int bw2 = Math.min(textRenderer.getWidth(sv) + 14, 75);
                    int bx = px + pw - bw2 - 7, by2 = iy + 5;
                    int dY = by2 + 13;
                    for (int o = 0; o < opts.length; o++) {
                        if (mx >= bx && mx < bx + bw2 && my >= dY + 2 + o * 12 && my < dY + 2 + o * 12 + 12) {
                            s.value = opts[o];
                            applyToMod(openMod, s);
                            SoundManager.playClickSound();
                        }
                    }
                    dropIdx = -1;
                } else {
                    dropIdx = idx;
                }
                break;
            }
        }
    }

    private void applyToMod(String name, SetItem s) {
        if (name == null) return;
        Module mod = ModuleManager.getModule(name);
        if (!(mod instanceof Aura)) return;
        Aura a = (Aura) mod;
        switch (s.name) {
            case "Range":           a.range = ((Number) s.value).floatValue(); break;
            case "FOV":             a.fov   = (int)((Number)s.value).floatValue(); break;
            case "Through Walls":   a.throughWalls    = (boolean) s.value; break;
            case "Smooth Rotation": a.smoothRotation  = (boolean) s.value; break;
            case "Shield Breaker":  a.shieldBreaker   = (boolean) s.value; break;
            case "Auto Weapon":     a.autoWeapon      = (boolean) s.value; break;
            case "Only Weapon":     a.onlyWeapon      = (boolean) s.value; break;
            case "Pause Eating":    a.pauseWhileEating= (boolean) s.value; break;
            case "Pause Inventory": a.pauseInInventory= (boolean) s.value; break;
            case "Rotation Mode": {
                String v = (String) s.value;
                a.rotationMode = v.equals("NONE") ? Aura.RotationMode.NONE : v.equals("LEGIT") ? Aura.RotationMode.LEGIT : Aura.RotationMode.NORMAL;
                break;
            }
            case "Aim Mode": {
                String v = (String) s.value;
                a.aimMode = v.equals("HEAD") ? Aura.AimMode.HEAD : v.equals("LEGS") ? Aura.AimMode.LEGS : Aura.AimMode.BODY;
                break;
            }
            case "Sort": {
                String v = (String) s.value;
                a.sort = v.equals("LowestHealth") ? Aura.SortMode.LowestHealth : v.equals("HighestHealth") ? Aura.SortMode.HighestHealth : Aura.SortMode.LowestDistance;
                break;
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // DRAW HELPERS – bo tròn giả lập bằng fill nhiều lớp
    // ════════════════════════════════════════════════════════════════

    /**
     * Vẽ hình chữ nhật bo tròn.
     * Với radius > 0, ta cắt 4 góc bằng các fill chồng nhau.
     */
    private void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int color, int r) {
        if (r <= 0 || (x2 - x1) < r * 2 || (y2 - y1) < r * 2) {
            ctx.fill(x1, y1, x2, y2, color);
            return;
        }
        // thân chính (không bao gồm 4 góc)
        ctx.fill(x1 + r, y1,     x2 - r, y2,     color);  // dải giữa
        ctx.fill(x1,     y1 + r, x1 + r, y2 - r, color);  // trái
        ctx.fill(x2 - r, y1 + r, x2,     y2 - r, color);  // phải
        // 4 góc bo tròn gần đúng (quarter-circle bằng từng pixel hàng)
        for (int i = 0; i < r; i++) {
            // khoảng cắt theo công thức tròn: offset = r - sqrt(r^2 - (r-i)^2)
            int dist = r - (int) Math.sqrt((double)(r * r) - (double)((r - i) * (r - i)));
            // top-left
            ctx.fill(x1 + dist, y1 + i, x1 + r, y1 + i + 1, color);
            // top-right
            ctx.fill(x2 - r, y1 + i, x2 - dist, y1 + i + 1, color);
            // bottom-left
            ctx.fill(x1 + dist, y2 - i - 1, x1 + r, y2 - i, color);
            // bottom-right
            ctx.fill(x2 - r, y2 - i - 1, x2 - dist, y2 - i, color);
        }
    }

    /** Viền bo tròn (1px) */
    private void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        if (r <= 0) { drawStraightBorder(ctx, x, y, w, h, color); return; }
        // top & bottom (trừ góc)
        ctx.fill(x + r, y,         x + w - r, y + 1,     color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h,     color);
        // left & right
        ctx.fill(x,         y + r, x + 1,     y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w,     y + h - r, color);
        // 4 góc cong
        for (int i = 0; i < r; i++) {
            int dist = r - (int) Math.sqrt((double)(r * r) - (double)((r - i) * (r - i)));
            // top-left
            ctx.fill(x + dist, y + i, x + dist + 1, y + i + 1, color);
            // top-right
            ctx.fill(x + w - dist - 1, y + i, x + w - dist, y + i + 1, color);
            // bottom-left
            ctx.fill(x + dist, y + h - i - 1, x + dist + 1, y + h - i, color);
            // bottom-right
            ctx.fill(x + w - dist - 1, y + h - i - 1, x + w - dist, y + h - i, color);
        }
    }

    private void drawStraightBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** Chuyển keyCode → tên phím hiển thị */
    private String keyCodeToName(int kc) {
        if (kc >= 65 && kc <= 90)  return String.valueOf((char) kc);      // A-Z
        if (kc >= 48 && kc <= 57)  return String.valueOf((char) kc);      // 0-9
        if (kc >= 290 && kc <= 301) return "F" + (kc - 289);              // F1-F12
        switch (kc) {
            case 32: return "SPACE"; case 257: return "ENTER"; case 258: return "TAB";
            case 340: return "SHIFT"; case 341: return "CTRL"; case 342: return "ALT";
            case 261: return "DEL";  case 259: return "BACK"; case 256: return "ESC";
            case 265: return "UP"; case 264: return "DOWN"; case 263: return "LEFT"; case 262: return "RIGHT";
            default:  return "KEY" + kc;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SCREEN OVERRIDES
    // ════════════════════════════════════════════════════════════════
    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        super.close();
        ConfigManager.saveAllConfigs();
    }

    // ════════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ════════════════════════════════════════════════════════════════
    private static class ColDef {
        final String   name;
        final String[] items;
        final String[] keybindHints;
        ColDef(String n, String[] items, String[] hints) {
            this.name = n; this.items = items; this.keybindHints = hints;
        }
    }

    private enum SetType { TOGGLE, SLIDER, SLIDER_INT, MODE }

    private static class SetItem {
        String  name;
        Object  value;
        float   min, max;
        SetType type;
        Object  extra;

        SetItem(String n, Object v, SetType t)                      { name = n; value = v; type = t; }
        SetItem(String n, Object v, float mn, float mx, SetType t)  { this(n, v, t); min = mn; max = mx; }
        SetItem(String n, Object v, String[] opts, SetType t)       { this(n, v, t); extra = opts; }
    }
}
