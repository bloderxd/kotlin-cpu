package com.bloder

import com.bloder.kasm.KasmLoader
import com.bloder.kasm.KasmParser
import com.bloder.kasm.KasmReader

object KasmTestProcesser {

    fun runExample(kasmFile: String): Cpu {
        val source = KasmReader(kasmFile)
        val program = KasmParser(source)
        val cpu = KasmLoader(program)
        val isa = Isa.create(cpu = cpu, debugger = IsaDebugger.Logger)
        isa.run()
        return isa.cpu
    }
}

