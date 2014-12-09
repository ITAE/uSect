package org.unbiquitous.games.uSect.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.games.uSect.objects.Player;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.util.math.Point;
import org.unbiquitous.uImpala.util.math.Rectangle;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class PlayerManager implements EnvironemtObjectManager {
	private static final Logger LOGGER = UOSLogging.getLogger();

	private Set<Player> players = new HashSet<Player>();
	private Map<UpDevice, Player> registeredDevices = new HashMap<UpDevice, Player>();
	private Environment env;
	private Gateway gateway;
	private boolean isPlayerDevice;

	public PlayerManager(Environment env) {
		this.env = env;
		gateway = GameSingletons.get(Gateway.class);
		GameSettings settings = GameSingletons.get(GameSettings.class);
		if(settings.containsKey("player.id")){
			isPlayerDevice = true;
		}
	}

	@Override
	public EnvironmentObject add(EnvironmentObject o) {
		if (o instanceof Player) {
			players.add((Player) o);
		}
		return o;
	}

	@Override
	public void remove(EnvironmentObject o) {
		players.remove(o);
	}

	@Override
	public void removeAll(Collection<EnvironmentObject> c) {
		players.removeAll(c);
	}
	
	@Override
	public void update() {
		updateDevicePlayers();
		env.frozen().clear();
		for (Player p : players) {
			update(p);
		}
	}

	private void updateDevicePlayers() {
		if (shouldCheckPlayers()) {
			List<UpDevice> devices = gateway.listDevices();
			checkNewPlayers(devices);
			checkPlayersThatLeft(devices);
		}
	}

	private boolean shouldCheckPlayers() {
		return !isPlayerDevice && env.turn() % 10 == 0 && gateway.listDevices() != null;
	}

	private void checkNewPlayers(List<UpDevice> devices) {
		for (UpDevice d : devices) {
			if (isNewDevice(d)) {
				Player p = createPlayer(d);
				registeredDevices.put(d, p);
			}
		}
	}

	private boolean isNewDevice(UpDevice d) {
		return !d.equals(gateway.getCurrentDevice()) && 
				!registeredDevices.containsKey(d);
	}

	private Player createPlayer(UpDevice d) {
		try {
			UUID id = callPlayerID(d);
			if(id != null){
				Player player = new Player(id);
				player.connect(d);
				return env.addPlayer(player);
			}
		} catch (ServiceCallException e) {
			LOGGER.log(Level.WARNING, "Not possible to handle call", e);
		}
		return null;
	}

	private UUID callPlayerID(UpDevice d) throws ServiceCallException {
		Call connect = new Call("usect.driver", "connect");

		Response r = gateway.callService(d, connect);
		if(r.getResponseData() != null && r.getResponseData().containsKey("player.id")){
			return UUID.fromString(r.getResponseString("player.id"));
		}
		return null;
	}

	private void checkPlayersThatLeft(List<UpDevice> devices) {
		Set<UpDevice> left = new HashSet<UpDevice>(registeredDevices.keySet());
		left.removeAll(devices);
		for (UpDevice d : left) {
			players.remove(registeredDevices.get(d));
			registeredDevices.remove(d);
		}
	}

	private void update(Player p) {
		p.update();
		checkInfuence(p);
	}

	private void checkInfuence(Player p) {
		for (Sect s : env.sects()) {
			checkInfluence(p, s);
		}
	}

	private void checkInfluence(Player p, Sect s) {
		Rectangle r = new Rectangle(p.position(), 40, 40);
		if (s.position().isInside(r)) {
			env.freeze(s);
			p.onCapture(s);
		} else if (isUnderInfluence(p, s)) {
			env.freeze(s);
			if (isReturning(p)) {
				moveTowardPlayer(p, s);
			}
		}
	}

	private boolean isUnderInfluence(Player p, Sect s) {
		int distance = p.position().distanceTo(s.position());
		return distance < p.influenceRadius();
	}

	private boolean isReturning(Player p) {
		return !p.growingInfluence();
	}

	private void moveTowardPlayer(Player p, Sect s) {
		env.unfreeze(s);
		env.moveTo(s, playerDirection(p, s));
		env.freeze(s);
	}

	private Point playerDirection(Player p, Sect s) {
		return new Point(p.position().x - s.position().x, p.position().y
				- s.position().y);
	}

	public List<Player> players() {
		return new ArrayList<Player>(players);
	}

}
