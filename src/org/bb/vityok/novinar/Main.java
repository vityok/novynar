package org.bb.vityok.novinar;

import org.bb.vityok.novinar.ui.NovinarApp;

/** Application launch entry point. */
public class Main
{
    public static void main(String... args)
	throws Exception
    {
	System.out.println("Launching novinar");

	NovinarApp.start(args);

        System.out.println("Novinar finished");
    }
}
