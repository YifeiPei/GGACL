import java.util.Random;
import java.io.File;

public class GaussianData
{
    public static void main(String[] args)
    {
	Random rand = new Random();

	double mean = 50;
	double sd = 10;
	int min_val = 0;
	int max_val = 100;
	int size = 120;
	String outputFile = null;
	Data data = new Data();
	int id = 1;
	int mark = 0;

	if (args.length != 6)
	{
	    System.out.println("Usage: ");
	    System.out.println("1. java GaussianData <mean> <standard deviation> <min score> <max score> <class size> <output file>");
		
	    return;
	}

	try{
	    mean = Double.parseDouble(args[0]);
	    sd = Double.parseDouble(args[1]);
	    min_val = Integer.parseInt(args[2]);
	    max_val = Integer.parseInt(args[3]);
	    size = Integer.parseInt(args[4]);
	    outputFile = args[5];
	    
	    for(int i = 0; i < size; i++)
		{
		    // ignore out range data
		    do{
			mark = (int) (0.5 + mean + sd*rand.nextGaussian());
		    }
		    while(mark < min_val || mark > max_val);
		    data.addData(id++, mark);
		    

		    // chop
		    /*
		    mark = (int) (0.5 + mean + sd*rand.nextGaussian());
		    if(mark < min_val)
			mark = min_val;

		    if(mark > max_val)
			mark = max_val;
		    data.addData(id++, mark);
		    */
		}
	    try{
		data.save(outputFile);
	    }
	    catch (java.io.IOException e){
		e.printStackTrace();
		System.exit(0);
	    }
	    
	}
	

	catch(NumberFormatException e){
	    System.out.println("Invalid Group size: "+args[1]);
	    System.out.println("Only positive number is accepted");
	    throw e;
	}
	
    }
   
}
