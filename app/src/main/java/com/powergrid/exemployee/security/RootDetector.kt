package com.powergrid.exemployee.security

import android.os.Build
import java.io.File
import java.net.InetAddress
import java.net.Socket

/**
 * State-of-the-Art, Momo-grade security scanner implementing advanced multi-layered
 * heuristics to discover system modifications, root permissions, dynamic hooks,
 * and virtualization boundaries without interrupting dev bypass features.
 */
object RootDetector {

    /**
     * Executes all advanced, multi-layered environment audits to identify if the device
     * has been rooted, hooked, or virtualized, using both traditional and state-of-the-art checks.
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildMetadata() ||
                checkSuPaths() ||
                checkSuCommand() ||
                checkPathEnvironment() ||
                checkXposedNativeMethodHooks() ||
                checkHookFrameworks() ||
                checkStackTrace() ||
                checkMemoryMaps() ||
                checkProcMounts() ||
                checkPartitionWritability() ||
                checkFridaLocalPort() ||
                checkThreadNamesForFrida() ||
                checkSelinuxEnforcing() ||
                checkUnixDomainSockets() ||
                checkBusyboxAppletSymlinks() ||
                checkSystemProperties()
    }

    /**
     * Audits OS Build metadata for signatures of custom ROMs, debug builds, or test distributions.
     */
    private fun checkBuildMetadata(): Boolean {
        val tags = Build.TAGS
        val fingerprint = Build.FINGERPRINT
        val product = Build.PRODUCT
        val device = Build.DEVICE
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER

        val isTestKeys = tags != null && tags.contains("test-keys")
        val isTestFingerprint = fingerprint != null && fingerprint.contains("test-keys")

        // Emulator signs
        val isEmulator = (product != null && (product.contains("generic") || product.contains("sdk") || product.contains("google_sdk") || product.contains("emulator") || product.contains("vbox86"))) ||
                (device != null && (device.contains("generic") || device.contains("sdk") || device.contains("emulator") || device.contains("vbox86"))) ||
                (model != null && (model.contains("google_sdk") || model.contains("Emulator") || model.contains("Android SDK built for x86"))) ||
                (manufacturer != null && (manufacturer.contains("Genymotion") || manufacturer.contains("nox") || manufacturer.contains("Bluestacks") || manufacturer.contains("Andy")))

        return isTestKeys || isTestFingerprint || isEmulator
    }

