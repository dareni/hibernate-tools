package org.hibernate.tool.orm.jbt.api;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.tool.orm.jbt.wrp.Wrapper;

public interface SessionWrapper extends Wrapper {

	default String getEntityName(Object o) { return ((Session)getWrappedObject()).getEntityName(o); }

	default SessionFactory getSessionFactory() { return ((Session)getWrappedObject()).getSessionFactory(); }

	default Query<?> createQuery(String s) { return ((Session)getWrappedObject()).createQuery(s); }

	default boolean isOpen() { return ((Session)getWrappedObject()).isOpen(); }

	default void close() { ((Session)getWrappedObject()).close(); }

	default boolean contains(Object o) { 
		boolean result = false;
		try {
			result = ((Session)getWrappedObject()).contains(o);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			if (!(message.startsWith("Class '") && message.endsWith("' is not an entity class"))) {
				throw e;
			}
		}
		return result;
	}

}