package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class JmpTest {

    @Test
    fun jmpInstructionSkipsCodeCorrectly() {
        val cpu = KasmTestProcesser.runExample("jmp.kasm")
        // R1 should be 0 because the "LI R1, 100" instruction was skipped
        assertEquals(0u.toUShort(), cpu.readReg(1))
        // R2 should be 18 (10 + 8)
        assertEquals(18u.toUShort(), cpu.readReg(2))
    }
}

