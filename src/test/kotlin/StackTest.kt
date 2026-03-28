package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class StackTest {

    @Test
    fun stackProgramProducesExpectedRegisterValues() {
        val cpu = KasmTestProcesser.runExample("stack.kasm")

        assertEquals(10u.toUShort(), cpu.readReg(1))
        assertEquals(20u.toUShort(), cpu.readReg(2))
        assertEquals(10u.toUShort(), cpu.readReg(3))
        assertEquals(20u.toUShort(), cpu.readReg(4))
        assertEquals(30u.toUShort(), cpu.readReg(5))
    }
}

