package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.unbiquitous.games.uSect.TestUtils.addSect;
import static org.unbiquitous.games.uSect.TestUtils.executeThisManyTurns;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.environment.EnvironmentObject;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.behavior.Carnivore;
import org.unbiquitous.games.uSect.objects.behavior.Herbivore;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.util.math.Point;

public class Sect_Behavior_ReproductionTest {
	private static final int ATTACK_ENERGY = 100;
	private static final int INITIAL_ENERGY = (int) (ATTACK_ENERGY * 20);

	private Environment e;

	@Before
	public void setUp() {
		GameSettings gameSettings = new GameSettings();
		gameSettings.put("usect.attack.energy", ATTACK_ENERGY);
		gameSettings.put("usect.mating.energy", ATTACK_ENERGY);
		gameSettings.put("usect.initial.energy", INITIAL_ENERGY);
		e = setUpEnvironment(gameSettings);
		e.disableNutrientsCreation();
	}

	@Test
	public void matingTakes50turns() {
		EnvironmentObject male = e.add(new Sect(new Carnivore()), new Stats(
				new Point(20, 20), 2 * INITIAL_ENERGY + 1));
		EnvironmentObject female = e.add(new Sect(new Carnivore()), new Stats(
				new Point(60, 20), 2 * INITIAL_ENERGY + 1));

		Random.setvalue(1);
		executeThisManyTurns(e, 5);

		assertThat(e.stats(male.id()).energy).isEqualTo(2 * INITIAL_ENERGY - 5);
		assertThat(e.stats(female.id()).energy).isEqualTo(
				2 * INITIAL_ENERGY - 5);

		executeThisManyTurns(e, 44);
		assertThat(e.stats(male.id()).energy)
				.isEqualTo(2 * INITIAL_ENERGY - 49);
		assertThat(e.stats(female.id()).energy).isEqualTo(
				2 * INITIAL_ENERGY - 49);

		assertThat(e.sects()).hasSize(2);
		executeThisManyTurns(e, 1);

		assertThat(e.sects()).hasSize(3);
	}

	@Test
	public void herbivoresAlsoMate() {
		e.add(new Sect(new Herbivore()), new Stats(new Point(20, 20),
				2 * INITIAL_ENERGY + 50));
		e.add(new Sect(new Herbivore()), new Stats(new Point(60, 20),
				2 * INITIAL_ENERGY + 50));

		Random.setvalue(1);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(3);
	}

	@Test
	public void matingConsumesTheEquivalentOfAnAttack() {
		EnvironmentObject male = e.add(new Sect(new Carnivore()), new Stats(
				new Point(20, 20), 2 * INITIAL_ENERGY + 50 + 1));
		EnvironmentObject female = e.add(new Sect(new Carnivore()), new Stats(
				new Point(60, 20), 2 * INITIAL_ENERGY + 50 + 1));

		Random.setvalue(1);
		executeThisManyTurns(e, 50);

		assertThat(e.stats(male.id()).energy).isEqualTo(
				2 * INITIAL_ENERGY - ATTACK_ENERGY);
		assertThat(e.stats(female.id()).energy).isEqualTo(
				2 * INITIAL_ENERGY - ATTACK_ENERGY);

		assertThat(e.sects()).hasSize(3);
	}

	@Test
	public void whileMatingCantWalk() {
		Sect male = addSect(e, new Carnivore(), new Point(20, 20));
		Sect female = addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(1);
		executeThisManyTurns(e, 15);

		assertThat(male.position()).isEqualTo(new Point(20, 20));
		assertThat(female.position()).isEqualTo(new Point(60, 20));
	}

	@Test
	public void matingSameSpeciesGivesBirthToSameSpecies_Herbivore() {
		Sect father = addSect(e, matingHerbivore(), new Point(20, 20));
		Sect mother = addSect(e, matingHerbivore(), new Point(60, 20));

		executeThisManyTurns(e, 50);

		e.sects().remove(father);
		e.sects().remove(mother);
		Sect son = e.sects().get(0);
		assertThat(son.behavior().feeding()).isEqualTo(Feeding.HERBIVORE);
	}

	@Test
	public void matingSameSpeciesGivesBirthToSameSpecies_Carnivore() {
		Sect father = addSect(e, matingCarnivore(), new Point(20, 20));
		Sect mother = addSect(e, matingCarnivore(), new Point(60, 20));

		executeThisManyTurns(e, 50);

		e.sects().remove(father);
		e.sects().remove(mother);
		Sect son = e.sects().get(0);
		assertThat(son.behavior().feeding()).isEqualTo(Feeding.CARNIVORE);
	}

	@Test
	public void matingSameSpeciesHasA50percentChance_positive() {
		addSect(e, new Carnivore(), new Point(20, 20));
		addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(0.51);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(3);
	}

