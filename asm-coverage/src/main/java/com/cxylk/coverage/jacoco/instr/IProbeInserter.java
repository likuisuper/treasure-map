/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package com.cxylk.coverage.jacoco.instr;

/**
 * Internal interface for insertion of probes into in the instruction sequence
 * of a method.
 */
interface IProbeInserter {

	/**
	 * Inserts the probe with the given id.
	 * 
	 * @param id
	 *            id of the probe to insert
	 */
	public void insertProbe(final int id);

}
