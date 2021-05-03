package AutomatonAlgorithms;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import AutomatonModels.Automaton;
import Viewer.AutomatonHelper;

public class AlgebraicModule 
{
    private static BigInteger ZERO = new BigInteger("0");

    private static void printMatrix(int[][] matrix) {
        System.out.println("");
        for(int i = 0;i<matrix.length;i++) { 
            for(int j =0;j<matrix[0].length;j++) { 
                System.out.print(matrix[i][j]);
            }
            System.out.println("");
        }
        System.out.println("");
    }
    
    private static void printArray(int[] vector) {
        System.out.println("");
        for(int i = 0;i<vector.length;i++) { 
                System.out.print(vector[i]+" ");
        }
        System.out.println("");
    }

    private static void printBase(ArrayList<int[]> base) {
        System.out.println("base: ");
        base.forEach(v -> printArray(v));
        System.out.println("------");
    }

    // computes array L, where L[i] = dim(span({[S][w] | w in Sigma^<=i}))
    public static ArrayList<Integer> DimensionsForSubset(Automaton automaton) 
    {   
        int [] subset = automaton.getSelectedStates();
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<int []> base = new ArrayList<>();
        ArrayList<String> candidates = new ArrayList<>();
        System.out.println("Adding vector from word: ");
                        printArray(matMul(subset, wordToMatrix(automaton, "")));
                        System.out.println("to base: ");
                        printBase(base);
                        
        candidates.add("");
        base.add(matMul(subset, wordToMatrix(automaton, "")));
        ArrayList<String> newCandidates = new ArrayList<>();
        while (candidates.size() > 0) 
        {
            for (String x : candidates) 
            {
                for (int k = 0;k<automaton.getK();k++) 
                {
                    char a = AutomatonHelper.TRANSITIONS_LETTERS[k];
                    int [] vec = matMul(subset, wordToMatrix(automaton,x+a));
                    System.out.println("checking vector for word: "+x+a);
                    printArray(subset);
                    System.out.println(" X Matrix:");
                    printMatrix(wordToMatrix(automaton,x+a));
                    printArray(vec);
                    if( !dependentFromBase(base, vec) )
                    {
                        System.out.println("Adding vector from word: "+x+a);
                        printArray(vec);
                        System.out.println("to base: ");
                        printBase(base);
                        base.add(vec);
                        newCandidates.add(x+a);
                    }
                }
            }
            if(result.size() == 0 || base.size() > result.get(result.size()-1))
                result.add(base.size());
            moveArrayList(newCandidates, candidates);
            newCandidates.clear();
        }
        return result;
    }

    public static BigInteger firstNonZeroElement(BigInteger[] vector) 
    {
        for(int i=0;i<vector.length;i++)
            if(!vector[i].equals(ZERO))
                return vector[i];
        return ZERO;
    }

    private static int leadingZerosCount(BigInteger[] vector) 
    {
        for(int i=0;i<vector.length;i++) {
            if(!vector[i].equals(ZERO))
                return i;
        }
        return vector.length;
    }

    private static int leadingZerosCount(int[] vector) 
    {
        for(int i=0;i<vector.length;i++) {
            if(vector[i] != 0)
                return i;
        }
        return vector.length;
    }

    // vector greater if one vector is lexicographically with abs values greater
    public static boolean vectorGreater(BigInteger[] v1, BigInteger[] v2) 
    {
        int cmp = 0;
        for(int i=0;i<v1.length;i++) 
        {
            cmp = v1[i].abs().compareTo(v2[i].abs());
            if(cmp > 0)
                return true;
            if(cmp < 0)
                return false;
        }
        return false;
    }

    public static BigInteger[] scalarMultiply(BigInteger scalar,BigInteger[] vector) 
    {
        BigInteger[] result = vector.clone();
        for(int i=0;i<vector.length;i++) 
        {
            result[i] = vector[i].multiply(scalar);
        }
        return result;
    }

    public static BigInteger[] vectorAdd(BigInteger[] v1,BigInteger[] v2) 
    {
        BigInteger[] result = v1.clone();
        for(int i=0;i<result.length;i++) 
        {
            result[i] = v1[i].add(v2[i]);
        }
        return result;
    }

    public static BigInteger[] vectorSubtract(BigInteger[] v1,BigInteger[] v2) 
    {
        return vectorAdd(v1,scalarMultiply(new BigInteger("-1"),v2));
    }

