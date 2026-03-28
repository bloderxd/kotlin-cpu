package com.bloder

import com.bloder.kasm.KasmParser
import com.bloder.kasm.KasmReader

private const val KASM = "jmp.kasm"

fun main() {
    val source = KasmReader(KASM)
    val program = KasmParser(source)
    val isa = Isa.loadProgram(program = program, debugger = IsaDebugger.Logger)
    isa.run()
}