	@Test
	public void matingSameSpeciesHasA50percentChance_negative() {
		addSect(e, new Carnivore(), new Point(20, 20));
		addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(0.49);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(2);
	}

	@Test
	public void matingDifferentSpeciesHasA50percentChance_positive() {
		addSect(e, new Carnivore(), new Point(20, 20));
		addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(0.76);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(3);
	}

	@Test
	public void matingDifferentSpeciesHasA50percentChance_negative() {
		addSect(e, new Herbivore(), new Point(20, 20));
		addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(0.74);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(2);
	}

	@Test
	public void matingMixFatherMotherAndFatherInformation() {
		addSect(e, new Carnivore(), new Point(20, 20));
		addSect(e, new Carnivore(), new Point(60, 20));

		Random.setvalue(0.51);
		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(3);
		Sect son = e.sects().get(2);
		assertThat(son.position()).isEqualTo(new Point(40, 20));
	}

	@Test
	public void multipleSimultaneousMatingsConsiderOnlyParentsInRangeOfMatingCall() {
		Sect s1 = addSect(e, new Carnivore(), new Point(20, 20));
		Sect s2 = addSect(e, new Carnivore(), new Point(60, 20));
		Sect s3 = addSect(e, new Carnivore(), new Point(20, 60));

		Random.setvalue(1);
		executeThisManyTurns(e, 50);

		List<Sect> sons = e.sects();
		sons.removeAll(Arrays.asList(s1, s2, s3));
		assertThat(sons).hasSize(2);
		assertThat(
				Arrays.asList(sons.get(0).position(), sons.get(1).position()))
				.containsOnly(new Point(40, 20), new Point(20, 40));
	}

	@Test
	public void multipleSimultaneousMatingsConsiderOnlyParentsInRangeOrEachOther() {
		Sect s1 = addSect(e, new Carnivore(), new Point(20, 20));
		Sect s2 = addSect(e, new Carnivore(), new Point(60, 20));
		Sect s3 = addSect(e, new Carnivore(), new Point(200, 220));
		Sect s4 = addSect(e, new Carnivore(), new Point(200, 270));

		Random.setvalue(1);
		executeThisManyTurns(e, 50);

		List<Sect> sons = e.sects();
		sons.removeAll(Arrays.asList(s1, s2, s3, s4));
		assertThat(sons).hasSize(2);
		assertThat(
				Arrays.asList(sons.get(0).position(), sons.get(1).position()))
				.containsOnly(new Point(40, 20), new Point(200, 245));
	}

	@Test
	public void matingProcessDontStartIfTheresNoMatch() {
		Sect male = addSect(e, new Carnivore(), new Point(20, 20));
		Sect female = addSect(e, new Carnivore() {
			public void update() {
				// Do nothing
			}
		}, new Point(70, 20));

		Random.setvalue(1);
		executeThisManyTurns(e, 50);

		List<Sect> sons = e.sects();
		sons.removeAll(Arrays.asList(female, male));
		assertThat(sons).isEmpty();
	}

	@Test
	public void carnivoresAfterMatingWaits10TimesTheMatingTimeToMateAgain() {
		addSect(e, new Carnivore(), new Point(20, 20), Long.MAX_VALUE / 2);
		addSect(e, new Carnivore(), new Point(60, 20), Long.MAX_VALUE / 2);

		Random.setvalue(1);
		executeThisManyTurns(e, 10 * 50);

		assertThat(e.sects()).hasSize(3);

		executeThisManyTurns(e, 50);

		assertThat(e.sects()).hasSize(4);
	}

	@Test
	public void herbivoresAfterMatingWaits20TimesTheMatingTimeToMateAgain() {
		e.addNutrient(new Point(400, 400));
		Sect male = addSect(e, new Herbivore(), new Point(20, 20),
				Long.MAX_VALUE / 2);
		Sect female = addSect(e, new Herbivore(), new Point(60, 20),
				Long.MAX_VALUE / 2);

		Random.setvalue(1);
		executeThisManyTurns(e, 20 * 50);

		assertThat(male.position()).isNotEqualTo(new Point(20, 20));
		assertThat(female.position()).isNotEqualTo(new Point(60, 20));
		assertThat(e.sects()).hasSize(3);
	}

	private Herbivore matingHerbivore() {
		return new Herbivore(){
			public void update() {
				e.mate(sect);
			}
		};
	}
	
	private Carnivore  matingCarnivore() {
		return new Carnivore(){
			public void update() {
				e.mate(sect);
			}
		};
	}
	
	// TODO: son must appear near father and mother and carry their
	// characteristics
	// TODO: CHanges must be queued as events

}
