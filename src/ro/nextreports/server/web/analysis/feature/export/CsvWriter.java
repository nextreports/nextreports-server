package ro.nextreports.server.web.analysis.feature.export;

import java.io.OutputStream;
import java.io.PrintWriter;

public class CsvWriter {
	private final PrintWriter out;
	private boolean first = true;

	public CsvWriter(OutputStream os) {
		out = new PrintWriter(os);
	}

	public CsvWriter write(Object value) {
		if (!first) {
			out.append(",");
		}
		out.append("\"");
		if (value != null) {
			out.append(value.toString().replace("\"", "\"\"").replace("\n", " "));
		}
		out.append("\"");
		first = false;
		return this;
	}

	public CsvWriter endLine() {
		out.append("\r\n");
		first = true;
		return this;
	}

	public CsvWriter flush() {
		out.flush();
		return this;
	}

	public void close() {
		out.close();
	}
}