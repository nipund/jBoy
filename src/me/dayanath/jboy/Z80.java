package me.dayanath.jboy;

public class Z80 {
	/**
	 * Clock variables
	 */
	private int m, t, r_m, r_t;

	/**
	 * 8-bit registers - a, b, c, d, e, h, l, f;
	 */

	private int[] reg_8 = { 0, 0, 0, 0, 0, 0, 0, 0 };

	/**
	 * 16-bit registers
	 */
	private int pc, sp;

	private void ADD(int reg0, int reg1) {
		/*reg_8[dest] += reg_8[add];
		reg_8[7] = 0;
		if ((reg_8[dest] & 255) == 0)
			reg_8[7] |= 0x80;
		if (reg_8[dest] > 255)
			reg_8[7] |= 0x10;
		reg_8[dest] &= 255;
		m = 1;
		t = 4;*/
	}

	private void NOP() {
		m = 1;
		t = 4;
	}

	private void LDXXnn(int reg0, int reg1) {
		reg_8[reg1] = MMU.rb(pc);
		reg_8[reg0] = MMU.rb(pc + 1);
		pc += 2;
		m = 3;
		t = 12;
	}

	private void LDXXmA(int reg0, int reg1) {
		MMU.wb((reg_8[reg0] << 8) + reg_8[reg1], reg_8[0]);
		m = 2;
		t = 8;
	}

	private void INCXX(int reg0, int reg1) {
		reg_8[reg1] = (reg_8[reg1] + 1) & 255;
		if (reg_8[reg1] == 0)
			reg_8[reg0] = (reg_8[reg0] + 1) & 255;
		m = 1;
		t = 4;
	}
	
	private void DECXX(int reg0, int reg1) {
		reg_8[reg1] = (reg_8[reg1] - 1) & 255;
		if (reg_8[reg1] == 255)
			reg_8[reg0] = (reg_8[reg0] - 1) & 255;
		m = 1;
		t = 4;
	}

	private void INCr_X(int reg) {
		reg_8[reg]++;
		reg_8[reg] &= 255;
		fz(reg_8[reg]);
		m = 1;
		t = 4;
	}

	private void DECr_X(int reg) {
		reg_8[reg]--;
		reg_8[reg] &= 255;
		fz(reg_8[reg]);
		m = 1;
		t = 4;
	}
	
	private void LDrn_X(int reg) {
		reg_8[reg] = MMU.rb(pc);
		pc++;
		m = 2;
		t = 8;
	}
	
	private void RLCA() {
		int ci = (reg_8[0] & 0x80) != 0 ? 1 : 0;
		int co = (reg_8[0] & 0x80) != 0 ? 0x10 : 0;
		reg_8[0] = (reg_8[0] << 1) + ci;
		reg_8[0] &= 255;
		reg_8[7] = (reg_8[7] & 0xEF) + co;
		m = 1;
		t = 4;
	}
	
	private void RRCA() {
		int ci = (reg_8[0] & 1) != 0 ? 0x80 : 0;
		int co = (reg_8[0] & 1) != 0 ? 0x10 : 0;
		reg_8[0] = (reg_8[0] >> 1) + ci;
		reg_8[0] &= 255;
		reg_8[7] = (reg_8[7] & 0xEF) + co;
		m = 1;
		t = 4;
	}
	
	private void LDnnSP() {
		MMU.ww((MMU.rb(pc) << 8) + MMU.rb(pc+1), sp);
		pc += 2;
		m = 5;
		t = 20;
	}
	
	private void ADDHLXX(int reg0, int reg1) {
		int hl = (reg_8[5] << 8) + reg_8[6];
		hl += (reg_8[reg0] << 8) + reg_8[reg1];
		if(hl > 65535) {
			reg_8[7] |= 0x10;
		} else {
			reg_8[7] &= 0xEF;
		}
		reg_8[5] = (hl >> 8) & 255;
		reg_8[6] = hl & 255;
		m = 3;
		t = 12;
	}
	
	private void LDAXXm(int reg0, int reg1) {
		reg_8[0] = MMU.rb((reg_8[reg0] << 8) + reg_8[reg1]);
		m = 2;
		t = 8;
	}
	
	/*private void DJNZn() {
		int i = MMU.rb(Z80._r.pc);
		if(i>127) i=-((~i+1)&255);
		Z80._r.pc++;
		Z80._r.m=2;
		Z80._r.t=8;
		Z80._r.b--;
		if(Z80._r.b) {
			Z80._r.pc+=i;
			Z80._r.m++;
			Z80._r.t+=4;
		}
	}*/
	
