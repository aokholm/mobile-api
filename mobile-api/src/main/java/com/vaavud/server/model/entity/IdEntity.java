package com.vaavud.server.model.entity;

import java.io.Serializable;

import org.hibernate.proxy.HibernateProxyHelper;

public abstract class IdEntity implements Serializable {

	/**
	 * Subclasses may change the return type and visibility as they please.
	 * 
	 * @return the ID of the entity.
	 */
	public abstract Long getId();
	
	@Override
	public final int hashCode() {
		return 31 + ((getId() == null) ? 0 : getId().hashCode());
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		// Note: we assume that "this" and the other object are subclasses of IdEntity,
		// and that they can only be equal if one is a subclass of the other (or they are the same class).
		// If an entity is lazy loaded and is declared as an abstract type (say Owner), it may actually
		// be some other concrete subtype (say User). So if object a is an Owner, and b is a User, they
		// might be equal. On the other hand, if a turns out to be a Challenge, they are not equal, but
		// the ID comparison is gonna catch this. So this only breaks if the declared type is something
		// that doesn't guarantee its subclasses to have unique IDs among one another.
		Class<?> thisCls = HibernateProxyHelper.getClassWithoutInitializingProxy(this);
		Class<?> thatCls = HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
		if (thisCls == IdEntity.class || thatCls == IdEntity.class) {
			throw new IllegalArgumentException("Expected subclass of IdEntity");
		}
		if (!thisCls.isAssignableFrom(thatCls) && !thatCls.isAssignableFrom(thisCls)) {
			return false;
		}
				
		IdEntity other = (IdEntity) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		}
		else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
}
