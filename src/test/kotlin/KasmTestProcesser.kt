package com.bloder

import com.bloder.kasm.KasmParser
import com.bloder.kasm.KasmReader

object KasmTestProcesser {

    fun runExample(kasmFile: String): Cpu {
        val source = KasmReader(kasmFile)
        val program = KasmParser(source)
        val isa = Isa.loadProgram(program = program, debugger = IsaDebugger.Logger)
        isa.run()
        return isa.cpu
    }
}

