/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *								Bug 424742 - [1.8] NPE in LambdaExpression.isCompatibleWith
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
public class LambdaExpressionsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testReferenceExpressionInference1"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaExpressionsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}

public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  int add(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    I i = (x, y) -> {\n" +
				"      return x + y;\n" +
				"    };\n" +
				"    System.out.println(i.add(1234, 5678));\n" +
				"  }\n" +
				"}\n",
			},
			"6912"
			);
}
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface Greetings {\n" +
				"  void greet(String head, String tail);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    Greetings g = (x, y) -> {\n" +
				"      System.out.println(x + y);\n" +
				"    };\n" +
				"    g.greet(\"Hello, \", \"World!\");\n" +
				"  }\n" +
				"}\n",
			},
			"Hello, World!"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    BinaryOperator<String> binOp = (x,y) -> { return x+y; };\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"    // System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
				"  }\n" +
				"}\n",
				"BiFunction.java",
				"@FunctionalInterface\n" + 
				"public interface BiFunction<T, U, R> {\n" + 
				"    R apply(T t, U u);\n" + 
				"}",
				"BinaryOperator.java",
				"@FunctionalInterface\n" + 
				"public interface BinaryOperator<T> extends BiFunction<T,T,T> {\n" + 
				"}"
			},
			"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178,  [1.8][compiler] Some functional interfaces are wrongly rejected
public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"  void foo(int x, int y);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    BinaryOperator binOp = (x,y) -> { return x+y; };\n" +
				"    System.out.println(\"SUCCESS\");\n" +
				"    // System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
				"  }\n" +
				"}\n",
				"BiFunction.java",
				"@FunctionalInterface\n" + 
				"public interface BiFunction<T, U, R> {\n" + 
				"    R apply(T t, U u);\n" + 
				"}",
				"BinaryOperator.java",
				"@FunctionalInterface\n" + 
				"public interface BinaryOperator<T> extends BiFunction<T,T,T> {\n" + 
				"}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	BinaryOperator binOp = (x,y) -> { return x+y; };\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"BinaryOperator is a raw type. References to generic type BinaryOperator<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	BinaryOperator binOp = (x,y) -> { return x+y; };\n" + 
			"	                                         ^^^\n" + 
			"The operator + is undefined for the argument type(s) java.lang.Object, java.lang.Object\n" + 
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test005() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String id(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.id(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test006() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String id(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s + s;\n" +
				"		System.out.println(i.id(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"HelloHello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test007() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> System.out.println(s);\n" +
				"		i.print(\"Hello\");\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> new String(s).toUpperCase();\n" +
				"		System.out.println(i.print(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"HELLO");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	String print(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> new String(s);\n" +
				"		System.out.println(i.print(\"Hello\"));\n" +
				"	}\n" +
				"}\n"
			},
			"Hello");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int unbox(Integer i);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.unbox(new Integer(1234)));\n" +
				"	}\n" +
				"}\n"
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test011() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer box(int i);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = (s) -> s;\n" +
				"		System.out.println(i.box(1234));\n" +
				"	}\n" +
				"}\n"
			},
			"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test012() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	X subType();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I i = () -> new Y();\n" +
				"		System.out.println(i.subType());\n" +
				"	}\n" +
				"}\n" +
				"class Y extends X {\n" +
				"    public String toString() {\n" +
				"        return \"Some Y\";\n" +
				"    }\n" +
				"}"
			},
			"Some Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code for lambdas with expression body.
public void test013() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo(String s);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        int in = 12345678;\n" +
				"        I i = (s) -> {\n" +
				"            I j = (s2) -> {\n" +
				"                System.out.println(s + s2 + in);  \n" +
				"            };\n" +
				"            j.foo(\"Number=\");\n" +
				"        };\n" +
				"        i.foo(\"The \");\n" +
				"    }\n" +
				"}\n"
			},
			"The Number=12345678");
}
public void test014() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void nonmain(String[] args) {\n" + 
					"    int var = 2;\n" + 
					"    I x2 = () -> {\n" + 
					"      System.out.println(\"Argc = \" + args.length);\n" + 
					"      for (int i = 0; i < args.length; i++) {\n" +
					"          System.out.println(\"Argv[\" + i + \"] = \" + args[i]);\n" +
					"      }\n" +
					"    };\n" +
					"    x2.doit();\n" +
					"    var=2;\n" + 
					"  }\n" +
					"  public static void main(String[] args) {\n" + 
					"      nonmain(new String[] {\"Hello! \", \"World!\" });\n" +
					"  }\n" +
					"}" ,
				},
				"Argc = 2\n" + 
				"Argv[0] = Hello! \n" + 
				"Argv[1] = World!");
}
public void test015() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    try {\n" + 
					"      new java.io.File((String) null).getCanonicalPath();\n" + 
					"    } catch (NullPointerException | java.io.IOException ioe) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" +
					"      x2.doit();\n" +
					"    };\n"+
					"  }\n" +
					"}\n"
				},
				"null");
}
public void test016() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" + 
					"	void doit();\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public static void main(String[] args) {\n" + 
					"    java.util.List<String> list = new java.util.ArrayList<>();\n" + 
					"    list.add(\"SomeString\");\n" +
					"    for (String s : list) {\n" + 
					"      I x2 = () -> {\n" + 
					"        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment\n" + 
					"      };\n" + 
					"      x2.doit();\n" +
					"    };\n" + 
					"  }\n" + 
					"\n" +
					"}\n" ,
				},
				"SomeString");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406181, [1.8][compiler][codegen] IncompatibleClassChangeError when running code with lambda method
public void test017() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"  void foo(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    BinaryOperator<String> binOp = (x,y) -> { return x+y; }; \n" +
					"    System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" +
					"  }\n" +
					"}\n" +
					"@FunctionalInterface\n" +
					"interface BiFunction<T, U, R> { \n" +
					"    R apply(T t, U u);\n" +
					"}\n" +
					"@FunctionalInterface \n" +
					"interface BinaryOperator<T> extends BiFunction<T,T,T> { \n" +
					"}\n",
				},
				"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test018() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (short x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy((short) 631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy((short) 136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test019() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (int x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test020() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (Integer x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(631);\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(136);\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code for array constructor references
