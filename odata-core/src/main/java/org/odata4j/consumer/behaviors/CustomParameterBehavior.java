package org.odata4j.consumer.behaviors;

import org.odata4j.consumer.ODataClientRequest;

public class CustomParameterBehavior implements OClientBehavior {

	private final String name;
	private final String value;

	public CustomParameterBehavior(String name, String value) {
		this.name = name;
		this.value = value;
	}

	 @Override
	 public ODataClientRequest transform(ODataClientRequest request) {
		 return request.queryParam(this.name, this.value);

	}

}