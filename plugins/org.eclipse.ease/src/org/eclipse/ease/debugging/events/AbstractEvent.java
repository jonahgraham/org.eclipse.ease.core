package org.eclipse.ease.debugging.events;

import java.lang.reflect.Field;

public abstract class AbstractEvent implements IDebugEvent {

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append(getClass().getSimpleName()).append(": ");

		for (Field field : getClass().getDeclaredFields()) {
			result.append(field.getName()).append('(');
			try {
				field.setAccessible(true);
				result.append(field.get(this));
			} catch (Exception e) {
				result.append("<Exception while reading field>");
			}
			result.append("), ");
		}

		if (getClass().getFields().length > 0)
			result.delete(result.length() - 2, result.length());

		return result.toString();
	}
}
