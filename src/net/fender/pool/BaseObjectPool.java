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
 * @author Eric Fenderbosch
 * @param <T>
 */
public abstract class BaseObjectPool<T> implements ObjectPool<T> {

	protected int size;
	protected PoolableObjectFactory<T> factory;
	protected long lockTimeout = -1;
	protected TimeUnit lockTimeUnit = TimeUnit.MILLISECONDS;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Optional length of time to wait on borrowObject. <= 0 means block
	 * indefinitely. Default is -1.
	 * 
	 * @param lockTimeout
	 */
	public void setLockTimeout(long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public long getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * Optional unit of time to wait on borrowObject default is MILLISECONDS.
	 * 
	 * @param lockTimeUnit
	 */
	public void setTimeUnit(TimeUnit lockTimeUnit) {
		this.lockTimeUnit = lockTimeUnit;
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public String getTimeUnit() {
		return lockTimeUnit.name();
	}

	public void setFactory(PoolableObjectFactory<T> factory) {
		this.factory = factory;
	}

	public abstract void invalidateObject(T obj);

	public abstract T borrowObject() throws Exception;

	public abstract void returnObject(T obj);

}
