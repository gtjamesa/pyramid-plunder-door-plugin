package com.pyramidplunderdoors;

import com.pyramidplunderdoors.config.RemoveNpc;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PyramidPlunderDoorsPlugin.CONFIG_GROUP)
public interface PyramidPlunderDoorsConfig extends Config
{
	@ConfigItem(
		keyName = "showChatMessage",
		name = "Show Chat Message",
		description = "Add a chat message when another player has opened a door",
		position = 1
	)
	default boolean showChatMessage()
	{
		return true;
	}

	@ConfigItem(
		keyName = "removeNpcs",
		name = "Hide Guardians",
		description = "Hides guardian Mummy and Scarab Swarm NPCs<br><br>" +
			"None - Do not remove any NPCs<br>" +
			"Other Players - Remove NPCs targeting other players<br>" +
			"All - Remove all guardian NPCs",
		position = 2
	)
	default RemoveNpc removeNpcs()
	{
		return RemoveNpc.OTHER_PLAYERS;
	}
}
