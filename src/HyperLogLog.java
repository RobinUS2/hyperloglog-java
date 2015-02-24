
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

        int j = val >> Math.abs(k);

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
            estimate = -EXP32 * Math.log(1.0D-estimate/EXP32);
        }
        
        return (long)Math.round(estimate);
    }
    
    public HyperLogLog Merge(HyperLogLog h1, HyperLogLog h2) throws Exception {
        if (h1.m != h2.m) {
            throw new Exception("Number of registers doesn't match: " + String.valueOf(h1.m) + " != " + String.valueOf(h2.m));
        }
        for (int i = 0; i < h2.registers.length; i++) {
            int r = h2.registers[i];
            if (r > h1.registers[i]) {
                h1.registers[i] = r;
            }
        }
        return h1;
    }
    
    public HyperLogLog Intersect(HyperLogLog h1, HyperLogLog h2) throws Exception {
        if (h1.m != h2.m) {
            throw new Exception("Number of registers doesn't match: " + String.valueOf(h1.m) + " != " + String.valueOf(h2.m));
        }
        
        // Merge
        HyperLogLog merged = Merge(h1, h2);
        
        // Placeholder for intersect
        HyperLogLog intersect = new HyperLogLog(h1.m);
        
        // Intersect
        for (int i = 0; i < h2.registers.length; i++) {
            // |A INTERSECT B| = |A| + |B| - |A UNION B|
            intersect.registers[i] = (Math.abs(h1.registers[i]) + Math.abs(h2.registers[i])) - Math.abs(merged.registers[i]);
        }
        
        return h1;
    }
    
    public String ToJson() {
        Gson g = new Gson();
        JsonObject elm = new JsonObject();
        elm.addProperty("M", this.m);
        elm.addProperty("B", this.b);
        elm.addProperty("A", this.alpha);
        JsonArray registerArr = new JsonArray();
        for (int i = 0; i < registers.length; i++) {
            registerArr.add(new JsonPrimitive(registers[i]));
        }
        elm.add("R", registerArr);
        return g.toJson(elm);
    }
}
