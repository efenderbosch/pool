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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Uses a Semaphore to control access to a ConcurrentLinkedQueue to implement a
 * FIFO pool. The semaphore is needed because ConcurrentLinkedQueue is
 * unbounded. If you need something more complex than this, then Jakarta Commons
 * GenericObjectPool is probably a better fit than extending this class.
 * 
 * @author Eric Fenderbosch
 */
public class SimpleObjectPool<T> extends BaseObjectPool<T> {

	private Semaphore semaphore;
	private ConcurrentLinkedQueue<T> pool;
	private AtomicInteger invalidations = new AtomicInteger();
	private AtomicInteger borrows = new AtomicInteger();
	private AtomicInteger exhaustions = new AtomicInteger();
	private AtomicInteger releases = new AtomicInteger();
	private AtomicInteger creations = new AtomicInteger();

	/**
	 * NB: Changing this after initial configuration, via JMX or whatever, will
	 * cause the pool to be re-created.
	 */
	@Override
	public void setSize(int size) {
		super.setSize(size);
		semaphore = new Semaphore(size, true);
		pool = new ConcurrentLinkedQueue<T>();
	}

	@Override
	public void invalidateObject(T obj) {
		if (obj != null) {
			semaphore.release();
			invalidations.incrementAndGet();
		}
	}

	@Override
	public T borrowObject() throws Exception {
		// TODO tryAcquire throws InterruptedException... what to do w/ that?
		boolean aquired = semaphore.tryAcquire(lockTimeout, lockTimeUnit);
		if (aquired) {
			T object = pool.poll();
			if (object == null) {
				try {
					object = factory.makeObject();
					creations.incrementAndGet();
				} catch (Exception e) {
					semaphore.release();
				}
			}
			borrows.incrementAndGet();
			return object;
		} else {
			exhaustions.incrementAndGet();
			throw new PoolExhaustedException("Increase lockTimeout and/or pool size.");
		}
	}

	@Override
	public void returnObject(T obj) {
		if (obj != null) {
			semaphore.release();
			pool.offer(obj);
			releases.incrementAndGet();
		}
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getInvalidations() {
		return invalidations.get();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getBorrows() {
		return borrows.get();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getExhaustions() {
		return exhaustions.get();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getReleases() {
		return releases.get();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getFree() {
		return semaphore.availablePermits();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getWaiting() {
		return semaphore.getQueueLength();
	}

	/**
	 * exposed for monitoring via JMX, etc.
	 * 
	 * @return
	 */
	public int getCreations() {
		return creations.get();
	}
}
