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
		
		System.out.println ("********************************\n" +
							"* TESTS de creation de trigger *\n" +
							"********************************");
		
		long id = 1635132135;
		Trigger trig = new Trigger(id,1);
		System.out.println ("Creation : OK!");
		System.out.println ("********************************\n" +
							"* TESTS des getters et setters *\n" +
							"********************************");
		CallList callList = new TextCallList("0692703856");
		WarningMessage message = new TextWarningMessage("Alerte : tout brule!!");
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName("localhost");
			trig.setInetAddress(inetAddress);
			System.out.println("set Host : " + trig.getInetAddress().toString());
			if (inetAddress.equals(trig.getInetAddress()))
				System.out.println("Get : OK!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		trig.setApplication("Sismo");
		System.out.println("set Application : " + trig.getApplication());
		if ("Sismo".equals(trig.getApplication()))
			System.out.println("Get : OK!");
		trig.setCallList(callList);
		System.out.println("set CallList : " + trig.getCallList().toString());
		if (callList.equals(trig.getCallList()))
			System.out.println("Get : OK!");
		trig.setMessage(message);
		System.out.println("set Message : " + trig.getMessage().toString());
		if (message.equals(trig.getMessage()))
			System.out.println("Get : OK!");
		trig.setType("v1");
		System.out.println(trig.getType());
		if ("v1".equals(trig.getType()))
			System.out.println("Get : OK!");
		trig.setProperty("test", "Test Value");
		System.out.println(trig.getProperties().toString());
		if ("Test Value".equals(trig.getProperties().get("test")))
			System.out.println("Get OK!");
		System.out.println(trig.toString());
		trig.setApplication("EDF");
		if ("EDF".equals(trig.getApplication()))
			System.out.println("Get : OK!");
		System.out.println(trig.getApplication());
		trig.setType("v2");
		if ("v2".equals(trig.getType()))
			System.out.println("Get : OK!");
		System.out.println(trig.getType());
		trig.setProperty("test1", "Test Value");
		if ("Test Value".equals(trig.getProperties().get("test1")))
			System.out.println("Get OK!");
		System.out.println(trig.getProperties().toString());
		trig.setPriority(2);
		System.out.println(trig.toString());
		
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
        		System.out.println(" < " + equalTest[i] + " - expected : " +expected[i]);
        	} else {
        		if (test[i] == 0) {
        			System.out.println(" = " + equalTest[i] + " - expected : " +expected[i]);
        		} else {
        			System.out.println(" > " + equalTest[i] + " - expected : " +expected[i]);
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
