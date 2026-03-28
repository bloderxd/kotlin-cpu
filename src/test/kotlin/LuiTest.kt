package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class LuiTest {

    @Test
    fun luiProgramProducesExpectedRegisterValues() {
        val cpu = KasmTestProcesser.runExample("lui.kasm")

        assertEquals(0x1A00u.toUShort(), cpu.readReg(1))
        assertEquals(0xFF00u.toUShort(), cpu.readReg(2))
        assertEquals(0xFF1Fu.toUShort(), cpu.readReg(3))
    }
}

