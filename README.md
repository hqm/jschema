jschema
=======

java implementation of Drescher's Schema Mechanism

http://youtu.be/in0GpCWmKxc movie of screen capture 

![alt text](screenshot_80.png "Screenshot")

This implements a 2 1/2 dimensinoal microworld with a small but functional visual system
and motor system.

The Processing application framework and graphics library is used to set up the image window
and display the microworld simulation.

It uses the jbox2d 2d physics library (a Java port of the Box2D physics library) to 
simulate phsyical objects. A visual system is built using a number of shortcuts to
provide higher level visual routines and processed input at the level of features
and simple motion detection.

The data can be inspected in-core using an HTTP connection to port 8080. 

I have been calling the main entry point from a JRuby interpreter, so I can use the jirb shell to
interact with the system for debugging and experiments. It's funny how far backward we've gone since the Lisp machines of 
30 years ago, in terms of interactive development environments with incremental in-core recompilation.

+ Building

I use `sbt` (the scala build tool) to compile. The build.sbt file specifies the main entry point class. 

the `package-bin` task produces a jar file

the `assembly` task produces a monlithic jar file with all dependencies, which can be executed
via `java -jar target/scala-2.10/jschema-assembly-1.1.jar`

+ Running

The jar file can be run from sbt or using java from the command line with 'java -jar'

For more interactive control, I launch the app from inside a JRuby jirb shell. 

directions:

    . setup-env
    jirb 
    > load '.irbrc' (if it doesn't load by itself automatically)

the .irbrc file contains some commands to poke the app into launching, and inspect it



