/*
 * Copyright 2008 - 2009 Eric Fenderbosch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fender.pool;

import java.util.concurrent.TimeUnit;

/**
 * Simpler version of Jakarta Commons ObjectPool, but supports Generics and
 * changes thrown Exceptions and adds lockTimeout/lockTimeUnit.
 * 
 * @author Eric Fenderbosch
 * @param <T>
 */
public interface ObjectPool<T> {

	/**
	 * @param size
	 */
	void setSize(int size);

	/**
	 * @return
	 */
	int getSize();

	/**
	 * @return
	 * @throws Exception
	 */
	T borrowObject() throws Exception;

	/**
	 * @param obj
	 */
	void invalidateObject(T obj);

	/**
	 * @param obj
	 */
	void returnObject(T obj);

	/**
	 * @param factory
	 */
	void setFactory(PoolableObjectFactory<T> factory);

	/**
	 * Length of time to wait on borrowObject. <= 0 means block.
	 * 
	 * @param lockTimeout
	 */
	void setLockTimeout(long lockTimeout);

	/**
	 * Unit of time to wait on borrowObject.
	 * 
	 * @param lockTimeUnit
	 */
	void setTimeUnit(TimeUnit lockTimeUnit);
}
