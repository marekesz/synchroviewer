package algorithms;

import java.math.BigInteger;

public class Rational {

    private BigInteger nominator;
    private BigInteger denominator;

    public Rational() {
        this(0, 1);
    }

    public Rational(BigInteger nominator) {
        this(nominator, new BigInteger("1"));
    }

    public Rational(BigInteger nominator, BigInteger denominator) {
        this.nominator = nominator;
        this.denominator = denominator;
        recalcualte();
    }

    public Rational(String nominator, String denominator) {
        this.nominator = new BigInteger(nominator);
        this.denominator = new BigInteger(denominator);
        recalcualte();
    }

    public Rational(String nominator) {
        this(nominator, "1");
    }

    public Rational(int nominator, int denominator) {
        this(new BigInteger(Integer.toString(nominator)), new BigInteger(Integer.toString(denominator)));
    }

    public Rational(int nominator) {
        this(new BigInteger(Integer.toString(nominator)));
    }

    private void recalcualte() {
        BigInteger gcd = nominator.abs().gcd(denominator.abs());
        this.nominator = nominator.divide(gcd);
        this.denominator = denominator.divide(gcd);
    }

    public BigInteger getNominator() {
        return this.nominator;
    }

    public BigInteger getDenominator() {
        return this.denominator;
    }

    public Rational copy() {
        return new Rational(this.nominator, this.denominator);
    }

    public Rational add(Rational other) {
        if (other.nominator.compareTo(new BigInteger("0")) == 0)
            return copy();
        BigInteger denomGCD = this.denominator.abs().gcd(other.denominator.abs());
        BigInteger thisMultiply = other.denominator.divide(denomGCD);
        BigInteger otherMultiply = this.denominator.divide(denomGCD);
        BigInteger resultNominator = this.nominator.multiply(thisMultiply).add(other.nominator.multiply(otherMultiply));
        BigInteger resultDenominator = this.denominator.multiply(thisMultiply);
        return new Rational(resultNominator, resultDenominator);
    }

    public Rational multiply(Rational other) {
        return new Rational(this.nominator.multiply(other.nominator), this.denominator.multiply(other.denominator));
    }

    public Rational subtract(Rational other) {
        return add(other.multiply(new Rational(new BigInteger("-1"))));
    }

    public int compareTo(Rational other) {
        Rational subtract = subtract(other);
        int nominatorCompare = subtract.getNominator().compareTo(new BigInteger("0"));
        int denominatorCompare = subtract.getDenominator().compareTo(new BigInteger("0"));
        if (nominatorCompare == 0)
            return 0;
        if (nominatorCompare > 0 && denominatorCompare > 0)
            return 1;
        return -1;
    }

    public boolean equals(Rational other) {
        if (other.nominator.equals(new BigInteger("0")) && nominator.equals(other.nominator))
            return true;
        return nominator.equals(other.nominator) && denominator.equals(other.denominator);
    }

    public Rational abs() {
        return new Rational(this.nominator.abs(), this.denominator.abs());
    }

    public double doubleValue() {
        return nominator.doubleValue() / denominator.doubleValue();
    }

    public String toString() {
        if (this.nominator.compareTo(new BigInteger("0")) == 0)
            return "0";
        if (this.denominator.compareTo(new BigInteger("1")) == 0)
            return nominator.toString();
        return nominator.toString() + "/" + denominator.toString();
    }

    public Rational inverse() {
        if (this.nominator.equals(new BigInteger("0")))
            throw new IllegalArgumentException("Cannot inverse 0");
        return new Rational(this.denominator, this.nominator);
    }
}