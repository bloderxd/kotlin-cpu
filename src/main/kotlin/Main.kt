package com.bloder

private const val KASM = "call.kasm"

fun main() {
    val program = KasmReader(KASM)
    val binary = assemble(program)

    val cpu = CPU()
    loadProgram(cpu, binary)

    while (cpu.running) {
        cpu.step(debug = true)
    }
}

fun loadProgram(cpu: CPU, program: List<UShort>) {
    var addr = 0

    for (inst in program) {
        cpu.memory[addr] = (inst.toInt() and 0xFF).toUByte()
        cpu.memory[addr + 1] = ((inst.toInt() shr 8) and 0xFF).toUByte()
        addr += 2
    }
}