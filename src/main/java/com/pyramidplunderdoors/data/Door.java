package com.pyramidplunderdoors.data;

import lombok.Data;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

@Data
public class Door
{
	private final TileObject wallObject;
	private final Tile tile;

	public WorldPoint getWorldLocation()
	{
		return tile.getWorldLocation();
	}
}
