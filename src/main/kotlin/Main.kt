package com.bloder

import com.bloder.kasm.KasmLoader
import com.bloder.kasm.KasmParser
import com.bloder.kasm.KasmReader

private const val KASM = "call.kasm"

fun main() {
    val source = KasmReader(KASM)
    val program = KasmParser(source)
    val cpu = KasmLoader(program)
    val isa = Isa.create(cpu = cpu, debugger = IsaDebugger.Logger)
    isa.run()
}