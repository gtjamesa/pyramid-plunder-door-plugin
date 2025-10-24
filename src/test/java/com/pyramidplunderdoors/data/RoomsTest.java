package com.pyramidplunderdoors.data;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RoomsTest
{
	@Mock
	private Client client;

	@Test
	public void shouldGetRoomByFloor()
	{
		Room room = Rooms.getRoom(3);
		assertEquals(3, room.getFloor());
	}

	@Test
	public void shouldGetRoomByPoint()
	{
		Room room = Rooms.getRoom(new WorldPoint(1950, 4450, 0));
		assertNotNull(room);
		assertEquals(5, room.getFloor());
	}
}
