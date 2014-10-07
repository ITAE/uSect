package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.unbiquitous.games.uSect.TestUtils.addSect;
import static org.unbiquitous.games.uSect.TestUtils.executeThisManyTurns;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.behavior.Carnivore;
import org.unbiquitous.games.uSect.objects.behavior.Herbivore;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.util.math.Point;

public class Sect_Behavior_CarnivoreTest {
	private static final int BASE_ENERGY = 100;
	private static final int CORPSE_ENERGY = (int) (BASE_ENERGY * 5);
	private static final int INITIAL_ENERGY = (int) (BASE_ENERGY * 10);
	private Environment e;

	@Before public void setUp(){
		GameSettings gameSettings = new GameSettings();
		gameSettings.put("usect.attack.energy", BASE_ENERGY);
		gameSettings.put("usect.nutrient.energy", BASE_ENERGY);
		gameSettings.put("usect.corpse.energy", CORPSE_ENERGY);
		gameSettings.put("usect.initial.energy", INITIAL_ENERGY);
		e = setUpEnvironment(gameSettings);
		Random.setvalue(0);
	}
	
	@Test public void goesAfterAHerbivoreAfterEachStep(){
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(20,20));
		Sect c = e.addSect(new Sect(new Carnivore()),new Point(51,20));
		
		e.update();
		
		assertThat(h.position()).isEqualTo(new Point(20,20));
		assertThat(c.position()).isEqualTo(new Point(50,20));
	}
	
	@Test public void goesAfterAHerbivoreAfterEachStepEvenWhenHesMoving(){
		e.addNutrient(new Point(100,20));
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(10,20));
		Sect c = e.addSect(new Sect(new Carnivore()),new Point(20,100));
		
		executeThisManyTurns(e, 90);
		
		assertThat(h.position()).isEqualTo(new Point(100,20));
		assertThat(c.position()).isNotEqualTo(new Point(10,20));
		assertThat(c.position().x).isGreaterThan(20);
	}
	
	@Test public void preferCorpsesThanLiveSects(){
		e.addCorpse(new Point(100,10));
		Random.setvalue(-1);
		e.addSect(new Sect(new Herbivore()),new Point(0,10));
		Sect c = e.addSect(new Sect(new Carnivore()),new Point(50,10));
		
		executeThisManyTurns(e, 1);
		
		assertThat(c.position()).isEqualTo(new Point(51,10));
	}
	
	@Test public void sectGainsEnergyFromCorpse(){
		Sect s = e.addSect(new Sect(), new Point(10,10));
		e.addCorpse(new Point(10,10));
		
		executeThisManyTurns(e, INITIAL_ENERGY+CORPSE_ENERGY);
		assertThat(e.sects()).containsOnly(s);
		
		e.update();
		assertThat(e.sects()).isEmpty();
	}
	
	@Test public void launchesAnAttackWhenIsNearItsTarget(){
		Random.setvalue(0);
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(10,20));
				 e.addSect(new Sect(new Carnivore()),new Point(10,121));
		
		executeThisManyTurns(e, 50);
		
		assertThat(e.stats(h.id()).energy).isEqualTo((long)INITIAL_ENERGY-50);
		
		executeThisManyTurns(e, 1);
		
		assertThat(e.stats(h.id()).energy).isEqualTo((long)INITIAL_ENERGY-50-1-BASE_ENERGY);
	}
	
	@Test public void anAttackHasACoolDownOf5turns(){
		Random.setvalue(0);
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(10,20));
				 e.addSect(new Sect(new Carnivore()),new Point(10,121));
		
		executeThisManyTurns(e, 50+5);
		
		assertThat(e.stats(h.id()).energy).isEqualTo((long)INITIAL_ENERGY-50-5-BASE_ENERGY);
	}
	
	@Test public void afterCoolDowncanattackAgain(){
		Random.setvalue(0);
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(10,20));
				 e.addSect(new Sect(new Carnivore()),new Point(10,120));
		
		executeThisManyTurns(e, 50+5+1);
		assertThat(e.stats(h.id()).energy).isEqualTo((long)INITIAL_ENERGY-50-5-2*BASE_ENERGY-1);
	}
	
	@Test
	public void onlyattacksOrMovesWhenIsHungry() {
		e.addNutrient(new Point(400,400));
		Sect s1 = addSect(e,new Carnivore(), new Point(20, 20),5l*INITIAL_ENERGY+10);
		Sect s2 = addSect(e,new Carnivore(), new Point(60, 20),5l*INITIAL_ENERGY+10);
		
		Random.setvalue(0);
		executeThisManyTurns(e, 10);
		
		assertThat(s1.position()).isEqualTo(new Point(20,20));
		assertThat(s1.energy()).isEqualTo(5l*INITIAL_ENERGY);
		assertThat(s2.position()).isEqualTo(new Point(60,20));
		assertThat(s2.energy()).isEqualTo(5l*INITIAL_ENERGY);
		
		executeThisManyTurns(e, 2);
		assertThat(s1.position()).isNotEqualTo(new Point(20,20));
		assertThat(s2.position()).isNotEqualTo(new Point(60,20));
	}

	
	//TODO: Check malicious behaviors (multiple attacks, moves, etc) during turns
}
