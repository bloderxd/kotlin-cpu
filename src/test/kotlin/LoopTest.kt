package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class LoopTest {

    @Test
    fun loopProgramProducesExpectedRegisterValues() {
        val cpu = KasmTestProcesser.runExample("loop.kasm")

        assertEquals(0u.toUShort(), cpu.readReg(1))
        assertEquals(16u.toUShort(), cpu.readReg(2))
    }
}

