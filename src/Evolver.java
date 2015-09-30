import java.io.*;
import java.util.Date;

public class Evolver
{
    public static final int END_BY_TIME = 0;
    public static final int END_BY_GEN = 1;
    public static final int END_BY_FITNESS = 2;
    
    private static GATimer timer = new GATimer();
    private static Data data = new Data();
    //private static int currentGen = 0;
    private static Population pop;
    private static FitnessFunction ff;
    private static boolean finish = false;
    private static double endPower = 50;
    private static double endTourSize = 10;

    private static String configFile = null;
    private static String dataFile = null;

    // define how the evolution ends, time or generation or something else
    private static int enddingMethod;
    private static int enddingCondition; 
    
    //---- These information will be taken in config file ----//
       
    // the minimum score from input data
    private static int min_val;

    // the maximum score from input data
    private static int max_val;

    // the population size 
    private static int popSize;
    
    // define the way to generate the initial population
    private static int initPop;
    
    // the group size 
    private static int groupSize;
    
    // the selection method is used for geneterating new population
    private static int selectionMethod;
    // the parameter for selection (eg. rank or size of tournament)
    private static int selectionPara;
    
    // the replacement method is used for geneterating new population
    private static int replaceMethod;
    // the parameter for replacement (may not need)
    private static int replacePara = 0;
    
    // the crossover method is used for geneterating new population
    private static int crossoverMethod;
    // the probability of crossover take place
    private static double crossProb;
    
    // the mutation method is used for geneterating new population
    private static int mutationMethod;
    // the probability of matution take place
    private static double mutatRate;

    // the parameters of setting up the fitness function
    private static int fitnessType;
    // intragroup variance
    private static double a_inter;
    private static double b_inter;

    // standard deviation of intergroup standard deviation
    private static double a_sd_intra_sd;
    private static double b_sd_intra_sd;

    // mean intergroup standard deviation
    private static double a_intra_sd;
    private static double b_intra_sd;

    private static boolean showDetail = true;
    private static boolean saveDetail = true;
    private static boolean simpleSave = true;
    

