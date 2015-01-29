package ro.nextreports.server.web.analysis.feature.export;

public class Pager {
	
	private final int p;
	private final int t;

	public Pager(int perPage, int total) {
		this.p = perPage;
		this.t = total;
	}

	public int pages() {
		return t / p + ((t % p > 0) ? 1 : 0);
	}

	public int offset(int page) {
		return p * page;
	}

	public int count(int page) {
		return Math.min(offset(page) + p, t);
	}
}