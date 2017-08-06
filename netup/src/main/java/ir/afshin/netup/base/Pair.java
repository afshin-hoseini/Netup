package ir.afshin.netup.base;

import java.io.Serializable;
import java.net.URLEncoder;

public class Pair<F extends String,S extends String> implements Serializable{
	
	private F first;
	private S second;
	ContentEncoder contentEncoder = null;

// ____________________________________________________________________

	public Pair(F first, S second) {

		init(first, second, null);
	}
// ____________________________________________________________________

	public Pair(F first, S second, ContentEncoder contentEncoder) {

		init(first, second, contentEncoder);
	}

// ____________________________________________________________________

	private void init(F first, S second, ContentEncoder contentEncoder) {

		this.first = first;
		this.second = second;
		this.contentEncoder = contentEncoder;
	}

// ____________________________________________________________________

	public String getFirst() {

		return first;
	}

// ____________________________________________________________________

	public String getSecond(String forUrl) {

		String encodedSecond = null;

		if(contentEncoder != null) {
			encodedSecond = contentEncoder.encodeContent(forUrl, first, second);
		}


		if(encodedSecond == null ||  encodedSecond.equals("")) {

			try {

				encodedSecond = URLEncoder.encode(second, "UTF-8");
			}catch (Exception e) {

				e.printStackTrace();
				encodedSecond=second;
			}
		}

		return encodedSecond;
	}

// ____________________________________________________________________

	public String getSecond() {

		return second;
	}

// ____________________________________________________________________

	String getSecondWithoutEncoding() {

		return second;
	}
// ____________________________________________________________________

}
