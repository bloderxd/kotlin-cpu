package com.bloder.kasm

object KasmReader {

    operator fun invoke(kasm: String): String {
        val program = object {}.javaClass.classLoader.getResource(kasm)?.readText() ?: ""
        return readKasm(program)
    }

    private fun readKasm(kasm: String): String {
        return kasm
            .lineSequence()
            .map { line -> line.substringBefore(';').trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
}