    public static void main(String[] args) throws IOException
    { 	
	if (args.length != 4) {
	    System.out.println("Usage: ");
	    System.out.println("java Evolver <End method> <End condition> <data file> <config file>");
	    return;
	}

	// ending method
	try {
	    if (args[0].toLowerCase().trim().equals("time")) 
		enddingMethod = END_BY_TIME;
	    else if (args[0].toLowerCase().trim().equals("generation")) 
		enddingMethod = END_BY_GEN;
	    else if (args[0].toLowerCase().trim().equals("fitness")) 
		enddingMethod = END_BY_FITNESS;
	    else throw new Exception();
	}
	catch (Exception e) {
	    System.out.println("Invalid end type: "+args[0]);
	    System.out.println("The valid end type are \"time\""
			       + ",  \"generation\" and \"fitness\"");
	    System.exit(0);
	}
	
	// ending condition
    	try {
	    enddingCondition = Integer.parseInt(args[1]);
	    if(enddingCondition < 0)
		throw new NumberFormatException();
    	}
    	catch (NumberFormatException e) {
	    System.out.println("Invalid number of seconds: "+args[0]);
	    System.exit(0);
    	}
    	
	configFile = args[3];
	dataFile = args[2];
	
    	if (!load_data(dataFile)) return;
        if (!load_config(configFile)) return;

	// striping needs sorted data
	data.sort();	

	// work out the possible maximum value for normaliztion
	// the minimum value for all of them should be 0
	double max_inter = maxSD(data.getNumData()/groupSize);
	double max_intra_sd = maxSD(groupSize);
	double max_sd_intra_sd = maxSD(data.getNumData()/groupSize);
	double powerStep = (endPower-1)/enddingCondition;
	double tourStep = (endTourSize - selectionPara)/enddingCondition;

	ff = new FitnessFunction(data.getId(), data.getValue(), 
				 a_inter, b_inter, a_sd_intra_sd, b_sd_intra_sd,
				 a_intra_sd, b_intra_sd, max_inter,
				 max_sd_intra_sd, max_intra_sd, fitnessType);

	pop = new Population(popSize, data.getId(), groupSize, initPop, 
			     selectionMethod, selectionPara, crossoverMethod, 
			     crossProb, mutationMethod, mutatRate, 
			     replaceMethod, replacePara, powerStep, tourStep, ff);

   
	// show Settings
	System.out.println(showSetting());

	// start the evolution
	System.out.println("\n\n ---------- Begin ---------- \n\n"); 
	System.out.println(showResult(pop, timer.getTime()));

	if(!showDetail)
	    System.out.println("\n\n ---------- running ---------- \n\n");

	
	BufferedWriter writer = null;
	Date date = null;
	String columns = "generation \t time \t best fitness \t average fitness \t"
	    + " worst fitness \t inter SD \t intra SD \t SD of intra SD\n";

	if(saveDetail)
	    {
		date = new Date();
		String filename = "Evolver.jave ran at" + date.toString().replace(':', '_') + ".out";
		writer = new BufferedWriter(new FileWriter(new File(filename)));
		writer.write(showSetting() + "\n\n");
		
		writer.write(columns);
		writer.write(showSummaryForFile(pop, timer.getTime()));
	    }
        if(enddingMethod == END_BY_GEN){
		    finish = pop.generation() >= enddingCondition;
        }
	timer.start();

	while(!finish)
	    {
	    
	    //System.out.println("\n ***********\n Pop(0) before: \n" + pop.get(0) + "*******\n");
		pop = pop.evolve();

		//System.out.println("\n ***********\n Pop(0) after: \n" + pop.get(0) + "*******\n");

		if(showDetail)
		    System.out.println(showSummary(pop, timer.getTime()));
		if(saveDetail && !simpleSave)
		    writer.write(showSummaryForFile(pop, timer.getTime()));
		
		if(enddingMethod == END_BY_TIME)
		    finish = timer.getTime() >= enddingCondition*1000;
		else if(enddingMethod == END_BY_GEN)
		    finish = pop.generation() >= enddingCondition;
		else if(enddingMethod == END_BY_FITNESS)
		    finish = pop.bestFitness() >= enddingCondition;

	    }


	// print out the result
	// should display more info
	System.out.println("\n\n ---------- End ---------- \n\n"); 
        System.out.println("new evaluation count:" +  Chromosome.getChildCount());
	System.out.println(showResult(pop, timer.getTime()));

	if(saveDetail)
	    {
		writer.write("\n\n");
		writer.write(showResultForFile(pop, timer.getTime()));
		writer.close();
	    }

    }

    // work out the maximum possible standard deviation of a given group size
    // used for both inter and intra group
    // the method is basical the same as what is done in PrefectData.java
    private static double maxSD(int numOfData)
    {
	double max_sd = 0;
	double mean =(max_val+min_val)/2 ;
	double[] temp_group = new double[numOfData];
	double temp_total = numOfData%2 == 0? numOfData*mean : (numOfData+1)*mean;
	
	for(int i = 0; i < numOfData && temp_total > 0; i++)
	    {
		if(temp_total >= max_val)
		    {
			temp_group[i] += max_val;
			temp_total -= max_val;
		    }
		else
		    {
			temp_group[i] += temp_total;
			temp_total = 0;
		    }
	    }
	
	for(int i = 0; i < numOfData; i++)
	    max_sd += (temp_group[i]- mean)*
		(temp_group[i]- mean)/(numOfData-1);
	
	return Math.sqrt(max_sd);
    }
    
