package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

import org.junit.Before;
import org.junit.Test;

/**
 * Can be used to mimic a class via post processing. On the target class, add
 * this annotation.
 * 
 * @author SNI
 *
 */
public class MimicCreatorTest {

	private MimicCreator mimicCreator;
	private CtClass src;
	private CtClass dst;

	@Before
	public void setUp() {
		mimicCreator = new MimicCreator();
		src = ClassPool.getDefault().makeClass("Src" + TestCounter.testCounter);
		dst = ClassPool.getDefault().makeClass("Dst" + TestCounter.testCounter);
		TestCounter.testCounter++;
	}

	@Test
	public void testMimicInterfaces() throws CannotCompileException,
			MimicException, NotFoundException, InstantiationException,
			IllegalAccessException, SecurityException, NoSuchFieldException {
		// GIVEN
		CtClass interfazz = ClassPool.getDefault().makeInterface(
				"Able" + TestCounter.testCounter);
		src.addInterface(interfazz);
		// to load the interface class
		Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

		// WHEN
		mimicCreator.mimicInterfaces(src, dst);

		// THEN
		assertHasInterface(interfaceClass, ClassPool.getDefault().toClass(dst));
	}

	@Test
	public void testMimicFields() throws CannotCompileException,
			MimicException, NotFoundException, InstantiationException,
			IllegalAccessException, SecurityException, NoSuchFieldException {
		// GIVEN
		src.addField(new CtField(CtClass.intType, "foo", src));

		// WHEN
		mimicCreator.mimicFields(src, dst);

		// THEN
		assertHasFooField();
	}

	@Test
	public void testMimicConstructors() throws CannotCompileException,
			MimicException, NotFoundException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			IllegalArgumentException, InvocationTargetException,
			SecurityException, NoSuchMethodException, NoSuchFieldException {
		// GIVEN
		src.addField(new CtField(CtClass.intType, "foo", src));
		src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }",
				src));

		// WHEN
		mimicCreator.mimicFields(src, dst);
		mimicCreator.mimicConstructors(src, dst);

		// THEN
		assertHasFooFieldAndConstructor(ClassPool.getDefault().toClass(dst));
	}

	@Test
	public void testMimicMethods() throws CannotCompileException,
			MimicException, NotFoundException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			IllegalArgumentException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		// GIVEN
		src.addMethod(CtNewMethod.make("public boolean foo() { return true;}",
				src));

		// WHEN
		mimicCreator.mimicMethods(src, dst);

		// THEN
		assertHasFooMethod(ClassPool.getDefault().toClass(dst));
	}

	@Test
	public void testMimicClass() throws CannotCompileException, MimicException,
			NotFoundException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException,
			InvocationTargetException, SecurityException,
			NoSuchMethodException, NoSuchFieldException {
		// GIVEN
		CtClass interfazz = ClassPool.getDefault().makeInterface(
				"Able" + TestCounter.testCounter);
		src.addInterface(interfazz);
		// to load the interface class
		Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

		src.addField(new CtField(CtClass.intType, "foo", src));
		src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }",
				src));
		src.addMethod(CtNewMethod.make("public boolean foo() { return true;}",
				src));

		// WHEN
		mimicCreator.mimicClass(src, dst);

		// THEN
		Class<?> dstClass = ClassPool.getDefault().toClass(dst);

		assertHasInterface(interfaceClass, dstClass);
		assertHasFooFieldAndConstructor(dstClass);
		assertHasFooMethod(dstClass);
	}

	private void assertHasInterface(Class<?> interfaceClass, Class<?> dstClass)
			throws NotFoundException, CannotCompileException {
		CtClass fooInterface = dst.getInterfaces()[0];
		assertNotNull(fooInterface);
		Class<?> realInterface = dstClass.getInterfaces()[0];
		assertEquals(realInterface, interfaceClass);
	}

	private void assertHasFooMethod(Class<?> dstClass)
			throws NotFoundException, InstantiationException,
			IllegalAccessException, CannotCompileException,
			NoSuchMethodException, InvocationTargetException {
		CtMethod fooMethod = dst.getDeclaredMethod("foo");
		assertNotNull(fooMethod);
		// we also need to check if code has been copied
		Object dstInstance = dstClass.newInstance();
		Method realFooMethod = dstInstance.getClass().getMethod("foo");
		assertEquals(true, realFooMethod.invoke(dstInstance));
	}

	private void assertHasFooFieldAndConstructor(Class<?> dstClass)
			throws NotFoundException, InstantiationException,
			IllegalAccessException, CannotCompileException,
			NoSuchFieldException {
		CtField fooField = dst.getField("foo");
		assertNotNull(fooField);
		CtClass fooFieldType = fooField.getType();
		assertEquals(CtClass.intType, fooFieldType);
		CtConstructor constructor = dst.getConstructor(Descriptor
				.ofConstructor(null));
		assertNotNull(constructor);
		// we also need to check if code has been copied
		Object dstInstance = dstClass.newInstance();
		Field realFooField = dstInstance.getClass().getDeclaredField("foo");
		realFooField.setAccessible(true);
		assertEquals(2, realFooField.get(dstInstance));
	}

	private void assertHasFooField() throws NotFoundException,
			InstantiationException, IllegalAccessException,
			CannotCompileException, NoSuchFieldException {
		CtField fooField = dst.getField("foo");
		assertNotNull(fooField);
		CtClass fooFieldType = fooField.getType();
		assertEquals(CtClass.intType, fooFieldType);
		Object dstInstance = ClassPool.getDefault().toClass(dst).newInstance();
		Field realFooField = dstInstance.getClass().getDeclaredField("foo");
		assertNotNull(realFooField);
	}

}
