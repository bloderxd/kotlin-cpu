package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class AluTest {

    @Test
    fun aluProgramProducesExpectedRegisterValues() {
        val cpu = KasmTestProcesser.runExample("alu.kasm")

        assertEquals(12u.toUShort(), cpu.readReg(1))
        assertEquals(12u.toUShort(), cpu.readReg(2))
        assertEquals(24u.toUShort(), cpu.readReg(3))
        assertEquals(6u.toUShort(), cpu.readReg(4))
        assertEquals(4u.toUShort(), cpu.readReg(5))
        assertEquals(1u.toUShort(), cpu.readReg(6))
        assertEquals(9u.toUShort(), cpu.readReg(7))
    }
}