    private static String showSetting()
    {
	String out = "";
 
	out += "data read from: " + dataFile + "\n";
	out += "config read from: " + configFile + "\n";
	out += "Number of student: " + data.getNumData()+ "\n";

	out += "Minimum score: " + min_val + "\n";
	out += "Maximum score: " + max_val + "\n";
	out += "Group size: " + groupSize + "\n";
	out += "Population size: " + popSize + "\n";

	out += "Initialize Population by: ";
	if(initPop == Chromosome.RANDOM) out += "Random\n";
	else if(initPop == Chromosome.GREEDY) out += "Greedy add\n";
	else if(initPop == Chromosome.STRIPING) out += "Striping\n";
	else if(initPop == Chromosome.STRIPING) out += "Striping2\n";
	else if(initPop == Chromosome.BEST) out += "BEST\n";
	
	out += "Selection method: ";
	if(selectionMethod == Population.ROULETTE) out += "Roulette\n";
	else if(selectionMethod == Population.RANK) out += "Rank\n";
	else if(selectionMethod == Population.TOURNAMENT) out += "Tournament\n";
	out += "Selection parameter: " + selectionPara + "\n";
	
	out += "Replace method: ";
	if(replaceMethod == Population.ELITISM) out += "Elitism\n";
	else if(replaceMethod == Population.NO_ELITISM) out += "no Elitism\n";
	out += "Replace parameter: " + selectionPara + "(not use now)\n";

        out += "Crossover method: " + "....\n";
	out += "Crossover probility: " + crossProb + "\n";
	
	out += "Mutation method: " + "....\n";
	out += "Mutation probility: " + mutatRate+ "\n";
    

	out += "Fitness Type: ";
	if(fitnessType == FitnessFunction.POLYNOMIAL) out += "polynomial\n";
	else if(fitnessType == FitnessFunction.EXPONENTIAL) out += "exponential\n";
	out += "weighting of intergroup variance: " + a_inter + "\n";
	out += "power of intergroup variance: " + b_inter + "\n";
	out += "weighting of standard deviation of intragroup standard deviation: " + a_sd_intra_sd + "\n";
	out += "power of standard deviation of intragroup standard deviation: " + b_sd_intra_sd + "\n";
	out += "weighting of mean intragroup standard deviation: " + a_intra_sd + "\n";
	out += "power of mean intragroup standard deviation: " + b_intra_sd + "\n";
	out += ff.toString();
	
	return out;
    }

    private static String showSummaryForFile(Population pop, long time)
    {
	Chromosome best = pop.bestChromosome();
	double[] detail = ff.detail(best);
	return pop.generation() + "\t" + (double)time/1000.0 + "\t" + pop.bestFitness() 
	    + "\t" + pop.averageFitness()+ "\t" + pop.worstFitness() +"\t"+ detail[0] +"\t" + detail[1] + "\t" + detail[2] +"\n";
    }

    // should print more info
    private static String showResultForFile(Population pop, long time)
    {
	Chromosome best = pop.bestChromosome();
	double[] detail = ff.detail(best);

	String out = showSummary(pop, time);
	
	out += "\nThe best solution is:\n" + best + "\n";
	out += "\nSD\tSD of Means\tSD of SD's\n";
	out += detail[1] + "\t" + detail[0] + "\t" + detail[2];
	return out;
    }

    private static String showResult(Population pop, long time)
    {
	Chromosome best = pop.bestChromosome();
	double[] detail = ff.detail(best);

	String out = showSummary(pop, time);
	
	out += "\nThe best solution is:\n" + best + "\n";
	out += "\nThe intergroup standard deviation is: " + detail[0] + "\n";
	out += "The mean intragroup standard deviation is: " + detail[1] + "\n";
	out += "The standard deviation of intragroup standard deviation is: " + detail[2] + "\n";
	
	return out;
    }

    private static String showSummary(Population pop, long time)
    {
	double[] detail = ff.detail(pop.bestChromosome());
	String out = "Generation no: " + pop.generation() + "\n";
	out += "Time spent: " + (double)time/1000.0 + "\n";
	out += "Best Fitness: " + pop.bestFitness() + "\n";
	out += "average Fitness: " + pop.averageFitness() + "\n";
	out += "worst Fitness: " + pop.worstFitness() + "\n";
	out += "inter group SD: "+detail[0] + "\n";
	out += "SD of SD: "+detail[2] + "\n";
	
	return out;
    }

    private static boolean load_data(String fstr)
    {
	File data_file = new File(fstr);
	try{
	    data.load(data_file);
	    return true;
	}
	catch(Exception e)
	    {
		System.out.println("Corrupted or Missing data file: "+fstr);
	    return false;
	    }
    }

