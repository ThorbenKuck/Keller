package com.github.thorbenkuck.keller.annotations;

import java.lang.annotation.*;

/**
 * Shows that a Class is thoroughly tested.
 *
 * You might provide the String, which represents the unit Test. With that, it should be absolutely clear, which Test
 * is responsible for the correct functionality of the annotated class.
 *
 * This is a String, because the Test-Class is not accessible at compile of the production-code.
 *
 * Also, you might provide a boolean, whether or not the annotated Class is an unit-Test or not.
 *
 * It is important to note that this classes RetentionPolicy is only Source. This means, it is not queried or
 * maintained at Runtime and therefore uninteresting for performance. It is only meant to show that the annotated Class
 * is tested. Further this annotation should not be relied upon by using developers.
 */
@APILevel
@Documented
@Repeatable(Tests.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Tested {

	/**
	 * You may provide the fully qualified Name fo the Test, which tests the annotated Class using this Method
	 *
	 * The Name is not checked to be correct or not. This is just for developers to see, which Test they should look at
	 *
	 * @return the fully qualified Name to the Test, responsible for the annotated Class
	 */
	String responsibleTest() default "Experimental";

	/**
	 * Describes, whether or not, the Test, responsible for testing the annotated Class, is a unit-test or not.
	 *
	 * @return true, if the responsible Test is a unit test or not.
	 */
	boolean unitTest() default true;

}
