package com.pyramidplunderdoors;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.pyramidplunderdoors.config.RemoveNpc;
import com.pyramidplunderdoors.data.Door;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PyramidPlunderDoorsPluginTest
{
	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private PyramidPlunderDoorsConfig config;

	@InjectMocks
	PyramidPlunderDoorsPlugin plugin = spy(new PyramidPlunderDoorsPlugin());

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	private void setUpDoorTiles()
	{
		WorldPoint[] points = {
			new WorldPoint(1923, 4432, 0),
			new WorldPoint(1923, 4466, 0),
			new WorldPoint(1924, 4472, 0),
			new WorldPoint(1925, 4440, 0),
			new WorldPoint(1929, 4440, 0),
			new WorldPoint(1931, 4432, 0),
			new WorldPoint(1931, 4472, 0),
			new WorldPoint(1932, 4450, 0),
			new WorldPoint(1932, 4466, 0), // room 1
			new WorldPoint(1937, 4448, 0),
			new WorldPoint(1937, 4459, 0),
			null,
			new WorldPoint(1941, 4456, 0),
			new WorldPoint(1946, 4433, 0),
			new WorldPoint(1949, 4422, 0),
			new WorldPoint(1962, 4448, 0),
		};

		// Create test data for tilesToHighlight
		Map<TileObject, Door> mockTiles = new HashMap<>();

		for (WorldPoint point : points)
		{
			TileObject mockTileObject = mock(TileObject.class);
			Tile mockTile = mock(Tile.class);
			when(mockTile.getWorldLocation()).thenReturn(point);
			mockTiles.put(mockTileObject, new Door(mockTileObject, mockTile));
		}

		// Add a null tile object to test null handling
		TileObject mockTileObject = mock(TileObject.class);
		mockTiles.put(mockTileObject, null);

		// Stub the getAllDoors() method
		doReturn(mockTiles).when(plugin).getAllDoors();
	}

	@Test
	public void shouldFindClosestDoor()
	{
		setUpDoorTiles();

		// player is 1 tile away from the closest door at (1932, 4466, 0)
		Door res = plugin.findClosestDoor(new WorldPoint(1931, 4466, 0));
		assertNotNull(res);
		assertEquals(1932, res.getTile().getWorldLocation().getX());
		assertEquals(4466, res.getTile().getWorldLocation().getY());
	}

	@Test
	public void shouldHideNoNpcs()
	{
		doReturn(true).when(plugin).isInPyramidPlunder();
		when(config.removeNpcs()).thenReturn(RemoveNpc.NONE);

		NPC npc = mock(NPC.class);

		boolean res = plugin.shouldDraw(npc, false);
		assertTrue(res);
	}

	@Test
	public void shouldHideAllNpcs()
	{
		doReturn(true).when(plugin).isInPyramidPlunder();
		when(config.removeNpcs()).thenReturn(RemoveNpc.ALL);

		Player player = mock(Player.class);
		when(player.getName()).thenReturn("LocalPlayer");
		when(client.getLocalPlayer()).thenReturn(player);

		NPC npc = mock(NPC.class);
		when(npc.getId()).thenReturn(7661);

		boolean res = plugin.shouldDraw(npc, false);
		assertFalse(res);
	}

	@Test
	public void shouldHideOtherNpcs()
	{
		doReturn(true).when(plugin).isInPyramidPlunder();
		when(config.removeNpcs()).thenReturn(RemoveNpc.OTHER_PLAYERS);

		Player player = mock(Player.class);
		when(player.getName()).thenReturn("LocalPlayer");
		when(client.getLocalPlayer()).thenReturn(player);

		// interacting with another player (hide=true)
		NPC npc1 = mock(NPC.class);
		when(npc1.getId()).thenReturn(7661);
		Actor interacting1 = mock(Actor.class);
		when(interacting1.getName()).thenReturn("OtherPlayer");
		when(npc1.getInteracting()).thenReturn(interacting1);

		// interacting with null (hide=true)
		NPC npc2 = mock(NPC.class);
		when(npc2.getId()).thenReturn(7661);
		Actor interacting2 = mock(Actor.class);
		when(interacting2.getName()).thenReturn(null);
		when(npc2.getInteracting()).thenReturn(interacting2);

		// interacting with local player (hide=false)
		NPC npc3 = mock(NPC.class);
		when(npc3.getId()).thenReturn(7661);
		Actor interacting3 = mock(Actor.class);
		when(interacting3.getName()).thenReturn("LocalPlayer");
		when(npc3.getInteracting()).thenReturn(interacting3);

		assertFalse(plugin.shouldDraw(npc1, false));
		assertFalse(plugin.shouldDraw(npc2, false));
		assertTrue(plugin.shouldDraw(npc3, false));
	}
}