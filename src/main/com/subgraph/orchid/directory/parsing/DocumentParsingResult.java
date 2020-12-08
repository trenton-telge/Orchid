package com.subgraph.orchid.directory.parsing;

import java.util.List;

@SuppressWarnings("unused")
public interface DocumentParsingResult<T> {
	T getDocument();
	List<T> getParsedDocuments();
	boolean isOkay();
	boolean isInvalid();
	T getInvalidDocument();
	boolean isError();
	String getMessage();
}
