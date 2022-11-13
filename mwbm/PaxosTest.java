package mwbm;

//import org.junit.Test;

import java.util.*;

//import static org.junit.Assert.*;
//import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class PaxosTest {
    static int[] ports;

    private static void cleanup(Agent[] agentList){
        for(int i = 0; i < agentList.length; i++){
            if(agentList[i] != null){
                agentList[i].Kill();
            }
        }
        try // wait for all threads to die
        {
            Thread.sleep(1000);
        }
        catch(Exception e) { }
    }

//    private static Agent[] initAgents(List<List<Integer>> pricesList, List<List<Integer>> betaList, List<HashMap<Integer, Integer>> neighborsList, int n, int epsilon){
//        Agent[] agentList = new Agent[n];
//        for(int i = 0; i < n; i++){
//            agentList[i] = new Agent(i, pricesList.get(i), betaList.get(i), neighborsList.get(i), n, epsilon);
//        }
//        return agentList;
//    }


//    public static void main(String[] args) {
//        final int n = 3;
//        final double epsilon = Math.round(100*1.0/n)/100.0;
//        final int m = 3;
//
//        System.out.println(epsilon);
//        Double[] tempPrices = {5.0, 7.0, 13.0};
//        Double[][] tempBeta = {{11.0, 12.0, 15.0}, {15.0, 16.0, 18.0}, {9.0, 9.0, 14.0}};
//        Integer[][] tempNeighbors = {{1}, {0, 2}, {1}};
//
//
//        List<List<Double>> pricesList = new ArrayList<>();
//        for(int i = 0; i<n; i++){
//            pricesList.add(Arrays.asList(tempPrices));
//        }
//
//        List<List<Double>> betaList = new ArrayList<>();
//        for(int i = 0; i<n; i++){
//            betaList.add(Arrays.asList(tempBeta[i]));
//        }
//
//        System.out.println(pricesList);
//        System.out.println(betaList);
//
//        ports = new int[n];
//        for(int i = 0 ; i < n; i++){
//            ports[i] = 1100+i;
//        }
//
//        List<HashMap<Integer, Integer>> neighborsList = new ArrayList<>();
//        HashMap<Integer, Integer> tempMap;
//        for(int i=0; i<n; i++){
//            tempMap = new HashMap<>();
//            for(int k = 0; k<tempNeighbors[i].length; k++){
//                tempMap.put(tempNeighbors[i][k], ports[tempNeighbors[i][k]]);
//            }
//            neighborsList.add(tempMap);
//        }
//
//        System.out.println(neighborsList);
//
//        Agent[] agentList = new Agent[n];
//        for(int i = 0; i < n; i++){
//            agentList[i] = new Agent(i, pricesList.get(i), betaList.get(i), neighborsList.get(i), ports[i], n, epsilon);
//        }
//
//        for(Agent agent: agentList)
//            System.out.println(agent);
//
//        for(Agent agent: agentList)
//            agent.StartAgent();
//
//        System.out.println("... Passed");
//
//        try {
//            Thread.sleep(10000);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//        cleanup(agentList);
//    }

    public static void main(String[] args) {
        final int n = 3;
//        final double epsilon = 1.0/n;
        final double epsilon = Math.round(100*1.0/n)/100.0;
        final int m = 3;
        System.out.println(epsilon);

        List<List<Double>> pricesList = new ArrayList<>();
        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));
        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));
        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));

        List<List<Double>> betaList = new ArrayList<>();
        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));
        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));
        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));
//        betaList.add(Arrays.asList(-1, -1));

        List<HashMap<Integer, Integer>> neighborsList = new ArrayList<>();

        ports = new int[n];
        for(int i = 0 ; i < n; i++){
            ports[i] = 1100+i;
        }
//        for(int i = 0;i<ports.length;i++) {
//            System.out.println(ports[i]);
//        }

        double[] tempPrices = {5.0, 7.0, 13.0};
        double[][] tempBeta = {{11.0, 12.0, 15.0}, {15.0, 16.0, 18.0}, {9.0, 9.0, 14.0}};

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                pricesList.get(i).set(j, tempPrices[j]);
                betaList.get(i).set(j, tempBeta[i][j]);
            }
        }
        HashMap<Integer, Integer> tempMap = new HashMap<>();
        tempMap.put(1, ports[1]);
        neighborsList.add(tempMap);
        tempMap = new HashMap<>();
        tempMap.put(0, ports[0]);
        tempMap.put(2, ports[2]);
        neighborsList.add(tempMap);
        tempMap = new HashMap<>();
        tempMap.put(1, ports[1]);
        neighborsList.add(tempMap);

        System.out.println(neighborsList);


        // initialize the above two list of lists - prices and beta
//        Agent[] agentList = initAgents(pricesList, betaList, neighborsList, n, epsilon);
        Agent[] agentList = new Agent[n];
        for(int i = 0; i < n; i++){
            agentList[i] = new Agent(i, pricesList.get(i), betaList.get(i), neighborsList.get(i), ports[i], n, epsilon);
        }
        System.out.println(agentList[0]);
        System.out.println(agentList[1]);
        System.out.println(agentList[2]);

        agentList[0].StartAgent();
        agentList[1].StartAgent();
        agentList[2].StartAgent();
        System.out.println("... Passed\n");
        try {
            Thread.sleep(10000);
        } catch (Exception e){
            e.printStackTrace();
        }
        cleanup(agentList);
    }
}


//        List<List<Double>> pricesList = new ArrayList<>();
//        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));
//        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));
//        pricesList.add(Arrays.asList(-1.0, -1.0, -1.0));
//
//        List<List<Double>> betaList = new ArrayList<>();
//        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));
//        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));
//        betaList.add(Arrays.asList(-1.0, -1.0, -1.0));

//        for(int i = 0;i<ports.length;i++) {
//            System.out.println(ports[i]);
//        }

//        Double[] tempPrices = {5.0, 7.0, 13.0};
//        Double[][] tempBeta = {{11.0, 12.0, 15.0}, {15.0, 16.0, 18.0}, {9.0, 9.0, 14.0}};

//        for(int i = 0; i < n; i++) {
//            for(int j = 0; j < m; j++) {
//                System.out.println(tempPrices[j]);
//                System.out.println(tempBeta[i][j]);
//                System.out.println(pricesList.get(i) + " " + j);
//                System.out.println(betaList.get(i) + " " + j);
////                pricesList.get(i).set(j, tempPrices[j]);
////                betaList.get(i).set(j, tempBeta[i][j]);
//            }
//        }





//        Double init_a[] = new Double[m];
//        Arrays.fill(a, -1.0);
//        Double b[] = new Double[m];
//        Arrays.fill(b, -1.0);




//            System.out.println(tempMap);
//            neighborsList.add(tempMap);
//            System.out.println(neighborsList);
//            tempMap.clear();
//                    }
//        tempMap.put(1, ports[1]);
//        neighborsList.add(tempMap);
//        tempMap = new HashMap<>();
//        tempMap.put(0, ports[0]);
//        tempMap.put(2, ports[2]);
//        neighborsList.add(tempMap);
//        tempMap = new HashMap<>();
//        tempMap.put(1, ports[1]);
//        neighborsList.add(tempMap);


//        System.out.println(agentList[0]);
//        System.out.println(agentList[1]);
//        System.out.println(agentList[2]);


//        agentList[0].StartAgent();
//        agentList[1].StartAgent();
//        agentList[2].StartAgent();