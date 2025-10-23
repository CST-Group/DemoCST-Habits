/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.representation.idea.HabitExecutionerCodelet;
import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;
import habits.behaviors.EatClosestAppleHabit;
import habits.behaviors.ForageHabit;
import habits.behaviors.GoToClosestAppleHabit;
import habits.motor.HandsActionHabit;
import habits.motor.LegsActionHabit;
import habits.perception.AppleDetectorHabit;
import habits.perception.ClosestAppleDetectorHabit;
import habits.sensors.InnerSenseHabit;
import habits.sensors.VisionHabit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ws3dproxy.model.Thing;
/**
 *
 * @author rgudwin
 */
public class AgentMind extends Mind {
    Environment env = null;

    private static int creatureBasicSpeed=3;
    private static int reachDistance=50;
    public ArrayList<Codelet> behavioralCodelets = new ArrayList<Codelet>();

    // Memories
    Memory visionMC;
    Memory innerSenseMC;
    Memory closestAppleMC;
    Memory knownApplesMC;
    Memory legsActionMC;
    Memory handsActionMC;

    // Habits Memories Containers
    MemoryContainer sensoryMC;
    MemoryContainer perceptualMC;
    MemoryContainer behavioralMC;
    MemoryContainer motorMC;
    
    public AgentMind(Environment env) {
        super();
        this.env = env;

        createMemories();
        createHabitsMemories();

        createSensorHabits();
        createPerceptualHabits();
        createBehavioralHabits();
        createMotorHabits();

        // Sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(20);
        
        start();
    }

    private void createMemories() {
        createMemoryGroup("Sensory");
        createMemoryGroup("Perceptual");
        createMemoryGroup("Motor");
        
        visionMC=createMemoryContainer("vision");
        innerSenseMC=createMemoryContainer("innerSense");
        closestAppleMC=createMemoryContainer("closestApple");
        knownApplesMC=createMemoryContainer("knownApples");
        legsActionMC=createMemoryContainer("legsAction");
        handsActionMC=createMemoryContainer("handsAction");
        
        registerMemory(visionMC,"Sensory");
        registerMemory(innerSenseMC,"Sensory");
        registerMemory(closestAppleMC,"Perceptual");
        registerMemory(knownApplesMC,"Perceptual");
        registerMemory(legsActionMC,"Motor");
        registerMemory(handsActionMC,"Motor");
    }
    
    private void createHabitsMemories() {
        createMemoryGroup("Habits");

        sensoryMC = createMemoryContainer("sensoryHabits");
        perceptualMC = createMemoryContainer("perceptualHabits");
        behavioralMC = createMemoryContainer("behavioralHabits");
        motorMC = createMemoryContainer("motorHabits");

        registerMemory(sensoryMC, "Habits");
        registerMemory(perceptualMC, "Habits");
        registerMemory(behavioralMC, "Habits");
        registerMemory(motorMC, "Habits");
    }

    private void createSensorHabits() {
        // Habit 1
        Idea vh = new Idea("visionHabit");
        Habit visionHabit = new VisionHabit(env.c);
        vh.setValue(visionHabit);
        vh.setScope(2);
        
        // Habit 2
        Idea ish = new Idea("innerSenseHabit");
        Habit innerSenseHabit = new InnerSenseHabit(env.c, getInnerSense());
        ish.setValue(innerSenseHabit);
        ish.setScope(2);

        sensoryMC.setI(vh);
        sensoryMC.setI(ish);
        sensoryMC.setPolicy(MemoryContainer.Policy.RANDOM_FLAT);

        HabitExecutionerCodelet sensoryHEC = new HabitExecutionerCodelet("sensory");
        sensoryHEC.addInput(sensoryMC);
        sensoryHEC.addOutput(visionMC);
        sensoryHEC.addOutput(innerSenseMC);
        insertCodelet(sensoryHEC);
    }

    private void createPerceptualHabits() {
        // Habit 1
        Idea adh = new Idea("appleDetectorHabit");
        Habit appleDetectorHabit = new AppleDetectorHabit();
        adh.setValue(appleDetectorHabit);
        adh.setScope(2);
        
        // Habit 2
        Idea cadh = new Idea("closestAppleDetectorHabit");
        Habit closestAppleDetectorHabit = new ClosestAppleDetectorHabit();
        cadh.setValue(closestAppleDetectorHabit);
        cadh.setScope(2);

        perceptualMC.setI(adh);
        perceptualMC.setI(cadh);
        perceptualMC.setPolicy(MemoryContainer.Policy.RANDOM_FLAT);

        HabitExecutionerCodelet percpetualHEC = new HabitExecutionerCodelet("perceptual");
        percpetualHEC.addInput(perceptualMC);
        percpetualHEC.addInput(innerSenseMC);
        percpetualHEC.addInput(visionMC);
        percpetualHEC.addInput(knownApplesMC);
        percpetualHEC.addOutput(knownApplesMC);
        percpetualHEC.addOutput(closestAppleMC);
        insertCodelet(percpetualHEC);
    }

