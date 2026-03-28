package com.bloder

@OptIn(ExperimentalUnsignedTypes::class)
class CPU {

    val memory = UByteArray(65536)
    val regs = UShortArray(8)

    var PC: UShort = 0u
    var SP: UShort = 0xFFFEu

    var zeroFlag = false
    var carryFlag = false
    var negativeFlag = false

    var running = true

    fun step(debug: Boolean = false) {
        val pcBefore = PC
        val inst = fetch16()

        if (debug) {
            println(
                "[DEBUGGER] PC=${pcBefore.toString(16).padStart(4, '0')} " +
                        "INST=0x${inst.toString(16).padStart(4, '0')} " +
                        disassemble(inst)
            )
        }

        val opcode = decodeOpcode(inst)

        when (opcode) {
            0x0 -> executeALU(inst)
            0x1 -> executeADDI(inst)
            0x2 -> executeLI(inst)
            0x3 -> executeLUI(inst)
            0x4 -> executeLOAD(inst)
            0x5 -> executeSTORE(inst)
            0x6 -> executeBEQ(inst)
            0x7 -> executeBNE(inst)
            0x8 -> executeJMP(inst)
            0x9 -> executeRET()
            0xC -> executePUSH(inst)
            0xD -> executePOP(inst)
            0xE -> executeCALL(inst)
            0xF -> executeHALT()
        }

        if (debug) {
            println("   → ${dumpRegs()}")
        }
    }

    fun updateZN(value: UShort) {
        zeroFlag = value == 0.toUShort()
        negativeFlag = (value.toInt() and 0x8000) != 0
    }

    fun fetch16(): UShort {
        val low = memory[PC.toInt()].toUInt()
        val high = memory[(PC + 1u).toInt()].toUInt()

        PC = (PC + 2u).toUShort()

        return ((high shl 8) or low).toUShort()
    }

    fun push16(value: UShort) {
        SP = (SP - 2u).toUShort()

        val addr = SP.toInt() and 0xFFFF

        memory[addr] = (value.toInt() and 0xFF).toUByte()
        memory[addr + 1] = ((value.toInt() shr 8) and 0xFF).toUByte()
    }

    fun pop16(): UShort {
        val addr = SP.toInt() and 0xFFFF

        val low = memory[addr].toUInt()
        val high = memory[addr + 1].toUInt()

        val value = ((high shl 8) or low).toUShort()

        SP = (SP + 2u).toUShort()

        return value
    }

    fun decodeOpcode(inst: UShort) = (inst.toInt() shr 12) and 0xF
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
    fun decodeFunct(inst: UShort) = inst.toInt() and 0x7

    // registers
    fun readReg(index: Int): UShort {
        return if (index == 0) 0u else regs[index]
    }

    fun writeReg(index: Int, value: UShort) {
        if (index != 0) {
            regs[index] = value
        }
    }

    fun executeLOAD(inst: UShort) {
        val rd = decodeRd(inst)
        val rs1 = decodeRs1(inst)
        val imm = decodeImm6(inst)

        val base = readReg(rs1).toInt()
        val address = ((base + imm) and 0xFFFF) and 0xFFFE

        val low = memory[address].toUInt()
        val high = memory[address + 1].toUInt()

        val value = ((high shl 8) or low).toUShort()

        writeReg(rd, value)
    }

    fun executeSTORE(inst: UShort) {
        val rd = decodeRd(inst)
        val rs1 = decodeRs1(inst)
        val imm = decodeImm6(inst)

        val base = readReg(rs1).toInt()
        val address = ((base + imm) and 0xFFFF) and 0xFFFE

        val value = readReg(rd)

        memory[address] = (value.toInt() and 0xFF).toUByte()
        memory[address + 1] = ((value.toInt() shr 8) and 0xFF).toUByte()
    }

    fun executeBEQ(inst: UShort) {
        val rs1 = decodeRd(inst)
        val rs2 = decodeRs1(inst)
        val offset = decodeImm6(inst)

        if (readReg(rs1) == readReg(rs2)) {
            PC = (PC.toInt() + offset * 2).toUShort()
        }
    }

    fun executeBNE(inst: UShort) {
        val rs1 = decodeRd(inst)
        val rs2 = decodeRs1(inst)
        val offset = decodeImm6(inst)

        if (readReg(rs1) != readReg(rs2)) {
            PC = (PC.toInt() + offset * 2).toUShort()
        }
    }

    fun executeJMP(inst: UShort) {
        val offset = decodeImm12(inst)

        PC = (PC.toInt() + offset * 2).toUShort()
    }

    fun executePUSH(inst: UShort) {
        val rs = decodeRd(inst)

        val value = readReg(rs)

        push16(value)
    }

