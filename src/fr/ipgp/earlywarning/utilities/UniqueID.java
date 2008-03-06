package fr.ipgp.earlywarning.utilities;

public class UniqueID {
	static long current= System.currentTimeMillis();
	static public synchronized long get(){
		return current++;
	}
}
