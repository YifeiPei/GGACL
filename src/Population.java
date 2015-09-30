import java.util.*;

public class Population
{
    public static final int ROULETTE = 0;
    public static final int RANK = 1;
    public static final int TOURNAMENT = 2;

    public static final int ELITISM = 0;
    public static final int NO_ELITISM = 1;

    private static int generation = 0;

    private FitnessFunction ff;
    private ArrayList<Chromosome> chromos;
    private double bestFitness = 0;
    private double averageFitness = 0;
    private double worstFitness = 0;
    private int bestChromoIndex = 0;
    private int worstChromoIndex = 0;
    private Chromosome bestChromo = null;
    private Chromosome worstChromo = null;
    private boolean evaluated = false;
    private double[] prob;
    private double rank;

    private int selMethod;
    private int selPara;
    private int mutMethod;
    private double mutPara;
    private int crossMethod;
    private double crossPara;
    private int replaceMethod;
    private int replacePara;
    private double powerStep;
    private double tourStep;

    // create new population with no chromosomes given
    public Population(int popSize, String[] studet_ids, int groupSize, 
		      int initPop, int selMethod, int selPara, 
		      int crossMethod, double crossPara, int mutMethod,
		      double mutPara, int replaceMethod, int replacePara,
		      double powerStep, double tourStep, FitnessFunction fitnessFunction)
    {
	this(selMethod, selPara, crossMethod, crossPara, mutMethod, mutPara, 
	     replaceMethod, replacePara, powerStep, tourStep, fitnessFunction);

	chromos = new ArrayList<Chromosome>();
	for(int i = 0; i < popSize; i++)
	    chromos.add(new Chromosome(studet_ids, groupSize, crossMethod,
				       crossPara, mutMethod, mutPara, initPop, ff));
	initialize();
    }

    // create a population with a set of chromosomes (used here)
    public Population(ArrayList<Chromosome> c, int selMethod, int selPara, 
		      int crossMethod, double crossPara, int mutMethod,
		      double mutPara, int replaceMethod, int replacePara,
		      double powerStep, double tourStep, FitnessFunction fitnessFunction)
    {
	this(selMethod, selPara, crossMethod, crossPara, mutMethod, mutPara, 
	     replaceMethod, replacePara, powerStep, tourStep, fitnessFunction);

	chromos = c;

	initialize();
    }

    private Population(int selMethod, int selPara, int crossMethod, 
		       double crossPara, int mutMethod, double mutPara, 
		       int replaceMethod, int replacePara, double powerStep,
		       double tourStep, FitnessFunction fitnessFunction)
    {
	ff = fitnessFunction;
	this.selMethod = selMethod;
	this.selPara = selPara;
	this.mutMethod = mutMethod;
	this.mutPara = mutPara;
	this.crossMethod = crossMethod;
	this.crossPara = crossPara;
	this.replaceMethod = replaceMethod;
	this.replacePara = replacePara;
	this.powerStep = powerStep;
	this.tourStep = tourStep;
    }

    public void initialize()
    {
	if(selMethod == RANK)
	    {
		ChromoComparator<Chromosome> cc = 
		    new ChromoComparator<Chromosome>();
		Collections.sort(chromos, cc);

		rank = selPara;
		while(rank > 1.0)
		    rank /= 10.0;
		    
	    }

	evaluate(); // may not need
	selectProb();
    }
    
    // return the selected chromosome
    public Chromosome get(int i)
    {
	return chromos.get(i);
    }
    
    private void remove(int i)
    {
	chromos.remove(i);
    }

    private void add(Chromosome c)
    {
	chromos.add(c);
    }

    // return the population size
    public int size()
    {
	return chromos.size();
    }

    // return the number of generation
    public int generation()
    {
	return generation;
    }
    
    // return the average Fitness of this population
    public double averageFitness()
    {
	return averageFitness;
    }

    // return the Fitness of the best chromosome in this population
    public double bestFitness()
    {
	return bestFitness;
    }

    // return the Fitness of the worst chromosome in this population
    public double worstFitness()
    {
	return worstFitness;
    }

    // return the best chromosome in this population
    public Chromosome bestChromosome()
    {
	return bestChromo.clone();
    }

    // return the worst chromosome in this population
    public Chromosome worstChromosome()
    {
	return worstChromo.clone();
    }

    public boolean isEvaluated()
    {
	return evaluated;
    }

    // evaluate the popluation
    // work out the best, average and worst fitness
    // and the best and worst chromosome
    public void evaluate()
    {
	double max = ff.evaluate(chromos.get(0),generation());
	double min = max;
	double sum = 0;
	int max_i = 0;
	int min_i = 0;

	double temp = 0;

	for(int i = 0; i < chromos.size(); i++)
	    {
		temp = ff.evaluate(chromos.get(i),generation());
		if(temp > max)
		    {
			max = temp;
			max_i = i;
		    }

		if(temp < min)
		    {
			min = temp;
			min_i = i;
		    }
		sum += temp;
	    }

	bestFitness = max;
	averageFitness = sum/chromos.size();
	worstFitness = min;
	bestChromoIndex = max_i;;
	worstChromoIndex = min_i;
	bestChromo = chromos.get(max_i);
	worstChromo = chromos.get(min_i);
	
	evaluated = true;
    } 

