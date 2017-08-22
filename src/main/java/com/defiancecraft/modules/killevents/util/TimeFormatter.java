package com.defiancecraft.modules.killevents.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

public class TimeFormatter {

	public TimeFormatter() {}
	
	public static class Builder {
		
		private List<Formatter> formatters = new ArrayList<>();
				
		public Builder() {}
		
		public Builder days(String suffix) {
			formatters.add(TimeFormatters.DAYS);
		}
		
		public Builder days(String suffix, boolean showZero) {
			
		}
		
	}
	
	interface Formatter {
		String format(Duration d);
	}

	enum TimeFormatters {
		DAYS((d)    -> Long.toString( d.getSeconds() / (60 * 60 * 24) )),
		HOURS((d)   -> Long.toString( d.getSeconds() % (60 * 60 * 24) / (60 * 60) )),
		MINUTES((d) -> Long.toString( d.getSeconds() % (60 * 60) / 60 )),
		SECONDS((d) -> Long.toString( d.getSeconds() % 60 ));
		
		private Formatter formatter;
		
		TimeFormatters(Formatter formatter) {
			this.formatter = formatter;
		}
		
		public String format(Duration duration) {
			return this.formatter.format(duration);
		}
	}
	
	public class DummyFormatter implements Formatter {
		private String dummyText;
		
		public DummyFormatter(String dummyText) {
			this.dummyText = dummyText;
		}
		
		public String format(Duration d) {
			return dummyText;
		}
	}
	
}
