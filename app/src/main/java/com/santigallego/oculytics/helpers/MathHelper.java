package com.santigallego.oculytics.helpers;

/**
 * Created by santigallego on 8/21/16.
 */
public class MathHelper {

    public MathHelper() {}

    public static long gcd(long a, long b)
    {
        while (b > 0)
        {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    private static long lcm(long a, long b)
    {
        return a * (b / gcd(a, b));
    }
}
