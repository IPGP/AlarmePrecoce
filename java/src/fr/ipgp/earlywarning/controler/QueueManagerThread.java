/**
 * Created Mon 11, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.triggers.*;
/**
 * Manage a trigger queue based on priorities. Launch the CallManager thread.
 * @author Patrice Boissier
 *
 */
public class QueueManagerThread {
	private Trigger[] triggers;
	private boolean reorder;
}
