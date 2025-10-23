package com.pyramidplunderdoors.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomChatMessage
{
	PLAYER_OPENED_DOOR("Opened door"),
	DOORS_RESET("Doors reset");

	private final String info;

	@Override
	public String toString()
	{
		return info;
	}
}
