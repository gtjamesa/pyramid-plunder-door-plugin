package com.pyramidplunderdoors;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
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

	@Spy
	PyramidPlunderDoorsPlugin plugin;

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
		Map<TileObject, Tile> mockTiles = new HashMap<>();

		for (WorldPoint point : points)
		{
			TileObject mockTileObject = mock(TileObject.class);
			Tile mockTile = mock(Tile.class);
			when(mockTile.getWorldLocation()).thenReturn(point);
			mockTiles.put(mockTileObject, mockTile);
		}

		// Add a null tile object to test null handling
		TileObject mockTileObject = mock(TileObject.class);
		mockTiles.put(mockTileObject, null);

		// Stub the getTilesToHighlight() method
		doReturn(mockTiles).when(plugin).getTilesToHighlight();
	}

	@Test
	public void shouldFindClosestDoor()
	{
		setUpDoorTiles();

		// player is 1 tile away from the closest door at (1932, 4466, 0)
		Tile res = plugin.findClosestDoor(new WorldPoint(1931, 4466, 0));
		assertNotNull(res);
		assertEquals(1932, res.getWorldLocation().getX());
		assertEquals(4466, res.getWorldLocation().getY());
	}
}