package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class BneTest {

    @Test
    fun bneInstructionBranchesWhenRegistersAreNotEqual() {
        val cpu = KasmTestProcesser.runExample("bne.kasm")

        assertEquals(5u.toUShort(), cpu.readReg(1))
        assertEquals(3u.toUShort(), cpu.readReg(2))
        assertEquals(9u.toUShort(), cpu.readReg(3))
    }
}

