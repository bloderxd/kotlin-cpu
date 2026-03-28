package com.bloder

sealed class AsmLine

data class Instruction(
    val name: String,
    val args: List<String>
) : AsmLine()

data class Label(val name: String) : AsmLine()

fun assemble(program: String): List<UShort> {
    val lines = parse(program)
    val labels = resolveLabels(lines)

    val output = mutableListOf<UShort>()
    var pc = 0

    for (line in lines) {
        if (line is Instruction) {

            val inst = when (line.name) {

                "ADD" -> Tiny16Encoder.ADD(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "SUB" -> Tiny16Encoder.SUB(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "AND" -> Tiny16Encoder.AND(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "OR" -> Tiny16Encoder.OR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "XOR" -> Tiny16Encoder.XOR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "MOV" -> Tiny16Encoder.MOV(
                    parseReg(line.args[0]),
                    parseReg(line.args[1])
                )

                "SHL" -> Tiny16Encoder.SHL(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "SHR" -> Tiny16Encoder.SHR(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseReg(line.args[2])
                )

                "LI" -> Tiny16Encoder.LI(
                    parseReg(line.args[0]),
                    parseImm(line.args[1], labels, pc)
                )

                "LUI" -> Tiny16Encoder.LUI(
                    parseReg(line.args[0]),
                    parseImm(line.args[1], labels, pc)
                )

                "LOAD" -> Tiny16Encoder.LOAD(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "STORE" -> Tiny16Encoder.STORE(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "ADDI" -> Tiny16Encoder.ADDI(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "JMP" -> Tiny16Encoder.JMP(
                    parseImm(line.args[0], labels, pc)
                )

                "RET" -> Tiny16Encoder.RET()

                "BEQ" -> Tiny16Encoder.BEQ(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "BNE" -> Tiny16Encoder.BNE(
                    parseReg(line.args[0]),
                    parseReg(line.args[1]),
                    parseImm(line.args[2], labels, pc)
                )

                "CALL" -> Tiny16Encoder.CALL(
                    parseImm(line.args[0], labels, pc)
                )

                "PUSH" -> Tiny16Encoder.PUSH(
                    parseReg(line.args[0])
                )

                "POP" -> Tiny16Encoder.POP(
                    parseReg(line.args[0])
                )

                "NOP" -> Tiny16Encoder.NOP()

                "HALT" -> Tiny16Encoder.HALT()

                else -> error("Unknown instruction: ${line.name}")
            }

            output.add(inst)
            pc += 2
        }
    }

    return output
}

fun parse(program: String): List<AsmLine> {
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

fun resolveLabels(lines: List<AsmLine>): Map<String, Int> {
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

fun parseReg(r: String): Int {
    return r.removePrefix("R").toInt()
}

fun parseImm(value: String, labels: Map<String, Int>, pc: Int): Int {
    return when {
        value.matches(Regex("-?\\d+")) -> value.toInt()

        labels.containsKey(value) -> {
            val target = labels[value]!!
            (target - (pc + 2)) / 2
        }

        else -> error("Unknown immediate: $value")
    }
}