package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.combat.Aura;
import acore.hack.features.modules.combat.AutoTNTcart;
import acore.hack.features.setting.BooleanSetting;
import acore.hack.features.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

/**
 * ClickGUI – Settings panel hiện INLINE bên dưới module (không kế bên).
 */
public class ClickGUI extends Screen {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ─── Màu Purple Glass ─────────────────────────────────────────
    private static final int C_WIN_BG    = 0xEA0D0820;
    private static final int C_WIN_BG2   = 0xEA100C28;
    private static final int C_TITLE_BG  = 0xFF140F30;
    private static final int C_CAT_BG    = 0xFF0F0B22;
    private static final int C_CAT_HOV   = 0xFF160D2E;
    private static final int C_CAT_OPEN  = 0xFF1A1040;
    private static final int C_ITEM_BG   = 0xFF0C0920;
    private static final int C_ITEM_HOV  = 0xFF110D2A;
    private static final int C_ITEM_EN   = 0xFF130F30;
    private static final int C_ACCENT    = 0xFFBB77FF;
    private static final int C_ACCENT2   = 0xFF9955EE;
    private static final int C_ACCENT3   = 0xFF7733CC;
    private static final int C_ACCENT_GL = 0x44BB77FF;
    private static final int C_BORDER    = 0xFF2A1A55;
    private static final int C_BORDER_HL = 0xFF6633BB;
    private static final int C_TEXT      = 0xFFEEEEFF;
    private static final int C_TEXT_DIM  = 0xFF998899;
    private static final int C_TEXT_HINT = 0xFF443344;
    private static final int C_TOG_ON    = 0xFFBB77FF;
    private static final int C_TOG_OFF   = 0xFF221A44;
    private static final int C_SL_BG     = 0xFF1A1040;
    private static final int C_SL_FILL   = 0xFFBB77FF;
    private static final int C_SET_BG    = 0xFF0E0B28;
    private static final int C_DROP_BG   = 0xFF0D0925;

    // ─── Layout ───────────────────────────────────────────────────
    private static final int WIN_W     = 220;
    private static final int TITLE_H   = 28;
    private static final int CAT_H     = 24;
    private static final int ITEM_H    = 20;
    private static final int SET_ITH   = 22;
    private static final int SCROLL_BT = 14;
    private static final int RADIUS    = 10;
    private static final int PAD       = 8;

    // ─── Categories ───────────────────────────────────────────────
    private static final AccCat[] ACCS = {
        new AccCat("⚔  Combat",   new String[]{"Aura","AimBot","AntiBot","AutoBuff","AutoCart","AutoGApple","AutoTrap","Criticals","Legit Aura","Mace Killer","More KnockBack","Trap","Auto Crystal","Hole Filler","AutoTNTcart"}),
        new AccCat("🏃  Movement", new String[]{"AirStack","AntiWeb","AutoSprint","AutoWalk","Blink","Elytra+","ElytraBoost","ElytraMotion","Flight","FreeLook","Move Fix","No Fall","Step","Velocity"}),
        new AccCat("👁  Visual",   new String[]{"Animations","BlockESP","Crosshair","ESP","FreeCam","FullBright","HUD","HitFx","Nametag","Particles","Tracers","Zoom","Cave Finder","Chams"}),
        new AccCat("⚙  Player",   new String[]{"AntiAFK","AutoArmor","AutoEat","AutoRespawn","AutoTool","Chest Stealer","Click Out","DurabilityAlert","Inv Manager","No Rotate","Packet Fly","Auto Fish"}),
        new AccCat("🔧  Misc",     new String[]{"Anti Crash","AutoAuth","Auto Dungeon","AutoSign","Chat Log","Client Spoof","Debug Record","Server Spoof","Timer","Command List","Fake Player"}),
    };

    // ─── State ────────────────────────────────────────────────────
    private final boolean[]          catOpen  = new boolean[ACCS.length];
    private final Set<String>        enabled  = new HashSet<>();
    private final Map<String,String> keybinds = new HashMap<>();

    // window drag
    private int winX, winY;
    private boolean dragWin = false;
    private int dWX, dWY;

    // scroll
    private int accScroll = 0;
    private int maxScroll = 0;

    // settings inline
    private String      openMod    = null;
    private List<SI>    settings   = new ArrayList<>();
    private int         dragSl     = -1;
    private int         dSlX, dSlW;
    private int         dropIdx    = -1;

