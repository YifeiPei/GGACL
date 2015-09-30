import java.util.*;

public class Chromosome
{
    public static final int RANDOM = 0;
    public static final int STRIPING = 1;
    public static final int GREEDY = 2;
    public static final int STRIPING2 = 3;
    public static final int BEST = 4;

    private static int childCount = 0;


    private ArrayList<Group> groups;
    private int groupsize;
    private int[] inversionPermute;  // permuation used

    ///////////
    private int[] groupsizes;
    private int maxsize;

    private int numstudents;
    private int numgroups;
    private int crossMethod;
    private double crossPara;
    private int mutMethod;
    private double mutPara;
    private double invPara = 0.0;

    public boolean fromInversion = false;

    FitnessFunction ff;

    // CSYIP modified

    //Takes in a list of the students, the number of groups and a boolean determining the initial distribution (TRUE for random, FALSE for greedy)
    //For now, assuming student_number is divisible by groupsize
    public Chromosome(String[] students, int size_of_groups, int crossMethod, 
		      double crossPara, int mutMethod, double mutPara,
		      int initPop, FitnessFunction fitnessfunction)
    {
	numstudents = students.length;
	groupsize = size_of_groups;

	///////////////////////////////////////////////////////////////////////////
	numgroups = (int)(numstudents/(float)groupsize);
	numgroups = (int)numgroups;


	groupsizes = new int[numgroups];
	for(int i=0;i<numgroups;i++)
	    groupsizes[i] = groupsize;

	int remainder = numstudents%groupsize;
	
	int ii = numgroups-1;

	maxsize = groupsize;

	while(remainder > 0)
	{
      	    groupsizes[ii]++;
	    remainder--;
	    ii--;
	    if(ii<0)
		ii = numgroups-1;
	}
	for(int j = 0;j<numgroups;j++)
	{
	    if(groupsizes[j] > maxsize)
		maxsize = groupsizes[j];
	}

        inversionPermute = new int[numgroups];
        for(int i=0; i< inversionPermute.length; i++){
           inversionPermute[i] = i;
	}

	///////////////////////////////////////////////////////////////////////////
	ff = fitnessfunction;

	this.crossMethod = crossMethod;
	this.crossPara = crossPara;
	this.mutMethod = mutMethod;
	this.mutPara = mutPara;

	groups = new ArrayList<Group>();

	if(initPop == RANDOM)
	{
	    ArrayList<Integer> numbers = new ArrayList<Integer>();
	    	    
	    for(int i=0;i<numstudents;i++)
		numbers.add(new Integer(i));

	    for(int i=0;i<numgroups;i++)
	    {
		String[] toadd = new String[groupsize];
		for(int j=0;j<groupsize;j++)
		    toadd[j] = students[((Integer) numbers.remove((int)(Math.random()*numbers.size())).intValue())];

		Group temp = new Group(toadd);	
		groups.add(temp);
	    }

	    replaceAndAdd();
	}

	else if(initPop == GREEDY)
	{
	    for(int i=0;i<numgroups;i++)
		groups.add(new Group());

	    for(int i=0;i<numstudents;i++)
		greedyAdd(students[i]);

	}

	else if(initPop == STRIPING)
	{
	    for(int i=0;i<numgroups;i++)
	    {
		String[] toadd = new String[groupsize];
		for(int j=0;j<groupsize;j++)
		    toadd[j] = students[i+j*numgroups];

		Group temp = new Group(toadd);	
		groups.add(temp);
	    }
            // insert the remainder
            int remGroup = 0;
            for(int i=numgroups*groupsize; i<numstudents; i++){
                groups.get(remGroup).add(students[i]);
                remGroup++;
            }
	}
	else if(initPop == STRIPING2)
	{
	    for(int i=0;i<numgroups;i++)
	    {
		String[] toadd = new String[groupsize];
		for(int j=0;j<groupsize;j++)
		    toadd[j] = students[((j%2)==0)?i+j*numgroups:((numgroups-1)-i) + (j*numgroups)];

		Group temp = new Group(toadd);	
		groups.add(temp);
	    }
            int remGroup = 0;
            if ((groupsize%2) == 0){
                // even go in forward direction
                for(int i=numgroups*groupsize; i<numstudents; i++){
                    groups.get(remGroup).add(students[i]);
                    remGroup++;
                }
            }else{
                // odd go in backward direction
                for(int i = numstudents-1; i>=numgroups*groupsize; i--){
                    groups.get(remGroup).add(students[i]);
                    remGroup++;
                }
            }
                    
	}else if(initPop == BEST){
            GroupPerfectOdd perf = new GroupPerfectOdd(students,groupsizes,ff);
            perf.group();
            Group[] best = perf.getBest();
            for(int i = 0; i<best.length; i++){
                groups.add(best[i]);
            }
        }
            
    }