	private void STOP() {
		pc++;
		m = 2;
		t = 8;
	}
	
	private void fz(int i) {
		reg_8[7] = 0;
		if ((i & 255) != 0)
			reg_8[7] |= 128;
	}

	private void fz(int i, int as) {
		fz(i);
		reg_8[7] |= (as == 0 ? 0x40 : 0);
	}

	private void op(int opcode) {
		switch (opcode & 0xFF) {
		case 0x00:
			NOP();
			break;
		case 0x01:
			LDXXnn(1, 2); // LDBCnn
			break;
		case 0x02:
			LDXXmA(1, 2); // LDBCmA
			break;
		case 0x03:
			INCXX(1, 2); // INCBC
			break;
		case 0x04:
			INCr_X(1); // INCr_b
			break;
		case 0x05:
			DECr_X(1); // DECr_b
			break;
		case 0x06:
			LDrn_X(1); // LDrn_b
			break;
		case 0x07:
			RLCA();
			break;
		case 0x08:
			LDnnSP();
			break;
		case 0x09:
			ADDHLXX(1, 2);
			break;
		case 0x0A:
			LDAXXm(1, 2);
			break;
		case 0x0B:
			DECXX(1, 2);
			break;
		case 0x0C:
			INCr_X(2);
			break;
		case 0x0D:
			DECr_X(2);
			break;
		case 0x0E:
			LDrn_X(2);
			break;
		case 0x0F:
			RRCA();
			break;
		case 0x10:
			STOP();
			break;
		case 0x11:
			LDXXnn(3, 4);
			break;
		case 0x12:
			LDXXmA(3, 4);
			break;
		case 0x13:
			INCXX(3, 4);
			break;
		case 0x14:
			INCr_X(3);
			break;
		case 0x15:
			DECr_X(3);
			break;
		case 0x16:
			break;
		case 0x17:
			break;
		case 0x18:
			break;
		case 0x19:
			break;
		case 0x1A:
			break;
		case 0x1B:
			break;
		case 0x1C:
			break;
		case 0x1D:
			break;
		case 0x1E:
			break;
		case 0x1F:
			break;
		case 0x20:
			break;
		case 0x21:
			break;
		case 0x22:
			break;
		case 0x23:
			break;
		case 0x24:
			break;
		case 0x25:
			break;
		case 0x26:
			break;
		case 0x27:
			break;
		case 0x28:
			break;
		case 0x29:
			break;
		case 0x2A:
			break;
		case 0x2B:
			break;
		case 0x2C:
			break;
		case 0x2D:
			break;
		case 0x2E:
			break;
		case 0x2F:
			break;
		case 0x30:
			break;
		case 0x31:
			break;
		case 0x32:
			break;
		case 0x33:
			break;
		case 0x34:
			break;
		case 0x35:
			break;
		case 0x36:
			break;
		case 0x37:
			break;
		case 0x38:
			break;
		case 0x39:
			break;
		case 0x3A:
			break;
		case 0x3B:
			break;
		case 0x3C:
			break;
		case 0x3D:
			break;
		case 0x3E:
			break;
		case 0x3F:
			break;
		case 0x40:
			break;
		case 0x41:
			break;
		case 0x42:
			break;
		case 0x43:
			break;
		case 0x44:
			break;
		case 0x45:
			break;
		case 0x46:
			break;
		case 0x47:
			break;
		case 0x48:
			break;
		case 0x49:
			break;
		case 0x4A:
			break;
		case 0x4B:
			break;
		case 0x4C:
			break;
		case 0x4D:
			break;
		case 0x4E:
			break;
		case 0x4F:
			break;
		case 0x50:
			break;
		case 0x51:
			break;
		case 0x52:
			break;
		case 0x53:
			break;
		case 0x54:
			break;
		case 0x55:
			break;
		case 0x56:
			break;
		case 0x57:
			break;
		case 0x58:
			break;
		case 0x59:
			break;
		case 0x5A:
			break;
		case 0x5B:
			break;
		case 0x5C:
			break;
		case 0x5D:
			break;
		case 0x5E:
			break;
		case 0x5F:
			break;
		case 0x60:
			break;
		case 0x61:
			break;
		case 0x62:
			break;
		case 0x63:
			break;
		case 0x64:
			break;
		case 0x65:
			break;
		case 0x66:
			break;
		case 0x67:
			break;
		case 0x68:
			break;
		case 0x69:
			break;
		case 0x6A:
			break;
		case 0x6B:
			break;
		case 0x6C:
			break;
		case 0x6D:
			break;
		case 0x6E:
			break;
		case 0x6F:
			break;
		case 0x70:
			break;
		case 0x71:
			break;
		case 0x72:
			break;
		case 0x73:
			break;
		case 0x74:
			break;
		case 0x75:
			break;
		case 0x76:
			break;
		case 0x77:
			break;
		case 0x78:
			break;
		case 0x79:
			break;
		case 0x7A:
			break;
		case 0x7B:
			break;
		case 0x7C:
			break;
		case 0x7D:
			break;
		case 0x7E:
			break;
		case 0x7F:
			break;
		case 0x80:
			break;
		case 0x81:
			break;
		case 0x82:
			break;
		case 0x83:
			break;
		case 0x84:
			break;
		case 0x85:
			break;
		case 0x86:
			break;
		case 0x87:
			break;
		case 0x88:
			break;
		case 0x89:
			break;
		case 0x8A:
			break;
		case 0x8B:
			break;
		case 0x8C:
			break;
		case 0x8D:
			break;
		case 0x8E:
			break;
		case 0x8F:
			break;
		case 0x90:
			break;
		case 0x91:
			break;
		case 0x92:
			break;
		case 0x93:
			break;
		case 0x94:
			break;
		case 0x95:
			break;
		case 0x96:
			break;
		case 0x97:
			break;
		case 0x98:
			break;
		case 0x99:
			break;
		case 0x9A:
			break;
		case 0x9B:
			break;
		case 0x9C:
			break;
		case 0x9D:
			break;
		case 0x9E:
			break;
		case 0x9F:
			break;
		case 0xA0:
			break;
		case 0xA1:
			break;
		case 0xA2:
			break;
		case 0xA3:
			break;
		case 0xA4:
			break;
		case 0xA5:
			break;
		case 0xA6:
			break;
		case 0xA7:
			break;
		case 0xA8:
			break;
		case 0xA9:
			break;
		case 0xAA:
			break;
		case 0xAB:
			break;
		case 0xAC:
			break;
		case 0xAD:
			break;
		case 0xAE:
			break;
		case 0xAF:
			break;
		case 0xB0:
			break;
		case 0xB1:
			break;
		case 0xB2:
			break;
		case 0xB3:
			break;
		case 0xB4:
			break;
		case 0xB5:
			break;
		case 0xB6:
			break;
		case 0xB7:
			break;
		case 0xB8:
			break;
		case 0xB9:
			break;
		case 0xBA:
			break;
		case 0xBB:
			break;
		case 0xBC:
			break;
		case 0xBD:
			break;
		case 0xBE:
			break;
		case 0xBF:
			break;
		case 0xC0:
			break;
		case 0xC1:
			break;
		case 0xC2:
			break;
		case 0xC3:
			break;
		case 0xC4:
			break;
		case 0xC5:
			break;
		case 0xC6:
			break;
		case 0xC7:
			break;
		case 0xC8:
			break;
		case 0xC9:
			break;
		case 0xCA:
			break;
		case 0xCB:
			break;
		case 0xCC:
			break;
		case 0xCD:
			break;
		case 0xCE:
			break;
		case 0xCF:
			break;
		case 0xD0:
			break;
		case 0xD1:
			break;
		case 0xD2:
			break;
		case 0xD3:
			break;
		case 0xD4:
			break;
		case 0xD5:
			break;
		case 0xD6:
			break;
		case 0xD7:
			break;
		case 0xD8:
			break;
		case 0xD9:
			break;
		case 0xDA:
			break;
		case 0xDB:
			break;
		case 0xDC:
			break;
		case 0xDD:
			break;
		case 0xDE:
			break;
		case 0xDF:
			break;
		case 0xE0:
			break;
		case 0xE1:
			break;
		case 0xE2:
			break;
		case 0xE3:
			break;
		case 0xE4:
			break;
		case 0xE5:
			break;
		case 0xE6:
			break;
		case 0xE7:
			break;
		case 0xE8:
			break;
		case 0xE9:
			break;
		case 0xEA:
			break;
		case 0xEB:
			break;
		case 0xEC:
			break;
		case 0xED:
			break;
		case 0xEE:
			break;
		case 0xEF:
			break;
		case 0xF0:
			break;
		case 0xF1:
			break;
		case 0xF2:
			break;
		case 0xF3:
			break;
		case 0xF4:
			break;
		case 0xF5:
			break;
		case 0xF6:
			break;
		case 0xF7:
			break;
		case 0xF8:
			break;
		case 0xF9:
			break;
		case 0xFA:
			break;
		case 0xFB:
			break;
		case 0xFC:
			break;
		case 0xFD:
			break;
		case 0xFE:
			break;
		case 0xFF:
			break;
		}
	}
}
