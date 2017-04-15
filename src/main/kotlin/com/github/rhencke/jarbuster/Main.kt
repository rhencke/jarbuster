package com.github.rhencke.jarbuster.rhencke

import org.pf.tools.cda.base.model.ClassInformation
import org.pf.tools.cda.base.model.Workset
import org.pf.tools.cda.base.model.processing.AClassInformationProcessor
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition
import org.pf.tools.cda.core.init.WorksetInitializer
import org.pfsw.odem.IType
import org.pfsw.odem.TypeClassification
import java.util.*
import kotlin.system.exitProcess

private val err = { s: String -> System.err.print(s) }
private val errln = { s: String -> System.err.println(s) }

fun main(args: Array<String>) {
    if (!args.any()) {
        errln("This program will suggest ways that a set of Java classes")
        errln("can be broken into modules, in such a way that there are no")
        errln("circular dependencies between modules.")
        errln("")
        errln("Please pass one or more classpath entries.")
        errln("For example, foo.jar, lib/*.jar, or /path/to/loose/classes")
        errln("")
        errln("You only need to pass the JARs/classfiles that make up")
        errln("your program.  In other words, omit third-party dependencies.")
        exitProcess(2)
    }
    val workset = Workset("")

    for (classpathPart in args.map { ClasspathPartDefinition(it) }) {
        workset.addClasspathPartDefinition(classpathPart)
    }

    val wsInitializer = WorksetInitializer(workset)
    err("loading dependencies...")
    wsInitializer.initializeWorksetAndWait(null)
    errln(" done")

    val v = CycleFinder()

    workset.processClassInformationObjects(v)

    // Each type must belong to only one module
    // If two cycles overlap, we must place the types from both cycles
    // in the same module
    val typeToModule = mutableMapOf<IType, Int>()

    val modules = arrayOfNulls<Set<IType>?>(v.cycles.size)
    fun mergeModules(from: Int, to: Int) {
        val fromModule = modules[from]!!
        val toModule = modules[to]!!
        for (cls in fromModule) {
            typeToModule.put(cls, to)
        }
        modules[from] = null
        modules[to] = fromModule + toModule
    }

    for ((i, cycle) in v.cycles.withIndex()) {
        modules[i] = cycle
        for (cls in cycle.toList()) {
            val existingCycle = typeToModule.put(cls, i)
            if (existingCycle != null) {
                mergeModules(existingCycle, i)
            }
        }
    }
    for ((i, module) in modules.filterNotNull().withIndex()) {
        println("/* Module ${i + 1} */")
        for (cls in module) {
            println(cls.name)
        }

        println()
    }
}

private class CycleFinder : AClassInformationProcessor<Unit>() {
    private val processed = mutableSetOf<IType>()
    private val path = Stack<IType>()
    private val pathSet = mutableSetOf<IType>()
    val cycles = mutableListOf<Set<IType>>()

    override fun process(cls: ClassInformation): Boolean {
        findCycles(cls)
        return true
    }

    private fun findCycles(cls: IType) {
        // We only care about types directly in this workset.
        // Others will have TypeClassification.UNKNOWN and can be discarded.
        for (dep in cls.directReferredTypes.filter { it.classification != TypeClassification.UNKNOWN }) {
            if (pathSet.contains(dep)) {
                val cycleFrom = path.indexOf(dep)
                // From the index of this class to the
                val cycleSet = (cycleFrom until path.size)
                        .map { path[it] }
                        .toSet()
                cycles.add(cycleSet)
            }
            if (!processed.add(dep)) {
                continue
            }
            path.push(dep)
            pathSet.add(dep)
            findCycles(dep)
            path.pop()
            pathSet.remove(dep)
        }
    }

    override fun matches(p0: ClassInformation): Boolean = true
}
