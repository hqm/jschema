package com.beartronics.jschema;

import java.util.*;
import java.io.*;
import com.typesafe.config.*;
import org.apache.log4j.Logger;

/**
 * The "Stage" holds the schemas and related items, and a pointer to a
 * sensorimotor system which is hooked to a microworld simulator.
 *
 */
public class A
{
    int id = 7;
    public B b;

    public A() {
        
    }

    public String toString() {
        return "[A  b="+b+"]";
    }
}

