package com.vengtimer;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.api.events.AnimationChanged;
import net.runelite.api.Actor;
import net.runelite.api.Player;

import java.time.Instant;
import java.time.Duration;

import net.runelite.api.events.GameTick;

import net.runelite.api.events.GraphicChanged;

import java.util.HashMap;
import java.util.Map;

import net.runelite.client.ui.overlay.OverlayManager;

import net.runelite.api.WorldType;



@Slf4j
@PluginDescriptor(
		name = "Vengeance Timer",
		description = "Tracks Vengeance Timers for All Players. This does NOT work in PVP worlds or areas.",
		tags = {"vengeance", "pvm", "timer", "spells", "cast", "taste", "other"}
)
public class VengTimerPlugin extends Plugin
{
	private final Map<Player, Instant> vengeTimers = new HashMap<>();
	private final Map<Player, Instant> vengeOtherTimers = new HashMap<>();

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private VengTimerOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private VengTimerConfig config;

	public Map<Player, Instant> getVengeTimers()
	{
		return vengeTimers;
	}

	public Map<Player, Instant> getVengeOtherTimers()
	{
		return vengeOtherTimers;
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (isInPvP() && config.localOnly())
		{
			return;
		}

		Actor actor = event.getActor();

		if (!(actor instanceof Player) || actor.getGraphic() != 726)
		{
			return;
		}

		Player player = (Player) actor;

		if (config.localOnly() && player != client.getLocalPlayer())
		{
			return;
		}

		// debounce per player
		Instant last = vengeTimers.get(player);
		if (last != null && Duration.between(last, Instant.now()).toMillis() < 1000)
		{
			return;
		}

		vengeTimers.put(player, Instant.now().plusSeconds(30));

		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				player.getName() + " cast Vengeance!",
				null
		);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (isInPvP() && !config.localOnly())
		{
			return;
		}

		Actor actor = event.getActor();

		if (!(actor instanceof Player) || actor.getAnimation() != 4411)
		{
			return;
		}

		Player player = (Player) actor;

		if (config.localOnly() && player != client.getLocalPlayer())
		{
			return;
		}

		Instant last = vengeOtherTimers.get(player);
		if (last != null && Duration.between(last, Instant.now()).toMillis() < 1000)
		{
			return;
		}

		vengeOtherTimers.put(player, Instant.now().plusSeconds(30));

		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				player.getName() + " cast Vengeance Other!",
				null
		);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (isInPvP() && !config.localOnly())
		{
			vengeTimers.clear();
			vengeOtherTimers.clear();
			return;
		}

		if (config.localOnly())
		{
			Player local = client.getLocalPlayer();

			vengeTimers.keySet().removeIf(p -> p != local);
			vengeOtherTimers.keySet().removeIf(p -> p != local);
		}

		Instant now = Instant.now();

		vengeTimers.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();

			if (player == null || now.isAfter(entry.getValue()))
			{
				if (player != null)
				{
					client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							"",
							player.getName() + " Vengeance READY",
							null
					);
				}
				return true;
			}
			return false;
		});

		vengeOtherTimers.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();

			if (player == null || now.isAfter(entry.getValue()))
			{
				if (player != null)
				{
					client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							"",
							player.getName() + " Vengeance Other READY",
							null
					);
				}
				return true;
			}
			return false;
		});
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Provides
	VengTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VengTimerConfig.class);
	}

	public boolean isInPvP()
	{
		// PvP worlds
		if (client.getWorldType().contains(WorldType.PVP))
		{
			return true;
		}

		// Wilderness check (varbit)
		int wildernessLevel = client.getVarbitValue(5963);
		return wildernessLevel > 0;
	}
}
