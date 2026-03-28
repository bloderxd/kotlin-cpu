package com.bloder

object Tiny16Encoder {

    fun ADD(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b000)

    fun SUB(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b001)

    fun AND(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b010)

    fun OR(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b011)

    fun XOR(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b100)

    fun MOV(rd: Int, rs1: Int) =
        rType(0x0, rd, rs1, 0, 0b101)

    fun SHL(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b110)

    fun SHR(rd: Int, rs1: Int, rs2: Int) =
        rType(0x0, rd, rs1, rs2, 0b111)

    fun ADDI(rd: Int, rs1: Int, imm: Int) =
        iType(0x1, rd, rs1, imm)

    fun LI(rd: Int, imm: Int) =
        iType(0x2, rd, 0, imm)

    fun LUI(rd: Int, imm: Int) =
        iType(0x3, rd, 0, imm)

    fun LOAD(rd: Int, base: Int, offset: Int) =
        iType(0x4, rd, base, offset)

    fun STORE(rs: Int, base: Int, offset: Int) =
        iType(0x5, rs, base, offset)

    fun BEQ(rs1: Int, rs2: Int, offset: Int): UShort {
        val imm6 = offset and 0x3F
        return (
                (0x6 shl 12) or
                        (rs1 shl 9) or
                        (rs2 shl 6) or
                        imm6
                ).toUShort()
    }

    fun BNE(rs1: Int, rs2: Int, offset: Int): UShort {
        val imm6 = offset and 0x3F
        return (
                (0x7 shl 12) or
                        (rs1 shl 9) or
                        (rs2 shl 6) or
                        imm6
                ).toUShort()
    }

    fun JMP(offset: Int) =
        jType(0x8, offset)

    fun RET() =
        jType(0x9, 0)

    fun CALL(offset: Int) =
        jType(0xE, offset)

    fun PUSH(reg: Int) = sType(0xC, reg)
    fun POP(reg: Int) = sType(0xD, reg)
    fun HALT() = (0xF shl 12).toUShort()
    fun NOP(): UShort {
        return ADD(0, 0, 0)
    }

    private fun rType(opcode: Int, rd: Int, rs1: Int, rs2: Int, funct: Int): UShort {
        return (
                (opcode shl 12) or
                        (rd shl 9) or
                        (rs1 shl 6) or
                        (rs2 shl 3) or
                        funct
                ).toUShort()
    }

    private fun iType(opcode: Int, rd: Int, rs1: Int, imm: Int): UShort {
        val imm6 = imm and 0x3F
        return (
                (opcode shl 12) or
                        (rd shl 9) or
                        (rs1 shl 6) or
                        imm6
                ).toUShort()
    }

    private fun jType(opcode: Int, imm: Int): UShort {
        val imm12 = imm and 0xFFF
        return (
                (opcode shl 12) or
                        imm12
                ).toUShort()
    }

    private fun sType(opcode: Int, reg: Int = 0): UShort {
        return (
                (opcode shl 12) or
                        (reg shl 9)
                ).toUShort()
    }
}