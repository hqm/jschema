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
public class B
{
    int myval = 259;
    String mystring = "Two hundred fifty nine";

    public A a;

    public B(A a) {
        this.a = a;
    }

    public String toString() {
        return "[B  myval="+myval+" mystring="+mystring+" a.id="+a.id+"]";
    }

}

