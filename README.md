jschema
=======

java implementation of Drescher's Schema Mechanism

This implements a 2 1/2 dimensinoal microworld with a small but functional visual system
and motor system.

The Processing application framework and graphics library is used to set up the image window
and display the microworld simulation.

It uses the jbox2d 2d physics library (a Java port of the Box2D physics library) to 
simulate phsyical objects. A visual system is built using a number of shortcuts to
provide higher level visual routines and processed input at the level of features
and simple motion detection.

The data can be inspected in-core using an HTTP connection to port 8080. 

The JRuby interpreter is linked in, so that a top level jruby interactive shell can be used to
interact with the system for debugging and experiments. 