public void test021() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X [][][] copy (Integer x);\n" +
					"}\n" +
					"public class X  {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X[][][]::new;\n" +
					"       I j = X[][][]::new;\n" +
					"		X[][][] x = i.copy(new Integer(631));\n" +
					"		System.out.println(x.length);\n" +
					"       x = j.copy(new Integer((short)136));\n" +
					"		System.out.println(x.length);\n" +
					"	}\n" +
					"}\n",
				},
				"631\n" + 
				"136");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test022() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"interface J {\n" +
					"	int [] copy(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>clone;\n" +
					"        int [] x = new int [] { 10, 20, 30 };\n" +
					"        int [] y = (int []) i.copy(x);\n" +
					"        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" +
					"        	System.out.println(\"Broken\");\n" +
					"        } else {\n" +
					"        	System.out.println(\"OK\");\n" +
					"        }\n" +
					"        J j = int []::clone;\n" +
					"        y = null;\n" +
					"        y = j.copy(x);\n" +
					"        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" +
					"        	System.out.println(\"Broken\");\n" +
					"        } else {\n" +
					"        	System.out.println(\"OK\");\n" +
					"        }\n" +
					"    }\n" +
					"}\n" ,
				},
				"OK\n" + 
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test023() {
this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy(int [] ia);\n" +
					"}\n" +
					"\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>clone;\n" +
					"        int [] ia = (int []) i.copy(new int[10]);\n" +
					"        System.out.println(ia.length);\n" +
					"    }\n" +
					"}\n",
				},
				"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test024() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    YBase copy(Y ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = Y::<String>copy;\n" +
					"        YBase yb = i.copy(new Y());\n" +
					"        System.out.println(yb.getClass());\n" +
					"    }\n" +
					"}\n" +
					"class YBase {\n" +
					"	public YBase copy() {\n" +
					"		return this;\n" +
					"	}\n" +
					"}\n" +
					"class Y extends YBase {\n" +
					"}\n",
				},
				"class Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388,  [1.8][compiler][codegen] Runtime evaluation of method reference produces "BootstrapMethodError: call site initialization exception"
public void test025() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    int foo(int [] ia);\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = int[]::<String>hashCode;\n" +
					"        i.foo(new int[10]);\n" +
					"    }\n" +
					"}\n",
				},
				"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched 
public void test026() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	Integer foo(int x, int y);\n" +
					"}\n" +
					"class Y {\n" +
					"	int foo(int x, int y) {\n" +
					"		System.out.println(\"Y.foo(\" + x + \",\" + y + \")\");\n" +
					"		return foo(x, y);\n" +
					"	}\n" +
					"}\n" +
					"public class X extends Y {\n" +
					"	int foo(int x, int y) {\n" +
					"		System.out.println(\"X.foo(\" + x + \",\" + y + \")\");\n" +
					"		return x + y;\n" +
					"	}\n" +
					"	void goo() {\n" +
					"		I i = super::foo;\n" +
					"		System.out.println(i.foo(1234, 4321));\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().goo();\n" +
					"	}\n" +
					"}\n",
				},
				"Y.foo(1234,4321)\n" + 
				"X.foo(1234,4321)\n" + 
				"5555");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call misdispatched 
public void test027() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	int foo(int x, int y);\n" +
					"}\n" +
					"interface J {\n" +
					"	default int foo(int x, int y) {\n" +
					"		System.out.println(\"I.foo(\" + x + \",\" + y + \")\");\n" +
					"		return x + y;\n" +
					"	}\n" +
					"}\n" +
					"public class X implements J {\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = new X().f();\n" +
					"		System.out.println(i.foo(1234, 4321));\n" +
					"		i = new X().g();\n" +
					"		try {\n" +
					"			System.out.println(i.foo(1234, 4321));\n" +
					"		} catch (Throwable e) {\n" +
					"			System.out.println(e.getMessage());\n" +
					"		}\n" +
					"	}\n" +
					"	I f() {\n" +
					"		return J.super::foo;\n" +
					"	}\n" +
					"	I g() {\n" +
					"		return new X()::foo;\n" +
					"	}\n" +
					"	public int foo(int x, int y) {\n" +
					"		throw new RuntimeException(\"Exception\");\n" +
					"	}\n" +
					"}\n",
				},
				"I.foo(1234,4321)\n" + 
				"5555\n" + 
				"Exception");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406584, Bug 406584 - [1.8][compiler][codegen] ClassFormatError: Invalid method signature 
public void test028() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    Object copy();\n" +
					"}\n" +
					"public class X  {\n" +
					"    public static void main(String [] args) {\n" +
					"    	int [] x = new int[] { 0xdeadbeef, 0xfeedface };\n" +
					"    	I i = x::<String>clone;\n" +
					"       System.out.println(Integer.toHexString(((int []) i.copy())[0]));\n" +
					"       System.out.println(Integer.toHexString(((int []) i.copy())[1]));\n" +
					"    }\n" +
					"}\n",
				},
				"deadbeef\n" + 
				"feedface");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test029() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X.Y.Z makexyz(int val);\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String args []) {\n" +
					"		new X().new Y().new Z().new P().goo();\n" +
					"	}\n" +
					"	class Y {\n" +
					"		class Z {\n" +
					"			Z(int val) {\n" +
					"				System.out.println(Integer.toHexString(val));\n" +
					"			}	\n" +
					"			Z() {\n" +
					"			}\n" +
					"			class P {\n" +
					"				void goo() {\n" +
					"					I i = Z::new;\n" +
					"					i.makexyz(0xdeadbeef);\n" +
					"				}\n" +
					"				I i = Z::new;\n" +
					"				{ i.makexyz(0xfeedface); }\n" +
					"			}\n" +
					"		}\n" +
					"		I i = Z::new;\n" +
					"		{ i.makexyz(0xbeeffeed); }\n" +
					"	}\n" +
					"}\n",
				},
				"beeffeed\n" + 
				"feedface\n" + 
				"deadbeef");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test030() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X.Y makeY();\n" +
					"}\n" +
					"public class X {\n" +
					"	public class Y {\n" +
					"       public String toString() {\n" +
					"           return \"class Y\";\n" +
					"   }\n" +
					"	}\n" +
					"	void foo() {\n" +
					"		I i = Y::new;\n" +
					"		System.out.println(i.makeY());\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().foo();\n" +
					"	}\n" +
					"}\n",
				},
				"class Y");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test031() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X.Y makeY(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Y {\n" +
					"		String state; \n" +
					"		Y(int x) {\n" +
					"			state = Integer.toHexString(x);\n" +
					"		}\n" +
					"		public String toString() {\n" +
					"			return state;\n" +
					"		}\n" +
					"	}\n" +
					"	class Z extends Y {\n" +
					"		Z(int x) {\n" +
					"			super(x);\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().f();\n" +
					"	}\n" +
					"	void f() {\n" +
					"		I i = Y::new;\n" +
					"		System.out.println(i.makeY(0xdeadbeef));\n" +
					"	}\n" +
					"}\n",
				},
				"deadbeef");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test032() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X.Y makeY(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Y {\n" +
					"		String state; \n" +
					"		Y(int x) {\n" +
					"			state = Integer.toHexString(x);\n" +
					"		}\n" +
					"		public String toString() {\n" +
					"			return state;\n" +
					"		}\n" +
					"	}\n" +
					"	class Z extends Y {\n" +
					"		Z(int x) {\n" +
					"			super(x);\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().f();\n" +
					"	}\n" +
					"	void f() {\n" +
					"		I i = Z::new;\n" +
					"		System.out.println(i.makeY(0xdeadbeef));\n" +
					"	}\n" +
					"}\n",
				},
				"deadbeef");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen] java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method newinvokespecial 
