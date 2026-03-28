package com.bloder.kasm

import com.bloder.AluInstruction
import com.bloder.OpCode

interface KasmEncoder {

    fun ADD(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.ADD)

    fun SUB(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.SUB)

    fun AND(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.AND)

    fun OR(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.OR)

    fun XOR(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.XOR)

    fun MOV(rd: Int, rs1: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, 0, AluInstruction.MOV)

    fun SHL(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.SHL)

    fun SHR(rd: Int, rs1: Int, rs2: Int) =
        encodeRegister(OpCode.ALU, rd, rs1, rs2, AluInstruction.SHR)

    fun ADDI(rd: Int, rs1: Int, imm: Int) =
        encodeImmediate(OpCode.ADDI, rd, rs1, imm)

    fun LI(rd: Int, imm: Int) =
        encodeImmediate(OpCode.LI, rd, 0, imm)

    fun LUI(rd: Int, imm: Int) =
        encodeImmediate(OpCode.LUI, rd, 0, imm)

    fun LOAD(rd: Int, base: Int, offset: Int) =
        encodeImmediate(OpCode.LOAD, rd, base, offset)

    fun STORE(rs: Int, base: Int, offset: Int) =
        encodeImmediate(OpCode.STORE, rs, base, offset)

    fun BEQ(rs1: Int, rs2: Int, offset: Int): UShort {
        val imm6 = offset and 0x3F
        return (
                (OpCode.BEQ.code shl 12) or
                        (rs1 shl 9) or
                        (rs2 shl 6) or
                        imm6
                ).toUShort()
    }

    fun BNE(rs1: Int, rs2: Int, offset: Int): UShort {
        val imm6 = offset and 0x3F
        return (
                (OpCode.BNE.code shl 12) or
                        (rs1 shl 9) or
                        (rs2 shl 6) or
                        imm6
                ).toUShort()
    }

    fun JMP(offset: Int) =
        encodeJump(OpCode.JMP, offset)

    fun RET() =
        encodeJump(OpCode.RET, 0)

    fun CALL(offset: Int) =
        encodeJump(OpCode.CALL, offset)

    fun PUSH(reg: Int) = encodeStack(OpCode.PUSH, reg)
    fun POP(reg: Int) = encodeStack(OpCode.POP, reg)
    fun HALT() = (OpCode.HALT.code shl 12).toUShort()
    fun NOP(): UShort {
        return ADD(0, 0, 0)
    }

    private fun encodeRegister(opcode: OpCode, rd: Int, rs1: Int, rs2: Int, aluOperation: AluInstruction): UShort {
        return (
                (opcode.code shl 12) or
                        (rd shl 9) or
                        (rs1 shl 6) or
                        (rs2 shl 3) or
                        aluOperation.operation
                ).toUShort()
    }

    private fun encodeImmediate(opcode: OpCode, rd: Int, rs1: Int, imm: Int): UShort {
        val imm6 = imm and 0x3F
        return (
                (opcode.code shl 12) or
                        (rd shl 9) or
                        (rs1 shl 6) or
                        imm6
                ).toUShort()
    }

    private fun encodeJump(opcode: OpCode, imm: Int): UShort {
        val imm12 = imm and 0xFFF
        return (
                (opcode.code shl 12) or
                        imm12
                ).toUShort()
    }

    private fun encodeStack(opcode: OpCode, reg: Int = 0): UShort {
        return (
                (opcode.code shl 12) or
                        (reg shl 9)
                ).toUShort()
    }

    companion object : KasmEncoder
}