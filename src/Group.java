import java.util.*;

//class for a group of students - simple implementation
//-may need to add remove(i) later on for greedy mutation;
public class Group
{
    ArrayList<String> students;

    //Empty consturctor
    public Group() 
    {
	students = new ArrayList<String>();
    }
    
    //constructor takes in an array of student numbers
    public Group(String[] stu)
    {
	students = new ArrayList<String>();
	set(stu);
    }

    //changes the group
    public void set(String[] stu)
    {
	students.clear();
	for(int i=0;i<stu.length;i++)
	    students.add(stu[i]);
    }

    //Removes a particular student, useful for greedy mutation
    public String remove(int index)
    {
	return students.remove(index);
    }

    //For adding students into an incomplete group
    public void add(String stu)
    {
	students.add(stu);
    }

    //For adding students into an incomplete group
    public void add(int index,String stu)
    {
	students.add(index, stu);
    }

    //accessor method
    String get(int index)
    {
	return students.get(index);
    }
    //returns the size
    int size()
    {
	return students.size();
    }

    //Useful to work out if a student is missing from a chromosone
    boolean contains(String particularstudent)
    {
	return students.contains(particularstudent);
    }

    public String toString()
    {
        String toret = "";
        for(int i=0;i<size();i++)
            toret+= get(i) + " ";

        return toret;
    }

    public String[] toArr()
    {
        String[] toret = new String[size()];

        for(int i=0;i<size();i++)
	{
            toret[i] = students.get(i);
        }
        return toret;
    }

    public Group clone()
    {
	Group toreturn = new Group();

	for(int i=0;i<students.size();i++)
	    toreturn.add(students.get(i));

	return toreturn;
    }

    // use simple insertion sort
    public void sort()
    {
	int j;
	String temp;

	 for (int i = 1; i < students.size(); i++)
	     {
		 temp = students.get(i);
		 j = i;
		 while((j > 0) && (students.get(j-1).compareTo(temp) > 0    ))
		     {
			 students.set(j, students.get(j-1));
			 j = j - 1;
		     }
		 students.set(j, temp);
	     }
	 
    }

    // WARNNING, before using this method, please ensure both groups are sorted
    // return +ve if "this > g2"
    // return -ve if "this < g2"
    // return 0 if "this = g2"
    public int compare(Group g2)
    {
	int size = g2.size();

	if(this.size() > size)
	    size = this.size();

	for(int i = 0; i < size; i++)
	    { 
		if(this.get(i).compareTo( g2.get(i)) > 0)
		    return 1;
		if(this.get(i).compareTo( g2.get(i)) < 0)
		    return -1;
	    }

	if(this.size() == g2.size())
	    return 0;
	else
	    return this.size() - g2.size();
    }
}
