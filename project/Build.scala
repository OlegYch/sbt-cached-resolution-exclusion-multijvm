import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.jvmOptions

object MyBuild extends Build {

  def logging(allDependencies: Seq[ModuleID]): Seq[ModuleID] = {
    Seq(
      "org.slf4j" % "slf4j-api" % "1.7.7"
      , "org.slf4j" % "jul-to-slf4j" % "1.7.7"
      , "ch.qos.logback" % "logback-classic" % "1.1.2" % Runtime exclude("org.slf4j", "slf4j-api")
      , "org.slf4j" % "jcl-over-slf4j" % "1.7.7" % Runtime
      , "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % Runtime
    ) ++
      allDependencies.map(
        _.exclude("commons-logging", "commons-logging")
          .exclude("log4j", "log4j")
          .exclude("org.slf4j", "slf4j-log4j12")
          .exclude("org.slf4j", "slf4j-jcl")
          .exclude("org.slf4j", "slf4j-jdk14")
          .excludeAll(ExclusionRule("org.slf4j", "jcl-over-slf4j", configurations = Compile.name :: Nil))
          .excludeAll(ExclusionRule("ch.qos.logback", "logback-classic", configurations = Compile.name :: Nil))
          .excludeAll(ExclusionRule("ch.qos.logback", "logback-core", configurations = Compile.name :: Nil))
      )
  }

  override def rootProject = Some(cache)
  lazy val cache = project.settings(SbtMultiJvm.multiJvmSettings: _*).settings(
    allDependencies ~= (_.map(_.
      exclude("org.kjkoster.zapcat", "zapcat").
      exclude("NettyExtension", "NettyExtension").
      exclude("org.apache.thrift", "libthrift")
    )),
    allDependencies ~= logging,
    updateOptions := updateOptions.value.withConsolidatedResolution(true).withLatestSnapshots(false),
    libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-multi-node-testkit" % "2.3.6" % "test", "org.scalatest" %% "scalatest" % "2.1.2" % "test"),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults) =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  ) configs (MultiJvm)

}

