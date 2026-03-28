package com.bloder

interface Memory<T> {
    operator fun get(address: Int): T
}

interface MutableMemory<T> : Memory<T> {
    operator fun set(address: Int, value: T)
}

@OptIn(ExperimentalUnsignedTypes::class)
class RAM(size: Int) : MutableMemory<UByte> {
    private val data = UByteArray(size)

    override operator fun get(address: Int): UByte {
        return data[address]
    }

    override operator fun set(address: Int, value: UByte) {
        data[address] = value
    }

    fun toUByteArray(): UByteArray {
        return data
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class RegisterFile(size: Int) : MutableMemory<UShort> {
    private val data = UShortArray(size)

    override operator fun get(address: Int): UShort {
        return data[address]
    }

    override operator fun set(address: Int, value: UShort) {
        data[address] = value
    }

    fun toShortArray(): UShortArray {
        return data
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class Cpu {
    val memory: Memory<UByte> field = RAM(65536)
    val regs: Memory<UShort> field = RegisterFile(8)
    private var PC: UShort = 0u
    private var SP: UShort = 0xFFFEu
    private var zeroFlag: Boolean = false
    private var carryFlag: Boolean = false
    private var negativeFlag: Boolean = false
    private var running: Boolean = true

    fun fetch(): UShort {
        val low = memory[PC.toInt()].toUInt()
        val high = memory[(PC + 1u).toInt()].toUInt()
        PC = (PC + 2u).toUShort()
        return ((high shl 8) or low).toUShort()
    }

    fun push(value: UShort) {
        SP = (SP - 2u).toUShort()
        val addr = SP.toInt() and 0xFFFF
        memory[addr] = (value.toInt() and 0xFF).toUByte()
        memory[addr + 1] = ((value.toInt() shr 8) and 0xFF).toUByte()
    }

    fun pop(): UShort {
        val addr = SP.toInt() and 0xFFFF
        val low = memory[addr].toUInt()
        val high = memory[addr + 1].toUInt()
        val value = ((high shl 8) or low).toUShort()
        SP = (SP + 2u).toUShort()
        return value
    }

    fun writeMemory(address: Int, value: UByte) {
        memory[address] = value
    }

    context(_: IsaDebugger)
    fun regs(): UShortArray {
        return regs.toShortArray()
    }

    fun readReg(index: Int): UShort {
        return if (index == 0) 0u else regs[index]
    }

    fun writeReg(index: Int, value: UShort) {
        if (index != 0) {
            regs[index] = value
        }
    }

    fun updateCarry(carry: Boolean) {
        carryFlag = carry
    }

    fun updateZeroNegative(value: UShort) {
        zeroFlag = value == 0.toUShort()
        negativeFlag = (value.toInt() and 0x8000) != 0
    }

    fun pc(update: (UShort) -> UShort): UShort {
        PC = update(PC)
        return PC
    }

    fun sp(update: (UShort) -> UShort): UShort {
        SP = update(SP)
        return SP
    }

    fun startRunning() {
        running = true
    }

    fun stopRunning() {
        running = false
    }

    fun isRunning(): Boolean {
        return running
    }
}