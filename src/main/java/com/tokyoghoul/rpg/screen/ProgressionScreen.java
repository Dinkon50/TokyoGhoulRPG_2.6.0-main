package com.tokyoghoul.rpg.screen;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.client.ClientGhoulData;
import com.tokyoghoul.rpg.client.TokyoGuiStyle;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.UpgradePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Full-screen RC progression interface with compact anime-style information hierarchy. */
public class ProgressionScreen extends Screen {
    private record Node(String stat, int rank, String title, String desc, int x, int y) {}
    private final List<Node> nodes = new ArrayList<>();
    private int hovered = -1;
    private int panelLeft, panelTop, panelW, panelH;

    public ProgressionScreen() { super(TokyoGuiStyle.ui("RC // ПРОГРЕССИЯ")); }

    @Override protected void init() {
        nodes.clear();
        String[][] branches = switch (ClientGhoulData.race()) {
            case CCG -> new String[][]{
                {"investigation", "РАССЛЕДОВАНИЕ", "Поиск следов и слабых мест"},
                {"quinque", "КВИНКЕ", "Урон, техника и дистанция"},
                {"tactics", "ТАКТИКА", "Контратаки и точность"},
                {"armor", "АРМАТА", "Защита и стойкость"},
                {"discipline", "ДИСЦИПЛИНА", "Контроль и выносливость"},
                {"quinque_mastery", "МАСТЕРСТВО", "Особые эффекты Квинке"}
            };
            case HUMAN -> new String[][]{
                {"strength", "БОЙ", "Удар и пробивание"}, {"speed", "ПОДВИЖНОСТЬ", "Рывок и уклонение"},
                {"stealth", "СКРЫТНОСТЬ", "Засада и отход"}, {"regen", "МЕДИЦИНА", "Лечение и выживание"}
            };
            default -> new String[][]{
                {"kagune", "КАГУНЕ", "Форма и контроль органа"}, {"strength", "СИЛА", "Удар и пробивание"},
                {"speed", "СКОРОСТЬ", "Рывок и мобильность"}, {"regen", "РЕГЕНЕРАЦИЯ", "Восстановление RC"},
                {"stealth", "СКРЫТНОСТЬ", "Засада и скрытность"}
            };
        };

        panelW = Math.min(980, width - 20);
        panelH = Math.min(620, height - 18);
        panelLeft = (width - panelW) / 2;
        panelTop = (height - panelH) / 2;

        int count = branches.length;
        int treeLeft = panelLeft + 46;
        int treeRight = panelLeft + panelW - 46;
        int colGap = count == 6 ? 104 : count == 5 ? 125 : 145;
        int center = width / 2;
        int startX = center - ((count - 1) * colGap) / 2;
        int top = panelTop + 112;
        int rowGap = 49;
        for (int b = 0; b < count; b++) {
            int x = Mth.clamp(startX + b * colGap, treeLeft, treeRight);
            int ranks = "quinque_mastery".equals(branches[b][0]) ? 8 : 5;
            for (int rank = 1; rank <= ranks; rank++) {
                int yy = top + (rank - 1) * rowGap;
                nodes.add(new Node(branches[b][0], rank, branches[b][1] + "  //  " + rank, branches[b][2], x, yy));
            }
        }
        if (ClientGhoulData.race() == GhoulData.Race.CCG) {
            int by = Math.min(panelTop + panelH - 130, top + 8 * rowGap + 14);
            int bx = center - 160;
            nodes.add(new Node("quinque_bleed", 1, "КРОВОТЕЧЕНИЕ", "Шанс наложить кровотечение", bx, by));
            nodes.add(new Node("quinque_poison", 1, "ТОКСИН", "Шанс наложить яд", bx + 105, by + 25));
            nodes.add(new Node("quinque_blind", 1, "ПОМУТНЕНИЕ", "Шанс ослепить", bx + 210, by));
            nodes.add(new Node("quinque_slow", 1, "ЗАМЕДЛЕНИЕ", "Шанс замедлить цель", bx + 315, by + 25));
        }
        if (ClientGhoulData.race() != GhoulData.Race.HUMAN) {
            nodes.add(new Node("rage", 1,
                ClientGhoulData.race() == GhoulData.Race.CCG ? "БОЕВОЙ ДУХ" : "ЯРОСТЬ",
                "Ультимативный режим. Требует развитое древо.", center, panelTop + panelH - 112));
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forMusic(TokyoGhoulRPG.SKILL_TREE_TENSION.get()));
    }