public void test033() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X.Y.Z makeY(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Y {\n" +
					"		Y() {\n" +
					"		}\n" +
					"		class Z {\n" +
					"			String state;\n" +
					"			Z(int x) {\n" +
					"				state = Integer.toHexString(x);\n" +
					"			}\n" +
					"			public String toString() {\n" +
					"				return state;\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"	class YS extends Y {\n" +
					"		YS() {\n" +
					"		}\n" +
					"		void f() {\n" +
					"			I i = Z::new;\n" +
					"			System.out.println(i.makeY(0xbeefface));\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new YS().f();\n" +
					"	}\n" +
					"}\n",
				},
				"beefface");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods. 
public void test034() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    int f = 1234;\n" +
					"    void foo() {\n" +
					"        int x = 4321;\n" +
					"        I i = () -> x + f;\n" +
					"        System.out.println(i.foo());\n" +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"		new X().foo();\n" +
					"	}\n" +
					"}\n",
				},
				"5555");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods. 
public void test035() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int p, int q);\n" +
					"}\n" +
					"public class X {\n" +
					"   int f;\n" +
					"	void foo(int outerp) {\n" +
					"       int locouter;\n" +
					"		I i = (int p, int q)  -> {\n" +
					"			class Local {\n" +
					"				void foo() {\n" +
					"               }\n" +
					"			};\n" +
					"			new Local();\n" +
					"		};\n" +
					"   }\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(\"OK\");\n" +
					"	}\n" +
					"}\n",
				},
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods. 
public void test036() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    String foo(String x, String y);\n" +
					"}\n" +
					"public class X {\n" +
					"    String xf = \"Lambda \";\n" +
					"    String x() {\n" +
					"    	String xl = \"code \";\n" +
					"    	class Y {\n" +
					"			String yf = \"generation \";\n" +
					"			String y () {\n" +
					"				String yl = \"with \";\n" +
					"				class Z {\n" +
					"					String zf = \"instance \";\n" +
					"					String z () {\n" +
					"						String zl = \"and \";\n" +
					"						class P {\n" +
					"							String pf = \"local \";\n" +
					"							String p () {\n" +
					"								String pl = \"capture \";\n" +
					"								I i = (x1, y1) -> {\n" +
					"									return (((I) ((x2, y2) -> {\n" +
					"										return ( ((I) ((x3, y3) -> {\n" +
					"											return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3;\n" +
					"										})).foo(\"works \", \"fine \") + x2 + y2);\n" +
					"									})).foo(\"in \", \"the \") + x1 + y1);\n" +
					"								};\n" +
					"								return i.foo(\"eclipse \", \"compiler \");\n" +
					"							}\n" +
					"						}\n" +
					"						return new P().p();\n" +
					"					}\n" +
					"				}\n" +
					"				return new Z().z();\n" +
					"			}\n" +
					"    	}\n" +
					"    	return new Y().y();\n" +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"	System.out.println(new X().x());\n" +
					"    }\n" +
					"}\n",
				},
				"Lambda code generation with instance and local capture works fine in the eclipse compiler");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code for enclosing instance capture in lambda methods. 
public void test037() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    String foo(String x, String y);\n" +
					"}\n" +
					"public class X {\n" +
					"    String xf = \"Lambda \";\n" +
					"    String x() {\n" +
					"    	String xl = \"code \";\n" +
					"    	class Y {\n" +
					"			String yf = \"generation \";\n" +
					"			String y () {\n" +
					"				String yl = \"with \";\n" +
					"				class Z {\n" +
					"					String zf = \"instance \";\n" +
					"					String z () {\n" +
					"						String zl = \"and \";\n" +
					"						class P {\n" +
					"							String pf = \"local \";\n" +
					"							String p () {\n" +
					"								String pl = \"capture \";\n" +
					"								I i = (x1, y1) -> {\n" +
					"									return (((I) ((x2, y2) -> {\n" +
					"										return ( ((I) ((x3, y3) -> {\n" +
					"                                           String exclaim = \"!\";\n" +
					"											return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3 + x2 + y2 + x1 + y1 + exclaim;\n" +
					"										})).foo(\"works \", \"fine \"));\n" +
					"									})).foo(\"in \", \"the \"));\n" +
					"								};\n" +
					"								return i.foo(\"eclipse \", \"compiler \");\n" +
					"							}\n" +
					"						}\n" +
					"						return new P().p();\n" +
					"					}\n" +
					"				}\n" +
					"				return new Z().z();\n" +
					"			}\n" +
					"    	}\n" +
					"    	return new Y().y();\n" +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"	System.out.println(new X().x());\n" +
					"    }\n" +
					"}\n",
				},
				"Lambda code generation with instance and local capture works fine in the eclipse compiler !");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test038() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"}\n" +
					"interface J {\n" +
					"}\n" +
					"public class X implements I, J {\n" +
					"	public static void main( String [] args) { \n" +
					"		f(new X());\n" +
					"	}\n" +
					"	static void f(Object o) {\n" +
					"		X x = (X & I & J) o;\n" +
					"       System.out.println(\"OK\");\n" +
					"	}\n" +
					"}\n",
				},
				"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test039() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"}\n" +
					"interface J {\n" +
					"}\n" +
					"public class X implements J {\n" +
					"	public static void main( String [] args) { \n" +
					"		f(new X());\n" +
					"	}\n" +
					"	static void f(Object o) {\n" +
					"       try {\n" +
					"		    X x = (X & I & J) o;\n" +
					"       } catch (ClassCastException e) {\n" +
					"           System.out.println(e.getMessage());\n" +
					"       }\n" +
					"	}\n" +
					"}\n",
				},
				"X cannot be cast to I");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406744, [1.8][compiler][codegen] LambdaConversionException seen when method reference targets a varargs method
