package fr.ipgp.earlywarning.test;

import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.UniqueID;

public class TriggerCompareTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        Trigger trigger1 = new Trigger(UniqueID.get(),4);
        Trigger trigger2 = new Trigger(UniqueID.get(),1);
        Trigger trigger3 = new Trigger(UniqueID.get(),2);
        Trigger trigger4 = new Trigger(UniqueID.get(),2);
        int[] test = {trigger1.compareTo(trigger2), trigger2.compareTo(trigger3), trigger3.compareTo(trigger3)};
        String[] triggers = {trigger1.toString(), trigger2.toString(), trigger3.toString(), trigger4.toString()}; 
        for (int i=0; i<3; i++) {
        	if (test[i] > 0) {
        		System.out.println(triggers[i]+" < "+triggers[i+1]);
        	} else {
        		if (test[i] == 0) {
        			System.out.println(triggers[i]+" = "+triggers[i]);
        		} else {
        			System.out.println(triggers[i]+" > "+triggers[i+1]);
        		}
        	}
        }
        System.out.println("Egalité : " + trigger1.compareTo(trigger1));
	}

}
