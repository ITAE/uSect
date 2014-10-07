package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.unbiquitous.games.uSect.TestUtils.executeThisManyTurns;
import static org.unbiquitous.games.uSect.TestUtils.movingSect;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.objects.Player;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.util.math.Point;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;

public class PlayerTest {
	
	private Environment e;
	private Gateway gateway;
	private GameSettings settings;

	@Before
	public void setUp() throws Exception{
		gateway = mock(Gateway.class);
//		when(gateway.callService(any(UpDevice.class), any(Call.class)))
//			.thenReturn(new Response());
		GameSingletons.put(Gateway.class, gateway);
		settings = new GameSettings();
		e = setUpEnvironment(settings);
	}

	@Test
	public void playerDoesNothingWhileNotAskedForIt() {
		 e.addPlayer(new Player(), new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 100));

		executeThisManyTurns(e, 60);
		assertThat(s.position()).isEqualTo(new Point(600, 100 + 60));
	}

	@Test
	public void playerAttackStopsSectWhenInsideRange() {
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 100));

		p.call();

		executeThisManyTurns(e, 60);
		// 20 turns to reach 100 + 4 turns to reach the other 20
		assertThat(s.position()).isEqualTo(new Point(600, 100 + 20 + 4));
	}
	
	@Test
	public void playerInfluenceIsOverAllSectsinRange() {
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		Sect s1 = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 100));
		Sect s2= e.addSect(movingSect(e,new Point(0, +1)), new Point(550, 50));
		Sect s3= e.addSect(movingSect(e,new Point(0, +1)), new Point(650, 50));

		p.call();

		executeThisManyTurns(e, 60);
		// 20 turns to reach 100 + 4 turns to reach the other 20
		assertThat(s1.position()).isEqualTo(new Point(600, 100 + 20 + 4));
		assertThat(s2.position()).isEqualTo(new Point(550, 50 + 20 + 4));
		assertThat(s3.position()).isEqualTo(new Point(650, 50 + 20 + 4));
	}

	@Test
	public void playerAttackbringsSectCloserToPlayerAsRangeShrinks() {
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 100));

		p.call();

		executeThisManyTurns(e, 2 * 60);
		assertThat(s.position()).isEqualTo(new Point(600, 98));
	}

	@Test
	public void ifASectIsNotCapturedItCanMoveAround() {
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 100));

		p.call();

		executeThisManyTurns(e, 2 * 60 + 10);
		assertThat(s.position()).isEqualTo(new Point(600, 108));
	}
	
	
	@Test
	public void ASectIsAttractedToThePlayerInRange() {
		Player p1 = e.addPlayer(new Player(), new Point(600, 0));
		Player p2 = e.addPlayer(new Player(), new Point(600, 1200));
		Sect s = e.addSect(movingSect(e,new Point(0, -1)), new Point(600, 1100));

		p1.call();
		p2.call();

		executeThisManyTurns(e, 60);
		assertThat(s.position()).isEqualTo(new Point(600, 1100-20 -4));
		
		executeThisManyTurns(e, 60);
		assertThat(s.position()).isEqualTo(new Point(600, 1100+2));
	}
	
	@Test
	public void sendCallToConnectedDevices() throws Exception{
		settings.put("usect.player.id", UUID.randomUUID());
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		UpDevice device = new UpDevice("avocado");
		p.connect(device);

		p.call();
		
		ArgumentCaptor<Call> captor = ArgumentCaptor.forClass(Call.class);
		verify(gateway).callService(eq(device), captor.capture());
		assertThat(captor.getValue().getDriver()).isEqualTo("usect.driver");
		assertThat(captor.getValue().getService()).isEqualTo("call");
		assertThat(captor.getValue().getParameterString("id")).isEqualTo(p.id().toString());
	}
	
	@Test
	public void nonPlayerDevicesDontSendCalls() throws Exception{
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		UpDevice device = new UpDevice("avocado");
		p.connect(device);
		
		p.call();
		
		ArgumentCaptor<Call> captor = ArgumentCaptor.forClass(Call.class);
		verify(gateway, never()).callService(eq(device), captor.capture());
	}
	
	@Test
	public void whenASectComesTooCloseToThePlayerItsCaptured() {
		final Sect[] captured = new Sect[]{null};
		Player p = e.addPlayer(new Player(){
			public void onCapture(Sect s){
				captured[0] = s;
			}
		}, new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 50));

		p.call();

		executeThisManyTurns(e, 2 * 60);
		assertThat(s.position()).isEqualTo(new Point(600, 20));
		assertThat(captured[0]).isEqualTo(s);
	}
	
	@Test
	public void migratesSectWhenItsCaptured() throws Exception {
		Player p = e.addPlayer(new Player(), new Point(600, 0));
		Sect s = e.addSect(movingSect(e,new Point(0, +1)), new Point(600, 19));
		
		UpDevice d = new UpDevice("Target");
		p.call();
		p.connect(d);
		
		executeThisManyTurns(e, 1);
		JSONObject sectJson = s.toJSON();
		executeThisManyTurns(e, 1);
		
		assertThat(e.sects()).isEmpty();
		ArgumentCaptor<Call> captor = ArgumentCaptor.forClass(Call.class);
		verify(gateway, times(1)).callService(eq(d), captor.capture());
		assertThat(captor.getAllValues()).hasSize(1);
		assertThat(captor.getValue().getParameter("sect")).isEqualTo(sectJson);
		assertThat(captor.getValue().getService()).isEqualTo("migrate");
		assertThat(captor.getValue().getDriver()).isEqualTo("usect.driver");
	}

}