public void _test040() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"    void foo(Integer a1, Integer a2, String a3);\n" +
					"}\n" +
					"class Y {\n" +
					"    static void m(Number a1, Object... rest) { \n" +
					"        System.out.println(a1);\n" +
					"        print(rest);\n" +
					"    }\n" +
					"    static void print (Object [] o) {\n" +
					"        for (int i = 0; i < o.length; i++)\n" +
					"            System.out.println(o[i]);\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n" +
					"    public static void main(String [] args) {\n" +
					"        I i = Y::m;\n" +
					"        i.foo(10, 20, \"10, 20\");\n" +
					"    }\n" +
					"}\n",
				},
				"X cannot be cast to I");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test041() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X makeX(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Z {\n" +
					"		void f() {\n" +
					"			I i = X::new;\n" +
					"			i.makeX(123456);\n" +
					"		}\n" +
					"	}\n" +
					"	private X(int x) {\n" +
					"		System.out.println(x);\n" +
					"	}\n" +
					"	X() {\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new Z().f();\n" +
					"	}\n" +
					"}\n",
				},
				"123456");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test042() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X makeX(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Y extends X {\n" +
					"		class Z {\n" +
					"			void f() {\n" +
					"				I i = X::new;\n" +
					"				i.makeX(123456);\n" +
					"				i = Y::new;\n" +
					"				i.makeX(987654);\n" +
					"			}\n" +
					"		}\n" +
					"		private Y(int y) {\n" +
					"			System.out.println(\"Y(\" + y + \")\");\n" +
					"		}\n" +
					"		private Y() {\n" +
					"			\n" +
					"		}\n" +
					"	}\n" +
					"	private X(int x) {\n" +
					"		System.out.println(\"X(\" + x + \")\");\n" +
					"	}\n" +
					"\n" +
					"	X() {\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new Y().new Z().f();\n" +
					"	}\n" +
					"\n" +
					"}\n",
				},
				"X(123456)\n" + 
				"Y(987654)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test043() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X makeX(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	class Y extends X {\n" +
					"		class Z extends X {\n" +
					"			void f() {\n" +
					"				I i = X::new;\n" +
					"				i.makeX(123456);\n" +
					"				i = Y::new;\n" +
					"				i.makeX(987654);\n" +
					"               i = Z::new;\n" +
					"               i.makeX(456789);\n" +
					"			}\n" +
					"       	private Z(int z) {\n" +
					"				System.out.println(\"Z(\" + z + \")\");\n" +
					"			}\n" +
					"           Z() {\n" +
					"           }\n" +
					"       }\n" +
					"		private Y(int y) {\n" +
					"			System.out.println(\"Y(\" + y + \")\");\n" +
					"		}\n" +
					"		private Y() {\n" +
					"			\n" +
					"		}\n" +
					"	}\n" +
					"	private X(int x) {\n" +
					"		System.out.println(\"X(\" + x + \")\");\n" +
					"	}\n" +
					"\n" +
					"	X() {\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new Y().new Z().f();\n" +
					"	}\n" +
					"\n" +
					"}\n",
				},
				"X(123456)\n" + 
				"Y(987654)\n" + 
				"Z(456789)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test044() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X makeX(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo() {\n" +
					"		int local;\n" +
					"		class Y extends X {\n" +
					"			class Z extends X {\n" +
					"				void f() {\n" +
					"					I i = X::new;\n" +
					"					i.makeX(123456);\n" +
					"					i = Y::new;\n" +
					"					i.makeX(987654);\n" +
					"					i = Z::new;\n" +
					"					i.makeX(456789);\n" +
					"				}\n" +
					"				private Z(int z) {\n" +
					"					System.out.println(\"Z(\" + z + \")\");\n" +
					"				}\n" +
					"				Z() {}\n" +
					"			}\n" +
					"			private Y(int y) {\n" +
					"				System.out.println(\"Y(\" + y + \")\");\n" +
					"			}\n" +
					"			private Y() {\n" +
					"			}\n" +
					"		}\n" +
					"		new Y().new Z().f();\n" +
					"	}\n" +
					"	private X(int x) {\n" +
					"		System.out.println(\"X(\" + x + \")\");\n" +
					"	}\n" +
					"\n" +
					"	X() {\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().foo();\n" +
					"	}\n" +
					"}\n",
				},
				"X(123456)\n" + 
				"Y(987654)\n" + 
				"Z(456789)");
}
public void test045() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	X makeX(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	I i = (x) -> {\n" +
					"		class Y extends X {\n" +
					"			private Y (int y) {\n" +
					"				System.out.println(y);\n" +
					"			}\n" +
					"			Y() {\n" +
					"			}\n" +
					"			void f() {\n" +
					"				I i = X::new;\n" +
					"				i.makeX(123456);\n" +
					"				i = X.Y::new;\n" +
					"				i.makeX(987654);\n" +
					"			}\n" +
					"		}\n" +
					"		return null; \n" +
					"	};\n" +
					"	private X(int x) {\n" +
					"		System.out.println(x);\n" +
					"	}\n" +
					"	X() {\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new Y().f();\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" + 
				"1. WARNING in X.java (at line 6)\n" + 
				"	class Y extends X {\n" + 
				"	      ^\n" + 
				"The type Y is never used locally\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 7)\n" + 
				"	private Y (int y) {\n" + 
				"	        ^^^^^^^^^\n" + 
				"The constructor Y(int) is never used locally\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 10)\n" + 
				"	Y() {\n" + 
				"	^^^\n" + 
				"The constructor Y() is never used locally\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 13)\n" + 
				"	I i = X::new;\n" + 
				"	  ^\n" + 
				"The local variable i is hiding a field from type X\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 15)\n" + 
				"	i = X.Y::new;\n" + 
				"	      ^\n" + 
				"Y cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 27)\n" + 
				"	new X().new Y().f();\n" + 
				"	            ^\n" + 
				"X.Y cannot be resolved to a type\n" + 
				"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406760, [1.8][compiler][codegen] "VerifyError: Bad type on operand stack" with qualified super method references
public void test046() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	String doit();\n" +
					"}\n" +
					"public class X extends B {\n" +
					"	class Y {\n" +
					"		class Z {\n" +
					"			void f() {\n" +
					"				\n" +
					"				 I i = X.super::toString; // Verify error\n" +
					"				 System.out.println(i.doit());\n" +
					"				 i = X.this::toString; // This call gets dispatched OK.\n" +
					"				 System.out.println(i.doit());\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"	\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().new Y().new Z().f(); \n" +
					"	}\n" +
					"	\n" +
					"	public String toString() {\n" +
					"		return \"X's toString\";\n" +
					"	}\n" +
					"}\n" +
					"class B {\n" +
					"	public String toString() {\n" +
					"		return \"B's toString\";\n" +
					"	}\n" +
					"}\n",
				},
				"B\'s toString\n" + 
				"X\'s toString");
}
public void test047() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		long lng = 1234;\n" +
					"		double d = 1234.5678;\n" +
					"		I i = (x, y) -> {\n" +
					"			System.out.println(\"long = \" + lng);\n" +
					"			System.out.println(\"args length = \" + args.length);\n" +
					"			System.out.println(\"double = \" + d);\n" +
					"			System.out.println(\"x = \" + x);\n" +
					"			System.out.println(\"y = \" + y);\n" +
					"		};\n" +
					"		i.foo(9876, 4321);\n" +
					"	}\n" +
					"}\n",
				},
				"long = 1234\n" + 
				"args length = 0\n" + 
				"double = 1234.5678\n" + 
				"x = 9876\n" + 
				"y = 4321");
}
public void test048() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I<T, J> {\n" +
					"	void foo(T x, J y);\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		long lng = 1234;\n" +
					"		double d = 1234.5678;\n" +
					"		I<Object, Object> i = (x, y) -> {\n" +
					"			System.out.println(\"long = \" + lng);\n" +
					"			System.out.println(\"args length = \" + args.length);\n" +
					"			System.out.println(\"double = \" + d);\n" +
					"			System.out.println(\"x = \" + x);\n" +
					"			System.out.println(\"y = \" + y);\n" +
					"		};\n" +
					"		i.foo(9876, 4321);\n" +
					"		\n" +
					"		I<String, String> i2 = (x, y) -> {\n" +
					"			System.out.println(x);\n" +
					"			System.out.println(y);\n" +
					"		};\n" +
					"		i2.foo(\"Hello !\",  \"World\");\n" +
					"	}\n" +
					"}\n",
				},
				"long = 1234\n" + 
				"args length = 0\n" + 
				"double = 1234.5678\n" + 
				"x = 9876\n" + 
				"y = 4321\n" + 
				"Hello !\n" + 
				"World");
}
public void test049() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I<T, J> {\n" +
					"	void foo(X x, T t, J j);\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		I<String, String> i = X::foo;\n" +
					"		i.foo(new X(), \"Hello\", \"World!\");\n" +
					"	}\n" +
					"	void foo(String s, String t) {\n" +
					"		System.out.println(s);\n" +
					"		System.out.println(t);\n" +
					"	}\n" +
					"}\n",
				},
				"Hello\n" + 
				"World!");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test050() {
	this.runConformTest(
			new String[] {
					"X.java",
					"interface I {\n" +
					"	void foo(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"	static private void add(int x, int y) {\n" +
					"		System.out.println(x + y);\n" +
					"	}\n" +
					"	private void multiply(int x, int y) {\n" +
					"		System.out.println(x * y);\n" +
					"	}\n" +
					"	static class Y {\n" +
					"		static private void subtract(int x, int y) {\n" +
					"			System.out.println(x - y);\n" +
					"		}\n" +
					"		private void divide (int x, int y) {\n" +
					"			System.out.println(x / y);\n" +
					"		}\n" +
					"		static void doy() {\n" +
					"			I i = X::add;\n" +
					"			i.foo(1234, 12);\n" +
					"			i = new X()::multiply;\n" +
					"			i.foo(12, 20);\n" +
					"			i = Y::subtract;\n" +
					"			i.foo(123,  13);\n" +
					"			i = new Y()::divide;\n" +
					"			i.foo(99, 9);\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		I i = X::add;\n" +
					"		i.foo(1234, 12);\n" +
					"		i = new X()::multiply;\n" +
					"		i.foo(12, 20);\n" +
					"		i = Y::subtract;\n" +
					"		i.foo(123,  13);\n" +
					"		i = new Y()::divide;\n" +
					"		i.foo(99, 9);\n" +
					"		Y.subtract(10,  7);\n" +
					"		Y.doy();\n" +
					"	}\n" +
					"}\n",
				},
				"1246\n" + 
				"240\n" + 
				"110\n" + 
				"11\n" + 
				"3\n" + 
				"1246\n" + 
				"240\n" + 
				"110\n" + 
				"11");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test051() {
	this.runConformTest(
			new String[] {
					"p2/B.java",
					"package p2;\n" +
					"import p1.*;								\n" +
					"interface I {\n" +
					"	void foo();\n" +
					"}\n" +
					"interface J {\n" +
					"	void foo();\n" +
					"}\n" +
					"public class B extends A {\n" +
					"	class Y {\n" +
					"		void g() {\n" +
					"			I i = B::foo;\n" +
					"			i.foo();\n" +
					"			J j = new B()::goo;\n" +
					"			j.foo();\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new B().new Y().g();\n" +
					"	}\n" +
					"}\n",
					"p1/A.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class A {\n" +
					"	protected static void foo() {\n" +
					"	    System.out.println(\"A's static foo\");\n" +
					"	}\n" +
					"	protected void goo() {\n" +
					"	    System.out.println(\"A's instance goo\");\n" +
					"	}\n" +
					"}"
				},
				"A\'s static foo\n" + 
				"A\'s instance goo");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen] "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
public void test052() {
	this.runConformTest(
			new String[] {
					"X.java", 
					"interface I {\n" +
					"	void foo(int x);\n" +
					"}\n" +
					"public class X {\n" +
					"	void foo() {\n" +
					"		int local = 10;\n" +
					"		class Y {\n" +
					"			void foo(int x) {\n" +
					"				System.out.println(local);\n" +
					"			}\n" +
					"			void goo() {\n" +
					"				I i = this::foo;\n" +
					"				i.foo(10);\n" +
					"			}\n" +
					"		}\n" +
					"		new Y().goo();\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().foo();\n" +
					"	}\n" +
					"}\n"
				},
				"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then produces IncompatibleClassChangeError when run
public void test053() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "import java.util.*;\n" +
	      "public class X {\n" +
	      "  public static <E> void printItem(E value, int index) {\n" +
	      "    String output = String.format(\"%d -> %s\", index, value);\n" +
	      "    System.out.println(output);\n" +
	      "  }\n" +
	      "  public static void main(String[] argv) {\n" +
	      "    List<String> list = Arrays.asList(\"A\",\"B\",\"C\");\n" +
	      "    eachWithIndex(list,X::printItem);\n" +
	      "  }\n" +
	      "  interface ItemWithIndexVisitor<E> {\n" +
	      "    public void visit(E item, int index);\n" +
	      "  }\n" +
	      "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" +
	      "    for (int i = 0; i < list.size(); i++) {\n" +
	      "         visitor.visit(list.get(i), i);\n" +
	      "    }\n" +
	      "  }\n" +
	      "}\n"
	    },
	    "0 -> A\n" + 
	    "1 -> B\n" + 
	    "2 -> C");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then produces IncompatibleClassChangeError when run
public void test054() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "import java.util.*;\n" +
	      "public class X {\n" +
	      "  public static <E> void printItem(E value) {}\n" +
	      "  public static void main(String[] argv) {\n" +
	      "    List<String> list = null;\n" +
	      "    eachWithIndex(list, X::printItem);\n" +
	      "  }\n" +
	      "  interface ItemWithIndexVisitor<E> {\n" +
	      "    public void visit(E item);\n" +
	      "  }\n" +
	      "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}\n" +
	      "}\n"
	    },
	    "");
}
public void test055() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "interface I {\n" +
		  "	void foo(int i);\n" +
		  "}\n" +
		  "public class X {\n" +
		  "	public static void main(String[] args) {\n" +
		  "		X x = null;\n" +
		  "		I i = x::foo;\n" +
		  "	}\n" +
		  "	int foo(int x) {\n" +
		  "		return x;\n" +
		  "	}\n" +
		  "}\n" 
	    },
	    "");
}
public void test056() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "interface I {\n" +
		  "	void foo(int i);\n" +
		  "}\n" +
		  "public class X {\n" +
		  "	public static void main(String[] args) {\n" +
		  "		X x = null;\n" +
		  "		I i = x::foo;\n" +
		  "		try {\n" +
		  "			i.foo(10);\n" +
		  "		} catch (NullPointerException npe) {\n" +
		  "			System.out.println(npe.getMessage());\n" +
		  "		}\n" +
		  "	}\n" +
		  "	int foo(int x) {\n" +
		  "		return x;\n" +
		  "	}\n" +
		  "}\n" 
	    },
	    "null");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=410114, [1.8] CCE when trying to parse method reference expression with inappropriate type arguments
