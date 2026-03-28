package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class MovTest {

    @Test
    fun movProgramCopiesRegisterValue() {
        val cpu = KasmTestProcesser.runExample("mov.kasm")

        // LI immediate is 6-bit signed, so 42 becomes -22 (0xFFEA)
        assertEquals(65514u.toUShort(), cpu.readReg(1))
        assertEquals(65514u.toUShort(), cpu.readReg(2))
    }
}

