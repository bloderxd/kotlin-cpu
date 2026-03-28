package com.bloder

interface IsaDebugger {

    context(decoder: IsaDecoder)
    fun Cpu.debug(opCode: OpCode, inst: UShort, step: (OpCode, UShort) -> Unit)

    data object None : IsaDebugger {

        context(decoder: IsaDecoder)
        override fun Cpu.debug(opCode: OpCode, inst: UShort, step: (OpCode, UShort) -> Unit) {
            step(opCode, inst)
        }
    }

    data object Logger : IsaDebugger {

        context(decoder: IsaDecoder)
        override fun Cpu.debug(opCode: OpCode, inst: UShort, step: (OpCode, UShort) -> Unit) {
            println(
                "[DEBUGGER] PC=${pc { it }.toString(16).padStart(4, '0')} " +
                        "INST=0x${inst.toString(16).padStart(4, '0')} " +
                        decoder.disassemble(opCode = opCode, inst = inst)
            )
            step(opCode, inst)
            println("   → ${dumpRegs()}")
        }

        private fun IsaDecoder.disassemble(opCode: OpCode, inst: UShort): String {
            return when (opCode) {
                OpCode.ALU -> {
                    val aluInstruction = decodeAluInstruction(inst)
                    val rd = decodeRd(inst)
                    val rs1 = decodeRs1(inst)
                    val rs2 = decodeRs2(inst)

                    when (aluInstruction) {
                        AluInstruction.ADD -> "ADD R$rd, R$rs1, R$rs2"
                        AluInstruction.SUB -> "SUB R$rd, R$rs1, R$rs2"
                        AluInstruction.AND -> "AND R$rd, R$rs1, R$rs2"
                        AluInstruction.OR -> "OR  R$rd, R$rs1, R$rs2"
                        AluInstruction.XOR -> "XOR R$rd, R$rs1, R$rs2"
                        AluInstruction.MOV -> "MOV R$rd, R$rs1"
                        AluInstruction.SHL -> "SHL R$rd, R$rs1, R$rs2"
                        AluInstruction.SHR -> "SHR R$rd, R$rs1, R$rs2"
                    }
                }

                OpCode.ADDI -> "ADDI R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"
                OpCode.LI -> "LI R${decodeRd(inst)}, ${decodeImm6(inst)}"
                OpCode.LUI -> "LUI R${decodeRd(inst)}, ${decodeImm6(inst)}"

                OpCode.LOAD -> "LOAD R${decodeRd(inst)}, [R${decodeRs1(inst)} + ${decodeImm6(inst)}]"
                OpCode.STORE -> "STORE R${decodeRd(inst)}, [R${decodeRs1(inst)} + ${decodeImm6(inst)}]"

                OpCode.BEQ -> "BEQ R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"
                OpCode.BNE -> "BNE R${decodeRd(inst)}, R${decodeRs1(inst)}, ${decodeImm6(inst)}"

                OpCode.JMP -> "JMP ${decodeImm12(inst)}"
                OpCode.RET -> "RET"

                OpCode.PUSH -> "PUSH R${decodeRd(inst)}"
                OpCode.POP -> "POP R${decodeRd(inst)}"

                OpCode.CALL -> "CALL ${decodeImm12(inst)}"
                OpCode.HALT -> "HALT"
            }
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun Cpu.dumpRegs(): String {
            return regs().mapIndexed { i, r -> "R$i=${r.toInt()}" }
                .joinToString(" ")
        }
    }
}