import java.util.Random;
import java.io.File;

public class PerfectData
{
    public static void main(String[] args)
    {
	Random rand = new Random();

	double mean = 50;
	double sd = 10;
	int numOfGroup = 30;
	int groupSize = 4;
	int min_val = 0;
	int max_val = 100;
	String outputFile = null;
	Data data = new Data();

	double sum_val = 0;
	double sum_diff_square = 0;

	double a = 0;
	double b = 0;
	double c = 0;
	double delta = 0;

	int[] group;

	boolean found = false;
	double max_sd = 0;

	if (args.length != 7 && args.length != 2)
	{
		System.out.println("Usage: ");
	    	System.out.println("1. java PerfectData <number of group> <group size> <mean> <standard deviation> <min score> <max score> <output file>");
	    	System.out.println("1. java PerfectData <file> <group size>");
		
	    	return;
	}

	try{
	    if(args.length == 7)
		{
		    numOfGroup = Integer.parseInt(args[0]);
		    groupSize = Integer.parseInt(args[1]);
		    mean = Double.parseDouble(args[2]);
		    sd = Double.parseDouble(args[3]);
		    min_val = Integer.parseInt(args[4]);
		    max_val = Integer.parseInt(args[5]);
		    outputFile = args[6];
		    group = new int[groupSize];
		    
		    // ---------- work out maximum possible standard deviation with given mean, group size, min and max value -------- //
		    		    // to find out the maximum possible standard deviation, a simple ideal is to maximum the difference between the data and the mean
		    // ie. group of 4, mean = 50, then SD is maximized to 57.73 with group [100, 100, 0, 0]
		    // for every group with even number of members, maximum SD occur exactly when mean = 50 
		    
		    // an interesting finding is, for group with odd number of members, the maximum SD is not occur at mean = 50
		    // indeed, the maximum SD for group of n (odd) members is same as the maximum SD for group of n+1
		    double[] temp_group = new double[groupSize];
		    double temp_total = groupSize%2 == 0? groupSize*mean : (groupSize+1)*mean;

		    for(int i = 0; i < groupSize && temp_total > 0; i++)
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

		    for(int i = 0; i < groupSize; i++)
			max_sd += (temp_group[i]- mean)*(temp_group[i]- mean)/(groupSize-1);
		    
		    max_sd = Math.sqrt(max_sd);
		    // ------------------------------------------------------------------------------------------------------------- //

		    if(sd > max_sd-0.5)
			{
			    System.out.println("The value of standard deviation is too high with current condition.\nTry less than " + max_sd + 
					       " (a bit less than to allow errors)");
			    return;
			}

		    for(int i = 0; i < numOfGroup; i++)
			{
			    found = false;
			    do{
				sum_val = 0;
				sum_diff_square = 0;
				for(int j = 0; j < groupSize - 2; j++)
				    group[j] = min_val + rand.nextInt(max_val - min_val);
				
				for(int j = 0; j < groupSize - 2; j++)
				    {
					sum_val += group[j];
					sum_diff_square += (group[j] - mean)*(group[j] - mean);
				    }
				
				// solve the second order equation
				a = 2;
				b = -2*(groupSize*mean - sum_val);
				c = mean*mean + ((groupSize-1)*mean - sum_val)*((groupSize-1)*mean - sum_val) + sum_diff_square - (groupSize-1)*sd*sd;
				
				delta = b*b - 4*a*c;
				if(delta >= 0)
				    {
					group[groupSize-2] = (int)((-b + Math.sqrt(delta))/(2*a) + 0.5);
					    group[groupSize-1] = (int)((groupSize*mean - sum_val - group[groupSize-2]) + 0.5);
					
					// don't need this pair as x1 = y2 and y1 = x2
					//x2 = (-b - Math.sqrt(delta))/(2*a);
					//y2 = (groupSize*mean - sum_val - x2);
					
					if(group[groupSize-2] >= 0 && group[groupSize-2] <= 100 
					   && group[groupSize-1] >= 0 && group[groupSize-1] <= 100)
					    found = true;
				    }   
			    }while(!found);
			
			    
			    for(int j = 0; j < groupSize; j++)
			    data.addData(String.valueOf(i*groupSize+j+1), group[j]);
			}

		    try{
			data.save(outputFile);
		    }
		    catch (java.io.IOException e){
			e.printStackTrace();
			System.exit(0);
		    }
			    
		}

	    else
		{
		    groupSize = Integer.parseInt(args[1]);

		    double[] intra;
		    double inter = 0;
		    double[] mean_arr;
		    double mean_of_mean = 0;
		    double mean_of_var = 0;
		    double mean_of_SD = 0;
		    int[] val;

		    try{
			data.load(new File(args[0]));
		    }
		    catch (java.io.IOException e){
			e.printStackTrace();
			System.exit(0);
		    }
		    intra = new double[data.getNumData()/groupSize];
		    mean_arr = new double[data.getNumData()/groupSize];
		    val = data.getValue();
		    
		    for(int i = 0; i < data.getNumData(); i+=groupSize)
			{
			    for(int j = 0; j < groupSize; j++)
				mean_arr[i/groupSize] += val[i+j];
			    
			    mean_arr[i/groupSize]/= groupSize;
			    
			    for(int j = 0; j < groupSize; j++)
				intra[i/groupSize] += (val[i+j] - mean_arr[i/groupSize])*(val[i+j] - mean_arr[i/groupSize]);
			    
			    intra[i/groupSize] /= groupSize -1;
			    
			    mean_of_mean += mean_arr[i/groupSize];
			    mean_of_var += intra[i/groupSize];
			    mean_of_SD += Math.sqrt(intra[i/groupSize]);
			}
		    mean_of_mean /= (data.getNumData()/groupSize);
		    mean_of_var /= (data.getNumData()/groupSize);
		    mean_of_SD /= (data.getNumData()/groupSize);
		    
		    for(int i = 0; i < mean_arr.length; i++)
			inter += (mean_arr[i] - mean_of_mean)*(mean_arr[i] - mean_of_mean);
		    
		    inter /= (data.getNumData()/groupSize - 1);
		    
		    for(int i = 0; i < mean_arr.length; i++)
			{
			    System.out.println("mean: " + mean_arr[i]);
			    System.out.println("intra: " + intra[i]);
			    System.out.println("intra_SD: " + Math.sqrt(intra[i]));
			    System.out.println("");
			}

		    System.out.println("Inter-group variance = " + inter);
		    System.out.println("mean Intra-group variance = " + mean_of_var);
		    System.out.println("mean Inter-group standard deviation = " + mean_of_SD);
		}
	}
	
	catch(NumberFormatException e){
	    System.out.println("Invalid Group size: "+args[1]);
	    System.out.println("Only positive number is accepted");
	    throw e;
	}
	
    }
   
}
