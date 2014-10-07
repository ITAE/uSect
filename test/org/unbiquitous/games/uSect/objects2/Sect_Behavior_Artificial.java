package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.unbiquitous.games.uSect.TestUtils.executeThisManyTurns;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.driver.execution.executionUnity.ExecutionUnity;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.behavior.Artificial;
import org.unbiquitous.games.uSect.objects.behavior.Herbivore;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.util.math.Point;

public class Sect_Behavior_Artificial {

	private static final int INITIAL_ENERGY = 10000;
	private Environment e;

	@Before public void setUp(){
		GameSettings gameSettings = new GameSettings();
		gameSettings.put("usect.initial.energy", INITIAL_ENERGY);
		e = setUpEnvironment(gameSettings);
		Random.setvalue(0);
	}
	
	@Test public void allowsSectToMoveArround(){
		StringBuilder script = new StringBuilder()
			.append("function update()\n")
			.append("	move(1,0)\n")
			.append("end\n")
			;
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.CARNIVORE)),new Point(20,20));
		
		e.update();
		assertThat(s.position()).isEqualTo(new Point(21,20));
	}
	
	@Test public void allowsSectToAttack(){
		StringBuilder script = new StringBuilder()
			.append("function update()\n")
			.append("	attack()\n")
			.append("end\n")
			;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.CARNIVORE)),new Point(20,20));
		Sect h = e.addSect(new Sect(new Herbivore()),new Point(50,20));
		
		e.update();
		assertThat(h.energy()).isLessThan(s.energy());
	}
	
	@Test public void allowsToMate(){
		StringBuilder script = new StringBuilder()
			.append("function update()\n")
			.append("	mate()\n")
			.append("end\n")
			;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		e.addSect(new Sect(new Artificial(unity, Feeding.CARNIVORE)),new Point(20,20));
		e.addSect(new Sect(new Herbivore(){
			public void update() {
				sect.mate();
			}
		}),new Point(50,20));
		
		executeThisManyTurns(e, 50);
		assertThat(e.sects()).hasSize(3);
	}
	
	@Test public void artificialAreAlsoFedWithNutrients(){
		StringBuilder script = new StringBuilder()
			.append("function update()\n")
			.append("end\n")
			;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect h = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));
		e.addNutrient(new Point(20,20));
		
		executeThisManyTurns(e, 5);
		assertThat(h.energy()).isGreaterThan(INITIAL_ENERGY);
	}
	
	@Test public void artificialAreAlsoFedWithCorpses(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect h = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));
		e.addCorpse(new Point(20,20));
		
		executeThisManyTurns(e, 5);
		assertThat(h.energy()).isGreaterThan(INITIAL_ENERGY);
	}
	
	@Test public void artificialAreNotifiedWhenSomethigAppear(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		.append("function onEntered(data)\n")
		.append("	move(0,1)\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		e.addSect(new Sect(new Herbivore()),new Point(100,20));
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(20,21));
	}
	
	@Test public void somethingIsNotifiedAsAMap(){
		Sect h = new Sect(new Herbivore());
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		.append("function onEntered(data)\n")
		.append("	if "
					+ "data['id'] == '"+h.id()+"' and "
					+ "data['type'] == 'SECT' and "
					+ "data['feeding'] == 'HERBIVORE' "
					+ "then\n")
		.append("		move(0,1)\n")
		.append("	end\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		e.addSect(h,new Point(100,20));
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(20,21));
	}
	
	@Test public void allowsToQueryPositionOfSomething(){
		Sect h = new Sect(new Herbivore());
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("	data = positionOf('"+h.id()+"')\n")
		.append("	if "
					+ "data['x'] == 100 and "
					+ "data['y'] == 20 "
					+ "then\n")
		.append("		move(0,-1)\n")
		.append("	end\n")
		.append("end\n")
		;
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		e.addSect(h,new Point(100,20));
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(20,19));
	}
	
	@Test public void artificialAreNotifiedWhenSomethigLeaves(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		.append("function onLeft(data)\n")
		.append("	move(0,1)\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		e.addNutrient(new Point(20,20));
		executeThisManyTurns(e, 5);
		assertThat(s.position()).isEqualTo(new Point(20,21));
	}
	
	@Test public void energyIsAvailableAsAGlobal(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("	if energy >= "+INITIAL_ENERGY+" then \n")		
		.append("		move(1,0)\n")
		.append("	else\n")
		.append("		move(0,1)\n")
		.append("	end\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(21,20));
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(21,21));
	}
	
	@Test public void positionIsAvailableAsAGlobal(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("	if position['x'] == 20 and position['y'] == 20  then \n")		
		.append("		move(1,0)\n")
		.append("	else\n")
		.append("		move(0,-1)\n")
		.append("	end\n")
		.append("end\n")
		;
		
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.HERBIVORE)),new Point(20,20));		
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(21,20));
		executeThisManyTurns(e, 1);
		assertThat(s.position()).isEqualTo(new Point(21,19));
	}
	
	
	@Test public void behaviorIsClonable(){
		StringBuilder script = new StringBuilder()
			.append("function update()\n")
			.append("	move(1,0)\n")
			.append("end\n")
			;
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Artificial movingBehavior = new Artificial(unity, Feeding.CARNIVORE);
		Sect s1 = e.addSect(new Sect(movingBehavior),new Point(20,20));
		Sect s2 = e.addSect(new Sect(movingBehavior.clone()),new Point(120,20));
		
		e.update();
		assertThat(s1.position()).isEqualTo(new Point(21,20));
		assertThat(s2.position()).isEqualTo(new Point(121,20));
	}
}
