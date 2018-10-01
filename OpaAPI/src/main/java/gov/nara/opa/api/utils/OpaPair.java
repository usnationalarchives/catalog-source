package gov.nara.opa.api.utils;

public class OpaPair {

	public final String fst;
	public final String snd;

	public OpaPair(String fst, String snd) {
		this.fst = fst;
		this.snd = snd;
	}

	public String toString() {
		return "OpaPair[" + fst + "," + snd + "]";
	}

	private static boolean equals(Object x, Object y) {
		return (x == null && y == null) || (x != null && x.equals(y));
	}

	public boolean equals(Object other) {
		return other instanceof OpaPair && equals(fst, ((OpaPair) other).fst)
				&& equals(snd, ((OpaPair) other).snd);
	}
}
