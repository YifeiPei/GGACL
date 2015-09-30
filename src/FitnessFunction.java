import java.util.*;

public class FitnessFunction
{
    public static final int POLYNOMIAL = 0;
    public static final int EXPONENTIAL = 1;
    public static final int LOGARITHMIC = 2;
    //private static int callCount = 0;

    private Hashtable<String,Integer> studentlookup;
    private double a_inter;
    private double b_inter;
    private double a_sd_intra_sd;
    private double b_sd_intra_sd;
    private double a_intra_sd;
    private double b_intra_sd;
    private double max_inter;
    private double max_sd_intra_sd;
    private double max_intra_sd;
    private int evaluate_mode;
    private double global_mean;
    

  

    public FitnessFunction(String[] studentnumbers, int[] studentmetric, double a_inter, 
			   double b_inter,double a_sd_intra_sd, double b_sd_intra_sd, double a_intra_sd, 
			   double b_intra_sd, double max_inter, double max_sd_intra_sd, 
			   double max_intra_sd, int mode)
    {
	this.a_inter = a_inter;
	this.b_inter = b_inter;
	this.a_sd_intra_sd = a_sd_intra_sd;
	this.b_sd_intra_sd = b_sd_intra_sd;
	this.a_intra_sd = a_intra_sd;
	this.b_intra_sd = b_intra_sd;
	this.max_inter = max_inter;
	this.max_sd_intra_sd = max_sd_intra_sd;
	this.max_intra_sd = max_intra_sd;	
	evaluate_mode = mode;

      global_mean = calcmean(studentmetric);

	studentlookup = 
	    new Hashtable<String,Integer>(studentnumbers.length);
	
	for(int i=0;i<studentnumbers.length;i++)
	    {
		studentlookup.put(studentnumbers[i],
				  new Integer(studentmetric[i]));
	    }
    }
    
    public double global()
    {
        return global_mean;
    }

    public int fitness(String studentnumber)
    {
	return studentlookup.get(studentnumber).intValue();
    }
    
    //Returs the keyset (list of student numbers, for use in checking which students are missing from a chromosone)
    //Note, any changes to the set will modify the hashtable
    public Set<String> studentNumbers()
    {
	return studentlookup.keySet();
    }
    
    public double calcmean(int[] array)
    {
	double sum = 0;	
	
	for(int i=0;i<array.length;i++)
	    {
		sum += array[i];
	    }
	
	return sum/array.length;
    }

    public double calcmean(double[] array)
    {
	double sum = 0;	
	
	for(int i=0;i<array.length;i++)
	    {
		sum += array[i];
	    }
	
	return sum/array.length;
    }

    public double calcvar(int[] array)
    {
	
	if(array.length == 1)
	    return 0;		
	
	double mean = calcmean(array);
	double var = 0;
	
	for(int i=0;i<array.length;i++)
	    {
		var += Math.pow(array[i] - mean,2);
	    }
	var /= array.length-1;
	return var;
    }

   public double calcvar(double[] array)
    {
	
	if(array.length == 1)
	    return 0;		
	
	double mean = calcmean(array);
	double var = 0;
	
	for(int i=0;i<array.length;i++)
	    {
		var += Math.pow(array[i] - mean,2);
	    }
	var /= array.length-1;
	return var;
    }

    // this method return an array with 3 elements
    // the frist element is the SD of intragroup means
    // the second element is the mean of intragroup standard deviation
    // the third element is the standard deviation of intragroup standard deviation

    public double[] detail(Chromosome c){
        return detail(c.toArray());
    }

    public double[] detail(Group[] g)
    {
	double[] out = {0,0,0};
	double[] means = new double[g.length];
	double[] var = new double[g.length];
	
	for(int i=0;i<g.length;i++)
	    {
		//get an array of student numbers
		String[] blah = g[i].toArr();
		
		String[] group = new String[blah.length];
		for(int j=0;j<group.length;j++)
		    group[j] = blah[j];
		
		double intra_var = 0;
		
		int[] values = new int[group.length];
		
		//convert it into an array of student metrics
		for(int j=0;j<group.length;j++)
		    values[j] = fitness(group[j]);
		
		means[i] = calcmean(values);
		
		//add on each intra group variance
		//add on each intra group standard deviation
		intra_var = calcvar(values);
		var[i] = Math.sqrt(intra_var);
		out[1] += var[i];
	    }

	out[0] = Math.sqrt(calcvar(means));	// SD of means
	out[1] /= g.length;                     // Mean of SD's
	out[2] = Math.sqrt(calcvar(var));       // SD of SD's
	return out;
    }
    
