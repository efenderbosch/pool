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

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple pool of size 1 that uses a Semaphore to restrict use. It should
 * perform slightly better than a SimpleObjectPool of size 1.
 * 
 * @author Eric Fenderbosch
 * @param <T>
 */
public class SingleObjectPool<T> extends BaseObjectPool<T> {

	private static final Log log = LogFactory.getLog(SingleObjectPool.class);

	private Semaphore semaphore = new Semaphore(1, true);
	private T pooledObject;

	@Override
	public T borrowObject() throws Exception {
		// TODO tryAcquire throws InterruptedException. Catch? Wrap? Ignore?
		boolean aquired = semaphore.tryAcquire(lockTimeout, lockTimeUnit);
		if (aquired) {
			if (pooledObject == null) {
				try {
					pooledObject = factory.makeObject();
				} catch (Exception e) {
					semaphore.release();
				}
			}
			return pooledObject;
		}
		throw new PoolExhaustedException("Timeout (" + lockTimeout + " " + lockTimeUnit
				+ ") waiting for available connection.");
	}

	@Override
	public void returnObject(T returnedObject) {
		// Make sure we don't increase the size of the semaphore when the
		// returnedObject was not borrowed from this pool.
		if (pooledObject == null && returnedObject == null) {
			semaphore.release();
		} else if (pooledObject != null && pooledObject.equals(returnedObject)) {
			semaphore.release();
		} else {
			log.warn("invalid returnObject attempted " + returnedObject);
		}
	}

	@Override
	public void invalidateObject(T invalidObject) {
		if (pooledObject != null && pooledObject.equals(invalidObject)) {
			pooledObject = null;
			semaphore.release();
		} else {
			log.warn("invalid invalidateObject attempted " + invalidObject);
		}
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public void setSize(int size) {
		throw new UnsupportedOperationException("size is always 1");
	}
}
