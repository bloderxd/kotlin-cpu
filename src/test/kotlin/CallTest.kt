package com.bloder

import kotlin.test.Test
import kotlin.test.assertEquals

class CallTest {

    @Test
    fun callInstructionReturnsToTheCallerUsingRet() {
        val cpu = KasmTestProcesser.runExample("call.kasm")

        assertEquals(4u.toUShort(), cpu.readReg(1))
        assertEquals(5u.toUShort(), cpu.readReg(2))
        assertEquals(10u.toUShort(), cpu.readReg(3))
        assertEquals(0xFFFEu.toUShort(), cpu.sp { it })
    }
}