    private static boolean load_config(String fstr)
    {
	//return true;

	File config_file = new File(fstr);
	FileReader freader = null;
	BufferedReader breader = null;
	String str;
	int temp;

	try {
	    freader = new FileReader(config_file);
	    breader = new BufferedReader(freader);
	}
	catch (Exception e) {
	    System.out.println("Missing config file: "+fstr);
	    return false;
	}

	/**********************  reading parameters  ************************/
	try {
	    
	    // min value
	    str = breader.readLine();
	    try {
		min_val = Integer.parseInt(str);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	    }

	    // group size
	    str = breader.readLine();
	    try {
		max_val = Integer.parseInt(str);
		if (max_val <= min_val) throw new Exception("Invalid maximum " +
							"score: " +str);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	    }

	    // group size
	    str = breader.readLine();
	    try {
		groupSize = Integer.parseInt(str);
		if (groupSize <= 0) throw new Exception("Invalid group " +
							"size: " +str);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	    }

	    // population
	    str = breader.readLine();
	    try {
		popSize = Integer.parseInt(str);
		if (popSize <= 0) throw new Exception("Invalid population " +
						      "size: " +str);
		if (popSize%2 != 0) throw new Exception("Population size" +
							" has to be even");
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
		throw e;
	    }

	    // random population
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("random")) 
		    initPop = Chromosome.RANDOM;
		else if (str.toLowerCase().trim().equals("striping")) 
		    initPop = Chromosome.STRIPING;
		else if (str.toLowerCase().trim().equals("greedy")) 
		    initPop = Chromosome.GREEDY;
		else if (str.toLowerCase().trim().equals("striping2")) 
		    initPop = Chromosome.STRIPING2;
		else if (str.toLowerCase().trim().equals("best")) 
		    initPop = Chromosome.BEST;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid selection type: "+str);
		System.out.println("The valid selection type are \"random\""
				   + ",  \"striping\" and \"greedy\"");
		throw e;
	    }

