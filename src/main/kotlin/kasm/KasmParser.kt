package com.bloder.kasm

private sealed class AsmLine

private data class Instruction(
    val name: String,
    val args: List<String>
) : AsmLine()

private data class Label(val name: String) : AsmLine()

class KasmParser private constructor(private val encoder: KasmEncoder) {

    companion object {
        operator fun invoke(program: String): List<UShort> {
            val encoder = KasmEncoder
            return KasmParser(encoder).parse(program)
        }
    }

    fun parse(program: String): List<UShort> {
        return context(encoder) { assemble(program = program) }
    }
}

context(encoder: KasmEncoder)
private fun assemble(program: String): List<UShort> {
    val lines = parse(program)
    val labels = resolveLabels(lines)

    val output = mutableListOf<UShort>()
    var pc = 0

    for (line in lines) {
        if (line is Instruction) {

            val inst = when (line.name) {

                "ADD" -> encoder.ADD(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "SUB" -> encoder.SUB(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "AND" -> encoder.AND(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "OR" -> encoder.OR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "XOR" -> encoder.XOR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "MOV" -> encoder.MOV(
                    parseReg(line.args[0]),
                    parseReg(line.args[1])
                )

                "SHL" -> encoder.SHL(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "SHR" -> encoder.SHR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "LI" -> encoder.LI(
                    parseReg(line.args[0]),
                    parseImm(line.args[1], labels, pc)
                )

                "LUI" -> encoder.LUI(
                    parseReg(line.args[0]),
                    parseImm(line.args[1], labels, pc)
                )

                "LOAD" -> encoder.LOAD(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "STORE" -> encoder.STORE(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "ADDI" -> encoder.ADDI(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "JMP" -> encoder.JMP(
                    parseImm(line.args[0], labels, pc)
                )

                "RET" -> encoder.RET()

                "BEQ" -> encoder.BEQ(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "BNE" -> encoder.BNE(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "CALL" -> encoder.CALL(
                    parseImm(line.args[0], labels, pc)
                )

                "PUSH" -> encoder.PUSH(
                    parseReg(line.args[0])
                )

                "POP" -> encoder.POP(
                    parseReg(line.args[0])
                )

                "NOP" -> encoder.NOP()

                "HALT" -> encoder.HALT()

                else -> error("Unknown instruction: ${line.name}")
            }

            output.add(inst)
            pc += 2
        }
    }

    return output
}

private fun parse(program: String): List<AsmLine> {
    return program.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { line ->
            if (line.endsWith(":")) {
                Label(line.dropLast(1))
            } else {
                val parts = line.split(" ", ",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                Instruction(parts[0].uppercase(), parts.drop(1))
            }
        }
}

private fun resolveLabels(lines: List<AsmLine>): Map<String, Int> {
    val labels = mutableMapOf<String, Int>()
    var pc = 0

    for (line in lines) {
        when (line) {
            is Label -> labels[line.name] = pc
            is Instruction -> pc += 2
        }
    }

    return labels
}

private fun parseReg(r: String): Int {
    return r.removePrefix("R").toInt()
}

private fun parseImm(value: String, labels: Map<String, Int>, pc: Int): Int {
    return when {
        value.matches(Regex("-?\\d+")) -> value.toInt()

        labels.containsKey(value) -> {
            val target = labels[value]!!
            (target - (pc + 2)) / 2
        }

        else -> error("Unknown immediate: $value")
    }
}