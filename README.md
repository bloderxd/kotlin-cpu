# kotlin-cpu

This project is part of a series of 2 blog posts and it's better explained there:

[Part 1: How a CPU Works](https://bloder.io/cpu-from-scratch-part-1)

[Part 2: Building the CPU](https://bloder.io/cpu-from-scratch-part-2)

A small **16-bit CPU emulator** written in Kotlin, with a tiny assembler (`.kasm`) and sample programs.

The project models a complete fetch-decode-execute loop with registers, RAM, stack, control flow, arithmetic/logic instructions, and an assembler that supports labels.

## What This Project Implements

- A 16-bit CPU core (`Cpu`) with:
  - 64KB RAM
  - 8 general-purpose 16-bit registers (`R0..R7`)
  - program counter (`PC`) and stack pointer (`SP`)
  - flags: zero, carry, negative
- An ISA executor (`Isa`) that decodes 16-bit instructions and dispatches operations.
- An ALU (`Alu`) with arithmetic, bitwise, move, and shift operations.
- A simple assembler pipeline:
  - `KasmReader`: strips comments/blank lines
  - `KasmParser`: parses labels and instructions
  - `KasmEncoder`: encodes instructions to 16-bit words
  - `KasmLoader`: writes words into emulated memory
- Example programs in `src/main/resources/*.kasm` and tests in `src/test/kotlin`.

## CPU Model Details

- **Word size**: 16-bit instructions and 16-bit register values.
- **Endianness in memory**: little-endian for 16-bit values (`low byte`, then `high byte`).
- **Instruction size**: every instruction is 2 bytes.
- **PC behavior**:
  - `fetch()` reads memory at `PC` and `PC+1`, then increments `PC` by 2.
  - branch/jump/call offsets are applied as `offset * 2` (word-based offsets).
- **Register file**: 8 registers.
  - `R0` is hard-wired to zero (reads as zero, writes ignored).
- **Stack**:
  - `SP` starts at `0xFFFE`.
  - stack grows downward.
  - `push` decrements `SP` by 2 then stores a 16-bit value.
  - `pop` reads 16-bit value at `SP` then increments `SP` by 2.

## Instruction Encoding

### Base 16-bit layout

Bits are numbered from Most Significant Bit to Least Significant Bit:

- `opcode = bits[15..12]`
- register / immediate fields depend on instruction format

### Formats

- **R-type (ALU)**
  - `opcode[15..12] rd[11..9] rs1[8..6] rs2[5..3] aluOp[2..0]`
- **I-type (6-bit immediate)**
  - `opcode[15..12] rd[11..9] rs1[8..6] imm6[5..0]`
- **J-type (12-bit immediate)**
  - `opcode[15..12] imm12[11..0]`
- **Stack format**
  - `opcode[15..12] reg[11..9]` (remaining bits unused)

### Immediate semantics

- `imm6` is sign-extended from 6 bits: range `-32..31`.
- `imm12` is sign-extended from 12 bits: range `-2048..2047`.
- Label immediates are assembler-generated as relative word offsets:
  - `offset = (target - (pc + 2)) / 2`

## ISA Table

| Mnemonic | Opcode | Format | Semantics |
|---|---:|---|---|
| `ADD rd, rs1, rs2` | `0x0` + `aluOp=000` | R | `rd = rs1 + rs2` |
| `SUB rd, rs1, rs2` | `0x0` + `aluOp=001` | R | `rd = rs1 - rs2` |
| `AND rd, rs1, rs2` | `0x0` + `aluOp=010` | R | `rd = rs1 & rs2` |
| `OR rd, rs1, rs2` | `0x0` + `aluOp=011` | R | `rd = rs1 \| rs2` |
| `XOR rd, rs1, rs2` | `0x0` + `aluOp=100` | R | `rd = rs1 ^ rs2` |
| `MOV rd, rs1` | `0x0` + `aluOp=101` | R | `rd = rs1` |
| `SHL rd, rs1, rs2` | `0x0` + `aluOp=110` | R | `rd = rs1 << (rs2 & 0xF)` |
| `SHR rd, rs1, rs2` | `0x0` + `aluOp=111` | R | `rd = rs1 >> (rs2 & 0xF)` (logical) |
| `ADDI rd, rs1, imm6` | `0x1` | I | `rd = rs1 + imm6` |
| `LI rd, imm6` | `0x2` | I | `rd = imm6` (sign-extended to 16-bit) |
| `LUI rd, imm6` | `0x3` | I | `rd = imm6 << 8` |
| `LOAD rd, base, off6` | `0x4` | I | `rd = mem16[(base + off6) & 0xFFFE]` |
| `STORE rs, base, off6` | `0x5` | I | `mem16[(base + off6) & 0xFFFE] = rs` |
| `BEQ rs1, rs2, off6` | `0x6` | I | if `rs1 == rs2`, `PC += off6 * 2` |
| `BNE rs1, rs2, off6` | `0x7` | I | if `rs1 != rs2`, `PC += off6 * 2` |
| `JMP off12` | `0x8` | J | `PC += off12 * 2` |
| `RET` | `0x9` | J | `PC = pop()` |
| `PUSH rs` | `0xC` | Stack | `push(rs)` |
| `POP rd` | `0xD` | Stack | `rd = pop()` |
| `CALL off12` | `0xE` | J | `push(returnPC); PC += off12 * 2` |
| `HALT` | `0xF` | J | stop execution |

### Pseudo-instruction

| Mnemonic | Expansion |
|---|---|
| `NOP` | `ADD R0, R0, R0` |

## Flags

- `zeroFlag`: set when result is `0`.
- `negativeFlag`: set when result has bit `15 = 1`.
- `carryFlag`: updated by arithmetic/shift operations.

Flags are updated by ALU and `ADDI` paths; control flow in current implementation compares register values directly (`BEQ`/`BNE`) rather than checking flags.

## How Assembly Is Processed

1. `KasmReader("file.kasm")`
   - loads from classpath resources
   - removes comments (`;`) and empty lines
2. `KasmParser(source)`
   - parses instruction and label lines
   - resolves label addresses
   - converts label references to relative offsets
3. `KasmEncoder`
   - encodes each instruction into `UShort`
4. `KasmLoader(program)`
   - writes encoded words into CPU memory at address `0x0000`
5. `Isa.create(cpu).run()`
   - executes until `HALT`

## Running

### Run a sample program

The entry point is `src/main/kotlin/Main.kt` and it loads the file set in `KASM`:

```kotlin
private const val KASM = "call.kasm"
```

With the current Gradle setup, the simplest way to execute samples is from the IDE:

1. Open `src/main/kotlin/Main.kt`.
2. Change `KASM` to the program you want (for example: `"alu.kasm"`, `"beq.kasm"`, `"call.kasm"`).
3. Run `main()`.

## Example Programs

- `alu.kasm`: ALU arithmetic/bitwise/shift operations
- `mov.kasm`: register-to-register move
- `memory.kasm`: load/store with base+offset
- `lui.kasm`: high-byte immediate behavior
- `beq.kasm` / `bne.kasm`: conditional branches
- `jmp.kasm`: unconditional PC-relative jump
- `call.kasm`: `CALL` + `RET` flow
- `loop.kasm`: loop with backward branch
- `stack.kasm`: explicit `PUSH`/`POP`
