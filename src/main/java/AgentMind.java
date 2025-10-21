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
import codelets.habits.sensors.InnerSenseHabit;
import codelets.habits.sensors.VisionHabit;
import codelets.habits.perception.ClosestAppleDetectorHabit;
import codelets.habits.perception.AppleDetectorHabit;
import codelets.habits.behaviors.GoToClosestAppleHabit;
import codelets.habits.behaviors.EatClosestAppleHabit;
import codelets.habits.behaviors.ForageHabit;
import codelets.habits.motor.LegsActionHabit;
import codelets.habits.motor.HandsActionHabit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ws3dproxy.model.Thing;
/**
 *
 * @author rgudwin
 */
public class AgentMind extends Mind {

    public MemoryContainer mc;
    public MemoryObject moi;
    public MemoryObject moo;

    private static int creatureBasicSpeed=3;
    private static int reachDistance=50;
    public ArrayList<Codelet> behavioralCodelets = new ArrayList<Codelet>();
    
    public AgentMind(Environment env) {
        super();

        // Codelets Groups
        createCodeletGroup("Sensory");
        createCodeletGroup("Perception");
        createCodeletGroup("Behavioral");
        createCodeletGroup("Motor");

        // Memories Groups
        createMemoryGroup("Sensory");
        createMemoryGroup("Working");
        createMemoryGroup("Motor");

        // Declare Memory Containers
        Memory visionMC;
        Memory innerSenseMC;
        Memory closestAppleMC;
        Memory knownApplesMC;
        Memory legsMC;
        Memory handsMC;

        // Initialize Memory Containers
        visionMC=createMemoryContainer("vision");
        innerSenseMC=createMemoryContainer("innerSense");
        closestAppleMC=createMemoryContainer("closestApple");
        knownApplesMC=createMemoryContainer("knownApples");
        legsMC=createMemoryContainer("legsAction");
        handsMC=createMemoryContainer("handsAction");

        // Register Memory Containers
        registerMemory(visionMC,"Sensory");
        registerMemory(innerSenseMC,"Sensory");
        registerMemory(closestAppleMC,"Working");
        registerMemory(knownApplesMC,"Working");
        registerMemory(legsMC,"Motor");
        registerMemory(handsMC,"Motor");

        // Habits Memories Groups
        createMemoryGroup("Sensory Habits");
        createMemoryGroup("Working Habits");
        createMemoryGroup("Motor Habits");

        // Declare Habits Memory Containers
        MemoryContainer visionHabitMC;
        MemoryContainer innerSenseHabitMC;
        MemoryContainer appleDetectorHabitMC;
        MemoryContainer closestAppleDetectorHabitMC;
        MemoryContainer goToClosestAppleHabitMC;
        MemoryContainer eatClosestAppleHabitMC;
        MemoryContainer forageHabitMC;
        MemoryContainer legsActionHabitMC;
        MemoryContainer handsActionHabitMC;

        // Initialize Habits Memory Containers
        visionHabitMC = createMemoryContainer("visionHabits");
        innerSenseHabitMC = createMemoryContainer("innerSenseHabits");
        appleDetectorHabitMC = createMemoryContainer("appleDetectorHabits");
        closestAppleDetectorHabitMC = createMemoryContainer("closestAppleDetectorHabits");
        goToClosestAppleHabitMC = createMemoryContainer("goToClosestAppleHabits");
        eatClosestAppleHabitMC = createMemoryContainer("eatClosestAppleHabits");
        forageHabitMC = createMemoryContainer("forageHabits");
        legsActionHabitMC = createMemoryContainer("legsActionHabits");
        handsActionHabitMC = createMemoryContainer("handsActionHabits");

        // Register Habits Memory Containers
        registerMemory(visionHabitMC,"Sensory Habits");
        registerMemory(innerSenseHabitMC,"Sensory Habits");
        registerMemory(appleDetectorHabitMC,"Working Habits");
        registerMemory(closestAppleDetectorHabitMC,"Working Habits");
        registerMemory(goToClosestAppleHabitMC,"Working Habits");
        registerMemory(eatClosestAppleHabitMC,"Working Habits");
        registerMemory(forageHabitMC,"Motor Habits");
        registerMemory(legsActionHabitMC,"Motor Habits");
        registerMemory(handsActionHabitMC,"Motor Habits");

        // Create Sensor Habits
        Idea vh = new Idea("visionHabit");
        Habit visionHabit = new VisionHabit(env.c);
        vh.setValue(visionHabit);
        vh.setScope(2);
        visionHabitMC.setI(vh);
        HabitExecutionerCodelet visionHEC = new HabitExecutionerCodelet("vision");
        visionHEC.addInput(visionHabitMC);
        visionHEC.addOutput(visionMC); // This is the output memory object
        insertCodelet(visionHEC);
        registerCodelet(visionHEC,"Sensory");

        Idea ish = new Idea("innerSenseHabit");
        Idea cis = getInnerSense();
        Habit innerSenseHabit = new InnerSenseHabit(env.c, cis);
        ish.setValue(innerSenseHabit);
        ish.setScope(2);
        innerSenseHabitMC.setI(ish);
        HabitExecutionerCodelet innerSenseHEC = new HabitExecutionerCodelet("innerSense");
        innerSenseHEC.addInput(innerSenseHabitMC);
        innerSenseHEC.addOutput(innerSenseMC); // This is the output memory object
        insertCodelet(innerSenseHEC);
        registerCodelet(innerSenseHEC,"Sensory");

        // Create Perception Codelets
        Idea adh = new Idea("appleDetectorHabit");
        Habit appleDetectorHabit = new AppleDetectorHabit();
        adh.setValue(appleDetectorHabit);
        adh.setScope(2);
        appleDetectorHabitMC.setI(adh);
        HabitExecutionerCodelet appleDetectorHEC = new HabitExecutionerCodelet("appleDetector");
        appleDetectorHEC.addInput(appleDetectorHabitMC);
        appleDetectorHEC.addInput(knownApplesMC);
        appleDetectorHEC.addInput(visionMC);
        appleDetectorHEC.addOutput(knownApplesMC); // This is the output memory object
        insertCodelet(appleDetectorHEC);
        registerCodelet(appleDetectorHEC,"Perception");

        Idea cadh = new Idea("closestAppleDetectorHabit");
        Habit closestAppleDetectorHabit = new ClosestAppleDetectorHabit();
        cadh.setValue(closestAppleDetectorHabit);
        cadh.setScope(2);
        closestAppleDetectorHabitMC.setI(cadh);
        HabitExecutionerCodelet closestAppleDetectorHEC = new HabitExecutionerCodelet("closestAppleDetector");
        closestAppleDetectorHEC.addInput(closestAppleDetectorHabitMC);
        closestAppleDetectorHEC.addInput(innerSenseMC);
        closestAppleDetectorHEC.addInput(knownApplesMC);
        closestAppleDetectorHEC.addOutput(closestAppleMC); // This is the output memory object
        insertCodelet(closestAppleDetectorHEC);
        registerCodelet(closestAppleDetectorHEC,"Perception");

        // Create Behavior Codelets
        Idea gtcah = new Idea("goToClosestAppleHabit");
        Habit goToClosestAppleHabit = new GoToClosestAppleHabit(creatureBasicSpeed, reachDistance);
        gtcah.setValue(goToClosestAppleHabit);
        gtcah.setScope(2);
        goToClosestAppleHabitMC.setI(gtcah);
        HabitExecutionerCodelet goToClosestAppleHEC = new HabitExecutionerCodelet("goToClosestApple");
        goToClosestAppleHEC.addInput(goToClosestAppleHabitMC);
        goToClosestAppleHEC.addInput(closestAppleMC);
        goToClosestAppleHEC.addInput(innerSenseMC);
        goToClosestAppleHEC.addOutput(legsMC); // This is the output memory object
        insertCodelet(goToClosestAppleHEC);
        registerCodelet(goToClosestAppleHEC,"Behavioral");
        behavioralCodelets.add(goToClosestAppleHEC);

        Idea ecah = new Idea("eatClosestAppleHabit");
        Habit eatClosestAppleHabit = new EatClosestAppleHabit(reachDistance);
        ecah.setValue(eatClosestAppleHabit);
        ecah.setScope(2);
        eatClosestAppleHabitMC.setI(ecah);
        HabitExecutionerCodelet eatClosestAppleHEC = new HabitExecutionerCodelet("eatClosestApple");
        eatClosestAppleHEC.addInput(eatClosestAppleHabitMC);
        eatClosestAppleHEC.addInput(closestAppleMC);
        eatClosestAppleHEC.addInput(innerSenseMC);
        eatClosestAppleHEC.addInput(knownApplesMC);
        eatClosestAppleHEC.addOutput(handsMC); // This is the output memory object
        eatClosestAppleHEC.addOutput(knownApplesMC); // This is the output memory object
        insertCodelet(eatClosestAppleHEC);
        registerCodelet(eatClosestAppleHEC,"Behavioral");
        behavioralCodelets.add(eatClosestAppleHEC);

        Idea fh = new Idea("forageHabit");
        Habit forageHabit = new ForageHabit();
        fh.setValue(forageHabit);
        fh.setScope(2);
        forageHabitMC.setI(fh);
        HabitExecutionerCodelet forageHEC = new HabitExecutionerCodelet("forage");
        forageHEC.addInput(forageHabitMC);
        forageHEC.addInput(knownApplesMC);
        forageHEC.addOutput(legsMC); // This is the output memory object
        insertCodelet(forageHEC);
        registerCodelet(forageHEC,"Behavioral");
        behavioralCodelets.add(forageHEC);

        // Create Actuator Codelets
        Idea lah = new Idea("legsActionHabit");
        Habit legsActionHabit = new LegsActionHabit(env.c);
        lah.setValue(legsActionHabit);
        lah.setScope(2);
        legsActionHabitMC.setI(lah);
        HabitExecutionerCodelet legsActionHEC = new HabitExecutionerCodelet("legsAction");
        legsActionHEC.addInput(legsActionHabitMC);
        legsActionHEC.addInput(legsMC);
        insertCodelet(legsActionHEC);
        registerCodelet(legsActionHEC,"Motor");

        Idea hah = new Idea("handsActionHabit");
        Habit handsActionHabit = new HandsActionHabit(env.c);
        hah.setValue(handsActionHabit);
        hah.setScope(2);
        handsActionHabitMC.setI(hah);
        HabitExecutionerCodelet handsActionHEC = new HabitExecutionerCodelet("handsAction");
        handsActionHEC.addInput(handsActionHabitMC);
        handsActionHEC.addInput(handsMC);
        insertCodelet(handsActionHEC);
        registerCodelet(handsActionHEC,"Motor");

        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);
        
        start();
        
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