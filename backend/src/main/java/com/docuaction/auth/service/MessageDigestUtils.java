package com.docuaction.auth.service;

import java.security.MessageDigest;

final class MessageDigestUtils {

	private MessageDigestUtils() {
	}

	static boolean constantTimeEquals(byte[] expected, byte[] actual) {
		return MessageDigest.isEqual(expected, actual);
	}
}

