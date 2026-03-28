package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class BeqTest {

    @Test
    fun beqInstructionBranchesWhenRegistersAreEqual() {
        val cpu = KasmTestProcesser.runExample("beq.kasm")

        assertEquals(5u.toUShort(), cpu.readReg(1))
        assertEquals(5u.toUShort(), cpu.readReg(2))
        assertEquals(7u.toUShort(), cpu.readReg(3))
    }
}

