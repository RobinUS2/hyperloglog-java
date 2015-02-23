/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author robin
 */
public class HyperLogLog {

    private int m; // Number of registers
    private int b;// Number of bits used to determine register index
    private double alpha;// Bias correction constant
    private int[] registers; // HLL registers

    public static double EXP32 = Math.pow(2, 32);

    public HyperLogLog(int registers) throws Exception {
        // Validate power of 2
        if ((registers & (registers - 1)) != 0) {
            throw new Exception("number of registers " + String.valueOf(registers) + " not a power of two");
        }

        this.m = registers;
        this.b = (int) Math.ceil(Math.log((double) registers) / Math.log(2));
        this.alpha = getAlpha();
        reset();
    }

    private double getAlpha() {
        switch (this.m) {
            case 16:
                return 0.673D;
            case 32:
                return 0.697D;
            case 64:
                return 0.709D;
            default:
                return 0.7213 / (1.0 + 1.079 / (double) m);
        }
    }

    private void reset() {
        this.registers = new int[this.m];
    }

    private int rho(int val, int max) {
        int r = 1;
        while ((val & 0x80000000) == 0 && r <= max) {
            r++;
            val <<= 1;
        }
        return r;
    }

    public void Add(int val) {
        int k = 32 - this.b;

        int r = rho(val << this.b, k);

        int j = val >> k;

        if (r > this.registers[j]) {
            this.registers[j] = r;
        }
    }

    public long Count() {
        double sum = 0.0D;
        double mDouble = (double) this.m;

        for (int register : registers) {
            sum += (1.0D / Math.pow(2.0D, (double) register));
        }
        
        double estimate = alpha * mDouble * mDouble / sum;
        
        if (estimate <= 5.0/2.0*m) {
            // Small range correction
            int v = 0;
            for (int register : registers) {
                if (register == 0) {
                    v++;
                }
            }
            
            if (v > 0) {
                estimate = m * Math.log(m / (double)v);
            }
        } else if (estimate > 1.0/30.0*EXP32) {
            // Large range correction
            estimate = -EXP32 * Math.log(1-estimate/EXP32);
        }
        
        return (long)estimate;
    }
}

/*

// Merge another HyperLogLog into this one. The number of registers in
// each must be the same.
// Add up two hyperlogslogs, basically the UNION
func (h1 *HyperLogLog) Merge(h2 *HyperLogLog) error {
	if h1.m != h2.m {
		return fmt.Errorf("number of registers doesn't match: %d != %d",
			h1.m, h2.m)
	}
	for j, r := range h2.registers {
		if r > h1.registers[j] {
			h1.registers[j] = r
		}
	}
	return nil
}
*/