public void test057() {
	String source = "interface I {\n" +
			"    void foo(Y<String> y);\n" +
			"}\n" +
			"public class Y<T> {\n" +
			"    class Z<K> {\n" +
			"        Z(Y<String> y) {\n" +
			"            System.out.println(\"Y<T>.Z<K>:: new\");\n" +
			"        }\n" +
			"        public void bar() {\n" +
			"            I i = Y<String>.Z<Integer>::<String> new;\n" +
			"            i.foo(new Y<String>());\n" +
			"            i = Y<String>.Z<Integer>:: new;\n" +
			"            i.foo(new Y<String>());\n" +
			"            i = Y.Z:: new;\n" +
			"            i.foo(new Y<String>());\n" +
			"        }\n" +
			"    }\n" +
			"	public void foo() {\n" +
		    "		Z<String> z = new Z<String>(null);\n" +
			"		z.bar();\n" +
		    "	}\n" +
		    "	public static void main(String[] args) {\n" +
		    "		Y<String> y = new Y<String>();\n" +
		    "		y.foo();\n" +
		    "	}\n" +
			"}\n";
this.runConformTest(
	new String[]{"Y.java",
				source},
				"Y<T>.Z<K>:: new\n" +
				"Y<T>.Z<K>:: new\n" +
				"Y<T>.Z<K>:: new\n" +
				"Y<T>.Z<K>:: new");
}
// Bug 411273 - [1.8][compiler] Bogus error about unhandled exceptions for unchecked exceptions thrown by method reference.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=411273
public void test058() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		F1 f = X::foo;\n" +
				"		System.out.println(\"Hello, World\");\n" +
				"	}\n" +
				"    static int foo (int x) throws NumberFormatException { return 0; }\n" +
				"}\n" +
				"interface F1 { int X(int x);}\n"
			},
			"Hello, World"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420582,  [1.8][compiler] Compiler should allow creation of generic array creation with unbounded wildcard type arguments
