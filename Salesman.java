package salesman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Pipjak on 12/12/2016.
 */
public class Salesman {

    /* STATIC FIELDS */
    private static int[][] graph;
    private static final int INF = 1000 * 1000 * 1000;
    private static int N;
    private static FastScanner in;

    /* DYNAMIC FIELDS */
    private int[][] seeSet;
    private PriorityQueue<Salesman.VecSet> sets;
    private Integer lastIndex;


    /* CONSTRUCTOR */

    // 0.
    public Salesman() throws IOException {

        Salesman.in = new FastScanner();

        // read the data
        try {
            this.readData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sets = new PriorityQueue<>();
        this.createSet();
    }



    /* INNER CLASSES */

    // 0.
    private static class FastScanner {
        private BufferedReader reader;
        private StringTokenizer tokenizer;

        public FastScanner() {
            reader = new BufferedReader(new InputStreamReader(System.in));
            tokenizer = null;
        }

        public String next() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                tokenizer = new StringTokenizer(reader.readLine());
            }
            return tokenizer.nextToken();
        }

        public int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }

    // 1.
    private class VecSet implements Comparable{

        /* dynamic fields */
        private Integer setID;
        private TreeSet<Integer> convert;

        /* constructor */
        
        // 0.
        public VecSet(TreeSet<Integer> inputHash){
            // inputHash already MUST contain 1
            if(!inputHash.contains(1)){
                System.out.println("PROBLEM: INPUT hash does not contain 1");
            }

            this.convert = inputHash;
            // in ID I store the ID of the set, meaning if you decompose it, take powers, you get members of a set
            this.setID = this.calcID();
        }

        /* helpful methods */
        
        // 0.
        public int getID(){
            return this.setID;
        }
        
        // 1.
        private TreeSet<Integer> getConvert(){
            return this.convert;
        }
        
        // 2.
        private int calcID(){
            Double result = 0.0;
            for(Integer in: this.convert){
                result += Math.pow(2,in);
            }
            return result.intValue();
        }
        
        // 3.
        // assuming that the convert includes j
        // method returns the ID of a set S\{j}
        public int subtractID(int j){
            if(this.convert.contains(j)){
                return (this.getID() - ((Double) Math.pow(2,j)).intValue());
            }
            else{
                System.out.println("ERROR");
                return -1;
            }
        }

        /* defining methods */
        @Override
        public int compareTo(Object o) {
            return ((Integer) this.convert.size()).compareTo(((Salesman.VecSet) o).getConvert().size());
        }
    }


    /* HELPFUL METHODS */

    // 0.
    // note binar is already tested (for our purpose I modified binar to shift powers it up by 1)
    private void binar(int in, TreeSet<Integer> hold, int counter){

        if(in == 0){
            return;
        }
        else if(in%2==1){
            hold.add(counter + 2);
            binar(in-1, hold, counter);
        }
        else if(in%2==0){
            counter++;
            binar(in/2, hold, counter);
        }
    }
    
    // 1.
    // creates the set: 1 \in S \in {1, 2,... , n}. Where 1 is always in the set.
    private void createSet(){
        TreeSet<Integer> temp;
        int counter;
        Salesman.VecSet vec;

        for(int i = 0; i<=this.upperBound(); i++){
            counter = 0;
            temp = new TreeSet<>();

            this.binar(i, temp, counter);
            // adding the "2 = 2^1", i.e. "1" into temp
            temp.add(1);


            // create the new vec:
            vec = new Salesman.VecSet(temp);

            // adding vec into the global PriorityQueue<VecSet>
            this.sets.add(vec);
        }

        // create the seeSet
        this.seeSet = new int[this.sets.size()][Salesman.N];

        // filling up the array with -1
        for(int i = 0; i<this.sets.size(); i++)
            for(int j = 0; j<Salesman.N; j++)
                this.seeSet[i][j] = -1;
    }
    
    // 2.
    private int upperBound(){
        return ((Double) (Math.pow(2, Salesman.N-1) - 1)).intValue();
    }
    
    // 3.
    // read the data
    private static void readData() throws IOException {

        int n = in.nextInt();
        int m = in.nextInt();

        Salesman.N = n;
        Salesman.graph = new int[n][n];

        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
                Salesman.graph[i][j] = INF;

        for (int i = 0; i < m; ++i) {
            int u = in.nextInt() - 1;
            int v = in.nextInt() - 1;
            int weight = in.nextInt();
            Salesman.graph[u][v] = Salesman.graph[v][u] = weight;
        }
    }
    
    // 4.
    // convert VecSet ID into the indexID
    private int id(int id){
        if(id>0){
            return ((id/2 - 1) /2);
        }
        else{
            return -1;
        }
    }
    
    // 5.
    // subtract last_label from set <--> ind = {1,...}
    private int subtractIND(int ind, int lastIndex){
        return (ind - ((Double) Math.pow(2,lastIndex)).intValue());
    }

    /* CORE METHODS */

    // 0.
    public int solveDistanceTSP(){
        VecSet vs;
        TreeSet<Integer> tset;
        int minimum;

       vs = this.sets.poll();
       this.seeSet[id(vs.getID())][0] = 0;

       // the main loop
        while(!this.sets.isEmpty()){

            vs = this.sets.poll();
            //System.out.println("the size is " + vs.convert.size());
            this.seeSet[id(vs.getID())][0] = Salesman.INF;

            // looping through the vs members in correct order, maybe you should use TreeSet in the first place
            tset =  vs.convert;

            for(Integer in: tset) {
                if (!in.equals(1)) {
                    for (Integer jn : tset) {
                        if (!jn.equals(in)) {

                            int temp = this.seeSet[id(vs.subtractID(in))][jn-1] + Salesman.graph[in-1][jn-1];

                            /* if temp is infinite just leave it infinite, do no increase */
                            if(temp>Salesman.INF){
                                temp = Salesman.INF;
                            }

                            /* look for a minimum */
                            if(this.seeSet[id(vs.getID())][in-1]==-1){
                                minimum = temp;
                            }
                            else {
                                minimum = Math.min(this.seeSet[id(vs.getID())][in-1], temp);
                            }
                            this.seeSet[id(vs.getID())][in-1] = minimum;
                        }
                    }
                }
            }
        }
        return this.min();
    }

    // 1.
    private int min(){
        final Integer maxID = id(2*(((Double) Math.pow(2, Salesman.N)).intValue() - 1));
        Integer minimum = Integer.MAX_VALUE;
        Integer lastIndex = -1;
        Integer tempMin;

        for(int i = 1; i<=Salesman.N; i++){
            tempMin = this.seeSet[maxID][i-1] + Salesman.graph[i-1][0];

            if(tempMin.compareTo(minimum)<0){
                lastIndex = i;
                minimum = tempMin;
            }
        }

        // uploading the last index
        this.lastIndex = lastIndex;

        return minimum;
    }

    // 2.
    private void backtrackTSP() {
        Integer ind = 2 * (((Double) Math.pow(2, Salesman.N)).intValue() - 1);
        Integer lastIndex, minimum, index, indTemp;
        Stack<Integer> path = new Stack();

        // push the last index to path
        path.push(this.lastIndex);

        // download the lastIndex
        lastIndex = this.lastIndex;

        // main backtracking loop
        for (int i = Salesman.N; 1 < i; i--) {
            minimum = Integer.MAX_VALUE;
            index = -1;
            indTemp = -1;

            // scan through all previous and look for minimum:
            for (int j = 1; j <= Salesman.N; j++) {
                int temp = this.seeSet[id(subtractIND(ind, lastIndex))][j-1];

                if(!lastIndex.equals(j) && temp!=-1 && minimum > (temp + Salesman.graph[lastIndex-1][j-1])){

                    minimum = temp + Salesman.graph[lastIndex-1][j-1];
                    index = j;
                    indTemp = subtractIND(ind, lastIndex);
                }
            }

            // push the result to the path
            path.push(index);

            // change the last index to current last index
            lastIndex = index;

            // change the index of a set of the one unit smaller cardinality
            ind = indTemp;
        }

        // print out the path
        while(!path.isEmpty()){
            System.out.print(path.pop() + " ");
        }
    }



    /* MAIN METHOD */
    public static void main(String[] args){
        int distance;
        Salesman g = null;

        try {
            g = new Salesman();
        } catch (IOException e) {
            e.printStackTrace();
        }

        distance = g.solveDistanceTSP();

        if(distance>Salesman.INF){
            System.out.println(-1);
        }
        else {
            System.out.println(distance);

            g.backtrackTSP();
        }
    }
}
