package com.bloder

import kotlin.test.assertFalse

object KasmTestProcesser {

    fun runExample(kasmFile: String, maxSteps: Int = 500): CPU {
        val source = KasmReader(kasmFile)
        val binary = assemble(source)
        val cpu = CPU()

        loadProgram(cpu, binary)

        var steps = 0
        while (cpu.running && steps < maxSteps) {
            cpu.step(debug = false)
            steps++
        }

        assertFalse(cpu.running, "Program '$kasmFile' did not HALT within $maxSteps steps")
        return cpu
    }
}