    fun executePOP(inst: UShort) {
        val rd = decodeRd(inst)

        val value = pop16()

        writeReg(rd, value)
    }

    fun executeCALL(inst: UShort) {
        val offset = decodeImm12(inst)
        push16(PC)
        PC = (PC.toInt() + offset * 2).toUShort()
    }

    fun executeRET() {
        PC = pop16()
    }

    fun executeLI(inst: UShort) {
        val rd = decodeRd(inst)
        val imm = decodeImm6(inst)

        val value = imm.toUShort()

        updateZN(value)
        writeReg(rd, value)
    }

    fun executeLUI(inst: UShort) {
        val rd = decodeRd(inst)
        val imm = decodeImm6(inst)

        val value = (imm shl 8).toUShort()

        writeReg(rd, value)
    }

    fun executeADDI(inst: UShort) {
        val rd = decodeRd(inst)
        val rs1 = decodeRs1(inst)
        val imm = decodeImm6(inst)

        val a = readReg(rs1).toUInt()
        val b = imm.toUInt()

        val result = a + b

        carryFlag = result > 0xFFFFu

        val final = result.toUShort()

        updateZN(final)
        writeReg(rd, final)
    }

    // ALU
    fun executeALU(inst: UShort) {
        val rd = decodeRd(inst)
        val rs1 = decodeRs1(inst)
        val rs2 = decodeRs2(inst)
        val funct = decodeFunct(inst)

        val a = readReg(rs1)
        val b = readReg(rs2)

        val result: UShort = when (funct) {

            0b000 -> { // ADD
                val r = a.toUInt() + b.toUInt()
                carryFlag = r > 0xFFFFu
                r.toUShort()
            }

            0b001 -> { // SUB
                val r = a.toUInt() - b.toUInt()
                carryFlag = a.toUInt() < b.toUInt()
                r.toUShort()
            }

            0b010 -> { // AND
                carryFlag = false
                a and b
            }

            0b011 -> { // OR
                carryFlag = false
                a or b
            }

            0b100 -> { // XOR
                carryFlag = false
                a xor b
            }

            0b101 -> { // MOV
                carryFlag = false
                a
            }

            0b110 -> { // SHL (shift by rs2)
                val shift = (b.toInt() and 0xF)
                val r = a.toUInt() shl shift
                carryFlag = (a.toUInt() shl (shift - 1)) and 0x8000u != 0u
                r.toUShort()
            }

            0b111 -> { // SHR
                val shift = (b.toInt() and 0xF)
                carryFlag = (a.toUInt() shr (shift - 1)) and 1u != 0u
                val r = a.toUInt() shr shift
                r.toUShort()
            }

            else -> 0u
        }

        updateZN(result)
        writeReg(rd, result)
    }

    fun executeHALT() {
        running = false
    }

    fun disassemble(inst: UShort): String {
        val opcode = decodeOpcode(inst)

        return when (opcode) {
            0x0 -> {
                val funct = decodeFunct(inst)
                val rd = decodeRd(inst)
                val rs1 = decodeRs1(inst)
                val rs2 = decodeRs2(inst)

                when (funct) {
                    0 -> "ADD R$rd, R$rs1, R$rs2"
                    1 -> "SUB R$rd, R$rs1, R$rs2"
                    2 -> "AND R$rd, R$rs1, R$rs2"
                    3 -> "OR  R$rd, R$rs1, R$rs2"
                    4 -> "XOR R$rd, R$rs1, R$rs2"
                    5 -> "MOV R$rd, R$rs1"
                    6 -> "SHL R$rd, R$rs1, R$rs2"
                    7 -> "SHR R$rd, R$rs1, R$rs2"
                    else -> "UNKNOWN"
                }
            }

            0x1 -> "ADDI R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"
            0x2 -> "LI R${decodeRd(inst)}, ${decodeImm6(inst)}"
            0x3 -> "LUI R${decodeRd(inst)}, ${decodeImm6(inst)}"

            0x4 -> "LOAD R${decodeRd(inst)}, [R${decodeRs1(inst)} + ${decodeImm6(inst)}]"
            0x5 -> "STORE R${decodeRd(inst)}, [R${decodeRs1(inst)} + ${decodeImm6(inst)}]"

            0x6 -> "BEQ R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"
            0x7 -> "BNE R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"

            0x8 -> "JMP ${decodeImm12(inst)}"
            0x9 -> "RET"

            0xC -> "PUSH R${decodeRd(inst)}"
            0xD -> "POP R${decodeRd(inst)}"

            0xE -> "CALL ${decodeImm12(inst)}"
            0xF -> "HALT"

            else -> "UNKNOWN"
        }
    }

    fun dumpRegs(): String {
        return regs.mapIndexed { i, r -> "R$i=${r.toInt()}" }
            .joinToString(" ")
    }
}