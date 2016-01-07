/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.impl;

public class IntConstant extends Constant {

	long value;

	private static final IntConstant MINUS_FOUR = new IntConstant(-4);
	private static final IntConstant MINUS_THREE = new IntConstant(-3);
	private static final IntConstant MINUS_TWO = new IntConstant(-2);
	private static final IntConstant MINUS_ONE = new IntConstant(-1);
	private static final IntConstant ZERO = new IntConstant(0);
	private static final IntConstant ONE = new IntConstant(1);
	private static final IntConstant TWO = new IntConstant(2);
	private static final IntConstant THREE = new IntConstant(3);
	private static final IntConstant FOUR = new IntConstant(4);
	private static final IntConstant FIVE = new IntConstant(5);
	private static final IntConstant SIX = new IntConstant(6);
	private static final IntConstant SEVEN = new IntConstant(7);
	private static final IntConstant EIGHT= new IntConstant(8);
	private static final IntConstant NINE = new IntConstant(9);
	private static final IntConstant TEN = new IntConstant(10);

	public static Constant fromValue(int value) {

		switch (value) {
			case -4 : return IntConstant.MINUS_FOUR;
			case -3 : return IntConstant.MINUS_THREE;
			case -2 : return IntConstant.MINUS_TWO;
			case -1 : return IntConstant.MINUS_ONE;
			case 0 : return IntConstant.ZERO;
			case 1 : return IntConstant.ONE;
			case 2 : return IntConstant.TWO;
			case 3 : return IntConstant.THREE;
			case 4 : return IntConstant.FOUR;
			case 5 : return IntConstant.FIVE;
			case 6 : return IntConstant.SIX;
			case 7 : return IntConstant.SEVEN;
			case 8 : return IntConstant.EIGHT;
			case 9 : return IntConstant.NINE;
			case 10 : return IntConstant.TEN;
		}
		return new IntConstant(value);
	}

	private IntConstant(int value) {
		this.value = value;
	}

	public byte byteValue() {
		return (byte) value;
	}

	public char charValue() {
		return (char) value;
	}

	public double doubleValue() {
		return value; // implicit cast to return type
	}

	public float floatValue() {
		return value; // implicit cast to return type
	}

	public int intValue() {
		return (int)value;
	}

	public long longValue() {
		return value; // implicit cast to return type
	}

	public short shortValue() {
		return (short) value;
	}

	public String stringValue() {
		//spec 15.17.11
		return String.valueOf(this.value);
	}

	public String toString() {
		return "(int)" + value; //$NON-NLS-1$
	}

	public int typeID() {
		return T_int;
	}
}
