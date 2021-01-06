import java.util.*;

public class PreferredServer {
    float[] costPerCpu;
    int[] serverRegion, serverCount;
    boolean[] isSkip;

    PreferredServer() {
        costPerCpu = new float[6];
        serverRegion = new int[6];
        isSkip = new boolean[6];
        serverCount = new int[6];
    }
    class RegionGroup implements Comparable<RegionGroup>{
        int regionId;
        float regionCost;
        RegionGroup(int regionId,float regionCost){
            this.regionId=regionId;
            this.regionCost=regionCost;
        }
        //sorting in descending order
        public int compareTo(RegionGroup region2) {
            return Float.compare(region2.regionCost,this.regionCost);
        }
    }

    public void updateServer(ServerGroup serverGroup) {
        //updating preferred Servers
        for (int i = 0; i < serverGroup.cost.length; i++) {
            if (serverGroup.cost[i] == 0) {
                continue;
            }
            float costPerCpu = serverGroup.cost[i] / CpuAllocator.cpuCountByType[i];
            if (this.costPerCpu[i] == 0 || costPerCpu < this.costPerCpu[i]) {
                this.costPerCpu[i] = costPerCpu;
                this.serverRegion[i] = serverGroup.region;
            }
        }
    }

    public void updateSkip() {
        //skipping poor cost/cpu servers
        int i = 0, j = 1;
        while (j < costPerCpu.length) {
            if (costPerCpu[j] > costPerCpu[i]) {
                isSkip[j] = true;
            } else {
                i = j;
            }
            j++;
        }

    }

    public List get_costs(int hour, int minCpuCount, int price){
        List finalList = new ArrayList();
        try{
            if(hour<=0)
                return finalList;
            //selecting server configuration
            if(price>0){
                float pricePerHour = (float) price / hour;
                updateServerCount(pricePerHour,minCpuCount);
            }else if(minCpuCount>0){
                updateServerCount((float)minCpuCount);
            }else{
                return finalList;
            }
            //forming result based on region from selected servers
            finalList = getResult(hour);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return finalList;
    }

    public void updateServerCount(float pricePerhour,int minCpuCount) {
        //first server with highercpu count is selected becoz they have less cost/cpu
        int totalServerCount=0;
        for (int i = costPerCpu.length - 1; i >= 0; i--) {
            //skippimg inefficient servers
            if (isSkip[i]) continue;
            float cpuCost=costPerCpu[i] * CpuAllocator.cpuCountByType[i];
            float tempPricePerHour = pricePerhour / cpuCost;
            int noOfServers = (int) tempPricePerHour / 1;
            if (noOfServers != 0) {
                pricePerhour = (tempPricePerHour % 1)*cpuCost;
                serverCount[i] = noOfServers;
                totalServerCount+=noOfServers;
            }
        }
        if(minCpuCount>0 && minCpuCount<totalServerCount){
            serverCount = new int[6];
        }
    }
    public void updateServerCount(float minCpuCount) {
        //first server with highercpu count is selected becoz they have less cost/cpu
        for (int i = costPerCpu.length - 1; i >= 0; i--) {
            //skipping inefficient servers
            if (isSkip[i]) continue;
            float tempMinCpuCount = minCpuCount /  CpuAllocator.cpuCountByType[i];
            int noOfServers = (int) tempMinCpuCount / 1;
            if (noOfServers != 0) {
                minCpuCount = (tempMinCpuCount % 1)*CpuAllocator.cpuCountByType[i];
                serverCount[i] = noOfServers;
            }
        }
    }
    public List getResult(int hour) {
        //Creating the result based on region with total cost sorted
        List finalList = new ArrayList();
        try {
            float[] regionCost = new float[CpuAllocator.regionStr.size()];
            List<RegionGroup> regionList = new ArrayList<RegionGroup>(CpuAllocator.regionStr.size());
            //Calculating cost based on region
            for (int i = 0; i < serverCount.length; i++) {
                float tempCost = serverCount[i] * costPerCpu[i] * CpuAllocator.cpuCountByType[i] * hour;
                regionCost[serverRegion[i]] += tempCost;
            }
            for (int i = 0; i < regionCost.length; i++) {
                regionList.add(new RegionGroup(i, regionCost[i]));
            }
            //sorting by cost
            Collections.sort(regionList);
            //forming the final result
            for (int i = 0; i < regionList.size(); i++) {
                Map<String, Object> map = new HashMap<>();

                List tupleList = new ArrayList();
                for (int j = 0; j < serverRegion.length; j++) {
                    if (regionList.get(i).regionId == serverRegion[j] && serverCount[j] != 0) {
                        Map<String, Object> tupleMap = new HashMap<>();
                        tupleMap.put(CpuAllocator.serverTypes[j], serverCount[j]);tupleList.add(tupleMap);
                    }
                }
                if (tupleList.size() != 0) {
                    map.put("region", CpuAllocator.regionStr.get(regionList.get(i).regionId));
                    map.put("total_cost", "$" + regionList.get(i).regionCost);
                    map.put("servers", tupleList);
                    finalList.add(map);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return finalList;
    }
}
