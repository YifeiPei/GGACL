public class GroupPerfect{


    String[] source;
    String[] groups;
    boolean[] used;
    int gsize;

    public GroupPerfect(String [] names, int gsize){
	source = names;
	groups = new String[names.length];
	used = new boolean[names.length]; // assume initialised to false
	this.gsize = gsize;
    }

    public void group(){
	group(0,0,0);
    }

    public void group(int lowerIndiv, int soFar, int gPointer){
	if (gPointer == groups.length){
	    // we have filled up the groups array
	    printGroups();

	}else if(soFar == gsize){

            String lowerBound = groups[((gPointer-1)/gsize)*gsize];
	    int lookFrom = lookup(lowerBound)+1;
            //System.out.println("filled up group new lowerBound: " + lowerBound +  " lookFrom: " + lookFrom );
	    // we have filled up the current group
	    group(lookFrom, 0, gPointer);
	    
        }else{
	    // group not yet full
	    
	    int i = lowerIndiv;
	    int upper = lastIndex(gsize-soFar);
	    while(i <= upper){
		if(!used[i]){
		    used[i]=true;
		    groups[gPointer] = source[i];
		    //  System.out.print("lowerIndiv: " + lowerIndiv + " ");
                    //System.out.print("soFar: " + soFar + " ");
		    //System.out.print("Added: " + source[i] + " at " + gPointer);
		    //System.out.println(" " +java.util.Arrays.toString(used));
		    group(i+1,soFar+1,gPointer+1);
		    used[i]=false;
		}
		i++;
	    }
	}
    }

    public void printGroups(){
	
	for (int i=0; i<groups.length; i++){
	    System.out.print(groups[i] + " ");
	    if ((i+1)%gsize == 0){
		System.out.print(":");
	    }
	}
	System.out.println();

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


    public static void main(String[] args){

	String[] names = {"a","b","c","d","e","f","g","h"};
	GroupPerfect g = new GroupPerfect(names,2);
	g.group();
    }


}