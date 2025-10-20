package com.pyramidplunderdoors.data;

import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
public class Room
{
	int floor;
	int x1, y1, x2, y2;

	public boolean contains(WorldPoint point)
	{
		return point.getX() >= x1 && point.getX() <= x2
			&& point.getY() >= y1 && point.getY() <= y2;
	}
}
