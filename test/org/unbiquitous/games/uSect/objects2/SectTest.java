package org.unbiquitous.games.uSect.objects2;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.unbiquitous.games.uSect.TestUtils.setUpEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.driver.execution.executionUnity.ExecutionUnity;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.behavior.Artificial;
import org.unbiquitous.games.uSect.objects.behavior.Carnivore;
import org.unbiquitous.uImpala.util.math.Point;

public class SectTest {

	private Environment e;
	private Environment e2;

	@Before public void setUp(){
		e = setUpEnvironment();
		e2 = setUpEnvironment();
	}
	
	@Test public void sectMustBeSerializable(){
		Stats stats = new Stats(new Point(10,10), 123456l);
		Sect s = (Sect) e.add(new Sect(new Carnivore()), stats);
		
		Sect deserialized = Sect.fromJSON(e2,s.toJSON());
		
		assertThat(deserialized).isEqualTo(s);
		assertThat(deserialized.behavior()).isInstanceOf(s.behavior().getClass());
		assertThat(deserialized.energy()).isEqualTo(s.energy());
	}
	
	@Test public void sectCanbePositionedWhereverWeWant(){
		Sect s = e.addSect(new Sect(new Carnivore()), new Point());
		
		Sect deserialized = Sect.fromJSON(e2,s.toJSON(),new Point(50,50));
		
		assertThat(deserialized.position()).isEqualTo(new Point(50,50));
	}
	
	@Test public void artificialBehaviorIsAlsoSerialized(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("	move(1,0)\n")
		.append("end\n")
		;
		ExecutionUnity unity = new ExecutionUnity(script.toString());
		Sect s = e.addSect(new Sect(new Artificial(unity, Feeding.CARNIVORE)),new Point(20,20));
		
		Sect deserialized = Sect.fromJSON(e2,s.toJSON(),new Point(50,50));
		assertThat(deserialized.behavior()).isInstanceOf(s.behavior().getClass());
		assertThat(deserialized.behavior().feeding()).isEqualTo(s.behavior().feeding());
		e2.update();
		assertThat(deserialized.position()).isEqualTo(new Point(51,50));
	}
}
