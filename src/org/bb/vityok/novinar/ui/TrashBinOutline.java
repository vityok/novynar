package org.bb.vityok.novinar.ui;

import org.w3c.dom.Node;
import org.bb.vityok.novinar.core.OPMLManager;
import org.bb.vityok.novinar.core.Outline;

public class TrashBinOutline extends Outline {
	public TrashBinOutline(OPMLManager oman, Node n, Outline ol) {
	    super();
	}

	@Override
	public String toString() {
	   return "Trash";
	}

    }