    @Override public void onClose() {
        Minecraft.getInstance().getSoundManager().stop(TokyoGhoulRPG.SKILL_TREE_TENSION.get().getLocation(), SoundSource.MUSIC);
        super.onClose();
    }

    private int value(String stat) {
        return switch (stat) {
            case "strength" -> ClientGhoulData.strength(); case "speed" -> ClientGhoulData.speed(); case "kagune" -> ClientGhoulData.kagune();
            case "regen" -> ClientGhoulData.regen(); case "stealth" -> ClientGhoulData.stealth(); case "investigation" -> ClientGhoulData.investigation();
            case "quinque" -> ClientGhoulData.quinque(); case "tactics" -> ClientGhoulData.tactics(); case "armor" -> ClientGhoulData.armor();
            case "discipline" -> ClientGhoulData.discipline(); case "quinque_mastery" -> ClientGhoulData.quinqueMastery();
            case "quinque_bleed" -> ClientGhoulData.quinqueBleed() ? 1 : 0; case "quinque_poison" -> ClientGhoulData.quinquePoison() ? 1 : 0;
            case "quinque_blind" -> ClientGhoulData.quinqueBlind() ? 1 : 0; case "quinque_slow" -> ClientGhoulData.quinqueSlow() ? 1 : 0;
            default -> 0;
        };
    }

    private boolean active(Node n) { return "rage".equals(n.stat) ? ClientGhoulData.rageUnlocked() : value(n.stat) >= n.rank; }

    private boolean available(Node n) {
        if (n.stat.startsWith("quinque_") && !n.stat.equals("quinque_mastery"))
            return ClientGhoulData.quinqueMastery() >= 3 && value(n.stat) == 0 && ClientGhoulData.points() > 0;
        if ("rage".equals(n.stat)) {
            int core = Math.max(Math.max(ClientGhoulData.strength(), ClientGhoulData.speed()), ClientGhoulData.kagune());
            return !ClientGhoulData.rageUnlocked() && ClientGhoulData.points() >= 5 && ClientGhoulData.level() >= 8 && core >= 3;
        }
        return value(n.stat) == n.rank - 1 && ClientGhoulData.points() > 0;
    }

    private void learn(Node n) { if (available(n)) NetworkHandler.CHANNEL.sendToServer(new UpgradePacket(n.stat)); }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        TokyoGuiStyle.background(g, width, height);
        int accent = ClientGhoulData.race() == GhoulData.Race.CCG ? TokyoGuiStyle.CCG : TokyoGuiStyle.RED;
        TokyoGuiStyle.panel(g, panelLeft, panelTop, panelW, panelH, accent);
        TokyoGuiStyle.title(g, font, "RC // ПРОГРЕССИЯ", raceName() + "  •  уровень " + ClientGhoulData.level(), width / 2, panelTop + 14);

        // Top status strip.
        int sy = panelTop + 58;
        g.fill(panelLeft + 18, sy, panelLeft + panelW - 18, sy + 34, 0xB40A0E13);
        g.drawString(font, TokyoGuiStyle.ui("ОЧКИ  " + ClientGhoulData.points()), panelLeft + 30, sy + 12, TokyoGuiStyle.GOLD, true);
        g.drawString(font, TokyoGuiStyle.ui("RC  " + ClientGhoulData.rc()), panelLeft + 142, sy + 12, TokyoGuiStyle.STEEL, false);
        g.drawString(font, TokyoGuiStyle.ui("КАГУНЕ  " + (ClientGhoulData.kaguneActive() ? "ВЫПУЩЕНО" : "СКРЫТО")), panelLeft + panelW - 170, sy + 12, ClientGhoulData.kaguneActive() ? TokyoGuiStyle.RED_BRIGHT : TokyoGuiStyle.STEEL, false);
        TokyoGuiStyle.divider(g, panelLeft + 18, sy + 42, panelW - 36);

