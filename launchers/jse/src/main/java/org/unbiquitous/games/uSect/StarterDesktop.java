package org.unbiquitous.games.uSect;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.KeyboardManager;
import org.unbiquitous.uImpala.engine.io.MouseManager;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uImpala.jse.impl.core.Game;
import org.unbiquitous.uos.core.UOSLogging;

public class StarterDesktop extends StartScene {

	static File scriptsFolder = new File("scripts/");
	
	public static void main(String[] args) {
		processArgs(args);
		GameSingletons.get(null);
		Random.setSeed(seed());
		
		GameSettings settings = createGameSettings();
		setProperMonitorSize(settings);
		loadLuaScripts(settings);
		
		Game.run(settings);
	}

	private static void processArgs(String[] _args) {
		List<String> args = Arrays.asList(_args);
		if (args.contains("--debug")) {
			UOSLogging.setLevel(Level.ALL);
		}
		if (args.contains("--sects")) {
			int folderIndex = args.indexOf("--sects") + 1;
			scriptsFolder = new File(args.get(folderIndex));
		}
	}

	@SuppressWarnings({ "serial", "unchecked" })
	private static GameSettings createGameSettings() {
		GameSettings settings = new GameSettings() {
			{
				put("first_scene", StartScene.class);
				put("game_id", "uSect");
				put("input_managers", Arrays.asList(MouseManager.class,
						KeyboardManager.class));
				put("output_managers", Arrays.asList(ScreenManager.class));
				put("usect.speed.value", 5);
				put("usect.devicestats", new DeviceStatsJSE());
				put("usect.player.id",UUID.randomUUID().toString());
			}
		};
		return settings;
	}

	private static void setProperMonitorSize(GameSettings settings) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		final int width = gd.getDisplayMode().getWidth();
		final int height = gd.getDisplayMode().getHeight() - 60;
		settings.put("usect.width", width);
		settings.put("usect.height", height);
	}

	private static void loadLuaScripts(GameSettings settings) {
		if(scriptsFolder.exists() && scriptsFolder.isDirectory()){
			List<String> scripts = new ArrayList<String>();
			for(File f : scriptsFolder.listFiles()){
				if(f.getName().endsWith(".lua")){
					try {
						scripts.add(readFileContent(f).toString());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			settings.put("usect.artificials", scripts);
		}
	}

	private static StringBuilder readFileContent(File f)
			throws FileNotFoundException, IOException {
		BufferedReader r = new BufferedReader(new FileReader(f));
		StringBuilder content = new StringBuilder();
		while(r.ready()){
			content.append(r.readLine());
			content.append('\n');
		}
		r.close();
		return content;
	}

	private static long seed() {
		try {
			return sumNetworkAddresses(0, NetworkInterface
					.getNetworkInterfaces());
		} catch (SocketException e) {
			return (long) (Math.random() * Long.MAX_VALUE);
		}
	}

	private static long sumNetworkAddresses(long seed,
			Enumeration<NetworkInterface> networkInterfaces)
			throws SocketException {
		while (networkInterfaces.hasMoreElements()) {
			seed = sumInterfaceAddress(seed, networkInterfaces.nextElement());
		}
		return seed;
	}

	private static long sumInterfaceAddress(long seed, NetworkInterface e)
			throws SocketException {
		if (isValidAddress(e)) {
			seed = addBytes(seed, e);
		}
		return seed;
	}

	private static boolean isValidAddress(NetworkInterface e)
			throws SocketException {
		return !e.isVirtual() && !e.isLoopback() && e.getHardwareAddress() != null;
	}

	private static long addBytes(long seed, NetworkInterface e)
			throws SocketException {
		for (byte b : e.getHardwareAddress()) {
			seed += b;
		}
		return seed;
	}

}

class DeviceStatsJSE extends DeviceStats {
	private CpuInfo cpuInfo;
	private Runtime runtime;

	public DeviceStatsJSE() {
		try {
			Sigar s = new Sigar();
			cpuInfo = s.getCpuInfoList()[0];
			runtime = Runtime.getRuntime();
		} catch (SigarException e) {
			throw new RuntimeException(e);
		}
	}

	public long totalMemory() {
		return cpuInfo.getMhz();
	}

	public long cpuSpeed() {
		return runtime.maxMemory() / 1024 / 1024;
	}
}