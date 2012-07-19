package sim.app.episim.persistence;

public class ExportException extends RuntimeException {

	public ExportException() {
	}

	public ExportException(String message) {
		super(message);
	}

	public ExportException(Throwable cause) {
		super(cause);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause);
	}

}
