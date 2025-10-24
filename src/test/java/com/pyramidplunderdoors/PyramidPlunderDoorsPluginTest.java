package com.pyramidplunderdoors;

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
	private Client client;

	@Mock
	private PyramidPlunderDoorsConfig config;

	@InjectMocks
	PyramidPlunderDoorsPlugin plugin = spy(new PyramidPlunderDoorsPlugin());

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

	private void setUpNpcHiding()
	{
		doReturn(true).when(plugin).isInPyramidPlunder();

		Player player = mock(Player.class);
		when(player.getName()).thenReturn("LocalPlayer");
		when(client.getLocalPlayer()).thenReturn(player);
	}

	private void setUpTaggedNpcs()
	{
		final HashMap<Integer, String> guardians = new HashMap<>();
		guardians.put(1, "OtherPlayer");
		guardians.put(2, null);
		guardians.put(3, "LocalPlayer");

		doReturn(guardians).when(plugin).getGuardians();
	}

	private NPC mockNpcInteract(String playerName, Integer index)
	{
		NPC npc = mock(NPC.class);
		when(npc.getId()).thenReturn(7661);
		Actor interacting = mock(Actor.class);
		when(interacting.getName()).thenReturn(playerName);
		when(npc.getInteracting()).thenReturn(interacting);

		if (index != null)
		{
			when(npc.getIndex()).thenReturn(index);
		}

		return npc;
	}

	private NPC mockNpcInteract(String playerName)
	{
		return mockNpcInteract(playerName, null);
	}

	@Test
	public void shouldHideNoNpcs()
	{
		setUpNpcHiding();
		when(config.removeNpcs()).thenReturn(RemoveNpc.NONE);

		NPC npc = mockNpcInteract("LocalPlayer");

		boolean res = plugin.shouldDraw(npc, false);
		assertTrue(res);
	}

	@Test
	public void shouldHideAllNpcs()
	{
		setUpNpcHiding();
		when(config.removeNpcs()).thenReturn(RemoveNpc.ALL);

		NPC npc = mockNpcInteract("LocalPlayer");

		boolean res = plugin.shouldDraw(npc, false);
		assertFalse(res);
	}

	@Test
	public void shouldHideOtherNpcs()
	{
		setUpNpcHiding();
		when(config.removeNpcs()).thenReturn(RemoveNpc.OTHER_PLAYERS);

		NPC npc1 = mockNpcInteract("OtherPlayer"); // interacting with another player (hide=true)
		NPC npc2 = mockNpcInteract(null); // interacting with null (hide=true)
		NPC npc3 = mockNpcInteract("LocalPlayer"); // interacting with local player (hide=false)

		assertFalse(plugin.shouldDraw(npc1, false));
		assertFalse(plugin.shouldDraw(npc2, false));
		assertTrue(plugin.shouldDraw(npc3, false));
	}

	@Test
	public void shouldRespectTaggedNpc()
	{
		setUpNpcHiding();
		setUpTaggedNpcs();

		when(config.removeNpcs()).thenReturn(RemoveNpc.OTHER_PLAYERS);

		NPC npc1 = mockNpcInteract("LocalPlayer", 1); // now interacting with local player (hide=true)
		NPC npc2 = mockNpcInteract(null, 2); // interacting with null (hide=true)
		NPC npc3 = mockNpcInteract("LocalPlayer", 3); // interacting with local player (hide=false)

		assertFalse(plugin.shouldDraw(npc1, false)); // idx1 = OtherPlayer -> LocalPlayer, sticks as OtherPlayer
		assertFalse(plugin.shouldDraw(npc2, false)); // idx2 = null
		assertTrue(plugin.shouldDraw(npc3, false)); // idx3 = null -> LocalPlayer
	}

//	@Test
//	public void shouldUpdateTaggedNpc()
//	{
//		setUpNpcHiding();
//		setUpTaggedNpcs();
//
//		when(config.removeNpcs()).thenReturn(RemoveNpc.OTHER_PLAYERS);
//
//		NPC npc1 = mockNpcInteract("OtherPlayer", 1); // interacting with another player (hide=true)
//		NPC npc2 = mockNpcInteract(null, 3); // interacting with null (hide=true)
//
//		// interacting with local player (hide=false)
//		// this npc was tagged as "null" on spawn (idx: 2), but is now interacting with local player
//		NPC npc3 = mockNpcInteract("LocalPlayer", 2);
//
//		assertFalse(plugin.shouldDraw(npc1, false));
//		assertTrue(plugin.shouldDraw(npc2, false)); // idx3 is LocalPlayer
//		assertTrue(plugin.shouldDraw(npc3, false)); // idx2 is null -> LocalPlayer
//	}
}