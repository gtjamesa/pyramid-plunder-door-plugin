package com.pyramidplunderdoors;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.pyramidplunderdoors.data.Door;
import com.pyramidplunderdoors.data.Room;
import com.pyramidplunderdoors.data.Rooms;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Pyramid Plunder QoL"
)
public class PyramidPlunderDoorsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "pyramidplunderqol";
	private static final int SEARCH_TRAPDOOR_ANIMATION = AnimationID.HUMAN_PICKUPTABLE;
	private static final int PYRAMID_PLUNDER_REGION = 7749;
	private static final int TICK_THRESHOLD = 4;
	private static final int TILE_THRESHOLD = 3;
	private final HashMap<String, PlayerInteraction> playerInteractions = new HashMap<>();

	static final Set<Integer> TOMB_DOOR_WALL_IDS = ImmutableSet.of(ObjectID.NTK_TOMB_DOOR1, ObjectID.NTK_TOMB_DOOR2, ObjectID.NTK_TOMB_DOOR3, ObjectID.NTK_TOMB_DOOR4);
	static final Set<Integer> MUMMY_IDS = ImmutableSet.of(NpcID.NTK_MUMMY_1, NpcID.NTK_MUMMY_2, NpcID.NTK_MUMMY_3, NpcID.NTK_MUMMY_4, NpcID.NTK_MUMMY_5);
	static final Set<Integer> SWARM_IDS = ImmutableSet.of(NpcID.NTK_SCARAB_SWARM);
	static final int TOMB_DOOR_CLOSED_ID = ObjectID.NTK_TOMB_DOOR_NOANIM;
	private static final String MESSAGE_DEAD_END = "This door leads to a dead end.";
	private static final String MESSAGE_ALREADY_OPENED = "You've already opened this door and it leads to a dead end.";

	@Getter
	private final Map<TileObject, Door> allDoors = new HashMap<>();

	@Getter
	private Door activeDoor;

	@Getter
	private int currentFloor = -1;

	@Inject
	private Client client;

	@Inject
	private PyramidPlunderDoorsConfig config;

	@Override
	protected void startUp()
	{
		reset();
	}

	@Override
	protected void shutDown()
	{
		reset();
	}

	private void reset()
	{
		if (isInPyramidPlunder())
		{
			playerInteractions.clear();
			allDoors.clear();
			clearActiveDoor();
//			currentFloor = -1;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (!isInPyramidPlunder() || !(e.getActor() instanceof Player))
		{
			return;
		}

		final Player p = (Player) e.getActor();
		final Room room = Rooms.getRoom(p.getWorldLocation());

		// if the animation didn't happen in our current floor, ignore it
		if (room == null || currentFloor < 1 || room.getFloor() != currentFloor)
		{
			return;
		}

		final int animation = p.getAnimation();
		final int currentTick = client.getTickCount();
		final boolean isSearching = animation == SEARCH_TRAPDOOR_ANIMATION;

		if (!isSearching)
		{
			return;
		}

		final PlayerInteraction i = playerInteractions.getOrDefault(p.getName(), new PlayerInteraction(p.getName()));
		final WorldPoint location = p.getWorldLocation();
		i.setInteraction(InteractionState.OPENING_DOOR);
		i.setTick(currentTick);
		i.setLocation(location);

		// attempt to find the door that the player is interacting with
		final Door door = findClosestDoor(location);
		i.setClosestDoor(door);

		playerInteractions.put(p.getName(), i);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		if (e.getVarbitId() == VarbitID.NTK_ROOM_NUMBER)
		{
			currentFloor = client.getVarbitValue(VarbitID.NTK_ROOM_NUMBER);

			if (isInPyramidPlunder())
			{
				client.clearHintArrow();
			}
		}
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned e)
	{
//		log.debug("Player spawned: {}", e.getPlayer().getName());
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned e)
	{
		final String playerName = e.getPlayer().getName();
		final PlayerInteraction interaction = getOpenDoorInteraction(playerName);
		if (interaction == null)
		{
			return;
		}

		final int since = client.getTickCount() - interaction.getTick();
		if (since > TICK_THRESHOLD)
		{
			playerInteractions.remove(playerName);
			return;
		}

		final WorldPoint playerLocation = interaction.getLocation();
		final WorldPoint despawnLocation = e.getPlayer().getWorldLocation();
		final Room currentRoom = Rooms.getRoom(currentFloor); // get local player room

		// recent door open in the current room as the local player
		if (currentRoom != null && currentRoom.contains(playerLocation))
		{
			if (config.showChatMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Door opened by " + playerName, null);
			}

			// first try to find door near the recorded player location
			Door door = interaction.closestDoor;

			// if not found, try to find door near the despawn location
			if (door == null && !playerLocation.equals(despawnLocation))
			{
				log.debug("Player {} despawned at different location than interaction recorded ({} != {})", playerName, playerLocation, despawnLocation);
				door = findClosestDoor(despawnLocation);
			}

			// finally, set the hint arrow on the door
			if (door != null)
			{
				log.debug("Player {} opened a door ({} ticks since, dist: {})", playerName, since, playerLocation.distanceTo(door.getWorldLocation()));
				setActiveDoor(door);
			}
			else
			{
				log.debug("Player {} opened a door ({} ticks since) - door tile not found", playerName, since);
			}

			playerInteractions.remove(playerName);
		}
	}

	void setActiveDoor(Door door)
	{
		this.activeDoor = door;
		client.setHintArrow(door.getWorldLocation());
	}

	void clearActiveDoor()
	{
		this.activeDoor = null;
		client.clearHintArrow();
	}

	Door findClosestDoor(WorldPoint playerLocation)
	{
		Map<TileObject, Door> tiles = getAllDoors();

		if (playerLocation == null || tiles.isEmpty())
		{
			log.error("Cannot find closest door - invalid player location or no door tiles");
			return null;
		}

		return tiles.values().stream()
			.filter(door -> {
				try
				{
					return door != null && door.getWorldLocation() != null;
				}
				catch (NullPointerException e)
				{
					return false;
				}
			})
			.map(door -> new Object()
			{
				final Door d = door;
				final int distance = playerLocation.distanceTo(d.getWorldLocation());
			})
			.filter(entry -> entry.distance <= TILE_THRESHOLD)
			.min(Comparator.comparingInt(entry -> entry.distance))
			.map(entry -> entry.d)
			.orElse(null);
	}

	PlayerInteraction getOpenDoorInteraction(String playerName)
	{
		final PlayerInteraction interaction = playerInteractions.get(playerName);
		if (interaction == null || currentFloor < 1 || !interaction.getInteraction().equals(InteractionState.OPENING_DOOR))
		{
			return null;
		}

		return interaction;
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		WallObject object = event.getWallObject();

		if (TOMB_DOOR_WALL_IDS.contains(object.getId()))
		{
			allDoors.put(object, new Door(object, event.getTile()));
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (!isInPyramidPlunder() || e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if ((e.getMessage().equals(MESSAGE_DEAD_END) || e.getMessage().equals(MESSAGE_ALREADY_OPENED)) && activeDoor != null)
		{
			final PlayerInteraction interaction = getOpenDoorInteraction(client.getLocalPlayer().getName());
			if (interaction != null && interaction.closestDoor.equals(activeDoor))
			{
				log.debug("Dead end door detected at {}", activeDoor.getWorldLocation());
				clearActiveDoor();
			}

			// TODO: remove this door from roomDoors
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			reset();
		}
	}

	public boolean isInPyramidPlunder()
	{
		return client.getLocalPlayer() != null
			&& PYRAMID_PLUNDER_REGION == client.getLocalPlayer().getWorldLocation().getRegionID()
			&& client.getVarbitValue(VarbitID.NTK_PLAYER_TIMER_COUNT) > 0;
	}

	@Provides
	PyramidPlunderDoorsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PyramidPlunderDoorsConfig.class);
	}

	@Data
	private static class PlayerInteraction
	{
		private final String playerName;
		private InteractionState interaction;
		private int tick;
		private WorldPoint location;
		private Door closestDoor;
	}

	private enum InteractionState
	{
		IDLE,
		OPENING_DOOR,
	}
}
