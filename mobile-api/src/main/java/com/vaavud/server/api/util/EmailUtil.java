package com.vaavud.server.api.util;

import java.util.regex.Pattern;

public final class EmailUtil {
	
	/* This validator uses the regular expression taken from the Perl implementation of RFC 822.
	 * @see <a href="http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html">Perl Regex implementation *
	 *      of RFC 822< /a>
	 * @see <a href="http://www.ietf.org/rfc/rfc0822.txt?number=822">RFC 822< /a>
	 */
	private static final String EMAIL_PATTERN = "(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t]"
		+ ")+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:"
		+ "\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:("
		+ "?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ "
		+ "\\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\0"
		+ "31]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\"
		+ "](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+"
		+ "(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:"
		+ "(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z"
		+ "|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)"
		+ "?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\"
		+ "r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?["
		+ " \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)"
		+ "?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t]"
		+ ")*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?["
		+ " \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*"
		+ ")(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t]"
		+ ")+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)"
		+ "*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+"
		+ "|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r"
		+ "\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:"
		+ "\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t"
		+ "]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031"
		+ "]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\]("
		+ "?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?"
		+ ":(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?"
		+ ":\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?"
		+ ":(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?"
		+ "[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\".\\[\\] "
		+ "\\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|"
		+ "\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>"
		+ "@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\""
		+ "(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t]"
		+ ")*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\"
		+ "\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?"
		+ ":[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\["
		+ "\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\".\\[\\] \\000-"
		+ "\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|("
		+ "?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;"
		+ ":\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[(["
		+ "^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\""
		+ ".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\"
		+ "]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\"
		+ "[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\"
		+ "r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] "
		+ "\\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]"
		+ "|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\".\\[\\] \\0"
		+ "00-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\"
		+ ".|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,"
		+ ";:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\"(?"
		+ ":[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*"
		+ "(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\"."
		+ "\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:["
		+ "^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]"
		+ "]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*("
		+ "?:(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\"
		+ "\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:("
		+ "?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=["
		+ "\\[\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t"
		+ "])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t"
		+ "])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?"
		+ ":\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|"
		+ "\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:"
		+ "[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\"
		+ "]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)"
		+ "?[ \\t])*(?:@(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\""
		+ "()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)"
		+ "?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>"
		+ "@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?["
		+ " \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,"
		+ ";:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t]"
		+ ")*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\"
		+ "\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?"
		+ "(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\"."
		+ "\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:"
		+ "\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\["
		+ "\"()<>@,;:\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])"
		+ "*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])"
		+ "+|\\Z|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\"
		+ ".(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z"
		+ "|(?=[\\[\"()<>@,;:\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:("
		+ "?:\\r\\n)?[ \\t])*))*)?;\\s*)";
	
	private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

	/**
	 * Validate email wrt. RFC 822.
	 * <p>
	 * The check is as follows:<p>
	 * 1. null check<p>
	 * 2. leading/trailing whitespace check<p>
	 * 3. rfc check
	 *
	 * @param email in question
	 * @return validity of email address
	 */
	public static boolean isValid(String email) {
		return email != null &&
			   email.length() == email.trim().length() &&
			   PATTERN.matcher(email).matches();
	}
	
	private EmailUtil() {
	}
}