public void testGenericArrayCreation() {
		this.runConformTest(
			new String[] {
					"X.java", 
					"interface I {\n" +
					"	X<?, ?, ?>[] makeArray(int i);\n" +
					"}\n" +
					"public class X<T, U, V> {\n" +
					"	public static void main(String [] args) {\n" +
					"		I i = X<?, ?, ?>[]::new; // OK.\n" +
					"		System.out.println(i.makeArray(1024).length);\n" +
					"	}\n" +
					"}\n" + 
					""
			},
			"1024"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421536, [1.8][compiler] Verify error with small program when preserved unused variables is off. 
public void test421536() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	I foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			I i = () -> null;\n" +
			"		} catch (NullPointerException npe) {}\n" +
			"       System.out.println(\"OK\");\n" +
			"	}\n" +
			"}\n"
		}, 
		"OK",
		customOptions);		
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421536, [1.8][compiler] Verify error with small program when preserved unused variables is off. 
public void test421536a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   public static void foo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			I i = X::foo;\n" +
			"		} catch (NullPointerException npe) {}\n" +
			"       System.out.println(\"OK\");\n" +
			"	}\n" +
			"}\n"
		}, 
		"OK",
		customOptions);		
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421607, [1.8][compiler] Verify Error with intersection casts 
public void test421607() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	public void foo();\n" +
			"}\n" +
			"class C implements I {\n" +
			"	public void foo() {\n" +
			"		System.out.println(\"You will get here\");\n" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		((C & I) (I) new C()).foo();\n" +
			"	}\n" +
			"}\n"
		}, 
		"You will get here");		
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421712, [1.8][compiler] java.lang.NoSuchMethodError with lambda expression in interface default method.
public void test421712() {
	runConformTest(
		new String[] {
			"X.java",
			"interface F {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface I {\n" +
			"	default void foo() {\n" +
			"		F f = () -> {\n" +
			"		};\n" +
			"   System.out.println(\"Lambda instantiated\");\n" +
			"	}\n" +
			"}\n" +
			"public class X implements I {\n" +
			"	public static void main(String argv[]) {\n" +
			"		X x = new X();\n" +
			"		x.foo();\n" +
			"	}\n" +
			"}\n"
		}, 
		"Lambda instantiated");		
}