    // CSYIP modified

    //Creates an empty chromosone
    //For now, assuming student_number is divisible by groupsize
    public Chromosome(int number_of_students,int size_of_groups,
		      int crossMethod, double crossPara, int mutMethod, 
		      double mutPara, FitnessFunction fitnessfunction)
    {

	groups = new ArrayList<Group>();
	numstudents = number_of_students;
	groupsize = size_of_groups;
	

	///////////////////////////////////////////////////////////////////////////
	numgroups = (int)(numstudents/(float)groupsize);
	numgroups = (int)numgroups;


	groupsizes = new int[numgroups];
	for(int i=0;i<numgroups;i++)
	    groupsizes[i] = groupsize;

	int remainder = numstudents%groupsize; 
	
	int ii = numgroups-1;

	maxsize = groupsize;

	while(remainder > 0)
	{
	    groupsizes[ii]++;
	    remainder--;
	    ii--;
	    if(ii<0)
		ii = numgroups-1;
	}

	for(int j = 0;j<numgroups;j++)
	{
	    if(groupsizes[j] > maxsize)
		maxsize = groupsizes[j];
	}
 
        /// allocate and initialise inversion array

        inversionPermute = new int[numgroups];
        for(int i=0; i< inversionPermute.length; i++){
        inversionPermute[i] = i;
        }
	///////////////////////////////////////////////////////////////////////////


	ff = fitnessfunction;
	this.crossMethod = crossMethod;
	this.crossPara = crossPara;
	this.mutMethod = mutMethod;
	this.mutPara = mutPara;
    }



