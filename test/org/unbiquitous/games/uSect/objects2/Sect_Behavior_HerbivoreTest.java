package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.unbiquitous.games.uSect.TestUtils.executeThisManyTurns;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Nutrient;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something;
import org.unbiquitous.uImpala.util.math.Point;

public class Sect_Behavior_HerbivoreTest {

	private static final int INITIAL_ENERGY = (int) (30*60*10);
	private Environment e;

	@Before public void setUp(){
		e = setUpEnvironment();
	}
	
	@Test public void standStillOnEmptyEnvironment(){
		Sect s = e.addSect(new Sect(),new Point(21,21));
		executeThisManyTurns(e, 100); 
		
		assertThat(s.position()).isEqualTo(new Point(21,21));
	}
	
	@Test public void goesAfterANutrientAfterEachStep(){
		Nutrient nutrient = e.addNutrient(new Point(20,20));
		
		Sect s = e.addSect(new Sect(),new Point(21,21));
		e.update();
		e.update(); 
		
		assertThat(s.position().x).isEqualTo(nutrient.position().x);
		assertThat(s.position().y).isEqualTo(nutrient.position().y);
	}
	
	@Test public void goesMovementIsAlwaysOnePixelAtAtimeRandomly(){
		e.addNutrient(new Point(20,20));
		
		Sect s = e.addSect(new Sect(),new Point(10, 10));
		
		Random.setvalue(0.51);
		e.update(); 
		
		assertThat(s.position()).isEqualTo(new Point(11,10));

		Random.setvalue(0.49);
		e.update(); 
		
		assertThat(s.position()).isEqualTo(new Point(11,11));
	}
	
	@Test public void ifTheresOnlyOneDirectionGoesThatWay(){
		e.addNutrient(new Point(40,40));
		
		Sect s1 = e.addSect(new Sect(),new Point(40, 10));
		Random.setvalue(0.51);
		e.update(); 
		assertThat(s1.position()).isEqualTo(new Point(40,11));

		Sect s2 = e.addSect(new Sect(),new Point(10, 40));
		Random.setvalue(0.49);
		e.update(); 
		assertThat(s2.position()).isEqualTo(new Point(11,40));
	}
	
	@Test public void stopsWalkingAfterEatingNutrient(){
		Nutrient nutrient = e.addNutrient(new Point(20,20));
		
		Sect s = e.addSect(new Sect(),new Point(30,15));
		executeThisManyTurns(e, 30);
		
		assertThat(s.position().x).isEqualTo(nutrient.position().x);
		assertThat(s.position().y).isEqualTo(nutrient.position().y);
	}
	
	@Test public void goesAfterTheNearestNutrient(){
		e.addNutrient(new Point(20,20));
		e.addNutrient(new Point(40,40));
		Nutrient n1 = e.addNutrient(new Point(5,5));
		Sect s =  e.addSect(new Sect(),new Point(10,10));
		
		executeThisManyTurns(e, 10);
		
		assertThat(s.position()).isEqualTo(n1.position());
	}

	@Test public void ifASectStaysMoreThan5turnsOnTopOfANutrientItEatsIt(){
		Nutrient n = e.addNutrient(new Point(10,10));
		final int[] count = new int[]{0};
		e.addSect(new Sect(){
			public void leftSight(Something n) {
				count[0]++;
			}
		},new Point(9,10));
		
		executeThisManyTurns(e, 5);
		assertThat(count[0]).isEqualTo(0);
		assertThat(e.nutrients()).contains(n);
		
		e.update();
		assertThat(count[0]).isEqualTo(1);
		assertThat(e.nutrients()).doesNotContain(n);
		
		e.update();
		assertThat(count[0]).isEqualTo(1);
	}
	
	@Test public void dontChaseDeadNutrients(){
		e.addNutrient(new Point(5,5));
		Sect s =  e.addSect(new Sect(),new Point(10,10));
		executeThisManyTurns(e, 14);
		Nutrient n2 = e.addNutrient(new Point(20,20));
		
		executeThisManyTurns(e, 30);
		
		assertThat(s.position()).isEqualTo(n2.position());
	}
	
	@Test public void afterAnutrientIsGoneMustChaseTheClosesInSight(){
		e.addNutrient(new Point(100,100));
		e.addNutrient(new Point(20,20));
		Nutrient n1 = e.addNutrient(new Point(40,40));
		e.addNutrient(new Point(5,5));
		Sect s =  e.addSect(new Sect(),new Point(10,10));
		
		executeThisManyTurns(e, 10+4+30+4+40);
		
		assertThat(s.position()).isEqualTo(n1.position());
	}
	
	@Test public void doesNotBotherByNutrientsEatenThatAreNotTheTarget(){
		Nutrient n1 = e.addNutrient(new Point(40,40));
		e.addNutrient(new Point(5,5));
		Sect s1 =  e.addSect(new Sect(),new Point(10,10));
		Sect s2 =  e.addSect(new Sect(),new Point(50,50));
		
		executeThisManyTurns(e, 100);
		
		assertThat(s1.position()).isNotEqualTo(n1.position());
		assertThat(s2.position()).isEqualTo(n1.position());
	}
	
	@Test public void standStillIfTheresNoOtherNutrientAtSight(){
		Nutrient n1 = e.addNutrient(new Point(40,40));
		Sect s1 =  e.addSect(new Sect(),new Point(10,10));
		
		executeThisManyTurns(e, 100);
		
		assertThat(s1.position()).isEqualTo(n1.position());
	}

	@Test public void sectDiesAfter10minutesStandingStill(){
		Sect s = e.addSect(new Sect(), new Point(10,10));
		e.update();
		assertThat(e.sects()).containsOnly(s);
		
		Random.setvalue(0);
		executeThisManyTurns(e, INITIAL_ENERGY-2);
		assertThat(e.sects()).containsOnly(s);
		
		e.update();
		assertThat(e.sects()).isEmpty();
	}
	
	@Test public void sectDiesTwiceAsFastWhenWalking(){
		Sect s = e.addSect(new Sect(), new Point(0,0));
		e.addNutrient(new Point(INITIAL_ENERGY,0));
		e.update();
		assertThat(e.sects()).containsOnly(s);
		
		executeThisManyTurns(e, INITIAL_ENERGY/2-2);
		assertThat(e.sects()).containsOnly(s);
		
		e.update();
		assertThat(e.sects()).isEmpty();
	}
	
	@Test public void sectGainsEnergyFromNutrients(){
		Sect s = e.addSect(new Sect(), new Point(10,10));
		e.addNutrient(new Point(10,10));
		
		int NUTRIENT_INCREMENT = 30*60;
		executeThisManyTurns(e, INITIAL_ENERGY+NUTRIENT_INCREMENT);
		assertThat(e.sects()).containsOnly(s);
		
		e.update();
		assertThat(e.sects()).isEmpty();
	}
	
	@Test public void deadSectsBecomesCorpses(){
		e.addSect(new Sect(), new Point(50,50));
		
		executeThisManyTurns(e, INITIAL_ENERGY);
		assertThat(e.sects()).isEmpty();
		assertThat(e.corpses()).hasSize(1);
		assertThat(e.corpses().get(0).position()).isEqualTo(new Point(50,50));
	}
}
