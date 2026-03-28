package com.bloder

enum class OpCode(val code: Int) {
    ALU(code = 0x0),
    ADDI(code = 0x1),
    LI(code = 0x2),
    LUI(code = 0x3),
    LOAD(code = 0x4),
    STORE(code = 0x5),
    BEQ(code = 0x6),
    BNE(code = 0x7),
    JMP(code = 0x8),
    RET(code = 0x9),
    PUSH(code = 0xC),
    POP(code = 0xD),
    CALL(code = 0xE),
    HALT(code = 0xF);
}

class Isa private constructor(
    val cpu: Cpu,
    private val decoder: IsaDecoder,
    private val alu: Alu,
    private val debugger: IsaDebugger
) : IsaExecution {

    companion object {
        fun create(
            cpu: Cpu,
            debugger: IsaDebugger = IsaDebugger.None
        ): Isa {
            return Isa(
                cpu = cpu,
                decoder = IsaDecoder,
                alu = Alu,
                debugger = debugger
            )
        }
    }

    fun run() {
        context(decoder, alu) {
            while (cpu.isRunning()) {
                val inst = cpu.fetch()
                val opcode = decoder.decodeOpcode(inst)
                with(debugger) {
                    cpu.debug(opCode = opcode, inst = inst, step = { op, inst ->
                        executeInstruction(opCode = op, inst = inst)
                    })
                }
            }
        }
    }

    context(decoder: IsaDecoder, alu: Alu)
    private fun executeInstruction(opCode: OpCode, inst: UShort) {
        when (opCode) {
            OpCode.ALU -> cpu.alu(inst = inst)
            OpCode.ADDI -> cpu.addi(inst = inst)
            OpCode.LI -> cpu.li(inst = inst)
            OpCode.LUI -> cpu.lui(inst = inst)
            OpCode.LOAD -> cpu.load(inst = inst)
            OpCode.STORE -> cpu.store(inst = inst)
            OpCode.BEQ -> cpu.beq(inst = inst)
            OpCode.BNE -> cpu.bne(inst = inst)
            OpCode.JMP -> cpu.jmp(inst = inst)
            OpCode.RET -> cpu.ret()
            OpCode.PUSH -> cpu.push(inst = inst)
            OpCode.POP -> cpu.pop(inst = inst)
            OpCode.CALL -> cpu.call(inst = inst)
            OpCode.HALT -> cpu.halt()
        }
    }
}

private interface IsaExecution {

    context(decoder: IsaDecoder, alu: Alu)
    fun Cpu.alu(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val rs1 = decoder.decodeRs1(inst)
        val rs2 = decoder.decodeRs2(inst)
        val a = readReg(rs1)
        val b = readReg(rs2)
        val result = alu.execute(
            operation = decoder.decodeAluInstruction(inst),
            a = a,
            b = b,
            updateCarryFlag = { carry -> updateCarry(carry = carry) }
        )
        updateZeroNegative(value = result)
        writeReg(index = rd, value = result)
    }

    context(decoder: IsaDecoder)
    fun Cpu.addi(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val rs1 = decoder.decodeRs1(inst)
        val imm = decoder.decodeImm6(inst)

        val a = readReg(rs1).toUInt()
        val b = imm.toUInt()

        val result = a + b

        updateCarry(carry = result > 0xFFFFu)

        val final = result.toUShort()

        updateZeroNegative(value = final)
        writeReg(index = rd, value = final)
    }

    context(decoder: IsaDecoder)
    fun Cpu.li(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val imm = decoder.decodeImm6(inst)

        val value = imm.toUShort()

        updateZeroNegative(value = value)
        writeReg(rd, value)
    }

    context(decoder: IsaDecoder)
    fun Cpu.lui(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val imm = decoder.decodeImm6(inst)

        val value = (imm shl 8).toUShort()

        writeReg(index = rd, value = value)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    context(decoder: IsaDecoder)
    fun Cpu.load(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val rs1 = decoder.decodeRs1(inst)
        val imm = decoder.decodeImm6(inst)

        val base = readReg(rs1).toInt()
        val address = ((base + imm) and 0xFFFF) and 0xFFFE

        val low = memory[address].toUInt()
        val high = memory[address + 1].toUInt()

        val value = ((high shl 8) or low).toUShort()

        writeReg(rd, value)
    }

    context(decoder: IsaDecoder)
    fun Cpu.store(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val rs1 = decoder.decodeRs1(inst)
        val imm = decoder.decodeImm6(inst)

        val base = readReg(rs1).toInt()
        val address = ((base + imm) and 0xFFFF) and 0xFFFE

        val value = readReg(rd)

        writeMemory(address = address, value = (value.toInt() and 0xFF).toUByte())
        writeMemory(address = address + 1, value = ((value.toInt() shr 8) and 0xFF).toUByte())
    }

    context(decoder: IsaDecoder)
    fun Cpu.beq(inst: UShort) {
        val rs1 = decoder.decodeRd(inst)
        val rs2 = decoder.decodeRs1(inst)
        val offset = decoder.decodeImm6(inst)

        if (readReg(rs1) == readReg(rs2)) {
            pc { pc -> (pc.toInt() + offset * 2).toUShort() }
        }
    }

    context(decoder: IsaDecoder)
    fun Cpu.bne(inst: UShort) {
        val rs1 = decoder.decodeRd(inst)
        val rs2 = decoder.decodeRs1(inst)
        val offset = decoder.decodeImm6(inst)

        if (readReg(rs1) != readReg(rs2)) {
            pc { pc -> (pc.toInt() + offset * 2).toUShort() }
        }
    }

    context(decoder: IsaDecoder)
    fun Cpu.jmp(inst: UShort) {
        val offset = decoder.decodeImm12(inst)
        pc { pc -> (pc.toInt() + offset * 2).toUShort() }
    }

    fun Cpu.ret() {
        pc { pop() }
    }

    context(decoder: IsaDecoder)
    fun Cpu.push(inst: UShort) {
        val rs = decoder.decodeRd(inst)
        val value = readReg(rs)
        push(value)
    }

    context(decoder: IsaDecoder)
    fun Cpu.pop(inst: UShort) {
        val rd = decoder.decodeRd(inst)
        val value = pop()
        writeReg(rd, value)
    }

    context(decoder: IsaDecoder)
    fun Cpu.call(inst: UShort) {
        val offset = decoder.decodeImm12(inst)
        push(pc { it })
        pc { pc -> (pc.toInt() + offset * 2).toUShort() }
    }

    fun Cpu.halt() {
        stopRunning()
    }
}