//https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler] "Missing code implementation in the compiler" when lambda body accesses array variable
public void test422515() {
	this.runConformTest(
			new String[] {
					"X.java", 
					"public class X {\n" +
					"    public static void main(String[] args) throws InterruptedException {\n" +
					"        final int[] result = { 0 };\n" +
					"        Thread t = new Thread(() -> result[0] = 42);\n" +
					"        t.start();\n" +
					"        t.join();\n" +
					"        System.out.println(result[0]);\n" +
					"    }\n" +
					"}\n"
			},
			"42"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler] "Missing code implementation in the compiler" when lambda body accesses array variable
public void test422515a() {
	this.runConformTest(
			new String[] {
					"X.java", 
					"public class X {\n" +
					"    public static void main(String[] args) throws InterruptedException {\n" +
					"        final int[] result= { 0 };\n" +
					"        final int x = args.length + 42;\n" +
					"        Thread t = new Thread(() -> {\n" +
					"            result[0]= x;\n" +
					"        });\n" +
					"        t.start();\n" +
					"        t.join();\n" +
					"        System.out.println(result[0]);\n" +
					"    }\n" +
					"}\n"
			},
			"42"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422800, [1.8][compiler] "Missing code implementation in the compiler" 2
public void test422800() {
	this.runConformTest(
			new String[] {
					"X.java", 
					"public class X {\n" +
					"    private String fField; // must be here; can be used or unused\n" +
					"    public void foo(Integer arg) {\n" +
					"        new Thread(() -> {\n" +
					"            arg.intValue();\n" +
					"        });\n" +
					"    }\n" +
					"    public static void main(String [] args) {\n" +
					"	     System.out.println(\"OK\");\n" +
					"    }\n" +
					"}\n"
			},
			"OK"
		);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421927, [1.8][compiler] Bad diagnostic: Unnecessary cast from I to I for lambdas.
public void test421927() {
	this.runConformTest(
			new String[] {
					"X.java", 
					"interface I { \n" +
					"	int foo();\n" +
					"}\n" +
					"public class X {\n" +
					"    static I i  = (I & java.io.Serializable) () -> 42;\n" +
					"    public static void main(String args[]) {\n" +
					"        System.out.println(i.foo());\n" +
					"    }\n" +
					"}\n"
			},
			"42");
}

public void testReferenceExpressionInference1() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<E> {\n" +
			"	E foo(E e);\n" +
			"}\n" +
			"public class X {\n" +
			"	<T> T print(I<T> i) { return null; }\n" +
			"	void test() {\n" +
			"		String s = print(this::bar);" +
			"	}\n" +
			"	<S> S bar(S s) { return s; }\n" +
			"}\n"
		});
}

public void testReferenceExpressionInference2() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<E,F> {\n" +
			"	F foo(E e);\n" +
			"}\n" +
			"public class X {\n" +
			"	<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }\n" +
			"	void test() {\n" +
			"		I<X,String> x2s = compose(this::bar, this::i2s);" +
			"	}\n" +
			"	String i2s (Integer i) { return i.toString(); }\n" +
			"	<V,W extends Number> W bar(V v) { return null; }\n" +
			"}\n"
		});
}

public void testReferenceExpressionInference3a() {
	runConformTest(
		new String[] {
			"X.java",
			"interface I<E,F> {\n" +
			"	F foo(E e);\n" +
			"}\n" +
			"public class X {\n" +
			"	<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }\n" +
			"	void test() {\n" +
			"		I<X,String> x2s = compose(this::bar, this::<String>i2s);" + // help inference with an explicit type argument
			"	}\n" +
			"	<Z> Z i2s (Integer i) { return null; }\n" +
			"	<V,W extends Number> W bar(V v) { return null; }\n" +
			"}\n"
		});
}

// previous test demonstrates that a solution exists, just inference doesn't find it.
public void testReferenceExpressionInference3b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"interface I<E,F> {\n" +
			"	F foo(E e);\n" +
			"}\n" +
			"public class X {\n" +
			"	<S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }\n" +
			"	void test() {\n" +
			"		I<X,String> x2s = compose(this::bar, this::i2s);\n" +
			"	}\n" +
			"	<Z> Z i2s (Integer i) { return null; }\n" +
			"	<V,W extends Number> W bar(V v) { return null; }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	I<X,String> x2s = compose(this::bar, this::i2s);\n" + 
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from I<Object,Object> to I<X,String>\n" +
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	I<X,String> x2s = compose(this::bar, this::i2s);\n" + 
		"	                                     ^^^^^^^^^\n" + 
		"The type X does not define i2s(Object) that is applicable here\n" + 
		"----------\n");
}
public void testLambdaInference1() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "import java.util.*;\n" +
	      "public class X {\n" +
	      "  public static void main(String[] argv) {\n" +
	      "    List<String> list = null;\n" +
	      "    eachWithIndex(list, s -> print(s));\n" +
	      "  }\n" +
	      "  static void print(String s) {}\n" +
	      "  interface ItemWithIndexVisitor<E> {\n" +
	      "    public void visit(E item);\n" +
	      "  }\n" +
	      "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}\n" +
	      "}\n"
	    },
	    "");
}

public void testLambdaInference2() {
	  this.runConformTest(
	    new String[] {
	      "X.java",
	      "import java.util.*;\n" +
	      "class A {}\n" +
	      "class B extends A {\n" +
	      "	void bar() {}\n" +
	      "}\n" +
	      "public class X {\n" +
	      "  public static void main(String[] argv) {\n" +
	      "    someWithIndex(getList(), (B b) -> b.bar());\n" +
	      "  }\n" +
	      "  interface ItemWithIndexVisitor<E> {\n" +
	      "    public void visit(E item);\n" +
	      "  }\n" +
	      "  public static <G> void someWithIndex(List<G> list, ItemWithIndexVisitor<G> visitor) {}\n" +
	      "  static <I extends A> List<I> getList() { return null; }\n" +
	      "}\n"
	    },
	    "");
}