    // keybind
    private String  bindMod = null;
    private boolean binding = false;

    private float tick = 0;

    public ClickGUI() {
        super(Text.literal("GUI"));
        catOpen[0] = true;
    }

    @Override
    protected void init() {
        super.init();
        winX = (width  - WIN_W) / 2 - 80;
        winY = (height - 400)   / 2;
        winY = Math.max(10, winY);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        tick += delta;
        ctx.fill(0, 0, width, height, 0x88000000);

        int contentH = computeContentH();
        int visH = Math.max(100, Math.min(500, contentH + SCROLL_BT * 2 + 8));
        int winH = TITLE_H + visH;

        ctx.fill(winX - 4, winY - 4, winX + WIN_W + 4, winY + winH + 4, C_ACCENT_GL);
        ctx.fill(winX + 5, winY + 5, winX + WIN_W + 5, winY + winH + 5, 0x66000000);

        fillR(ctx, winX, winY, winX + WIN_W, winY + winH, C_WIN_BG, RADIUS);
        borderR(ctx, winX, winY, WIN_W, winH, C_BORDER_HL, RADIUS);

        fillR(ctx, winX, winY, winX + WIN_W, winY + TITLE_H, C_TITLE_BG, RADIUS);
        ctx.fill(winX, winY + TITLE_H - 5, winX + WIN_W, winY + TITLE_H, C_TITLE_BG);
        ctx.fill(winX, winY + TITLE_H, winX + WIN_W, winY + TITLE_H + 1, C_BORDER);
        ctx.fill(winX + 1, winY + RADIUS, winX + 3, winY + TITLE_H - 2, C_ACCENT);
        ctx.drawText(textRenderer, "§l✦ CatLean", winX + 10, winY + 10, C_ACCENT, false);

        int cx2 = winX + WIN_W - 18, cy2 = winY + 8;
        boolean ch = mx >= cx2 && mx < cx2 + 12 && my >= cy2 && my < cy2 + 12;
        fillR(ctx, cx2, cy2, cx2 + 12, cy2 + 12, ch ? 0xFF330022 : 0xFF1A0F33, 4);
        borderR(ctx, cx2, cy2, 12, 12, ch ? 0xFFFF44AA : C_BORDER, 4);
        ctx.drawText(textRenderer, "✕", cx2 + 2, cy2 + 2, ch ? 0xFFFF44AA : C_TEXT_DIM, false);

        int bodyY0 = winY + TITLE_H;
        int clipY0 = bodyY0 + SCROLL_BT + 2;
        int clipY1 = winY + winH - SCROLL_BT - 2;

        int upY = bodyY0 + 2;
        boolean upH = mx >= winX && mx < winX + WIN_W && my >= upY && my < upY + SCROLL_BT;
        ctx.fill(winX, upY, winX + WIN_W, upY + SCROLL_BT,
                 accScroll > 0 ? (upH ? C_CAT_HOV : C_CAT_BG) : C_WIN_BG);
        int auw = textRenderer.getWidth("▲");
        ctx.drawText(textRenderer, "▲", winX + (WIN_W - auw) / 2, upY + 3,
                     accScroll > 0 ? C_ACCENT : C_TEXT_HINT, false);

        int iy = clipY0 - accScroll;
        maxScroll = 0;

        for (int c = 0; c < ACCS.length; c++) {
            AccCat ac = ACCS[c];
            boolean open = catOpen[c];

            if (iy + CAT_H > clipY0 && iy < clipY1)
                renderCatHeader(ctx, mx, my, c, ac, iy, open, clipY0, clipY1);
            iy += CAT_H;

            if (open) {
                for (String mod : ac.items) {
                    if (iy + ITEM_H > clipY0 && iy < clipY1)
                        renderModItem(ctx, mx, my, mod, iy, clipY0, clipY1);
                    iy += ITEM_H;

                    if (mod.equals(openMod) && !settings.isEmpty()) {
                        int setH = settings.size() * SET_ITH + 4;
                        if (iy + setH > clipY0 && iy < clipY1)
                            renderInlineSettings(ctx, mx, my, iy, clipY0, clipY1);
                        iy += setH;
                    }
                }
            }
        }
        maxScroll = Math.max(0, iy + accScroll - clipY1);

        int dnY = winY + winH - SCROLL_BT - 2;
        boolean dnH = mx >= winX && mx < winX + WIN_W && my >= dnY && my < dnY + SCROLL_BT;
        boolean canDn = accScroll < maxScroll;
        ctx.fill(winX, dnY, winX + WIN_W, dnY + SCROLL_BT,
                 canDn ? (dnH ? C_CAT_HOV : C_CAT_BG) : C_WIN_BG);
        int adw = textRenderer.getWidth("▼");
        ctx.drawText(textRenderer, "▼", winX + (WIN_W - adw) / 2, dnY + 3,
                     canDn ? C_ACCENT : C_TEXT_HINT, false);

        if (binding) renderBindOverlay(ctx);
        super.render(ctx, mx, my, delta);
    }

