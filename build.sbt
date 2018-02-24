mainClass in (Compile,run) := Some("com.beartronics.jschema.JSchema")

name := "jschema"

version := "1.1"

//scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.4")

resolvers += "twitter shit" at "http://maven.twttr.com/"

//libraryDependencies += "org.scalatest"             % "scalatest_2.10"          % "1.9.1"      % "test"

libraryDependencies += "log4j"                     %  "log4j"                  % "1.2.16"

libraryDependencies +=  "junit"                    % "junit"                   % "4.11"       % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

libraryDependencies += "com.eed3si9n" %% "sbt-assembly" 




//javaOptions ++= Seq("-Djava.awt.headless=false")
