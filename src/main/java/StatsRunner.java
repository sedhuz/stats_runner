import db.DatabaseAPI;
import io.InputAPI;
import properties.PropertiesAPI;

public class StatsRunner
{
	public static void main(String[] args)
	{
		String query = InputAPI.getQuery();
		if (PropertiesAPI.isParallelEnabled()) {
			DatabaseAPI.executeParallely(query);
		} else {
			DatabaseAPI.execute(query);
		}
	}
}
