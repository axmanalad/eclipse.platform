/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;

/**
 * Checks conversion of primitive types
 */
public class InjectBaseTypeTest extends TestCase {

	static class TestClass {
		@Inject @Named("test_int")
		public int intField;
		
		@Inject @Named("test_int_optional") @Optional
		public int intFieldOptional;
		
		@Inject @Named("test_long")
		public long longField;
		
		@Inject @Named("test_float")
		public float floatField;
		
		@Inject @Named("test_double")
		public double doubleField;
		
		@Inject @Named("test_short")
		public short shortField;
		
		@Inject @Named("test_byte")
		public byte byteField;
		
		@Inject @Named("test_boolean")
		public boolean booleanField;
		
		@Inject @Named("test_char")
		public char charField;
		
		public int intArg;
		public char charArg;
		public boolean booleanArg;
		
		@Inject
		public void set(@Named("test_int") int intArg, @Named("test_char") char charArg, @Named("test_boolean") boolean booleanArg) {
			this.intArg = intArg;
			this.charArg = charArg;
			this.booleanArg = booleanArg;
		}
		
	}

	public void testPrimitiveTypes() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("test_int", 12);
		context.set("test_long", 124564523466L);
		context.set("test_float", 12.34f);
		context.set("test_double", 12.34534534563463466546d);
		context.set("test_short", (short)10);
		context.set("test_byte", (byte)55);
		context.set("test_boolean", true);
		context.set("test_char", 'a');
		
		TestClass testClass = ContextInjectionFactory.make(TestClass.class, context);
		
		assertEquals(12, testClass.intField);
		assertEquals(0, testClass.intFieldOptional);
		assertEquals(124564523466L, testClass.longField);
		assertEquals(12.34f, testClass.floatField);
		assertEquals(12.34534534563463466546d, testClass.doubleField);
		assertEquals((short)10, testClass.shortField);
		assertEquals((byte)55, testClass.byteField);
		assertEquals(true, testClass.booleanField);
		assertEquals('a', testClass.charField);
		
		assertEquals(12, testClass.intArg);
		assertEquals('a', testClass.charArg);
		assertEquals(true, testClass.booleanArg);
		
		// test end-of-life reset of values
		ContextInjectionFactory.uninject(testClass, context);
		
		assertEquals(0, testClass.intField);
		assertEquals(0, testClass.intFieldOptional);
		assertEquals(0L, testClass.longField);
		assertEquals(0f, testClass.floatField);
		assertEquals(0.0d, testClass.doubleField);
		assertEquals((short)0, testClass.shortField);
		assertEquals((byte)0, testClass.byteField);
		assertEquals(false, testClass.booleanField);
		assertEquals((char)0, testClass.charField);
		
		assertEquals(0, testClass.intArg);
		assertEquals((char)0, testClass.charArg);
		assertEquals(false, testClass.booleanArg);
		
	}
}
