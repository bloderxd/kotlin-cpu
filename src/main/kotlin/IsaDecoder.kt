package com.bloder

interface IsaDecoder {

    fun decodeOpcode(inst: UShort): OpCode {
        val code = (inst.toInt() shr 12) and 0xF
        return OpCode.entries.first { it.code == code }
    }
    fun decodeRd(inst: UShort) = (inst.toInt() shr 9) and 0x7
    fun decodeRs1(inst: UShort) = (inst.toInt() shr 6) and 0x7
    fun decodeRs2(inst: UShort) = (inst.toInt() shr 3) and 0x7
    fun decodeImm6(inst: UShort): Int {
        val imm = inst.toInt() and 0x3F
        return if (imm and 0x20 != 0) imm or -0x40 else imm
    }
    fun decodeImm12(inst: UShort): Int {
        val imm = inst.toInt() and 0xFFF
        return if (imm and 0x800 != 0) imm or -0x1000 else imm
    }
    fun decodeAluInstruction(inst: UShort): AluInstruction {
        val code = inst.toInt() and 0x7
        return AluInstruction.entries.first { it.operation == code }
    }

    companion object : IsaDecoder
}