        drawConnections(g);
        drawInfo(g, accent);
        super.render(g, mx, my, pt);
        // Draw the nodes last so their highlights stay crisp above the panel.
        drawNodes(g, mx, my);
    }

    private String raceName() {
        return switch (ClientGhoulData.race()) { case GHOUL -> "ГУЛЬ"; case HALF_GHOUL -> "ПОЛУГУЛЬ"; case CCG -> "CCG"; default -> "ЧЕЛОВЕК"; };
    }

    private void drawConnections(GuiGraphics g) {
        for (Node a : nodes) for (Node b : nodes) {
            if (a.stat.equals(b.stat) && b.rank == a.rank + 1) line(g, a.x, a.y + 14, b.x, b.y - 14, active(b) ? 0xFF6E2334 : 0xFF292F38);
        }
    }

    private void drawNodes(GuiGraphics g, int mx, int my) {
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            boolean a = active(n), ok = available(n), hot = i == hovered;
            int r = "rage".equals(n.stat) ? 20 : 16;
            int border = a ? TokyoGuiStyle.RED_BRIGHT : ok ? TokyoGuiStyle.GOLD : 0xFF3A424D;
            if (hot) border = TokyoGuiStyle.WHITE;
            g.fill(n.x - r - 3, n.y - r - 3, n.x + r + 3, n.y + r + 3, 0xFF050608);
            g.fill(n.x - r, n.y - r, n.x + r, n.y + r, border);
            g.fill(n.x - r + 2, n.y - r + 2, n.x + r - 2, n.y + r - 2, a ? 0xFF281018 : 0xFF10151B);
            g.blit(new net.minecraft.resources.ResourceLocation(TokyoGhoulRPG.MODID, "textures/gui/skill_icons/" + n.stat + ".png"), n.x - 10, n.y - 10, 0, 0, 20, 20, 32, 32);
            if (!"rage".equals(n.stat)) g.drawString(font, TokyoGuiStyle.ui(String.valueOf(n.rank)), n.x + 9, n.y + 6, TokyoGuiStyle.WHITE, true);
            if (hot) {
                g.fill(n.x - r - 4, n.y - r - 4, n.x + r + 4, n.y - r - 2, TokyoGuiStyle.RED_BRIGHT);
                g.fill(n.x - r - 4, n.y + r + 2, n.x + r + 4, n.y + r + 4, TokyoGuiStyle.RED_BRIGHT);
            }
        }
    }

    private void drawInfo(GuiGraphics g, int accent) {
        int x = panelLeft + 18, y = panelTop + panelH - 78, w = panelW - 36;
        g.fill(x, y, x + w, y + 58, 0xD30A0E13);
        g.fill(x, y, x + 3, y + 58, accent);
        if (hovered >= 0) {
            Node n = nodes.get(hovered);
            g.drawString(font, TokyoGuiStyle.ui(n.title), x + 14, y + 10, TokyoGuiStyle.WHITE, true);
            g.drawString(font, TokyoGuiStyle.ui(n.desc), x + 14, y + 27, TokyoGuiStyle.STEEL, false);
            String state = active(n) ? "РАЗВИТО" : available(n) ? "ДОСТУПНО  •  ЛКМ" : "ТРЕБУЕТСЯ ПРЕДЫДУЩИЙ РАНГ";
            g.drawString(font, TokyoGuiStyle.ui(state), x + w - 220, y + 19, active(n) ? TokyoGuiStyle.GREEN : available(n) ? TokyoGuiStyle.GOLD : 0xFF66707B, true);
        } else {
            g.drawString(font, TokyoGuiStyle.ui("ВЫБЕРИ УЗЕЛ"), x + 14, y + 10, TokyoGuiStyle.WHITE, true);
            g.drawString(font, TokyoGuiStyle.ui("Каждое очко меняет реальные параметры твоего персонажа."), x + 14, y + 28, TokyoGuiStyle.STEEL, false);
        }
    }

    private void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(1, (int)Math.hypot(x2 - x1, y2 - y1));
        for (int i = 0; i < steps; i += 2) {
            float t = i / (float) steps;
            int x = (int) Mth.lerp(t, x1, x2), y = (int) Mth.lerp(t, y1, y2);
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    @Override public void mouseMoved(double mx, double my) {
        hovered = -1;
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (Math.hypot(mx - n.x, my - n.y) <= 22) { hovered = i; break; }
        }
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && hovered >= 0) { learn(nodes.get(hovered)); return true; }
        return super.mouseClicked(mx, my, button);
    }
}
