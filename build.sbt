// *****************************************************************************
// Projects
// *****************************************************************************
lazy val `scalajs-react-components-scala` =
  project.in(file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, AutomateHeaderPlugin, GitVersioning)
  .settings(settings)
  .settings(commonDependencies)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    webpackResources :=
      webpackResources.value +++
        PathFinder(
          Seq(
            baseDirectory.value / "src" / "main" / "html" / "js",
            baseDirectory.value / "src" / "main" / "html" / "images",
            baseDirectory.value / "src" / "main" / "html" / "css",
            baseDirectory.value / "src" / "main" / "html" / "index.html"
          )) ** "*.*"
  )

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "scalajs-react-components" %%% "scalajs-react-components" % "1.2.0-SNAPSHOT" withSources(),
    "scalajs-react-components" %%% "scalajs-react-components-macros" % "1.2.0-SNAPSHOT" withSources(),
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.0" withSources(),
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.0" withSources(),
    "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.1" withSources(),
    "org.scala-js" %%% "scalajs-dom" % "0.9.6" withSources(),
    "com.github.japgolly.scalacss" %%% "core" % "0.5.5" withSources(),
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.5" withSources(),
    "org.scalatest" %% "scalatest" % "3.0.5" % "test" withSources()
  )
)


lazy val settings =
  bundlerSettings ++
    commonSettings ++
    gitSettings

lazy val SuiVersion = "0.79.1"
lazy val EuiVersion   = "0.6.1"
lazy val MuiVersion   = "0.20.0"
lazy val reactVersion = "16.7.0"
lazy val webpackVersion = "4.28.3"

lazy val bundlerSettings =
  Seq(
    version in webpack := webpackVersion,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    fork in run := true,
    scalaJSStage in Global := FastOptStage,
    scalaJSUseMainModuleInitializer in Compile := true,
    scalaJSUseMainModuleInitializer in Test := false,
    skip in packageJSDependencies := false,
    artifactPath
      .in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value /
      ((moduleName in fastOptJS).value + "-opt.js")),
    artifactPath
      .in(Compile, fullOptJS) := ((crossTarget in(Compile, fullOptJS)).value /
      ((moduleName in fullOptJS).value + "-opt.js")),
    webpackConfigFile := Some(baseDirectory.value / "custom.webpack.config.js"),
    webpackEmitSourceMaps := true,
    //enableReloadWorkflow := false,
    useYarn := true,
    npmDependencies.in(Compile) := Seq(
      "elemental"                         -> EuiVersion,
      "highlight.js"                      -> "9.9.0",
      "material-ui"                       -> MuiVersion,
      "react"                             -> reactVersion,
      "react-dom"                         -> reactVersion,
      "react-addons-create-fragment"      -> "15.6.2",
      "react-addons-css-transition-group" -> "15.6.2",
      "react-addons-pure-render-mixin"    -> "15.6.2",
      "react-addons-transition-group"     -> "15.6.2",
      "react-addons-update"               -> "15.6.2",
      "react-geomicons"                   -> "2.1.0",
      "react-infinite"                    -> "0.12.1",
      "react-select"                      -> "1.2.1",
      "react-slick" -> "0.23.2",
      "react-dropzone" -> "4.2.9",
      "react-dnd" -> "7.0.2",
      "react-dnd-html5-backend" -> "7.0.2",
      "react-spinner" -> "0.2.7",
      "react-split-pane" -> "0.1.85",
      "react-tagsinput" -> "3.19.0",
      "react-tap-event-plugin" -> "3.0.3",
      "semantic-ui-react" -> SuiVersion,
      "svg-loader" -> "0.0.2"
    ),
    npmDevDependencies.in(Compile) := Seq(
      "style-loader" -> "0.23.1",
      "css-loader" -> "2.1.0",
      "sass-loader" -> "7.1.0",
      "compression-webpack-plugin" -> "2.0.0",
      "file-loader" -> "3.0.1",
      "gulp-decompress" -> "2.0.2",
      "image-webpack-loader" -> "4.6.0",
      "imagemin" -> "6.1.0",
      "less" -> "3.9.0",
      "less-loader" -> "4.1.0",
      "lodash" -> "4.17.11",
      "node-libs-browser" -> "2.1.0",
      "react-hot-loader" -> "4.6.3",
      "url-loader" -> "1.1.2",
      "expose-loader" -> "0.7.5",
      "webpack" -> webpackVersion
    )
  )

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    organization := "scalajs-react-components",
    organizationName := "scalajs-react-components",
    startYear := Some(2019),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-P:scalajs:sjsDefinedByDefault",
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value)
  )

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )


// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripiton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
// ThisBuild / publishMavenStyle := true

