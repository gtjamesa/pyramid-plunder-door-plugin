package com.pyramidplunderdoors;

import com.pyramidplunderdoors.config.CustomChatMessage;
import com.pyramidplunderdoors.config.RemoveNpc;
import java.util.Collections;
import java.util.Set;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(PyramidPlunderDoorsPlugin.CONFIG_GROUP)
public interface PyramidPlunderDoorsConfig extends Config
{
	@ConfigSection(
		name = "Door Tracking",
		description = "Track doors opened by other players",
		position = 2
	)
	String DOOR_TRACKING = "Door Tracking";

	@ConfigItem(
		keyName = "removeNpcs",
		name = "Hide Guardians",
		description = "Hides guardian Mummy and Scarab Swarm NPCs<br><br>" +
			"None - Do not remove any NPCs<br>" +
			"Other Players - Remove NPCs targeting other players<br>" +
			"All - Remove all guardian NPCs",
		position = 1
	)
	default RemoveNpc removeNpcs()
	{
		return RemoveNpc.OTHER_PLAYERS;
	}

	@ConfigItem(
		keyName = "chatMessageInfo",
		name = "Chat Messages",
		description = "Add a chat messages for certain door events",
		position = 1,
		section = DOOR_TRACKING
	)
	default Set<CustomChatMessage> chatMessageInfo()
	{
		return Set.of(CustomChatMessage.PLAYER_OPENED_DOOR, CustomChatMessage.DOORS_RESET);
	}
}