    //Adds the students, based on EPD
    public void greedyAdd(String student)
    {
	

	if(Math.random() < 0.1/*RANDOM PROBABILITY*/)
	{
            //System.out.println("random!");
		boolean tempfull = true;

		for(int i=0;i<groups.size();i++)
		{
		    if (groups.get(i).size() < groupsize)
		    {
			tempfull = false;
			break;
		    }
		}
	

		int randomgroup = (int)(Math.random()*groups.size());

		if(tempfull == true)
		{		

			while(groups.get(randomgroup).size() >= maxsize)
				randomgroup = (int)(Math.random()*groups.size());

			groups.get(randomgroup).add(student);

		}
		else
		{
			while(groups.get(randomgroup).size() >= groupsize)
				randomgroup = (int)(Math.random()*groups.size());

			groups.get(randomgroup).add(student);
		}

		return;
	}

        //System.out.println("Greedy!");
	Group lowest = groups.get(0);
	int lowgroup=0;

	boolean tempfull = true;

	for(int i=0;i<groups.size();i++)
	{
	    if (groups.get(i).size() < groupsize)
	    {
		tempfull = false;
		break;
	    }
	}


	if(tempfull == true)
	{
	    while(lowest.size() >= maxsize)
	    {
		lowgroup++;
		lowest = groups.get(lowgroup);
	    }
	    	    
	    //Work out the mean of this group
	    float lowsum = 0;
	    for(int i=0;i<lowest.size();i++)
		lowsum += ff.fitness(lowest.get(i));


	    //Now find the group with the lowest ideal mean
	    for(int i=lowgroup;i<groups.size();i++)
	    {
		float tempsum = 0;
		//Only check those groups which aren't full
		if(groups.get(i).size() < maxsize)
		{
		    for(int j=0;j<groups.get(i).size();j++)
			tempsum += ff.fitness(groups.get(i).get(j));

		    if(groups.get(i).size() != 0)
			tempsum = tempsum/(float)groups.get(i).size();

		    if(ff.fitness(student) < ff.global())
		    {
			if(tempsum > lowsum)
			{
			    lowsum = tempsum;
			    lowest = groups.get(i);
			}
		
		    }
		    else
		    {
			if(tempsum < lowsum)
			{
			    lowsum = tempsum;
			    lowest = groups.get(i);
			}
		    }
		}
	    }

	}
	else
	{
		while(lowest.size() >= groupsize)
		{
		    lowgroup++;
		    lowest = groups.get(lowgroup);
		}
		
		//Work out the mean of this group
		float lowsum = 0;
		for(int i=0;i<lowest.size();i++)
		    lowsum += ff.fitness(lowest.get(i));
	
	
		//Now find the group with the lowest sum
		for(int i=lowgroup;i<groups.size();i++)
		{
			float tempsum = 0;
			//Only check those groups which aren't full
			if(groups.get(i).size() < groupsize)
			{
				for(int j=0;j<groups.get(i).size();j++)
			    		tempsum += ff.fitness(groups.get(i).get(j));
	

				if(ff.fitness(student) < ff.global())
				{
					if(tempsum > lowsum)
					{
						lowsum = tempsum;
						lowest = groups.get(i);
					}
		
				}
				else
				{
					if(tempsum < lowsum)
					{
						lowsum = tempsum;
						lowest = groups.get(i);
					}
				}
			}
		}
	}
	
	//Add the student to this group
	lowest.add(student);
    }



    public void replaceAndAdd()
    {

       //SortedSet<String> temp = new TreeSet<String>();
	Set<String> temp = new HashSet<String>();

	Set<String> total = ff.studentNumbers();
	      

	String[] list2 = new String[total.size()];
	total.toArray(list2);

	for(int i=0;i<list2.length;i++)
	{
	    if(!contains(list2[i]))
		temp.add(list2[i]);
	}
        
        //Now temp contains all the students not in the group
	String[] list = new String[temp.size()];
	temp.toArray(list);
	
	
	for(int i=0;i<list.length;i++)
	    greedyAdd(list[i]);

	
    }

  

