package habits.sensors;

import java.util.List;

import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import java.util.ArrayList;

public class VisionHabit implements Habit {
    private Creature c;

    public VisionHabit(Creature nc) {
        this.c = nc;
    }

    @Override 
    public Idea exec(Idea idea) {
        Idea root = new Idea("root", "");

        c.updateState();
             
        List<Thing> lt = c.getThingsInVision();
        root.add(new Idea("vision", lt));
        return root;
    }
}
