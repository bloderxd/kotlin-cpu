package com.bloder.kasm

import com.bloder.Cpu

interface KasmLoader {

    operator fun invoke(program: List<UShort>): Cpu {
        val cpu = Cpu()
        var address = 0
        for (inst in program) {
            cpu.writeMemory(address = address, value = (inst.toInt() and 0xFF).toUByte())
            cpu.writeMemory(address = address + 1, value = ((inst.toInt() shr 8) and 0xFF).toUByte())
            address += 2
        }
        return cpu
    }

    companion object : KasmLoader
}