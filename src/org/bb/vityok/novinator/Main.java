package org.bb.vityok.novinator;

import org.bb.vityok.novinator.db.Backend;

public class Main
{
    public static void main(String... args)
	throws Exception
    {
	System.out.println("Hello world");

        new Backend().go(args);

        System.out.println("SimpleApp finished");
    }
}
