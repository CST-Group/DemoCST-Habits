package habits.motor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

import br.unicamp.cst.representation.idea.Habit;
import br.unicamp.cst.representation.idea.Idea;
import ws3dproxy.model.Creature;

public class LegsActionHabit implements Habit {
	private String previousLegsAction="";
    private Creature c;
    double old_angle = 0;
    int k=0;
    static Logger log = Logger.getLogger(LegsActionHabit.class.getCanonicalName());

    public LegsActionHabit(Creature nc) {
        this.c = nc;
    }

    @Override 
    public Idea exec(Idea idea) {
        // get legs action
        String comm = null;
        Idea comm_idea = idea.get("legsAction");
        if (comm_idea != null && comm_idea.getValue() instanceof String) {
            comm = (String) comm_idea.getValue();
        }
        if (comm == null) comm = "";
		
		if(!comm.equals("") ){
			try {
				JSONObject command=new JSONObject(comm);
                if (command.has("ACTION")) {
                    String action=command.getString("ACTION");
                    if (action.equals("FORAGE")) {
                        if (!comm.equals(previousLegsAction)) { 
                            log.fine("Sending Forage command to agent");
                            try {  
                                c.rotate(2);     
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } 
					}
                    else if (action.equals("GOTO")) {
                        if (!comm.equals(previousLegsAction)) {
                            double speed=command.getDouble("SPEED");
					        double targetx=command.getDouble("X");
					        double targety=command.getDouble("Y");
					        if (!comm.equals(previousLegsAction)) {
                                log.fine("Sending move command to agent: ["+targetx+","+targety+"]");
                                try {
                                    c.moveto(speed, targetx, targety);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                                        
				    } else {
					    log.fine("Sending stop command to agent");
                        try {
                            c.moveto(0,0,0);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }  
				    }
                }
			    previousLegsAction=comm;
                k++;	
			} catch (JSONException e) {e.printStackTrace();}
		}
        else {
			previousLegsAction = comm;
            log.fine("Sending stop command to agent"); 
        }

        return null;
    }
}