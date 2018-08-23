package org.bb.vityok.novinar.feed;

/**
 * Common exception for all possible problems with downloading, parsing and
 * updating feeds.
 *
 */
public class FeedHandlingException extends Exception {
	public FeedHandlingException(String msg) {
		super(msg);
	}
}
