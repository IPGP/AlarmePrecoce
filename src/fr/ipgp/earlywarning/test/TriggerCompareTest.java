package fr.ipgp.earlywarning.test;

import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.messages.*;
import java.net.*;
import java.util.*;

public class TriggerCompareTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		System.out.println ("************************\n" +
							"* TESTS de comparaison *\n" +
							"************************");

        Trigger trigger1 = new Trigger(CommonUtilities.getUniqueId(),4);
        Trigger trigger2 = new Trigger(CommonUtilities.getUniqueId(),1);
        Trigger trigger3 = new Trigger(CommonUtilities.getUniqueId(),2);
        Trigger trigger4 = new Trigger(CommonUtilities.getUniqueId(),1);
        Trigger trigger5 = new Trigger(CommonUtilities.getUniqueId(),2);
        Trigger trigger6 = new Trigger(CommonUtilities.getUniqueId(),2);

        
        boolean[] equalTest = {trigger1.equals(trigger2),trigger2.equals(trigger3),trigger3.equals(trigger3)};
        int[] test = {trigger1.compareTo(trigger2), trigger2.compareTo(trigger3), trigger3.compareTo(trigger3)};
        String[] expected = {" < false "," > false "," = true "};
        for (int i=0; i<3; i++) {
        	if (test[i] > 0) {
        		System.out.println(test[i] + " < " + equalTest[i] + " - expected : " +expected[i]);
        	} else {
        		if (test[i] == 0) {
        			System.out.println(test[i] + " = " + equalTest[i] + " - expected : " +expected[i]);
        		} else {
        			System.out.println(test[i] + " > " + equalTest[i] + " - expected : " +expected[i]);
        		}
        	}
        }
        System.out.println("Egalité : " + trigger1.compareTo(trigger1));
        
        Vector<Trigger> triggers = new Vector<Trigger>();
        triggers.add(trigger6);
        triggers.add(trigger1);
        triggers.add(trigger2);
        triggers.add(trigger3);
        triggers.add(trigger3);
        triggers.add(trigger4);
        triggers.add(trigger5);

        System.out.println(triggers);
        Collections.sort(triggers);
        System.out.println(triggers);
        
	}

}
