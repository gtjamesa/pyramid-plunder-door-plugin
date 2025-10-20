package com.pyramidplunderdoors.data;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

public class Rooms
{
	private static final List<Room> ROOMS = new ArrayList<>();

	static
	{
		ROOMS.add(new Room(1, 1921, 4461, 1936, 4481));
		ROOMS.add(new Room(2, 1946, 4462, 1962, 4481));
		ROOMS.add(new Room(3, 1968, 4452, 1982, 4475));
		ROOMS.add(new Room(4, 1921, 4447, 1945, 4462));
		ROOMS.add(new Room(5, 1948, 4442, 1968, 4459));
		ROOMS.add(new Room(6, 1919, 4422, 1934, 4444));
		ROOMS.add(new Room(7, 1939, 4419, 1959, 4436));
		ROOMS.add(new Room(8, 1965, 4418, 1981, 4441));
	}

	public static Room getRoom(WorldPoint point)
	{
		for (Room room : ROOMS)
		{
			if (room.contains(point))
			{
				return room;
			}
		}

		return null;
	}

	public static Room getRoom(int floorNumber)
	{
		return ROOMS.get(floorNumber - 1);
	}

	public static boolean inRoom(WorldPoint point)
	{
		return getRoom(point) != null;
	}
}

