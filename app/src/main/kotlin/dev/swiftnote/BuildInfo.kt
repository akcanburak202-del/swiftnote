package dev.swiftnote

/** Pure formatting of the build identity shown on the start screen. */
object BuildInfo {
    fun describe(sha: String, builtUtc: String): String = "SwiftNote\nbuild $sha\n$builtUtc"
}