    //Adds a group to the chromosone, useful for copy and for crossover
    //NOTE it does not replace any deleted students
    public void add(Group toadd, int index)
    {
	if(index > groups.size())
		index = groups.size()/2;

	for(int i=0;i<toadd.size();i++)
	{
	    int tempsize = groups.size();

	    for(int j=0;j<tempsize;j++)
	    {
		//if an element in the group we're trying to add is already in the chromosone, get rid the group in the chromosone
		if(groups.get(j).contains(toadd.get(i)))
		{
                    

		    //gets rid of the group and replaces it with a blank one
		    groups.remove(j);
		    groups.add(j,new Group());
		    
		    
		    if(j<index)
			index--;
		    
		    j = tempsize+1;
		    break;
		}
	    }
	}

	groups.add(index,toadd);

	//We may be oversized by 1, if so, delete a blank group
	//Possibly add better code here to delete a more appropriate blank one?
	if(groups.size() > numgroups)
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).size() == 0)
			{
				groups.remove(groups.get(i));
				break;
			}
		}
	}
  }
	      


    //Group accessor method
    public Group get(int index)
    {
	if(index >= groups.size())
	    index = groups.size()-1;
	if(index<0)
	    index = 0;

	return groups.get(index);
    }

    //the number of groups, we may not need this?
    public int size()
    {
	return groups.size();
    }

    //Is this particular student inside this Chromosome
    boolean contains(String particularstudent)
    {
	for(int i=0;i<groups.size();i++)
	{
	    if(groups.get(i).contains(particularstudent))
		return true;
	}
	return false;
    }
    
    //A crossover implementation for use in child (or independantly) - should we make this private?
    public Chromosome greedyCrossover(Chromosome other)
    {
	int cut1, cut2;

	cut1 = (int)(Math.random()*size());
	cut2 = (int)(Math.random()*other.size());
		
	while(Math.abs(cut2 - cut1) < size()/10)
	    cut2 = (int)(Math.random()*other.size());

	Chromosome temp = clone();//.inversion();

	while(cut2 != cut1)
	{
	    temp.add(other.get(cut2),cut1);
	    cut2--;

	    if(cut2 == -1)
		cut2 = temp.size()-1;
	}

	while(temp.groups.size() < numgroups)
		temp.groups.add(new Group());

	temp.replaceAndAdd();

	return temp;//.inversion();
    }


    public Chromosome inversionDo(){
    
	Group[] storage = new Group[numgroups];
      
    
        for (int i=0;i < inversionPermute.length; i++){
	    storage[i]= groups.get(inversionPermute[i]);
	}
    
        Chromosome temp = this.clone();

	for (int i=0;i< storage.length; i++){
	    temp.groups.set(i,storage[i]);
        }
    
	return temp;
    }


    public Chromosome inversionUndo(){
    Group [] storage = new Group[numgroups];
       
    for (int i=0;i< inversionPermute.length; i++){
        storage[inversionPermute[i]] = groups.get(i);
    }
    
    Chromosome temp = this.clone();

    for (int i=0;i< storage.length; i++){
        temp.groups.set(i,storage[i]);;
    }
    
    return temp;
    }

    // An inversion operator that changes the InversionPermutation 
    // array by swapping locations.

    public void invertApply(){
	int indexA = (int)(Math.random()*numgroups);
	int indexB = (int)(Math.random()*numgroups);
	int temp;
    
	// now do swap
    
        temp = inversionPermute[indexA];
	inversionPermute[indexA] = inversionPermute[indexB];
        inversionPermute[indexB] = temp;

    }
    

    public Chromosome inversion()
    {

	ArrayList<Group> storage = new ArrayList<Group>();

	for(int i=0;i<groups.size();i++)
	    storage.add(groups.get(i));



	Chromosome temp = new Chromosome(numstudents,groupsize,
					     crossMethod, crossPara, 
					     mutMethod, mutPara, ff);
	temp.fromInversion = true;

	while(storage.size() != 0)
	    temp.add(storage.remove(((int)Math.random()*storage.size())),0);

	temp.fromInversion = false;
	return temp;

    }


     //A mutation implementation for use in child (or independantly) - should we make this private?
    public Chromosome mutation()
    {
        Chromosome temp = inversion();
	//Chromosome temp = clone();
        if(this.checkSizes()){
	    System.out.println("******* empty group for mutation *****");
            System.out.println(" returning inverted chromosome");
	    return temp;
	}
	int high = 0;
	int low = 0;
	
	String[] students = temp.groups.get(0).toArr();
	int[] values = new int[students.length];


	  for(int k=0;k<students.length;k++)
		values[k] = ff.fitness(students[k]);

        double highest = ff.calcvar(values);
	  double lowest = highest;
        for(int i=1;i<temp.groups.size();i++)
	  {

		students = temp.groups.get(i).toArr();
	    values = new int[students.length];

	    for(int k=0;k<students.length;k++)
		values[k] = ff.fitness(students[k]);

		double var = ff.calcvar(values);

		if(var > highest)
		{
			high = i;
			highest = var;
		}
		else if(var < lowest)
		{
		    low = i;
			lowest = var;
		}
	  }

	
	temp.get(high).remove((int)(Math.random()*temp.groups.get(high).size()));
	temp.get(low).remove((int)(Math.random()*temp.groups.get(low).size()));
	 

        for(int i=0;i<temp.size();i++)
        {
	    for(int j=0;j<temp.get(i).size()/4;j++)
	    {

		    temp.get(i).remove((int)(Math.random()*temp.get(i).size()));
        
	    }
	}

	temp.replaceAndAdd();

	return temp;
    }

    public String toString()
    {
	String toret = "";

        for(int i=0;i<groups.size();i++)
        {
            toret += groups.get(i).toString() + " :  ";
        }
        //return toret;    

        //String toret = "";

	toret += "\n\n";
	Group g = null;

        for(int i=0;i<groups.size();i++)
        {
	    g = groups.get(i);

	    for(int j = 0; j <g.size(); j++)
	 	toret += ff.fitness(g.get(j)) + " ";
	    
	    if(i < groups.size()-1)
		toret += ": ";
	}
        return toret;        
    }

    // CSYIP modified



    private boolean hasDuplicates(){
	ArrayList<String> vals = new ArrayList<String>();

	for (int i =0; i<groups.size(); i++){
	    for (int j=0; j< groups.get(i).size(); j++){
		if (vals.contains(groups.get(i).get(j))){
		    return true;
                }
		vals.add(groups.get(i).get(j));
	    }
	}
	return false;
    }

		    
    private boolean checkSizes(){
	for (int i=0; i< groups.size(); i++){
	    if (groups.get(i).size() < 1){
		System.out.println("Bad group " + i + "out of" + groups.size() +groups.get(i)); 
		return true;
	    }
	}
	return false;
    }

    //returns a child based on the parent and option parameters - NOTE a.child(b) will be different to b.child(a)
    //TODO: add parameters to child so it can select mutation etc
    public Chromosome child(Chromosome parent)
    {
	double rand = 0;
	Chromosome out = parent.clone();
	//if(crossMethod == .....)
	rand = Math.random();
	if(rand < crossPara){
	    out = inversion().greedyCrossover(parent.inversion());
	    if(out.checkSizes()) System.out.println("Bad Crossover");}
	//if(mutMethod == .....)
	rand = Math.random();
	if(rand < mutPara){

	    
	    out = out.mutation();
	    if(out.checkSizes()) System.out.println("Bad Mutation");}

	rand = Math.random();
	if(rand < invPara){
	    out.invertApply();
	}
	
	if(ff.evaluate(out) < ff.evaluate(parent) && ff.evaluate(out) < ff.evaluate(this)){
	    
	    out = inversion().greedyCrossover(parent.inversion());
	    
          }

        // Warning KLUDGE BELOW
        // greedyCrossover misses cleanup for about 1 in 100000 individuals
        // on some configs on some data .. need to search for cause 
        // of bug though fixing unlikely to yield large improvement.

        if(out.checkSizes()){
	    System.out.println("******* greedyCrossover cleaup failure *****");
	    System.out.println(" returning parent ");
	    return parent;
	}
	    
	out.sort();	    
        childCount++;	

	return out;
    }

    public static int getChildCount(){
        return childCount;
    }


    // CSYIP modified
    public Chromosome clone()
    {
	Chromosome toreturn = new Chromosome(numstudents,groupsize,
					     crossMethod, crossPara, 
					     mutMethod, mutPara, ff);

	for(int i=groups.size()-1;i>=0;i--)
	{
		Group temp = new Group();

		for(int j=0;j<groups.get(i).size();j++)
			temp.add(groups.get(i).get(j));

		toreturn.add(temp,0);
	}


        toreturn.inversionPermute = inversionPermute.clone();
	return toreturn;
    }

    public Group[] toArray(){
        return (Group []) groups.toArray(new Group[0]);
    }

    // use simple insertion sort
    public void sort()
    {
	int j;
	Group temp;

	for(int i = 0; i < groups.size(); i++)
	    groups.get(i).sort();
	
	 for (int i = 1; i < groups.size(); i++)
	     {
		 temp = groups.get(i);
		 j = i;
		 while((j > 0) && (groups.get(j-1).compare(temp) > 0))
		     {
			 groups.set(j, groups.get(j-1));
			 j = j - 1;
		     }
		 groups.set(j, temp);
	     }
    }
}





