    // reduces base to quasi-gaussian form. 
    // After reduction each vector maintains its integer values and they don't need to be ones.
    // Biginteger necessary, because we may count lcm of many vector values obtaingin very big number
    public static void reduceBase(ArrayList<BigInteger[]> base) 
    {
        for (int i=0;i<base.size();i++) 
        {
            for (int j=i+1;j<base.size();j++) 
            {
                if (vectorGreater(base.get(j), base.get(i)))
                    Collections.swap(base,i,j);    
            }
            if(firstNonZeroElement(base.get(i)).compareTo(ZERO) < 0) 
                    base.set(i,scalarMultiply(new BigInteger("-1"),base.get(i)));

            BigInteger nz1 = firstNonZeroElement(base.get(i));
            if (nz1.equals(ZERO)) 
                return;

            for (int j=i+1;j<base.size();j++) 
            {
                // check if non zero
                if(firstNonZeroElement(base.get(j)).compareTo(ZERO) < 0) {
                    base.set(j,scalarMultiply(new BigInteger("-1"),base.get(j)));
                }
                BigInteger nz2 = firstNonZeroElement(base.get(j));
                if (nz2.equals(ZERO) || leadingZerosCount(base.get(i))!= leadingZerosCount(base.get(j)))
                    continue;
                BigInteger gcd = nz1.gcd(nz2);
                BigInteger scm = nz1.multiply(nz2).divide(gcd);
                base.set(j,scalarMultiply(scm.divide(nz2),base.get(j)));
                base.set(j,vectorSubtract(base.get(j), scalarMultiply(scm.divide(nz1),base.get(i))));
            }
        }
    }

    // checks if vector depenedent from base
    public static boolean dependentFromBase(ArrayList<int[]> base, int[] vec) {
        if (leadingZerosCount(vec) == vec.length)
            return true;
        if (base.size() == 0)
            return false;
        ArrayList<int[]> baseCopy = new ArrayList<>();
        base.forEach(e -> {baseCopy.add(e);});
        int before = getSpaceBase(baseCopy).size();
        baseCopy.add(vec);
        int after = getSpaceBase(baseCopy).size();
        return before == after; 
    }

	public static BigInteger[] toBigIntArray(int [] vector) 
    {
        BigInteger[] result = new BigInteger[vector.length];
        for (int i=0; i<result.length;i++) 
        {
            result[i] = new BigInteger(String.valueOf(vector[i]));
        }
        return result;
    }

    // returns linear independent set of vectors reduced from original base
    public static ArrayList<BigInteger[]> getSpaceBase(ArrayList<int[]> base) 
    {
        ArrayList<BigInteger[]> bigIntBase = new ArrayList<>();
        for(int i=0;i<base.size();i++) 
            bigIntBase.add(toBigIntArray(base.get(i)));
        reduceBase(bigIntBase);
        ArrayList<BigInteger[]> result = new ArrayList<>();
        for (int i=0;i<bigIntBase.size();i++) 
        {
            if (!firstNonZeroElement(bigIntBase.get(i)).equals(ZERO))
                result.add(bigIntBase.get(i));
        }
        return result;
    }

    public static void moveArrayList(ArrayList<String> from, ArrayList<String> to) 
    {
        to.clear();
        for(String s : from)
            to.add(s);
    }

    public static int[] matMul(int[] vector, int [][] matrix) 
    {
        int[] result = new int[matrix[0].length];
        for (int i=0;i<result.length;i++) 
        {
            result[i] = 0;
            for (int j=0;j<vector.length;j++) 
            {
                result[i] += vector[j]*matrix[j][i];
            }
        }
        return result;
    }

    private static int charArrayIndexOf(char array[],char x) 
    {
        int i = 0;
        for (char c : array) 
        {
            if(c == x)
                return i;
            i++;
        }
        return -1;
    }

    public static int[][] wordToMatrix(Automaton automaton,String word) 
    {
        int n = automaton.getN();
        int[][] result = new int[n][n];
        int[][] matrix = automaton.getMatrix();
        for (int i=0;i<n;i++) 
        {
            int j=i;
            for(int k=0;k<word.length();k++) 
            {
                int char_id = charArrayIndexOf(AutomatonHelper.TRANSITIONS_LETTERS, word.charAt(k));
                j = matrix[j][char_id];
            }
            result[i][j] = 1;
        }
        return result;
    }
}
