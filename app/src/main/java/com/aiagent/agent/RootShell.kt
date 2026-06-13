package com.aiagent.agent

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Runs shell commands on the device, optionally as root via `su`. Stdout and
 * stderr are drained on separate threads so a chatty command can't deadlock by
 * filling a pipe buffer.
 */
object RootShell {

    data class Result(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val timedOut: Boolean = false,
    )

    /** True if a `su` binary is present and grants a root shell. */
    fun isRootAvailable(): Boolean = try {
        val r = run("id", asRoot = true, timeoutMs = 8000)
        r.exitCode == 0 && r.stdout.contains("uid=0")
    } catch (_: Exception) {
        false
    }

    fun run(command: String, asRoot: Boolean, timeoutMs: Long = 120_000): Result {
        val process = if (asRoot) {
            Runtime.getRuntime().exec(arrayOf("su"))
        } else {
            Runtime.getRuntime().exec(arrayOf("sh"))
        }

        val out = StringBuilder()
        val err = StringBuilder()

        val outThread = thread {
            BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
                lines.forEach { synchronized(out) { out.append(it).append('\n') } }
            }
        }
        val errThread = thread {
            BufferedReader(InputStreamReader(process.errorStream)).useLines { lines ->
                lines.forEach { synchronized(err) { err.append(it).append('\n') } }
            }
        }

        OutputStreamWriter(process.outputStream).use { writer ->
            writer.write(command)
            writer.write("\n")
            writer.write("exit\n")
            writer.flush()
        }

        val finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            outThread.join(2000)
            errThread.join(2000)
            return Result(-1, out.toString().trim(), err.toString().trim(), timedOut = true)
        }

        outThread.join(2000)
        errThread.join(2000)
        return Result(process.exitValue(), out.toString().trim(), err.toString().trim())
    }
}
