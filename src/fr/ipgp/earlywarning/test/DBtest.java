package fr.ipgp.earlywarning.test;

import fr.ipgp.earlywarning.utilities.*;

public class DBtest {

	public void main(String[] args) {
		try {
			DataBaseHeartBeat dbhb = new DataBaseHeartBeat();
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Le support DB ne fonctionnera pas!!!");
		}
		
	}
	
}
