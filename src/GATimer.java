// DO NOT MODIFY THIS FILE

public class GATimer
{
	private long start_time = 0;
	public void start()
	{
		start_time = System.currentTimeMillis();
	}
	public long getTime()
	{
		return (System.currentTimeMillis() - start_time);
	}
}