    private void renderCatHeader(DrawContext ctx, int mx, int my, int idx, AccCat ac,
                                  int iy, boolean open, int clipY0, int clipY1) {
        boolean hov = mx >= winX && mx < winX + WIN_W && my >= iy && my < iy + CAT_H;
        int bg = open ? C_CAT_OPEN : (hov ? C_CAT_HOV : C_CAT_BG);
        int y1 = Math.max(clipY0, iy), y2 = Math.min(clipY1, iy + CAT_H);
        if (y2 > y1) {
            ctx.fill(winX, y1, winX + WIN_W, y2, bg);
            ctx.fill(winX, y2 - 1, winX + WIN_W, y2, C_BORDER);
        }
        if (iy >= clipY0) {
            if (open) ctx.fill(winX, iy, winX + 3, iy + CAT_H, C_ACCENT);
            int tc = open ? C_ACCENT : (hov ? C_TEXT : C_TEXT_DIM);
            ctx.drawText(textRenderer, ac.name, winX + PAD + (open ? 4 : 0), iy + 8, tc, false);
            ctx.drawText(textRenderer, open ? "▼" : "▶", winX + WIN_W - 16, iy + 8,
                         open ? C_ACCENT : C_TEXT_HINT, false);
            String cnt = String.valueOf(ac.items.length);
            int cw = textRenderer.getWidth(cnt) + 6;
            fillR(ctx, winX + WIN_W - cw - 20, iy + 7, winX + WIN_W - 20, iy + 17,
                  open ? C_ACCENT3 : 0xFF1A1040, 4);
            ctx.drawText(textRenderer, cnt, winX + WIN_W - cw - 17, iy + 9,
                         open ? C_TEXT : C_TEXT_HINT, false);
        }
    }

    private void renderModItem(DrawContext ctx, int mx, int my, String mod,
                                int iy, int clipY0, int clipY1) {
        boolean en      = enabled.contains(mod);
        boolean hasSet  = mod.equals(openMod);
        boolean hov     = mx >= winX && mx < winX + WIN_W && my >= iy && my < iy + ITEM_H;

        int bg = en ? C_ITEM_EN : (hasSet || hov ? C_ITEM_HOV : C_ITEM_BG);
        int y1 = Math.max(clipY0, iy), y2 = Math.min(clipY1, iy + ITEM_H);
        if (y2 > y1) ctx.fill(winX + 3, y1, winX + WIN_W, y2, bg);

        if (iy >= clipY0) {
            if (en) ctx.fill(winX + 3, iy + 2, winX + 5, iy + ITEM_H - 2, C_ACCENT);

            int tw = 18, th = 8;
            int tx = winX + 9, ty = iy + (ITEM_H - th) / 2;
            fillR(ctx, tx, ty, tx + tw, ty + th, en ? C_TOG_ON : C_TOG_OFF, th / 2);
            int kx = en ? tx + tw - th + 1 : tx + 1;
            fillR(ctx, kx, ty + 1, kx + th - 2, ty + th - 1, 0xFFFFFFFF, (th - 2) / 2);

            int tc = en ? C_ACCENT : (hov ? C_TEXT : C_TEXT_DIM);
            ctx.drawText(textRenderer, mod, tx + tw + 5, iy + 6, tc, false);

            if (hasSet) {
                ctx.drawText(textRenderer, "▾", winX + WIN_W - 14, iy + 6, C_ACCENT, false);
            } else if (hov) {
                ctx.drawText(textRenderer, "▸", winX + WIN_W - 14, iy + 6, C_TEXT_HINT, false);
            }

            String kb = keybinds.get(mod);
            if (kb != null) {
                int lw = textRenderer.getWidth(kb) + 6;
                int lx = winX + WIN_W - lw - 18, ly = iy + 4;
                fillR(ctx, lx, ly, lx + lw, ly + 12, C_ACCENT3, 3);
                ctx.drawText(textRenderer, kb, lx + 3, ly + 2, C_TEXT, false);
            }
        }
    }

