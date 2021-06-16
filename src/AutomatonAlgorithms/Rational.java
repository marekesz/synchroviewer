package AutomatonAlgorithms;

import java.math.BigInteger;

class Rational {

    private BigInteger nominator;
    private BigInteger denominator;

    public Rational() {
        this(new BigInteger("0"), new BigInteger("1"));
    }

    public Rational(BigInteger nominator) {
        this(nominator, new BigInteger("1"));
    }

    public Rational(BigInteger nominator, BigInteger denominator) {
        this.nominator = nominator;
        this.denominator = denominator;
        recalcualte();
    }

    public Rational(int nominator, int denominator) {
        this(new BigInteger(Integer.toString(nominator)), new BigInteger(Integer.toString(denominator)));
    }

    public Rational(int nominator) {
        this(new BigInteger(Integer.toString(nominator)));
    }

    public void recalcualte() {
        BigInteger gcd = nominator.gcd(denominator);
        this.nominator = nominator.divide(gcd);
        this.denominator = denominator.divide(gcd);
    }

    public void setNominator(BigInteger nominator) {
        this.nominator = nominator;
    }

    public void setDenominator(BigInteger denominator) {
        this.denominator = denominator;
    }

    public BigInteger getNominator() {
        return this.nominator;
    }

    public BigInteger getDenominator() {
        return this.denominator;
    }

    public Rational add(Rational other) {
        BigInteger denomGCD = this.denominator.gcd(other.denominator);
        BigInteger thisMultiply = other.denominator.divide(denomGCD);
        BigInteger otherMultiply = this.denominator.divide(denomGCD);
        BigInteger resultNominator = this.nominator.multiply(thisMultiply).add(other.nominator.multiply(otherMultiply));
        BigInteger resultDenominator = this.denominator.multiply(otherMultiply);
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

    public double doubleValue() {
        return nominator.doubleValue() / denominator.doubleValue();
    }

    public String toString() {
        return nominator.toString() + "/" + denominator.toString();
    }
}