public void testBug419048_1() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"import java.util.stream.*;\n" +
			"public class X {\n" +
			"	public void test() {\n" +
			"		 List<Person> roster = new ArrayList<>();\n" + 
			"        \n" + 
			"        Map<String, Person> map = \n" + 
			"            roster\n" + 
			"                .stream()\n" + 
			"                .collect(\n" + 
			"                    Collectors.toMap(\n" + 
			"                        p -> p.getLast(),\n" + 
			"                        p -> p\n" + 
			"                    ));\n" +
			"	}\n" +
			"}\n" +
			"class Person {\n" + 
			"  public String getLast() { return null; }\n" + 
			"}\n"
		});
}

public void testBug419048_2() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"public class X {\n" +
			"	public void test() {\n" +
			"		 List<Person> roster = new ArrayList<>();\n" + 
			"        \n" + 
			"        Map<String, Person> map = \n" + 
			"            roster\n" + 
			"                .stream()\n" + 
			"                .collect(\n" + 
			"                    Collectors.toMap(\n" + 
			"                        Person::getLast,\n" + 
			"                        Function.identity()\n" + 
			"                    ));\n" +
			"	}\n" +
			"}\n" +
			"class Person {\n" + 
			"  public String getLast() { return null; }\n" + 
			"}\n"
		});
}

public void testBug419048_3() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import java.util.stream.*;\n" +
			"public class X {\n" +
			"	public void test() {\n" +
			"		 List<Person> roster = new ArrayList<>();\n" + 
			"        \n" + 
			"        Map<String, Person> map = \n" + 
			"            roster\n" + 
			"                .stream()\n" + 
			"                .collect(\n" + 
			"                    Collectors.toMap(\n" + 
			"                        new Function<Person, String>() {\n" + 
			"                            public String apply(Person p) { \n" + 
			"                                return p.getLast(); \n" + 
			"                            } \n" + 
			"                        },\n" + 
			"                        Function.identity()\n" + 
			"                    ));\n" +
			"	}\n" +
			"}\n" +
			"class Person {\n" + 
			"  public String getLast() { return null; }\n" + 
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424226,  [1.8] Cannot use static method from an interface in static method reference
public void test424226() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void fun1() {\n" +
			"		FI fi = I::staticMethod; \n" +
			"	}\n" +
			"   public static void main(String [] args) {\n" +
			"       System.out.println(\"OK\");\n" +
			"   }\n" +
			"}\n" +
			"@FunctionalInterface\n" +
			"interface FI {\n" +
			"	void foo();	\n" +
			"}\n" +
			"interface I {\n" +
			"	static FI staticMethod() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		}, "OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423684, [1.8][compiler] IllegalAccessError using functional consumer calling inherited method
public void test423684() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"import mypackage.MyPublicClass;\n" +
			"public class Test {\n" +
			"    public static void main(String[] args) {\n" +
			"        doesWork();\n" +
			"        doesNotWork();\n" +
			"    }\n" +
			"    public static void doesNotWork() {\n" +
			"        MyPublicClass victim = new MyPublicClass();\n" +
			"        List<String> items = Arrays.asList(\"first\", \"second\", \"third\");\n" +
			"        items.forEach(victim::doSomething); //illegal access error here\n" +
			"    }\n" +
			"    public static void doesWork() {\n" +
			"        MyPublicClass victim = new MyPublicClass();\n" +
			"        List<String> items = Arrays.asList(\"first\", \"second\", \"third\");\n" +
			"        for (String item : items) {\n" +
			"            victim.doSomething(item);\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
			"mypackage/MyPublicClass.java",	
			"package mypackage;\n" +
			"class MyPackagePrivateBaseClass {\n" +
			"    public void doSomething(String input) {\n" +
			"        System.out.println(input);\n" +
			"    }\n" +
			"}\n" +
			"public class MyPublicClass extends MyPackagePrivateBaseClass {\n" +
			"}\n"
		}, 
		"first\n" + 
		"second\n" + 
		"third\n" + 
		"first\n" + 
		"second\n" + 
		"third");
}
public void testBug424742() {
	runNegativeTest(
		new String[] {
			"TestInlineLambdaArray.java",
			"package two.test;\n" + 
			"\n" + 
			"class TestInlineLambdaArray {\n" + 
			"	TestInlineLambdaArray h = new TestInlineLambdaArray(x -> x++);	// [9]\n" + 
			"	public TestInlineLambda(FI fi) {}\n" + 
			"}\n" + 
			"\n" + 
			"interface FI {\n" + 
			"		void foo();\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in TestInlineLambdaArray.java (at line 4)\n" + 
		"	TestInlineLambdaArray h = new TestInlineLambdaArray(x -> x++);	// [9]\n" + 
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor TestInlineLambdaArray((<no type> x) -> x ++) is undefined\n" + 
		"----------\n" + 
		"2. ERROR in TestInlineLambdaArray.java (at line 5)\n" + 
		"	public TestInlineLambda(FI fi) {}\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424589, [1.8][compiler] NPE in TypeSystem.getUnannotatedType
public void test424589() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.Collection;\n" +
			"import java.util.function.Supplier;\n" +
			"import java.util.Set;\n" +
			"public class X {\n" +
			"    public static <T, Y extends Collection<T>>\n" +
			"        Y foo(Supplier<Y> y) {\n" +
			"            return null;\n" +
			"    }  \n" +
			"    public static void main(String[] args) {\n" +
			"        Set<Z> x = foo(Set::new);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	Set<Z> x = foo(Set::new);\n" + 
		"	    ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	Set<Z> x = foo(Set::new);\n" + 
		"	           ^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Collection<Object> to Set<Z>\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 11)\n" + 
		"	Set<Z> x = foo(Set::new);\n" + 
		"	               ^^^\n" + 
		"Cannot instantiate the type Set\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425152, [1.8] [compiler] NPE in LambdaExpression.analyzeCode
public void test425152() {
	runConformTest(
		new String[] {
			"Main.java",
			"interface Base { \n" +
			"	Base get(int x);\n" +
			"}\n" +
			"class Main {\n" +
			"    <T> Base foo(Base b) { \n" +
			"        return null; \n" +
			"     }\n" +
			"    void bar(Base b) { }\n" +
			"    void testCase() {\n" +
			"        bar(foo((int p)->null));\n" +
			"     }\n" +
			"}\n"
		});
}
public static Class testClass() {
	return LambdaExpressionsTest.class;
}
}
