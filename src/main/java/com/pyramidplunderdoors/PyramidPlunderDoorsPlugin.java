package com.pyramidplunderdoors;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Pyramid Plunder Doors"
)
public class PyramidPlunderDoorsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "pyramidplunderdoors";

	@Inject
	private Client client;

	@Inject
	private PyramidPlunderDoorsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		//
	}

	@Override
	protected void shutDown() throws Exception
	{
		//
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	PyramidPlunderDoorsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PyramidPlunderDoorsConfig.class);
	}
}