	    // seletion method
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("roulette")) 
		    selectionMethod = Population.ROULETTE;
		else if (str.toLowerCase().trim().equals("rank")) 
		    selectionMethod = Population.RANK;
		else if (str.toLowerCase().trim().equals("tournament")) 
		    selectionMethod = Population.TOURNAMENT;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid selection type: "+str);
		System.out.println("The valid selection type are \"roulette\""
				   + ",  \"rank\" and \"tournament\"");
		throw e;
	    }

	    // selection parameter

	    // note, for roulette, this parameter is not needed
	    // for tournament, this parameter is the tournament size
	    // for rank, this parameter should between 0 - 100 which
	    // will be divided by 100 to form the rank
	    str = breader.readLine();
	    try {
		selectionPara = Integer.parseInt(str);
		if (selectionMethod == Population.TOURNAMENT && 
		    (selectionPara > popSize || selectionPara < 0)) 
		    throw new Exception("Invalid tournament size: " +str);
		
		if (selectionMethod == Population.RANK && 
		    (selectionPara > 100 || selectionPara < 0)) 
		    throw new Exception("Invalid rank: " +str);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
		System.out.println("The valid selection parameter for \"rank\""
				   +" should be between 0 - 100 which will then"
				   +" be divided by 100 to form the rank");
		System.out.println("The valid selection parameeter for "
				   +"\"tournament\" should be between"
				   +" 0 - population size");
		throw e;
	    }

	    // replacement method
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("elitism")) 
		    replaceMethod = Population.ELITISM;
		else if (str.toLowerCase().trim().equals("no elitism")) 
		    replaceMethod = Population.NO_ELITISM;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid replacement type: "+str);
		System.out.println("The valid selection type are \"elitism\""
				   + " and \"no elitism\"");
		throw e;
	    }

	    // replacement para
	    str = breader.readLine();
	    try {
		replacePara = Integer.parseInt(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid replacement parameter: "+str);
		throw e;
	    }

	    // crossover type
	    str = breader.readLine();
	    try {
		crossoverMethod = 0;
	    }
	    catch (Exception e) {
		System.out.println("Invalid crossover type: "+str);
		System.out.println("The valid crossover type are .....");
		throw e;
	    }

	    // crossover probability
	    str = breader.readLine();
	    try {
		crossProb = Double.parseDouble(str);
		if (crossProb < 0.0 ||
		    crossProb > 1.0) throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid crossover probability: "+str);
		throw e;
	    }

	    // mutation type
	    str = breader.readLine();
	    try {
		mutationMethod = 0;
	    }
	    catch (Exception e) {
		System.out.println("Invalid muation type: "+str);
		System.out.println("The valid mutation type are .....");
		throw e;
	    }

	    // mutation probability
	    str = breader.readLine();
	    try {
		mutatRate = Double.parseDouble(str);
		if (mutatRate < 0.0 ||
		    mutatRate > 1.0) throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid mutation probability: "+str);
		throw e;
	    }
	    
	    // Fitness type
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("polynomial")) 
		    fitnessType = FitnessFunction.POLYNOMIAL;
		else if (str.toLowerCase().trim().equals("exponential")) 
		    fitnessType = FitnessFunction.EXPONENTIAL;
		else if (str.toLowerCase().trim().equals("logarithmic")) 
		    fitnessType = FitnessFunction.LOGARITHMIC;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid fitness type: "+str);
		System.out.println("The valid fitness type are \"polynomial\""
				   + ", \"exponential\" and \"logarithmic\"");
		throw e;
	    }

	    // intergroup standard deviation weight
	    str = breader.readLine();
	    try {
		a_inter= Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid intergroup standard deviation weight: "+str);
		throw e;
	    }

	    // intergroup standard deviation power
	    str = breader.readLine();
	    try {
		if(str.equals("e"))
		    b_inter = Math.E;
		else
		    b_inter= Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid intergroup standard deviation power: "+str);
		throw e;
	    }

	    // intragroup standard deviation weight
	    str = breader.readLine();
	    try {
		a_intra_sd = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid intragroup standard deviation weight: "+str);
		throw e;
	    }

	    // intragroup standard deviation power
	    str = breader.readLine();
	    try {
		if(str.equals("e"))
		    b_intra_sd = Math.E;
		else
		    b_intra_sd = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid intragroup standard deviation power: "+str);
		throw e;
	    }

	    // standard deviation of intragroup standard deviation weight
	    str = breader.readLine();
	    try {
		a_sd_intra_sd = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid standard deviation of intragroup standard deviation weight: "+str);
		throw e;
	    }

	    // standard deviation of intragroup standard deviation power
	    str = breader.readLine();
	    try {
		if(str.equals("e"))
		    b_sd_intra_sd = Math.E;
		else
		    b_sd_intra_sd = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid standard deviation of intragroup standard deviation power: "+str);
		throw e;
	    }

	    // end power
	    str = breader.readLine();
	    try {
		endPower = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid end power: "+str);
		throw e;
	    }

	    // end tournament size presentage
	    str = breader.readLine();
	    try {
		endTourSize = Double.parseDouble(str);
	    }
	    catch (Exception e) {
		System.out.println("Invalid end tournament presentage: "+str);
		throw e;
	    }

	    // show detail ?
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("show")) 
		    showDetail = true;
		else if (str.toLowerCase().trim().equals("not show")) 
		    showDetail = false;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid show type: "+str);
		System.out.println("The valid show option type are \"show\""
				   + " and \"not show\"");
		throw e;
	    }

	    // save detail ?
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("save"))
		    saveDetail = true;
		else if (str.toLowerCase().trim().equals("not save")) 
		    saveDetail = false;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid save type: "+str);
		System.out.println("The valid save option type are \"save\""
				   + " and \"not save\"");
		throw e;
	    }

	    // save simply?
	    str = breader.readLine();
	    try {
		if (str.toLowerCase().trim().equals("simple"))
		    simpleSave = true;
		else if (str.toLowerCase().trim().equals("not simple")) 
		    simpleSave = false;
		else throw new Exception();
	    }
	    catch (Exception e) {
		System.out.println("Invalid save type: "+str);
		System.out.println("The valid save option type are \"simple\""
				   + " and \"not simple\"");
		throw e;
	    }

	}
	
	catch (Exception e) {
	    System.out.println("Error reading from config file: "+fstr);
	    return false;
	}
	return true;

    }

}
