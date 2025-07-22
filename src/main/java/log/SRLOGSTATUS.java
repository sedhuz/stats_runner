package log;

public enum SRLOGSTATUS
{
	SUCCESS(0),
	SKIPPED(1),
	FAILURE(2);

	private final int code;

	SRLOGSTATUS(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
