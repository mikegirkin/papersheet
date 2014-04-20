name := "papersheet"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "postgresql" %  "postgresql" % "9.1-901-1.jdbc4",
  "securesocial" %% "securesocial" % "2.1.2",
  "org.mockito" % "mockito-all" % "1.9.0",
  "org.hamcrest" % "hamcrest-core" % "1.3"
)
     
resolvers ++= Seq(
  Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
)

play.Project.playScalaSettings