    /**
     * Scans standard system folders for binaries and files associated with root permissions
     * and custom configurations of KernelSU/APatch/Magisk.
     */
    private fun checkSuPaths(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/bin/su/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/sbin/busybox",
            "/data/adb/ksu",
            "/data/adb/apatch",
            "/data/adb/magisk"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists()) return true
        }
        return false
    }

    /**
     * Searches for 'su' binary directly inside the current process shell context.
     */
    private fun checkSuCommand(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = process.inputStream.bufferedReader()
            val line = reader.readLine()
            line != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Splits and parses the system's active executable search path (PATH variable)
     * and audits every folder for the presence of the 'su' or 'busybox' binaries.
     */
    private fun checkPathEnvironment(): Boolean {
        val pathEnv = System.getenv("PATH") ?: return false
        val folders = pathEnv.split(":")
        for (folder in folders) {
            try {
                val suFile = File(folder, "su")
                val busyFile = File(folder, "busybox")
                if (suFile.exists() || busyFile.exists()) return true
            } catch (t: Throwable) {
                // Ignore IO errors for isolated environment segments
            }
        }
        return false
    }

    /**
     * Audits standard system methods to check if their Java implementation has been
     * dynamically turned into native code (which is the universal signature of Xposed hooks).
     */
    private fun checkXposedNativeMethodHooks(): Boolean {
        try {
            // These SDK methods are NEVER native in pure Android, but commonly hooked by LSPosed/Xposed.
            val newActivityMethod = android.app.Instrumentation::class.java.getDeclaredMethod(
                "newActivity",
                ClassLoader::class.java,
                String::class.java,
                android.content.Intent::class.java
            )
            if (java.lang.reflect.Modifier.isNative(newActivityMethod.modifiers)) {
                return true
            }

            val onCreateMethod = android.app.Application::class.java.getDeclaredMethod("onCreate")
            if (java.lang.reflect.Modifier.isNative(onCreateMethod.modifiers)) {
                return true
            }
        } catch (t: Throwable) {
            // Safe fallback
        }
        return false
    }

    /**
     * Inspects active runtime class declarations to see if hooking framework
     * endpoints (like LSPosed, Xposed, or Frida bridges) are loaded in memory.
     */
    private fun checkHookFrameworks(): Boolean {
        val classes = arrayOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "com.saurik.substrate.MS",
            "com.noshufou.android.su.Su",
            "io.github.libxposed.api.XposedInterface",
            "org.meowcat.edxposed.manager.api.EdXposedApi"
        )
        for (className in classes) {
            try {
                Class.forName(className)
                return true
            } catch (e: ClassNotFoundException) {
                // Clean class state
            }
        }

        // Check active xposed JVM attributes
        try {
            val xposedActive = System.getProperty("xposed.active")
            if (xposedActive != null && xposedActive == "1") return true
        } catch (t: Throwable) {}

        return false
    }

    /**
     * Forces an execution trace trace-back and audits the resulting frames.
     * Active hooking agents (like LSPosed, Xposed, or dynamic hook interceptors)
     * inject custom wrappers that immediately trigger anomalies in the call stack.
     */
    private fun checkStackTrace(): Boolean {
        try {
            throw Exception("EnvironmentAudit")
        } catch (e: Exception) {
            for (stackTraceElement in e.stackTrace) {
                val className = stackTraceElement.className
                if (className.contains("de.robv.android.xposed") ||
                    className.contains("lsposed") ||
                    className.contains("edxposed") ||
                    (className.contains("zygote") && className.contains("hook")) ||
                    className.contains("frida")
                ) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Audits the memory allocation mappings of the current application process (/proc/self/maps).
     * Bypasses simple Java file hooking by spawning a shell `cat` command to read the virtual maps file.
     */
    private fun checkMemoryMaps(): Boolean {
        val pid = android.os.Process.myPid()
        val signatures = arrayOf(
            "frida", "xposed", "lsposed", "edxposed", "apatch", "libksu", "libapatch", "supersu", "magisk"
        )

        // Method 1: Bypassing Java hooks using process-isolated shell command (extremely advanced!)
        try {
            val process = Runtime.getRuntime().exec(arrayOf("cat", "/proc/$pid/maps"))
            val reader = process.inputStream.bufferedReader()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val l = line?.lowercase() ?: ""
                for (sig in signatures) {
                    if (l.contains(sig)) {
                        process.destroy()
                        return true
                    }
                }
            }
            process.destroy()
        } catch (t: Throwable) {
            // Fallback to Method 2 if process boundary fails
        }

        // Method 2: Direct Java file read
        try {
            val file = File("/proc/self/maps")
            if (file.exists() && file.isFile && file.canRead()) {
                file.useLines { lines ->
                    val hasTampering = lines.any { line ->
                        signatures.any { sig -> line.lowercase().contains(sig) }
                    }
                    if (hasTampering) return true
                }
            }
        } catch (t: Throwable) {
            // Ignore isolated maps
        }
        return false
    }

    /**
     * Parses the active OS file mounts (/proc/mounts and /proc/self/mountinfo).
     * Performs direct shell parsing to avoid JNI or Java-level overlays.
     * Identifies KSU/APatch/Magisk loop mirrors and system OverlayFS/TmpFS violations.
     */
    private fun checkProcMounts(): Boolean {
        val pid = android.os.Process.myPid()

        val parseMountLine = fun(line: String): Boolean {
            val parts = line.split(" ")
            if (parts.size >= 3) {
                val mountSource = parts[0]
                val mountPath = parts[1]
                val fsType = parts[2]

                // 1. Scan for stealth keywords
                val isStealthKeyword = mountSource.contains("magisk", ignoreCase = true) ||
                        mountSource.contains("ksu", ignoreCase = true) ||
                        mountSource.contains("apatch", ignoreCase = true) ||
                        mountPath.contains("magisk", ignoreCase = true) ||
                        mountPath.contains("ksu", ignoreCase = true) ||
                        mountPath.contains("apatch", ignoreCase = true) ||
                        mountPath.contains(".magisk", ignoreCase = true) ||
                        mountPath.contains("mirror", ignoreCase = true) ||
                        mountPath.contains("/su", ignoreCase = true)

                // 2. Audit overlayfs mounted on system read-only directories
                val isOverlayViolation = fsType == "overlay" && (
                        mountPath.startsWith("/system") ||
                                mountPath.startsWith("/vendor") ||
                                mountPath.startsWith("/product") ||
                                mountPath.startsWith("/apex")
                        )

                // 3. Audit tmpfs mounted on system subpaths (common for KSU/Magisk certificate and hosts injection)
                val isTmpfsViolation = fsType == "tmpfs" && (
                        mountPath.startsWith("/system/etc") ||
                                mountPath.startsWith("/system/framework") ||
                                mountPath.startsWith("/system/bin")
                        )

                return isStealthKeyword || isOverlayViolation || isTmpfsViolation
            }
            return false
        }

        // Method 1: Bypassing Java hooks using process-isolated shell command (extremely advanced!)
        try {
            val process = Runtime.getRuntime().exec(arrayOf("cat", "/proc/$pid/mounts"))
            val reader = process.inputStream.bufferedReader()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line != null && parseMountLine(line!!)) {
                    process.destroy()
                    return true
                }
            }
            process.destroy()
        } catch (t: Throwable) {
            // Fallback to Method 2
        }

        // Method 2: Direct Java file read
        val files = arrayOf(File("/proc/mounts"), File("/proc/self/mountinfo"))
        for (file in files) {
            try {
                if (file.exists() && file.canRead()) {
                    file.useLines { lines ->
                        if (lines.any { parseMountLine(it) }) return true
                    }
                }
            } catch (t: Throwable) {
                // Ignore mount inspection exceptions
            }
        }
        return false
    }

    /**
     * Verifies if directories that are supposed to be strictly read-only
     * (like /system, /vendor, /) are mounted as writable.
     */
    private fun checkPartitionWritability(): Boolean {
        val testDirs = arrayOf("/system", "/vendor", "/")
        for (dir in testDirs) {
            try {
                val file = File(dir, "ex_employee_security_check.txt")
                if (file.createNewFile()) {
                    file.delete()
                    return true // Write access achieved on read-only system boundary!
                }
            } catch (t: Throwable) {
                // Correct behavior: fail to write on safe devices
            }
        }
        return false
    }

    /**
     * Probes standard local interface ports (e.g. Frida's standard port 27042)
     * to identify active background execution modules.
     */
    private fun checkFridaLocalPort(): Boolean {
        var socket: Socket? = null
        return try {
            // Check Frida default loopback injection port
            socket = Socket(InetAddress.getByName("127.0.0.1"), 27042)
            true
        } catch (e: Exception) {
            false
        } finally {
            try {
                socket?.close()
            } catch (t: Throwable) {
                // Ignore close socket exception
            }
        }
    }

    /**
     * Audits all active JVM threads for Frida and other injection engine loops.
     */
    private fun checkThreadNamesForFrida(): Boolean {
        try {
            val threads = Thread.getAllStackTraces().keys
            for (thread in threads) {
                val name = thread.name.lowercase()
                if (name.contains("frida") ||
                    name.contains("gum-js-loop") ||
                    name.contains("gmain") ||
                    name.contains("pool-frida")
                ) {
                    return true
                }
            }
        } catch (t: Throwable) {}
        return false
    }

    /**
     * Audits SELinux configuration state. If SELinux is permissive or disabled,
     * the system kernel security is compromised.
     */
    private fun checkSelinuxEnforcing(): Boolean {
        // Method 1: exec getenforce
        try {
            val process = Runtime.getRuntime().exec(arrayOf("getenforce"))
            val reader = process.inputStream.bufferedReader()
            val line = reader.readLine()
            if (line != null && line.trim().equals("Permissive", ignoreCase = true)) {
                process.destroy()
                return true
            }
            process.destroy()
        } catch (t: Throwable) {}

        // Method 2: read enforce status file directly
        try {
            val enforceFile = File("/sys/fs/selinux/enforce")
            if (enforceFile.exists() && enforceFile.canRead()) {
                val content = enforceFile.readText().trim()
                if (content == "0") return true // SELinux is Permissive!
            }
        } catch (t: Throwable) {}

        return false
    }

    /**
     * Audits Unix Domain Sockets (/proc/net/unix) to detect IPC socket channels
     * created by Magisk, Zygisk, LSPosed, Frida, or APatch.
     */
    private fun checkUnixDomainSockets(): Boolean {
        try {
            val unixFile = File("/proc/net/unix")
            if (unixFile.exists() && unixFile.canRead()) {
                unixFile.useLines { lines ->
                    val hasTampering = lines.any { line ->
                        val lower = line.lowercase()
                        lower.contains("magisk") ||
                                lower.contains("xposed") ||
                                lower.contains("lsposed") ||
                                lower.contains("zygisk") ||
                                lower.contains("apatch") ||
                                lower.contains("ksu") ||
                                lower.contains("frida")
                    }
                    if (hasTampering) return true
                }
            }
        } catch (t: Throwable) {}
        return false
    }

    /**
     * Audits common system binaries for symlink anomalies or linkages to busybox/su.
     */
    private fun checkBusyboxAppletSymlinks(): Boolean {
        val testPaths = arrayOf("/system/bin/ls", "/system/bin/cat", "/system/bin/chmod")
        for (path in testPaths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val canonicalPath = file.canonicalPath
                    if (canonicalPath.contains("busybox") || canonicalPath.contains("su")) {
                        return true
                    }
                }
            } catch (t: Throwable) {}
        }
        return false
    }

    /**
     * Audits system properties via multiple layers. Includes executing "getprop" as well as
     * calling SystemProperties.get through hidden JVM API reflection to bypass active Java-level mocks.
     */
    private fun checkSystemProperties(): Boolean {
        // Method 1: Reflection query of hidden android.os.SystemProperties (bypasses simple Java API level mocks!)
        try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java)

            val secure = getMethod.invoke(null, "ro.secure") as? String
            val debuggable = getMethod.invoke(null, "ro.debuggable") as? String
            val apatch = getMethod.invoke(null, "init.svc.apatch") as? String
            val magisk = getMethod.invoke(null, "init.svc.magisk_service") as? String
            val ksu = getMethod.invoke(null, "init.svc.ksud") as? String

            if (secure == "0" ||
                debuggable == "1" ||
                apatch == "running" ||
                magisk == "running" ||
                ksu == "running"
            ) {
                return true
            }
        } catch (t: Throwable) {
            // Fallback
        }

        // Method 2: Shell getprop query
        try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop"))
            val reader = process.inputStream.bufferedReader()
            reader.useLines { lines ->
                val hasMatch = lines.any { line ->
                    (line.contains("ro.secure") && line.contains("0")) ||
                            (line.contains("ro.debuggable") && line.contains("1")) ||
                            (line.contains("init.svc.magisk_service") && line.contains("running")) ||
                            (line.contains("init.svc.ksud") && line.contains("running")) ||
                            (line.contains("init.svc.apatch") && line.contains("running")) ||
                            (line.contains("persist.sys.root_access") && line.contains("1"))
                }
                if (hasMatch) return true
            }
        } catch (t: Throwable) {
            // Ignore property audits
        }

        return false
    }
}
