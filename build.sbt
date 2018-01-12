name := "Quiz League"

EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.withSource := true

val circeVersion = "0.8.0"
val appengineVersion = "1.9.59"
val angularVersion = "^4.0.0"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val commonSettings = Seq(
  organization := "quizleague",
  version := "0.0.1",
  scalaVersion := "2.12.3",
  scalacOptions ++= Seq("-deprecation","-unchecked","-feature","-Xlint"),
  resolvers += Resolver.sonatypeRepo("snapshots")
  
)

lazy val root = project.in(file(".")).
  aggregate(web, server).settings(commonSettings: _*).
  settings(
    publish := {},
    publishLocal := {},
    resolvers += Resolver.sonatypeRepo("releases")
  )

lazy val quizleague = crossProject.in(file(".")).
  settings(commonSettings: _*).
  settings( 
    name := "quizleague",
    libraryDependencies ++= Seq(
	  "io.circe" %%% "circe-core",
	  "io.circe" %%% "circe-generic",
	  "io.circe" %%% "circe-parser"
	).map(_ % circeVersion),
	
	libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.1" % "test").
  jvmSettings(
     name := "quizleague-jvm",
     
         
	libraryDependencies += "com.google.appengine" % "appengine-testing" % appengineVersion % "test",
	libraryDependencies += "com.google.appengine" % "appengine-api-stubs" % appengineVersion % "test",
	libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.10.0",
	libraryDependencies += "com.google.appengine.tools" % "appengine-gcs-client" % "0.7",
	libraryDependencies += "org.apache.directory.studio" % "org.apache.commons.io" % "2.4",
    libraryDependencies += "org.glassfish.jersey.containers" % "jersey-container-servlet-core" % "2.25.1",
    libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
	libraryDependencies += "io.netty" % "netty-tcnative-boringssl-static" % "2.0.7.Final",
    libraryDependencies += "com.google.cloud" % "google-cloud-firestore" % "0.28.0-beta"/*,
    libraryDependencies += "io.grpc" % "grpc-netty" % "1.7.0",
    libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.7.0",
    libraryDependencies += "io.grpc" % "grpc-stub" % "1.7.0"*/


  ).
  jsSettings(
    name := "quizleague-js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M12"

  )

lazy val server = quizleague.jvm.settings(
  //scalaJSProjects := Seq(web),
  //pipelineStages in Assets := Seq(scalaJSPipeline),
  //compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  //classesAsJar in Compile := true,
  //webappResources in Compile += file(s"${baseDirectory.value}/../js/target/scala-2.11/scalajs-bundler/main/dist")
).enablePlugins(AppenginePlugin)
lazy val web = quizleague.js .
enablePlugins(ScalaJSPlugin)

