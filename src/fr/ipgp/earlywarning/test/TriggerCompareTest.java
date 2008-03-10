package fr.ipgp.earlywarning.test;

import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.UniqueID;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.messages.*;
import java.net.*;

public class TriggerCompareTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long id = 1635132135;
		Trigger trig = new Trigger(id,1);
		System.out.println(trig.toString());
		CallList callList = new TextCallList();
		WarningMessage message = new TextWarningMessage();
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName("localhost");
			trig.setInetAddress(inetAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		trig.setApplication("Sismo");
		trig.setCallList(callList);
		trig.setMessage(message);
		trig.setPriority(2);
		System.out.println(trig.toString());
		trig.setProperty("test", "Test Value");
		trig.setType("Type");
		

        Trigger trigger1 = new Trigger(UniqueID.get(),4);
        Trigger trigger2 = new Trigger(UniqueID.get(),1);
        Trigger trigger3 = new Trigger(UniqueID.get(),2);
        Trigger trigger4 = new Trigger(UniqueID.get(),2);
        boolean[] equalTest = {trigger1.equals(trigger2),trigger2.equals(trigger3),trigger3.equals(trigger3)};
        int[] test = {trigger1.compareTo(trigger2), trigger2.compareTo(trigger3), trigger3.compareTo(trigger3)};
        String[] triggers = {trigger1.toString(), trigger2.toString(), trigger3.toString(), trigger4.toString()}; 
        for (int i=0; i<3; i++) {
        	if (test[i] > 0) {
        		System.out.println(triggers[i]+" < "+triggers[i+1] + " " + equalTest[i]);
        	} else {
        		if (test[i] == 0) {
        			System.out.println(triggers[i]+" = "+triggers[i] + " " + equalTest[i]);
        		} else {
        			System.out.println(triggers[i]+" > "+triggers[i+1] + " " + equalTest[i]);
        		}
        	}
        }
        System.out.println("Egalité : " + trigger1.compareTo(trigger1));
	}

}
