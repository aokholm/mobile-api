package com.vaavud.server.model.migration;

import java.util.Date;


/**
 * Fancy ID generator that creates 20-character string identifiers with the
 * following properties:
 * 
 * 1. They're based on timestamp so that they sort *after* any existing ids.
 * 2. They contain 72-bits of random data after the timestamp so that IDs won't
 * collide with other clients' IDs.
 * 3. They sort *lexicographically* (so the timestamp is converted to characters
 * that will sort properly).
 * 4. They're monotonically increasing. Even if you generate more than one in
 * the same timestamp, the
 * latter ones will sort after the former ones. We do this by using the previous
 * random bits
 * but "incrementing" them by 1 (only in the case of a timestamp collision).
 * 
 * @author jfbyers@about.me 
 * @see <a href="https://gist.github.com/mikelehen/3596a30bd69384624c11#file-generate-pushid-js">
   Original port by mikelehen </a> 
   MODIFIED BY ANDREAS OKHOLM in order to generate FirebaseIDs, based on Tomcat IDs
 */



public class FirebasePushIdGenerator {
	// Modeled after base64 web-safe chars, but ordered by ASCII.
	private final static String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
	
	public static String generatePushId(Date date, Long objectId) {
		// Timestamp of last push, used to prevent local collisions if you push twice in one ms.
		long lastPushTime = 0L;

		// We generate 72-bits of randomness which get turned into 12 characters and
		// appended to the timestamp to prevent collisions with other clients. We store the last
		// characters we generated because in the event of a collision, we'll use those same
		// characters except "incremented" by one.
		char[] lastRandChars = new char[12];

		long now = date == null ? new Date().getTime() : date.getTime();

		boolean duplicateTime = (now == lastPushTime);

		char[] timeStampChars = new char[8];
		for (int i = 7; i >= 0; i--) {
			final long module = now % 64;
			timeStampChars[i] = PUSH_CHARS.charAt(Long.valueOf(module).intValue());
			now = (long) Math.floor(now / 64);
		}
		if (now != 0)
			throw new AssertionError("We should have converted the entire timestamp.");

		String id = new String(timeStampChars);
		if (!duplicateTime) {
			if (objectId != null) {
				for (int i = 11; i >= 0; i--) {
					final long module = objectId % 64;
					lastRandChars[i] = (char) Long.valueOf(module).intValue();
					objectId = (long) Math.floor(objectId / 64);
				}
			}
			else {
				for (int i = 0; i < 12; i++) {
					final double times = Math.random() * 64;
					lastRandChars[i] = (char) Math.floor(Double.valueOf(times).intValue());
				}
			}
		} else {
			// If the timestamp hasn't changed since last push, use the same random number,
 			//except incremented by 1.
			int lastValueOfInt=0;
			for (int i = 11; i >= 0 && lastRandChars[i] == 63; i--) {
				lastValueOfInt = i;
				lastRandChars[i] = 0;
			}
			lastRandChars[lastValueOfInt]++;
		}
		for (int i = 0; i < 12; i++) {
			id += PUSH_CHARS.charAt(lastRandChars[i]);
		}
		if (id.length() != 20)
			throw new AssertionError("Length should be 20.");

		return id;
	};
	
}
