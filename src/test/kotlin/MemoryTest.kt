package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryTest {

    @Test
    fun memoryProgramProducesExpectedRegisterValues() {
        val cpu = KasmTestProcesser.runExample("memory.kasm")

        // LI immediate is 6-bit signed, so 123 becomes -5 (0xFFFB)
        assertEquals(65531u.toUShort(), cpu.readReg(1))
        assertEquals(0u.toUShort(), cpu.readReg(2))
        assertEquals(65531u.toUShort(), cpu.readReg(3))
    }
}

