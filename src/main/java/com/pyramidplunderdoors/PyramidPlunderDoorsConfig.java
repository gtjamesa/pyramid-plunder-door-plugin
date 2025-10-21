package com.pyramidplunderdoors;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PyramidPlunderDoorsPlugin.CONFIG_GROUP)
public interface PyramidPlunderDoorsConfig extends Config
{
	@ConfigItem(
		keyName = "showChatMessage",
		name = "Show Chat Message",
		description = "Add a chat message when another player has opened a door"
	)
	default boolean showChatMessage()
	{
		return true;
	}
}
