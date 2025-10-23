package com.pyramidplunderdoors.data;

import lombok.Data;
import net.runelite.api.NPC;

@Data
public class Guardian
{
	private final NPC npc;
	private String targetPlayerName;
	private int spawnTick;

	public String getTargetPlayerName()
	{
		return npc.getInteracting() != null ? npc.getInteracting().getName() : null;
	}

	public int getNpcIndex()
	{
		return npc.getIndex();
	}
}