    private void renderInlineSettings(DrawContext ctx, int mx, int my,
                                       int iy, int clipY0, int clipY1) {
        int px = winX + 3, pw = WIN_W - 3;
        int setH = settings.size() * SET_ITH + 4;
        int y1 = Math.max(clipY0, iy), y2 = Math.min(clipY1, iy + setH);
        if (y2 > y1) {
            ctx.fill(px, y1, px + pw, y2, C_SET_BG);
            ctx.fill(px, y1, px + 2, y2, C_ACCENT2);
            ctx.fill(px, y2, px + pw, y2, C_BORDER);
        }

        int rowY = iy + 2;
        for (int i = 0; i < settings.size(); i++) {
            SI s = settings.get(i);
            int rY1 = Math.max(clipY0, rowY);
            int rY2 = Math.min(clipY1, rowY + SET_ITH);
            if (rY2 <= rY1) { rowY += SET_ITH; continue; }

            boolean hov = mx >= px && mx < px + pw && my >= rowY && my < rowY + SET_ITH;
            if (hov && rY2 > rY1) ctx.fill(px + 2, rY1, px + pw, rY2, C_ITEM_HOV);

            if (rowY >= clipY0) {
                ctx.drawText(textRenderer, s.name, px + 8, rowY + 7, C_TEXT_DIM, false);
                renderSettingControl(ctx, mx, my, s, i, px, rowY, pw);
            }
            rowY += SET_ITH;
        }
    }

    private void renderSettingControl(DrawContext ctx, int mx, int my, SI s, int idx,
                                       int px, int iy, int pw) {
        switch (s.type) {
            case TOGGLE -> {
                boolean v = (Boolean) s.value;
                int tw = 24, th = 10;
                int tx = px + pw - tw - 8, ty = iy + (SET_ITH - th) / 2;
                fillR(ctx, tx, ty, tx + tw, ty + th, v ? C_TOG_ON : C_TOG_OFF, th / 2);
                int kx = v ? tx + tw - th + 1 : tx + 1;
                fillR(ctx, kx, ty + 1, kx + th - 2, ty + th - 1, 0xFFFFFFFF, (th - 2) / 2);
            }
            case SLIDER, SLIDER_INT -> {
                float val = ((Number) s.value).floatValue();
                float pct = Math.max(0, Math.min(1, (val - s.min) / (s.max - s.min)));
                int slW = 64, slH = 4;
                int slX = px + pw - slW - 8, slY = iy + (SET_ITH - slH) / 2;
                fillR(ctx, slX, slY, slX + slW, slY + slH, C_SL_BG, 2);
                if (pct > 0)
                    fillR(ctx, slX, slY, slX + (int)(slW * pct), slY + slH, C_SL_FILL, 2);
                int knobX = slX + (int)(slW * pct) - 3;
                fillR(ctx, knobX, slY - 3, knobX + 6, slY + slH + 3, 0xFFFFFFFF, 3);
                String vs = s.type == SType.SLIDER_INT
                        ? String.valueOf((int) val)
                        : String.format("%.1f", val);
                ctx.drawText(textRenderer, vs, slX - textRenderer.getWidth(vs) - 4, iy + 7, C_TEXT_DIM, false);
            }
            case MODE -> {
                String v = (String) s.value;
                int bw = Math.min(textRenderer.getWidth(v) + 14, 80);
                int bx = px + pw - bw - 8, by = iy + 5;
                fillR(ctx, bx, by, bx + bw, by + 13, C_DROP_BG, 3);
                borderR(ctx, bx, by, bw, 13, C_BORDER_HL, 3);
                ctx.drawText(textRenderer, v, bx + 4, by + 3, C_ACCENT2, false);
                ctx.drawText(textRenderer, "▾", bx + bw - 8, by + 3, C_TEXT_HINT, false);
                if (idx == dropIdx) {
                    String[] opts = (String[]) s.extra;
                    int dY = by + 13;
                    fillR(ctx, bx, dY, bx + bw, dY + opts.length * 12 + 4, C_DROP_BG, 3);
                    borderR(ctx, bx, dY, bw, opts.length * 12 + 4, C_ACCENT, 3);
                    for (int o = 0; o < opts.length; o++) {
                        boolean oh = mx >= bx && mx < bx + bw
                                  && my >= dY + 2 + o * 12 && my < dY + 14 + o * 12;
                        if (oh) ctx.fill(bx + 1, dY + 2 + o * 12, bx + bw - 1, dY + 2 + o * 12 + 12, C_CAT_HOV);
                        ctx.drawText(textRenderer, opts[o], bx + 4, dY + 4 + o * 12,
                                     opts[o].equals(v) ? C_ACCENT : C_TEXT_DIM, false);
                    }
                }
            }
        }
    }

