package org.unbiquitous.games.usect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;

import org.unbiquitous.games.uSect.DeviceStats;
import org.unbiquitous.games.uSect.StartScene;
import org.unbiquitous.games.uSect.USectDriver;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.uImpala.dalvik.GameActivity;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.MouseManager;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uos.core.UOSLogging;

import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends GameActivity {
	@Override
	@SuppressWarnings({ "serial", "unchecked" })
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UOSLogging.setLevel(Level.ALL);
		Random.setSeed(seed());
		run(new GameSettings() {
			{ 
				put("main_activity", LaunchActivity.this);
				put("game_id", "uSect");
				put("first_scene", StartScene.class);
				put("input_managers", Arrays.asList(MouseManager.class));
				put("output_managers", Arrays.asList(ScreenManager.class));
				put("usect.devicestats", new DeviceStatsDalvik());
				put("usect.player.id", UUID.randomUUID().toString());
				//put("ubiquitos.eth.tcp.ignoreFilter", "192.168.56.*");
			}
		});
	}
	
	private static long seed() {
		try {
			long seed = 0;
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface e = networkInterfaces.nextElement();
				if (!e.isVirtual() && !e.isLoopback()) {
					if (e.getHardwareAddress() != null) {
						for (byte b : e.getHardwareAddress()) {
							seed += b;
						}
					}
				}
			}
			return seed;
		} catch (SocketException e) {
			return (long) (Math.random() * Long.MAX_VALUE);
		}
	}
}

class DeviceStatsDalvik extends DeviceStats {
	private Runtime runtime;
	private long cpu; 

	public static final String TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

	public DeviceStatsDalvik() {
		try {
			getCpu();

			runtime = Runtime.getRuntime();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void getCpu() throws FileNotFoundException, IOException {
		File freqFile = new File(TIME_IN_STATE_PATH); 
		if(!freqFile.exists()){
			Log.i("Warn","No CPU freq file found");
			return;
		}
		InputStream is = new FileInputStream(freqFile);
		InputStreamReader ir = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(ir);
		/*System.out.println("cpufreq");
		while(br.ready()){
			System.out.println(br.readLine());
		}*/
		readInStates(br);
		is.close();
	}
	
	private void readInStates(BufferedReader br) {
		try {
			String line;
			while ((line = br.readLine()) != null) {
				// split open line and convert to Integers
				String[] nums = line.split(" ");
				cpu = Integer.parseInt(nums[0]);
			}
		} catch (IOException e) {
			throw new RuntimeException("Problem processing time-in-states file");
		}
	}

	public long totalMemory() {
		return cpu;
	}

	public long cpuSpeed() {
		return runtime.maxMemory() / 1024 / 1024;
	}
}
