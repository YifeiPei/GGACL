import java.io.*;
import java.util.ArrayList;
public class Data
{
    private ArrayList<String> id;
    private ArrayList<Integer> val;

    public Data()
    {
	id = new ArrayList<String>();
	val = new ArrayList<Integer>();
    }

    public Data(int size)
    {
	id = new ArrayList<String>(size);
	val = new ArrayList<Integer>(size);
    }

    public int getNumData()
    {
	return id.size();
    }

    public String[] getId()
    {
	String[] out = new String[id.size()];
	for(int i = 0; i < id.size(); i++)
	    out[i] = id.get(i);//.intValue();

	return out;
    }

    public int[] getValue()
    {
	int[] out = new int[val.size()];
	for(int i = 0; i < val.size(); i++)
	    out[i] = (int)val.get(i).intValue();

	return out;
    }

    public boolean contains(String ID)
    {
	return id.contains(ID);
    }

    public void addData(String ID, int value)
    {
	id.add(ID);
	val.add(new Integer(value));
    }

    public void addData(String ID, int value, int idx)
    {
	id.add(idx, ID);
	val.add(idx, new Integer(value));
    }

    // This method will make change to both id and val
    // the val will be sorted to increasing order
    // and the id will be sort according to the corresponded val
    public void sort()
    {
	quicksort(id, val, 0, id.size()-1);
    }

    private void quicksort(ArrayList<String> id, ArrayList<Integer> val, 
			   int lo, int hi)
    {
	int left;
	int right;
	int pivot;
	String temp_id;
	Integer temp_val;
	
	//Base case
	if (lo >= hi)
	    return; //data is already sorted
	
	//Recursive case
	left = lo;
	right = hi;

	pivot = val.get((left+right)/2).intValue();

	//Note: this for loop has a middle-exit!
	for(;;)
	{
		while (val.get(left).intValue() < pivot)
		    left++;
		

		while (val.get(right).intValue() > pivot)
		    right--;
	     
		if (left >= right)
		    break;
		
		temp_id = id.get(left);
		temp_val = val.get(left);
		id.set(left, id.get(right));
		val.set(left, val.get(right));
		id.set(right, temp_id);
		val.set(right, temp_val);
		left++;
		right--;
	}

	quicksort(id, val, lo, left-1);
	quicksort(id, val, right+1, hi);
    }


    public void save(String filename) throws IOException
    {
	BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
	for(int i=0;i<id.size();i++) {
	    writer.write(id.get(i)+" "+val.get(i)+"\r\n");
	}
	writer.close();
    }
    public void load(File file) throws IOException
    {
	id = new ArrayList<String>();
	val = new ArrayList<Integer>();
	BufferedReader reader = new BufferedReader(new FileReader(file));
	String line = reader.readLine();
	while(line != null) {
	    String[] result = line.split(" ");
	    id.add(result[0]);
	    val.add(new Integer(result[1]));
	    line = reader.readLine();
	}
    }
    
}
