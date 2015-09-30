import java.util.*;
public class GroupPerfectOdd{


    String[] source;
    String[] groups;
    Group[] bestSoFar;
    Group[] tmp;
    boolean[] used;
    int[] gsizes;
    FitnessFunction ff;

    public GroupPerfectOdd(String [] names, int[] gsizes, FitnessFunction ff){
	source = names;
        this.ff = ff;
	groups = new String[names.length];
	used = new boolean[names.length]; // assume initialised to false
	this.gsizes = gsizes;
    }

    public void group(){
	group(0,0,0,0);
    }

    public void group(int lowerIndiv, int soFar, int gPointer, int groupNum){
	if (gPointer == groups.length){
	    // we have filled up the groups array
	    printGroups();

	}else if(soFar == gsizes[groupNum]){

            String lowerBound = getLowerBound(groupNum);
	    int lookFrom = lookup(lowerBound)+1;
            //System.out.println("filled up group new lowerBound: " + lowerBound +  " lookFrom: " + lookFrom );
	    // we have filled up the current group
	    group(lookFrom, 0, gPointer,groupNum+1);
	    
        }else{
	    // group not yet full
	    
	    int i = lowerIndiv;
	    int upper = lastIndex(gsizes[groupNum]-soFar);
	    while(i <= upper){
		if(!used[i]){
		    used[i]=true;
		    groups[gPointer] = source[i];
		    //  System.out.print("lowerIndiv: " + lowerIndiv + " ");
                    //System.out.print("soFar: " + soFar + " ");
		    //System.out.print("Added: " + source[i] + " at " + gPointer);
		    //System.out.println(" " +java.util.Arrays.toString(used));
		    group(i+1,soFar+1,gPointer+1,groupNum);
		    used[i]=false;
		}
		i++;
	    }
	}
    }

    public void printGroups(){
        tmp = new Group[gsizes.length];
	int groupNum = 0;
        int gCount = 0;
        Group gtmp = new Group();
	for (int i=0; i<groups.length; i++){
	    //System.out.print(groups[i] + " ");
            gtmp.add(groups[i]);
            gCount++;
	    if (gsizes[groupNum] == gCount){
                tmp[groupNum] = gtmp;
		gtmp = new Group();
		//System.out.print(":");
                gCount=0;
		groupNum++;
	    }
	}
	System.out.println();
        if (bestSoFar != null){
            
            if(ff.evaluate(tmp)>
                  ff.evaluate(bestSoFar)){
                bestSoFar = tmp;
                System.out.println("Best so far: " + ff.evaluate(bestSoFar));
                System.out.println(Arrays.toString(tmp));
            }
        }else{
            bestSoFar = tmp;
        }
    }


    public Group[] getBest(){
        return bestSoFar;
    }

    public int lastIndex(int toGo){
	int countDown = toGo;
	for(int i=used.length-1;  i >= 0; i--){
	    if(! used[i]){
		countDown--;
	    }
	    if(countDown == 0){
		return i;
	    }
	}
	return -1;
    }

    public int lookup(String x){
        int i = 0;
	while(!x.equals(source[i])){
	    i++;
	}
	return i;
    }

    public String getLowerBound(int groupNum){
	int index = 0;
	for(int i = 0; i < groupNum; i++){
	    index += gsizes[i];
	}
	return groups[index];
    }


    /*public static void main(String[] args){

	String[] names = {"a","b","c","d","e","f","g","h"};
        int[] gsizes = {2,3,3};
	GroupPerfectOdd g = new GroupPerfectOdd(names,gsizes);
	g.group();
    }  */


}