    private void renderBindOverlay(DrawContext ctx) {
        ctx.fill(0, 0, width, height, 0xCC000000);
        int bw = 280, bh = 100;
        int bx = (width - bw) / 2, by = (height - bh) / 2;
        ctx.fill(bx - 4, by - 4, bx + bw + 4, by + bh + 4, 0x33BB77FF);
        fillR(ctx, bx, by, bx + bw, by + bh, C_SET_BG, RADIUS);
        borderR(ctx, bx, by, bw, bh, C_ACCENT, RADIUS);
        ctx.drawText(textRenderer, "§lSET KEYBIND", bx + 12, by + 8, C_ACCENT, false);
        ctx.drawText(textRenderer, "Module: §f" + bindMod, bx + 12, by + 26, C_TEXT_DIM, false);
        ctx.drawText(textRenderer, "§7Nhấn phím: F  D  X  Z  C  T  ...", bx + 12, by + 44, C_TEXT, false);
        ctx.drawText(textRenderer, "§7ESC = huỷ  |  DELETE = xoá keybind", bx + 12, by + 62, C_TEXT_DIM, false);
        String dots = ".".repeat(((int)(tick / 10)) % 4);
        ctx.drawText(textRenderer, "§5Chờ phím" + dots, bx + 12, by + 82, C_ACCENT2, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;
        if (binding) return true;

        int contentH = computeContentH();
        int visH  = Math.max(100, Math.min(500, contentH + SCROLL_BT * 2 + 8));
        int winH  = TITLE_H + visH;

        int cx2 = winX + WIN_W - 18, cy2 = winY + 8;
        if (mx >= cx2 && mx < cx2 + 12 && my >= cy2 && my < cy2 + 12) { close(); return true; }

        if (mx >= winX && mx < winX + WIN_W && my >= winY && my < winY + TITLE_H) {
            dragWin = true; dWX = mx - winX; dWY = my - winY; return true;
        }

        int upY = winY + TITLE_H + 2;
        if (mx >= winX && mx < winX + WIN_W && my >= upY && my < upY + SCROLL_BT) {
            if (accScroll > 0) { accScroll = Math.max(0, accScroll - ITEM_H); SoundManager.playClickSound(); } return true;
        }
        int dnY = winY + winH - SCROLL_BT - 2;
        if (mx >= winX && mx < winX + WIN_W && my >= dnY && my < dnY + SCROLL_BT) {
            if (accScroll < maxScroll) { accScroll = Math.min(maxScroll, accScroll + ITEM_H); SoundManager.playClickSound(); } return true;
        }

        {
            int clipY0 = upY + SCROLL_BT;
            int clipY1 = winY + winH - SCROLL_BT - 2;
            int iy = clipY0 - accScroll;

            for (int c = 0; c < ACCS.length; c++) {
                AccCat ac = ACCS[c];
                if (mx >= winX && mx < winX + WIN_W && my >= iy && my < iy + CAT_H
                        && iy < clipY1 && iy + CAT_H > clipY0) {
                    catOpen[c] = !catOpen[c];
                    if (!catOpen[c] && openMod != null) {
                        for (String m : ac.items) if (m.equals(openMod)) { openMod = null; settings.clear(); }
                    }
                    SoundManager.playClickSound(); return true;
                }
                iy += CAT_H;

                if (catOpen[c]) {
                    for (String mod : ac.items) {
                        if (mx >= winX && mx < winX + WIN_W && my >= iy && my < iy + ITEM_H
                                && iy < clipY1 && iy + ITEM_H > clipY0) {
                            if (button == 0) {
                                if (enabled.contains(mod)) enabled.remove(mod); else enabled.add(mod);
                                SoundManager.playClickSound();
                            } else if (button == 1) {
                                if (mod.equals(openMod)) {
                                    openMod = null; settings.clear(); dropIdx = -1;
                                } else {
                                    openMod = mod;
                                    settings = buildSI(mod);
                                    dropIdx = -1;
                                }
                                SoundManager.playClickSound();
                            }
                            return true;
                        }
                        iy += ITEM_H;

                        if (mod.equals(openMod) && !settings.isEmpty()) {
                            int setH = settings.size() * SET_ITH + 4;
                            int px = winX + 3, pw = WIN_W - 3;
                            int rowY = iy + 2;
                            for (int i = 0; i < settings.size(); i++) {
                                if (mx >= px && mx < px + pw && my >= rowY && my < rowY + SET_ITH
                                        && rowY < clipY1 && rowY + SET_ITH > clipY0) {
                                    clickSI(settings.get(i), i, mx, my, px, rowY, pw, button);
                                    return true;
                                }
                                rowY += SET_ITH;
                            }
                            iy += setH;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mx2, double my2, int btn, double dx, double dy) {
        int mx = (int) mx2, my = (int) my2;
        if (dragWin) {
            winX = Math.max(0, Math.min(width - WIN_W,   mx - dWX));
            winY = Math.max(0, Math.min(height - 200, my - dWY));
            return true;
        }
        if (dragSl >= 0 && dragSl < settings.size()) {
            SI s = settings.get(dragSl);
            float pct = Math.max(0, Math.min(1, (float)(mx - dSlX) / dSlW));
            float v = s.min + pct * (s.max - s.min);
            s.value = s.type == SType.SLIDER_INT ? (int) v : v;
            applyMod(openMod, s);
            return true;
        }
        return super.mouseDragged(mx2, my2, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragWin = false; dragSl = -1;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx2, double my2, double hA, double vA) {
        int d = vA > 0 ? -ITEM_H : ITEM_H;
        accScroll = Math.max(0, Math.min(maxScroll, accScroll + d));
        return true;
    }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (binding && bindMod != null) {
            if (kc == 256) { binding = false; bindMod = null; return true; }
            if (kc == 261) { keybinds.remove(bindMod); binding = false; bindMod = null; SoundManager.playClickSound(); return true; }
            String name = keyName(kc);
            keybinds.put(bindMod, name);
            Module m = ModuleManager.getModule(bindMod);
            if (m != null) { m.setKeybind(name); m.setKeyCode(kc); }
            binding = false; bindMod = null;
            SoundManager.playClickSound();
            return true;
        }
        if (kc == 256) { close(); return true; }
        return super.keyPressed(kc, sc, mod);
    }

    private void clickSI(SI s, int idx, int mx, int my, int px, int iy, int pw, int btn) {
        switch (s.type) {
            case TOGGLE -> {
                s.value = !(Boolean) s.value;
                applyMod(openMod, s);
                SoundManager.playClickSound();
            }
            case SLIDER, SLIDER_INT -> {
                int slW = 64, slX = px + pw - slW - 8;
                if (mx >= slX - 4 && mx <= slX + slW + 4) {
                    dragSl = idx; dSlX = slX; dSlW = slW;
                    float pct = Math.max(0, Math.min(1, (float)(mx - slX) / slW));
                    float v = s.min + pct * (s.max - s.min);
                    s.value = s.type == SType.SLIDER_INT ? (int) v : v;
                    applyMod(openMod, s);
                    SoundManager.playClickSound();
                }
            }
            case MODE -> {
                if (dropIdx == idx) {
                    String[] opts = (String[]) s.extra;
                    String sv = (String) s.value;
                    int bw = Math.min(textRenderer.getWidth(sv) + 14, 80);
                    int bx = px + pw - bw - 8, by = iy + 5, dY = by + 13;
                    for (int o = 0; o < opts.length; o++) {
                        if (mx >= bx && mx < bx + bw && my >= dY + 2 + o * 12 && my < dY + 14 + o * 12) {
                            s.value = opts[o]; applyMod(openMod, s); SoundManager.playClickSound();
                        }
                    }
                    dropIdx = -1;
                } else { dropIdx = idx; }
            }
        }
    }

    /** Build danh sách setting dựa theo module name */
    private List<SI> buildSI(String name) {
        List<SI> l = new ArrayList<>();
        Module rawMod = ModuleManager.getModule(name);

        // ── AutoTNTcart ──────────────────────────────────────────
        if (name.equals("AutoTNTcart") && rawMod instanceof AutoTNTcart m) {
            l.add(new SI("Place Delay", (float) m.placeDelay.getValue(), 1, 20, SType.SLIDER_INT));
            l.add(new SI("Shoot Delay", (float) m.shootDelay.getValue(), 1, 20, SType.SLIDER_INT));
            l.add(new SI("Rail Count",  (float) m.railCount.getValue(),  1, 8,  SType.SLIDER_INT));
            l.add(new SI("Place Range", (float) m.placeRange.getValue(), 1, 5,  SType.SLIDER));
            l.add(new SI("Skip Friend", m.skipFriend.isEnabled(),             SType.TOGGLE));
            l.add(new SI("Silent Swap", m.silentSwap.isEnabled(),              SType.TOGGLE));
            return l;
        }

        // ── Aura ─────────────────────────────────────────────────
        if (name.equals("Aura") && rawMod instanceof Aura a) {
            l.add(new SI("Range",          a.range, 1f, 6f, SType.SLIDER));
            l.add(new SI("FOV",            (float) a.fov, 1f, 180f, SType.SLIDER_INT));
            l.add(new SI("Through Walls",  a.throughWalls,     SType.TOGGLE));
            l.add(new SI("Smooth Rot",     a.smoothRotation,   SType.TOGGLE));
            l.add(new SI("Shield Breaker", a.shieldBreaker,    SType.TOGGLE));
            l.add(new SI("Auto Weapon",    a.autoWeapon,       SType.TOGGLE));
            l.add(new SI("Only Weapon",    a.onlyWeapon,       SType.TOGGLE));
            l.add(new SI("Rotation",       a.rotationMode.name(), new String[]{"NONE","LEGIT","NORMAL"}, SType.MODE));
            l.add(new SI("Aim",            a.aimMode.name(),      new String[]{"HEAD","BODY","LEGS"},   SType.MODE));
            l.add(new SI("Sort",           a.sort.name(),         new String[]{"Distance","LowestHealth","HighestHealth"}, SType.MODE));
            l.add(new SI("Pause Eating",   a.pauseWhileEating,    SType.TOGGLE));
            l.add(new SI("Pause Inventory",a.pauseInInventory,    SType.TOGGLE));
            return l;
        }

        // Generic fallback
        l.add(new SI("Enabled", true,         SType.TOGGLE));
        l.add(new SI("Range",   3f, 1f, 6f,   SType.SLIDER));
        l.add(new SI("Speed",   1f, 0f, 5f,   SType.SLIDER));
        l.add(new SI("Mode",    "Default", new String[]{"Default","Silent","Strict"}, SType.MODE));
        return l;
    }

    private void applyMod(String name, SI s) {
        if (name == null) return;
        Module rawMod = ModuleManager.getModule(name);

        if (rawMod instanceof AutoTNTcart m) {
            switch (s.name) {
                case "Place Delay" -> m.placeDelay.setValue(((Number)s.value).doubleValue());
                case "Shoot Delay" -> m.shootDelay.setValue(((Number)s.value).doubleValue());
                case "Rail Count"  -> m.railCount.setValue(((Number)s.value).doubleValue());
                case "Place Range" -> m.placeRange.setValue(((Number)s.value).doubleValue());
                case "Skip Friend" -> m.skipFriend.setEnabled((boolean)s.value);
                case "Silent Swap" -> m.silentSwap.setEnabled((boolean)s.value);
            }
            return;
        }

        if (!(rawMod instanceof Aura a)) return;
        switch (s.name) {
            case "Range"           -> a.range = ((Number)s.value).floatValue();
            case "FOV"             -> a.fov   = (int)((Number)s.value).floatValue();
            case "Through Walls"   -> a.throughWalls    = (boolean)s.value;
            case "Smooth Rot"      -> a.smoothRotation  = (boolean)s.value;
            case "Shield Breaker"  -> a.shieldBreaker   = (boolean)s.value;
            case "Auto Weapon"     -> a.autoWeapon      = (boolean)s.value;
            case "Only Weapon"     -> a.onlyWeapon      = (boolean)s.value;
            case "Pause Eating"    -> a.pauseWhileEating= (boolean)s.value;
            case "Pause Inventory" -> a.pauseInInventory= (boolean)s.value;
            case "Rotation"        -> { String v=(String)s.value; a.rotationMode=v.equals("NONE")?Aura.RotationMode.NONE:v.equals("LEGIT")?Aura.RotationMode.LEGIT:Aura.RotationMode.NORMAL; }
            case "Aim"             -> { String v=(String)s.value; a.aimMode=v.equals("HEAD")?Aura.AimMode.HEAD:v.equals("LEGS")?Aura.AimMode.LEGS:Aura.AimMode.BODY; }
            case "Sort"            -> { String v=(String)s.value; a.sort=v.equals("LowestHealth")?Aura.SortMode.LowestHealth:v.equals("HighestHealth")?Aura.SortMode.HighestHealth:Aura.SortMode.LowestDistance; }
        }
    }

    private int computeContentH() {
        int h = 0;
        for (int c = 0; c < ACCS.length; c++) {
            h += CAT_H;
            if (catOpen[c]) {
                for (String mod : ACCS[c].items) {
                    h += ITEM_H;
                    if (mod.equals(openMod) && !settings.isEmpty())
                        h += settings.size() * SET_ITH + 4;
                }
            }
        }
        return h;
    }

    private String keyName(int kc) {
        if (kc >= 65 && kc <= 90)   return String.valueOf((char) kc);
        if (kc >= 48 && kc <= 57)   return String.valueOf((char) kc);
        if (kc >= 290 && kc <= 301) return "F" + (kc - 289);
        return switch (kc) {
            case 32  -> "SPC"; case 257 -> "ENT"; case 258 -> "TAB";
            case 340 -> "SHF"; case 341 -> "CTL"; case 342 -> "ALT";
            case 265 -> "↑";   case 264 -> "↓";   case 263 -> "←"; case 262 -> "→";
            default  -> "K" + kc;
        };
    }

    private void fillR(DrawContext ctx, int x1, int y1, int x2, int y2, int col, int r) {
        if (r <= 0 || x2 - x1 < r * 2 || y2 - y1 < r * 2) { ctx.fill(x1, y1, x2, y2, col); return; }
        ctx.fill(x1 + r, y1, x2 - r, y2, col);
        ctx.fill(x1, y1 + r, x1 + r, y2 - r, col);
        ctx.fill(x2 - r, y1 + r, x2, y2 - r, col);
        for (int i = 0; i < r; i++) {
            int d = r - (int)Math.sqrt((double)(r*r) - (double)((r-i)*(r-i)));
            ctx.fill(x1+d, y1+i, x1+r, y1+i+1, col);
            ctx.fill(x2-r, y1+i, x2-d, y1+i+1, col);
            ctx.fill(x1+d, y2-i-1, x1+r, y2-i, col);
            ctx.fill(x2-r, y2-i-1, x2-d, y2-i, col);
        }
    }

    private void borderR(DrawContext ctx, int x, int y, int w, int h, int col, int r) {
        ctx.fill(x+r, y, x+w-r, y+1, col); ctx.fill(x+r, y+h-1, x+w-r, y+h, col);
        ctx.fill(x, y+r, x+1, y+h-r, col); ctx.fill(x+w-1, y+r, x+w, y+h-r, col);
        for (int i = 0; i < r; i++) {
            int d = r - (int)Math.sqrt((double)(r*r) - (double)((r-i)*(r-i)));
            ctx.fill(x+d, y+i, x+d+1, y+i+1, col);
            ctx.fill(x+w-d-1, y+i, x+w-d, y+i+1, col);
            ctx.fill(x+d, y+h-i-1, x+d+1, y+h-i, col);
            ctx.fill(x+w-d-1, y+h-i-1, x+w-d, y+h-i, col);
        }
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { super.close(); ConfigManager.saveAllConfigs(); }

    // ════════════════════════════════════════════════════════════════
    // DATA
    // ════════════════════════════════════════════════════════════════
    private static class AccCat { String name; String[] items; AccCat(String n, String[] i){ name=n; items=i; } }
    private enum SType { TOGGLE, SLIDER, SLIDER_INT, MODE }
    private static class SI {
        String name; Object value; float min, max; SType type; Object extra;
        SI(String n, Object v, SType t)                            { name=n; value=v; type=t; }
        SI(String n, Object v, float mn, float mx, SType t)        { this(n,v,t); min=mn; max=mx; }
        SI(String n, Object v, String[] opts, SType t)             { this(n,v,t); extra=opts; }
    }
}