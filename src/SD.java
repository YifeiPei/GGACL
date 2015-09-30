import java.io.*;

public class SD
{
    public static void main(String[] args)
    {
	double mean = 0;
	double SD = 0;

	int[] values;
	int sum = 0;

	Data data = new Data();

	if (args.length != 1)
	    {
		System.out.println("Usage: ");
	    	System.out.println("1. java SD <file>");
		return;
	    }

	try
	    {
		data.load(new File(args[0]));

		values = data.getValue();
		
		for(int i = 0; i < values.length; i++)
		    sum += values[i];
		
		mean = (double)sum/values.length;
		sum = 0;
		
		for(int i = 0; i < values.length; i++)
		    sum += (values[i]-mean) * (values[i]-mean);
		
		SD = Math.sqrt(sum/(values.length-1));
		
		System.out.println("mean: " + mean);
		System.out.println("SD: " + SD);
	    }
	catch (java.io.IOException e)
	    {
		e.printStackTrace();
		System.exit(0);
	    }
    }
}
