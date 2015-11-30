name := "parComposite"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.5"
libraryDependencies += "org.scalaz" %% "scalaz-effect" % "7.1.5"
libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.1.5"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.6" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.14"
