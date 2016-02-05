package ir.afshin.netup.base;

import java.io.Serializable;

public class Pair<F,S> implements Serializable{
	
	public F first;
	public S second;
	
	public Pair(F first, S second) {
		
		this.first = first;
		this.second = second;
	}

}
