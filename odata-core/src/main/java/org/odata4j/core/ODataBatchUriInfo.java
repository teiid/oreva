package org.odata4j.core;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.urlencoder.ConversionUtil;

/**
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ODataBatchUriInfo implements UriInfo {
	
	final private URI requestUri;
	final private URI baseUri;

	public ODataBatchUriInfo(URI fullUri, URI baseUri) {
		this.requestUri = fullUri;
		this.baseUri = baseUri;
	}

	@Override
	public URI getAbsolutePath() {
		return UriBuilder.fromUri(requestUri).replaceQuery("").fragment("").build();
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		return UriBuilder.fromUri(getAbsolutePath());
	}

	@Override
	public URI getBaseUri() {
		return baseUri;
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		return UriBuilder.fromUri(getBaseUri());
	}

	@Override
	public List<Object> getMatchedResources() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getMatchedURIs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getMatchedURIs(boolean arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPath() {
		return getPath(true);
	}

	@Override
	public String getPath(boolean decode) {
		if (decode) {
			return ConversionUtil.decodeString(getEncodedPath());
		} else {
			return getEncodedPath();
		}
		
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		return getPathParameters(true);
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<PathSegment> getPathSegments() {
		return getPathSegments(true);
	}

	@Override
	public List<PathSegment> getPathSegments(boolean decode) {
	  throw new NotImplementedException("OdataBachUriInfo.getPathSegments() has not been implemented");
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return getQueryParameters(true);
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		return ConversionUtil.decodeQueryString(getRequestUri());
	}

	@Override
	public URI getRequestUri() {
		return requestUri;
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		return UriBuilder.fromUri(getRequestUri());
	}

	private String getEncodedPath() {
		return getRequestUri().getRawPath().substring(getBaseUri().getRawPath().length());
	}

}
