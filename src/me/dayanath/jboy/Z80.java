package me.dayanath.jboy;

public class Z80 {
	/**
	 * Clock variables
	 */
	private int m, t, r_m, r_t;
	
	/**
	 * 8-bit registers - a, b, c, d, e, h, l, f;
	 */
	
	private int[] reg_8 = {0, 0, 0, 0, 0, 0, 0, 0};
	
	/**
	 * 16-bit registers
	 */
	private int pc, sp;
	
	private void ADD(int dest, int add) {
		reg_8[dest] += reg_8[add];
        reg_8[7] = 0;
        if((reg_8[dest] & 255) == 0)
        	reg_8[7] |= 0x80;				
        if(reg_8[dest] > 255)
        	reg_8[7] |= 0x10;				
        reg_8[dest] &= 255;
        m = 1;
        t = 4;
	}
	
	private void NOP() {
		m = 1;
		t = 4;
	}
	
	private void LDXXnn(int reg0, int reg1) {
		reg_8[reg1] = MMU.rb(pc);
		reg_8[reg0] = MMU.rb(pc+1);
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
		if(reg_8[reg1] == 0)
			reg_8[reg0] = (reg_8[reg0] + 1) & 255;
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
	
	private void fz(int i) {
		reg_8[7] = 0;
		if((i & 255) != 0)
			reg_8[7] |= 128;
	}
	
	private void fz(int i, int as) {
		fz(i);
		reg_8[7] |= (as == 0 ? 0x40 : 0);
	}
	
	private void op(int opcode) {
		switch(opcode) {
			case 0x00:
				NOP();
				break;
			case 0x01:
				LDXXnn(1, 2); //LDBCnn
				break;
			case 0x02:
				LDXXmA(1, 2); //LDBCmA
				break;
			case 0x03:
				INCXX(1, 2); //INCBC
				break;
			case 0x04:
				INCr_X(1); //INCr_b
				break;
		}
	}
}
