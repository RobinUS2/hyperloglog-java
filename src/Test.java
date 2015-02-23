/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author robin
 */
public class Test {
    public static void main(String[] args) {
        try {
            HyperLogLog hll = new HyperLogLog(64);
            hll.Add(12345);
            System.out.println(hll.Count());
            hll.Add(1234567891);
            hll.Add(12345);
            System.out.println(hll.Count());
            hll.Add(1234567891);
            hll.Add(12345);
            System.out.println(hll.Count());
            
            System.out.println(hll.ToJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
