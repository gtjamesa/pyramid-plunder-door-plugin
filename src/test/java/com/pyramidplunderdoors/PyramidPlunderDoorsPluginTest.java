package com.pyramidplunderdoors;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PyramidPlunderDoorsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PyramidPlunderDoorsPlugin.class);
		RuneLite.main(args);
	}
}