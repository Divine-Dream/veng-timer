package com.vengtimer;

import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import net.runelite.api.Perspective;

import net.runelite.api.Client;

import net.runelite.client.game.SpriteManager;
import net.runelite.api.SpriteID;

public class VengTimerOverlay extends Overlay
{
    private final VengTimerPlugin plugin;
    @Inject
    private Client client;

    @Inject
    private VengTimerConfig config;

    @Inject
    private SpriteManager spriteManager;

    private static final int VENGE_SPRITE = SpriteID.SPELL_VENGEANCE;
    private static final int VENGE_OTHER_SPRITE = SpriteID.SPELL_VENGEANCE_OTHER;

    @Inject
    public VengTimerOverlay(VengTimerPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.isInPvP() && !config.localOnly())
        {
            return null;
        }

        renderMap(graphics, plugin.getVengeTimers(), VENGE_SPRITE);
        renderMap(graphics, plugin.getVengeOtherTimers(), VENGE_OTHER_SPRITE);
        return null;
    }

    private void renderMap(Graphics2D graphics, Map<Player, Instant> map, int spriteId)
    {
        for (Map.Entry<Player, Instant> entry : map.entrySet())
        {
            Player player = entry.getKey();
            Instant end = entry.getValue();

            if (player == null || player.getLocalLocation() == null)
                continue;

            long millis = Duration.between(Instant.now(), end).toMillis();
            if (millis < 0)
                continue;

            String timeText = config.useTicks()
                    ? String.valueOf(millis / 600)
                    : String.valueOf(millis / 1000);

            double offset = Math.max(1.0, config.heightOffset());
            int height = (int) (player.getLogicalHeight() / offset);

            LocalPoint lp = player.getLocalLocation();

            Point textLocation = Perspective.getCanvasTextLocation(
                    client,
                    graphics,
                    lp,
                    timeText,
                    height
            );

            if (textLocation == null)
                continue;

            // Get sprite
            Image sprite = spriteManager.getSprite(spriteId, 0);

            int x = textLocation.getX();
            int y = textLocation.getY();

            // Draw sprite slightly left of text
            if (sprite != null)
            {
                int iconSize = config.fontSize(); // scale with font
                graphics.drawImage(sprite, x - iconSize - 2, y - iconSize + 4, iconSize, iconSize, null);
            }

            // Set font
            Font originalFont = graphics.getFont();
            graphics.setFont(new Font("Arial", Font.BOLD, config.fontSize()));

            // Shadow
            OverlayUtil.renderTextLocation(graphics, textLocation, timeText, Color.BLACK);

            // Main Text
            OverlayUtil.renderTextLocation(graphics, textLocation, timeText, config.color());

            graphics.setFont(originalFont);
        }
    }
}