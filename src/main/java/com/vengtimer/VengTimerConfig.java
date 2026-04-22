package com.vengtimer;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("vengtimer")
public interface VengTimerConfig extends Config
{
	@ConfigItem(
			keyName = "localOnly",
			name = "Local Player Only",
			description = "Only track your own Vengeance timers"
	)
	default boolean localOnly()
	{
		return false;
	}

	@ConfigItem(
			keyName = "fontSize",
			name = "Font Size",
			description = "Size of the timer text"
	)
	default int fontSize()
	{
		return 16;
	}

	@ConfigItem(
			keyName = "heightOffset",
			name = "Text Position",
			description = "Vertical position divisor (e.g. 1.5 = higher, 3.0 = lower)"
	)
	@Range(
			min = 1,
			max = 5
	)
	default double heightOffset()
	{
		return 2.0;
	}

	@ConfigItem(
			keyName = "color",
			name = "Text Color",
			description = "Color of the timer"
	)
	default Color color()
	{
		return Color.WHITE;
	}

	@ConfigItem(
			keyName = "useTicks",
			name = "Use Ticks",
			description = "Show time in game ticks instead of seconds"
	)
	default boolean useTicks()
	{
		return false;
	}
}