    private void createBehavioralHabits() {
        // Habit 1
        Idea gtcah = new Idea("goToClosestAppleHabit");
        Habit goToClosestAppleHabit = new GoToClosestAppleHabit(creatureBasicSpeed, reachDistance);
        gtcah.setValue(goToClosestAppleHabit);
        gtcah.setScope(2);

        // Habit 2
        Idea ecah = new Idea("eatClosestAppleHabit");
        Habit eatClosestAppleHabit = new EatClosestAppleHabit(reachDistance);
        ecah.setValue(eatClosestAppleHabit);
        ecah.setScope(2);

        // Habit 3
        Idea fh = new Idea("ForageHabit");
        Habit forageHabit = new ForageHabit();
        fh.setValue(forageHabit);
        fh.setScope(2);

        behavioralMC.setI(gtcah);
        behavioralMC.setI(ecah);
        behavioralMC.setI(fh);
        behavioralMC.setPolicy(MemoryContainer.Policy.RANDOM_FLAT);

        HabitExecutionerCodelet behavioralHEC = new HabitExecutionerCodelet("behavioral");
        behavioralHEC.addInput(behavioralMC);
        behavioralHEC.addInput(innerSenseMC);
        behavioralHEC.addInput(closestAppleMC);
        behavioralHEC.addInput(knownApplesMC);
        behavioralHEC.addOutput(knownApplesMC);
        behavioralHEC.addOutput(handsActionMC);
        behavioralHEC.addOutput(legsActionMC);
        insertCodelet(behavioralHEC);
        behavioralCodelets.add(behavioralHEC);
    }

    private void createMotorHabits() {
        // Habit 1
        Idea lah = new Idea("legsActionHabit");
        Habit legsActionHabit = new LegsActionHabit(env.c);
        lah.setValue(legsActionHabit);
        lah.setScope(2);
        
        // Habit 2
        Idea hah = new Idea("handsActionHabit");
        Habit handsActionHabit = new HandsActionHabit(env.c);
        hah.setValue(handsActionHabit);
        hah.setScope(2);

        motorMC.setI(lah);
        motorMC.setI(hah);
        motorMC.setPolicy(MemoryContainer.Policy.RANDOM_FLAT);

        HabitExecutionerCodelet motorHEC = new HabitExecutionerCodelet("motor");
        motorHEC.addInput(motorMC);
        motorHEC.addInput(legsActionMC);
        motorHEC.addInput(handsActionMC);
        insertCodelet(motorHEC);
    }

    private Idea getInnerSense() {
        Idea cis = Idea.createIdea("innerSense","", Idea.guessType("AbstractObject",null,1.0,0.5));
        cis.add(Idea.createIdea("innerSense.pitch", 0D, Idea.guessType("Property", null,1.0,0.5)));
        cis.add(Idea.createIdea("innerSense.fuel", 0D, Idea.guessType("Property", null,1.0,0.5)));
        Idea position = Idea.createIdea("innerSense.position","", Idea.guessType("Property",null,1.0,0.5));
        position.add(Idea.createIdea("innerSense.position.x",0D,Idea.guessType("QualityDimension",null,1.0,0.5)));
        position.add(Idea.createIdea("innerSense.position.y",0D,Idea.guessType("QualityDimension",null,1.0,0.5)));
        cis.add(position);
        Idea fov = Idea.createIdea("innerSense.FOV","", Idea.guessType("Property", null,1.0,0.5));
        Idea bounds = Idea.createIdea("innerSense.FOV.bounds","", Idea.guessType("Property", null,1.0,0.5));
        bounds.add(Idea.createIdea("innerSense.FOV.bounds.x",null, Idea.guessType("Property", null,1.0,0.5)));
        bounds.add(Idea.createIdea("innerSense.FOV.bounds.y",null, Idea.guessType("Property", null,1.0,0.5)));
        bounds.add(Idea.createIdea("innerSense.FOV.bounds.height",null, Idea.guessType("Property", null,1.0,0.5)));
        bounds.add(Idea.createIdea("innerSense.FOV.bounds.width",null, Idea.guessType("Property", null,1.0,0.5)));
        fov.add(bounds);
        fov.add(Idea.createIdea("innerSense.FOV.npoints",0, Idea.guessType("Property", null,1.0,0.5)));
        fov.add(Idea.createIdea("innerSense.FOV.points","", Idea.guessType("Property", null,1.0,0.5)));
        cis.add(fov);

        return cis;
    }
}