    // an adaptor function of a single arg with generation 
    // set to one.

    public double evaluate(Chromosome evalthis){
	return evaluate(evalthis,1);
    }


    public double evaluate(Group[] g){
        return evaluate(g,1);
    }

    public double evaluate(Chromosome evalthis, int gen){
        return evaluate(evalthis.toArray(),gen);
    }

    // the value should now be normalized
    // maximum is 1 but should never hit it
    public double evaluate(Group[] g,int gen)
    {
	//System.out.println(callCount++);
	//final double df = 20.0; dilution factor for arithmetic
        // reduction of log-base.
	double[] detail = detail(g);
	double out = 0;
	
	
	if(evaluate_mode == POLYNOMIAL)
	    out = a_intra_sd*Math.pow(detail[1]/max_intra_sd, b_intra_sd) -
		a_sd_intra_sd*Math.pow(detail[2]/max_sd_intra_sd, b_sd_intra_sd) - 
		a_inter*Math.pow(detail[0]/max_inter,b_inter);
	    
	else if(evaluate_mode == EXPONENTIAL)
	    out = a_intra_sd*(Math.expm1((detail[1]-max_intra_sd)*b_intra_sd)+1) 
		+ a_sd_intra_sd*(Math.expm1((-detail[2])*b_sd_intra_sd)+1)
		+ a_inter*(Math.expm1((-detail[0])*b_inter)+1);
	
	else if(evaluate_mode == LOGARITHMIC){
            //double baseMean = Math.max((b_inter-1)-(gen/df),1.005);
	    //double baseSDFactor = Math.max((b_sd_intra_sd-1)-(gen/df),1.005);
            double baseMean = 1 + Math.pow(0.98,gen) * b_inter;
            double baseSDFactor = 1 + Math.pow(0.98,gen) * b_sd_intra_sd;
	    
            // Note.. I have removed the Max SD term from the equation below
            //out = a_intra_sd*(Math.log1p(b_intra_sd-1)*detail[1]/max_intra_sd)/Math.log1p(b_intra_sd-1))

	    out = a_sd_intra_sd*(Math.log1p(-baseSDFactor*((detail[2]-max_sd_intra_sd)/max_sd_intra_sd))/
				 Math.log1p(baseSDFactor))
                  + a_inter*(Math.log1p(-((detail[0]-max_inter)/max_inter)*(baseMean))/
			     Math.log1p(baseMean));
	} 
	out /=  a_sd_intra_sd + a_inter + a_intra_sd;
       

	return out;
    }


   
    
    public String toString()
    {
	String out = "The fitness function:\n";
	if(evaluate_mode == POLYNOMIAL)
	    out += "f = [" + a_intra_sd + "*(intra_SD/" + max_intra_sd + ")^" 
		+ b_intra_sd + " + " + a_sd_intra_sd + "*(SD of intra_SD/" + max_sd_intra_sd + ")^" 
		+ b_sd_intra_sd + " - " +  a_inter + "*(inter/" + max_inter + ")^" 
		+ b_inter + "]/" + (a_intra_sd + a_sd_intra_sd + a_inter); 

	else if(evaluate_mode == EXPONENTIAL)
	    out += "f = {" + a_intra_sd + "*e^[(intra_SD - " + max_intra_sd + ")*"
		+ b_intra_sd + "] + " + a_sd_intra_sd + "*e^[-(SD of intra_SD)*" 
		+ b_sd_intra_sd + "] + " + a_inter + "*e^[(-inter)*" +  b_inter + 
		"]}/" + (a_intra_sd + a_sd_intra_sd + a_inter);

	else if(evaluate_mode == LOGARITHMIC)
	    out += "f = {" + a_intra_sd + "*ln[1 + (" + (b_intra_sd-1) + ")*intra_SD/" + max_intra_sd + "]/ln(" + b_intra_sd + ") + "
		+ a_sd_intra_sd + "*ln[1 - " + (b_sd_intra_sd-1) + "*((SD of intra_SD - " + max_sd_intra_sd + ")/" + max_sd_intra_sd + ")]/ln(" + b_sd_intra_sd + ") + "
		+ a_inter + "*ln[1 - " + (b_inter-1) + "*((inter_SD - " + max_inter + ")/" + max_inter + ")]/ln(" + b_inter + ")}/" 
		+ (a_intra_sd + a_sd_intra_sd + a_inter);

	return out;
    }
}
