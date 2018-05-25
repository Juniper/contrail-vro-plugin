/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests

import net.juniper.contrail.vro.config.globalProjectInfo
import java.io.File

val baseProjectRoot = globalProjectInfo.finalProjectRoot
val workflowModuleRoot = "$baseProjectRoot-workflows"
val testModuleRoot = "$baseProjectRoot-tests"

val actions = "actions"
val workflows = "workflows"

val js = "js"
val groovy = "groovy"

val scriptEndPattern = "\\.$js$".toRegex()
val testEndPattern = "Spec\\.$groovy$".toRegex()

val scriptsRoot = "$workflowModuleRoot/src/main/js"
val actionsDir = "$scriptsRoot/$actions"
val workflowsDir = "$scriptsRoot/$workflows"

val testsRoot = "$testModuleRoot/src/test/groovy/net/juniper/contrail/vro/tests"
val actionsTestDir = "$testsRoot/$actions"
val workflowsTestDir = "$testsRoot/$workflows"

val actionScripts = listScripts(actionsDir)
val workflowScripts = listScripts(workflowsDir)

val actionTests = listTests(actionsTestDir)
val workflowTests = listTests(workflowsTestDir)

fun printScriptTestSummary(printer: (String) -> Unit = { println("$it") }) {
    printer("========================== SCRIPTS =============================")
    printer("---> Workflows")
    printSummary(workflowScripts, workflowTests, printer)
    printer("")
    printer("---> Actions")
    printSummary(actionScripts, actionTests, printer)
    printer("")
    printer("================================================================")
}

private fun printSummary(scripts: List<File>, tests: List<File>, printer: (String) -> Unit) {
    val scriptMap = scripts.associateBy { it.toIdMatching(scriptEndPattern) }
    val testMap = tests.associateBy { it.toIdMatching(testEndPattern) }

    val missingTests = scriptMap.filter { ! testMap.containsKey(it.key) }
    val extraTests = testMap.filter { ! scriptMap.containsKey(it.key) }

    printer("Number of scripts: ${scripts.size}")
    printer("Number of tests: ${tests.size}")
    if (missingTests.isEmpty()) {
        printer("Found tests for all scripts")
    } else {
        printer("Missing tests for scripts")
        missingTests.forEach { printer("    ${it.value.name}") }
    }
    if (! extraTests.isEmpty()) {
        printer("Tests not matching any script")
        extraTests.forEach { printer("    ${it.value.name}") }
    }
}

private fun File.toIdMatching(pattern: Regex) =
    name.replace(pattern, "").toLowerCase()

private fun listScripts(root: String) =
    listFiles(root, js)

private fun listTests(root: String) =
    listFiles(root, groovy)

private fun listFiles(root: String, extension: String) =
    File(root).listFiles().asSequence()
        .filter { it.isFile }
        .filter { it.name.endsWith(".$extension") }
        .toList()