    // this method will generate the next generation of the population
    // according to the given seletion method and the replacement method
    // the use of this method will NOT affect current population
    public Population evolve()
    {
	ArrayList<Chromosome> tempPop = 
	    new ArrayList<Chromosome>(chromos.size());
	Chromosome u;
	Chromosome v;

	for(int i = 0; i < chromos.size()/2; i++)
	    {
		u = selectParent();
		v = selectParent();
		
		//System.out.println("*****Parent 1 : \n" + v);
		
        Chromosome c1 = u.child(v);
        Chromosome c2 = v.child(u);
		tempPop.add(c1);
		tempPop.add(c2);
		
       //System.out.println("****Generated new child: \n" + c1);
	    }	    
	
	// if needed, add in the code for keeping the best chromosome
	Population pop = new Population(tempPop, selMethod, selPara, 
					crossMethod, crossPara, mutMethod, mutPara, 
					replaceMethod, replacePara, powerStep, tourStep, ff);

	if(replaceMethod == ELITISM)
	    {
		pop.remove(worstChromoIndex);
		pop.add(bestChromo.clone());
		pop.initialize();
	    }

	generation++;
	return pop;
    }

    private Chromosome selectParent()
    {
    
	if(selMethod == ROULETTE){
	    Chromosome tempOld = chromos.get(roulette());
	    //System.out.println("*****Parent Old : \n" + tempOld);
	    Chromosome tempNew = tempOld.clone();
	    //System.out.println("*****Parent New : \n" + tempNew);
	    return tempNew;
	}

	else if(selMethod == RANK)
	    return chromos.get(rankMethod()).clone();

	else if(selMethod == TOURNAMENT){
	    Chromosome temptOld = chromos.get(tournament());
	    //System.out.println("*****Parent Old : \n" + temptOld);
	    Chromosome temptNew = temptOld.clone();
	    //System.out.println("*****Parent New : \n" + temptNew);
	    return temptNew;
	}
	
	else return null;
    }

    // spin the wheel in order to select the parent
    private int roulette()
    {
	return spinTheWheel();
    }

   // spin the wheel in order to select the parent
    private int rankMethod()
    {
	return spinTheWheel();
    }

   // spin the wheel few times, pick the best one to be the parent
    private int tournament()
    {
	int out = spinTheWheel();
	double max = ff.evaluate(chromos.get(out),generation());
	double temp_fitness = 0;
	int temp_index = 0;

	for(int i = 0; i < selPara - 1 + tourStep*generation(); i++)
	    {
		temp_index = spinTheWheel();
		temp_fitness = ff.evaluate(chromos.get(temp_index),generation());
		if(temp_fitness > max)
		    {
			max = temp_fitness;
			out = temp_index;
		    }
	    }
	return out;
    }

    // this method gives a random index to the chromosome array
    private int spinTheWheel()
    {
	int i;
	// generate a random number from 0 to 1
	double r = Math.random();
	// find the index where the random number lie in the 
	// probability array
	for(i = 0; i < prob.length && r > prob[i]; i++);

	// sometimes, due to the rounding error, the last 
	// accumulate probability is less than 1 and the search
	// will fail. 
	// when this case happen, return the last index
	if(i == prob.length)
	    i--;
	return i;
    }

    private void selectProb()
    {
	double[] fitness = new double[chromos.size()];
	double total = 0;
	double sum = 0;

	prob = new double[chromos.size()];

	// collect the fitness from chromosomes
	for(int i = 0; i < chromos.size(); i++)
	    {
		fitness[i] = Math.pow(ff.evaluate(chromos.get(i),generation()), 1 + powerStep*generation());
		total += fitness[i];
	    }
	
	// roulette method uses fitness to calculate
	if(selMethod == ROULETTE || selMethod == TOURNAMENT)
	    {
		prob[0] = fitness[0]/total;
		for(int i = 1; i < fitness.length; i++)
		    prob[i] = prob[i-1] + fitness[i]/total;
	    }

	/*
	// tournament uses uniform probability
	else if(selMethod == TOURNAMENT)
	    {
		prob[0] = 1/fitness.length;
		for(int i = 1; i < fitness.length; i++)
		    prob[i] = prob[i-1] + 1/fitness.length;
	    }
	*/

	// rank method calculate based on the rank
	else if(selMethod == RANK)
	    {
		prob[0] = rank;
		sum = rank;
		for(int i = 1; i < fitness.length; i++)
		    {
			prob[i] = (1 - sum)*rank;
			sum += prob[i];
		    }
	    }
    }

    // Note: this comparator imposes orderings that are 
    // inconsistent with equals.
    private class ChromoComparator<T> implements Comparator<T>
    {
	public int compare(T o1, T o2)  
	{
	    double a1 = ff.evaluate((Chromosome)o1,generation());
	    double a2 = ff.evaluate((Chromosome)o2,generation());

	    if(a1 > a2)
		return 1;
	    else if(a1 == a2)
		return 0;
	    else
		return -1;
		
	}
	
	public boolean equals(Object obj)
	{
	    return this.equals(obj);
	}
    }
}
