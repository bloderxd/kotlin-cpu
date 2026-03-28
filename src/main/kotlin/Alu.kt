package com.bloder

enum class AluInstruction(val operation: Int) {
    ADD(0b000),
    SUB(0b001),
    AND(0b010),
    OR(0b011),
    XOR(0b100),
    MOV(0b101),
    SHL(0b110),
    SHR(0b111);
}

interface Alu {

    context(decoder: IsaDecoder)
    fun execute(
        operation: AluInstruction,
        a: UShort,
        b: UShort,
        updateCarryFlag: (Boolean) -> Unit
    ): UShort {
        return when(operation) {
            AluInstruction.ADD -> {
                val r = a.toUInt() + b.toUInt()
                updateCarryFlag(r > 0xFFFFu)
                r.toUShort()
            }
            AluInstruction.SUB -> {
                val r = a.toUInt() - b.toUInt()
                updateCarryFlag(a.toUInt() < b.toUInt())
                r.toUShort()
            }
            AluInstruction.AND -> {
                updateCarryFlag(false)
                a and b
            }
            AluInstruction.OR -> {
                updateCarryFlag(false)
                a or b
            }
            AluInstruction.XOR -> {
                updateCarryFlag(false)
                a xor b
            }
            AluInstruction.MOV -> {
                updateCarryFlag(false)
                a
            }
            AluInstruction.SHL -> {
                val shift = (b.toInt() and 0xF)
                val r = a.toUInt() shl shift
                updateCarryFlag((a.toUInt() shl (shift - 1)) and 0x8000u != 0u)
                r.toUShort()
            }
            AluInstruction.SHR -> {
                val shift = (b.toInt() and 0xF)
                updateCarryFlag((a.toUInt() shr (shift - 1)) and 1u != 0u)
                val r = a.toUInt() shr shift
                r.toUShort()
            }
        }
    }

    companion object : Alu
}