
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;


public class CpuAllocator {

    static List<String> regionStr =new ArrayList<>();
    static List<float[]> serverCost=new ArrayList<>();
    //Default server configuration data
    static String[] serverTypes= {"large", "xlarge", "2xlarge", "4xlarge", "8xlarge", "10xlarge"};
    static int[] cpuCountByType={1,2,4,8,16,32};

    public static void main(String[] arr){
        try {
            //getting input
            Map<String, Map<String,Float>> inputMap=getInputData();
            //forming region names and cost array
            for(Map.Entry<String, Map<String,Float>> mapEntry:inputMap.entrySet()){
                String regionName=mapEntry.getKey();
                Map<String,Float> regionServerCost=mapEntry.getValue();
                regionStr.add(regionName); //Fetching the region names
                float[] costArray=new float[6]; //Storing the cost based on region
                for(int i=0;i<6;i++){
                    if(regionServerCost.containsKey(serverTypes[i]))
                        costArray[i]=regionServerCost.get(serverTypes[i]);
                        else
                        costArray[i]=0f;
                }
                serverCost.add(costArray);
            }
            //Selecting the best server based on cost
            PreferredServer preferredServer=new PreferredServer();
            for(int i=0;i<regionStr.size();i++){
                preferredServer.updateServer(new ServerGroup(serverCost.get(i),i));
            }
            // Skipping the server with poor cost/cpu
            preferredServer.updateSkip();
            int[][] testCases={
                    {24,115,0}, // 115 mincpu for 24 hours
                    {8,0,29}, // 29 max price for 8 hours
                    {7,214,95}, //214 minCpu , max price 95 for 7 hours
                    {8,1000,20},
                    {100,2,300},
                    {0,54,0},
                    {0,0,0}
            };
            List result =new ArrayList();
            for(int i=0;i<testCases.length;i++) {
                //retriving the server configuration based on the test cases
                result = preferredServer.get_costs(testCases[i][0], testCases[i][1],testCases[i][2]);
                if (result.size() > 0) {
                    System.out.println("Successfully allocated server resources for the user test case :"+Arrays.toString(testCases[i]));
                    printResult(result); // formatting print for readability
                } else {
                    System.out.println("Failed to allocate the server, requested configuration is not possible :"+Arrays.toString(testCases[i]));
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public  static Map<String, Map<String,Float>> getInputData() {
        // Inititalising the input data
        Map<String, Map<String,Float>> inputMap= new HashMap<>();
        Map<String,Float> usEast =new HashMap<>();
        usEast.put("large",0.12f);
        usEast.put("xlarge",0.23f);
        usEast.put("2xlarge",0.45f);
        usEast.put("4xlarge",0.774f);
        usEast.put("8xlarge",1.4f);
        usEast.put("10xlarge",2.82f);
        inputMap.put("us-east", usEast);

        Map<String,Float> usWest =new HashMap<>();
        usWest.put("large",0.14f);
//        usWest.put("xlarge",0.23f);
        usWest.put("2xlarge",0.413f);
        usWest.put("4xlarge",0.89f);
        usWest.put("8xlarge",1.3f);
        usWest.put("10xlarge",2.97f);
        inputMap.put("us-west", usWest);

        Map<String,Float> asia =new HashMap<>();
        asia.put("large",0.11f);
        asia.put("xlarge",0.20f);
//        asia.put("2xlarge",0.45f);
        asia.put("4xlarge",0.67f);
        asia.put("8xlarge",1.18f);
//        asia.put("10xlarge",2.82f);
        inputMap.put("asia", asia);
        return inputMap;
    }
    static void printResult(List result) {
        try{
            System.out.println("Requested configuration: ");
            System.out.println(" { ");
            for (int i=0;i<result.size();i++){
                System.out.println("  { ");
                Map<String, Object> map = (Map<String, Object>)result.get(i);
                System.out.println("    Region: "+map.get("region"));
                System.out.println("    total_cost: "+map.get("total_cost"));
                System.out.println("    Servers: "+map.get("servers"));
                System.out.println("  } ");
            }
            System.out.println(" } ");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}

