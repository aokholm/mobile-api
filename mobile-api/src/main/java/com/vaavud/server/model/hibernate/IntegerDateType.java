package com.vaavud.server.model.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * A Hibernate custom type that for storing java.util.Date as an integer, representing
 * the milliseconds value of the date.
 * <p>
 * This custom type has the following useful features:
 * <ul>
 * <li>It is an exact, lossless representation of java.util.Date, as opposed to using MySQL DATETIME or TIMESTAMP,
 * none of which have milliseconds precision.</li>
 * <li>It has no problems related to daylight savings time, as opposed to using MySQL DATETIME, for which a
 * DATETIME value may represent two different points in time, in a period where daylight savings time changes.
 * It is unclear whether MySQL TIMESTAMP also has this problem, i.e. whether MySQL stores a TIMESTAMP as a
 * seconds-since-epoch value or as a formatted date string.</li>
 * <li>It has no problems with time zone conversions at either the JDBC driver level or the database level.
 * Although it is not well documented, the MySQL JDBC driver performs time zone conversions for DATETIME,
 * and maybe also for TIMESTAMP. And MySQL itself performs time zone conversions for TIMESTAMP at the database
 * level. These conversions are done based on the time zone of the JDBC client. Furthermore it is unclear whether
 * these conversions can be trusted to perform correct conversions in all cases, e.g. in relation to daylight savings
 * time. In particular, the JDBC driver has a number of suspect time zone conversion options that suggest otherwise.</li>
 * </ul>
 * <p>
 * Note: This type assumes that a java.util.Date is used as an immutable value.
 *
 * @author Esben Krag Hansen
 */
public class IntegerDateType implements UserType {

    /**
     * The SQL type used for storing the milliseconds value.
     */
    private static int SQL_TYPE = Types.BIGINT;

	/**
     * Returns true if the two specified objects are equal, taking null into account, false if not.
     *
     * @param obj1 the first object, or null if none.
     * @param obj2 the second object, or null if none.
     * @return true if the two specified objects are equal, taking null into account, false if not. 
     */
    private static Boolean equal(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }
        else {
            return obj1.equals(obj2);
        }
    }


    /**
     * Returns the hash code of the specified object, which can possibly be null.
     *
     * @param obj the object, or null if none.
     * @return the hash code of the specified object, or 0 if null was specified.
     */
    private static int getHashCode(Object obj) {
        return (obj != null) ? obj.hashCode() : 0;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { SQL_TYPE };
    }

    @Override
    public Class returnedClass() {
        return Date.class;
    }

    @Override
    public boolean isMutable() {
        return false; // (we assumed that a java.util.Date is always used as an immutable value)
    }

    @Override
    public Object deepCopy(Object value) {
        return value; // (since value is assumed immutable)
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Date) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original; // (since value is assumed immutable)
    }

    @Override
    public boolean equals(Object value1, Object value2) {
        return equal(value1, value2);
    }

    @Override
    public int hashCode(Object value) throws HibernateException {
        return getHashCode(value);
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        long dateValue = resultSet.getLong(names[0]);

        if (resultSet.wasNull()) {
            return null;
        }
        return new Date(dateValue);
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, SQL_TYPE);
        }
        else {
            statement.setLong(index, ((Date) value).getTime());
        }
    }
}
