package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.di.DependencyManager;
import com.github.thorbenkuck.keller.di.RequireNew;
import com.github.thorbenkuck.keller.di.SingleInstanceOnly;
import com.github.thorbenkuck.keller.di.Use;
import org.junit.Test;

public class DITest {

	@Test
	public void test() {
		DependencyManager dependencyManager = DependencyManager.create();
		dependencyManager.get(DP5.class);

		System.out.println(dependencyManager.getCachedDependencies());
	}

	@SingleInstanceOnly
	private class DP1 {
		public DP1() {
			System.out.println("DP1 constructed");
		}
	}

	@SingleInstanceOnly
	private class DP2 {
		public DP2() {
			System.out.println("DP2 constructed");
		}
	}

	@SingleInstanceOnly
	private class DP3 {
		@Use
		public DP3(DP1 dp1, @RequireNew DP2 dp2) {
			System.out.println("DP3 constructed with: " + dp1 + " and " + dp2);
		}
	}

	private class DP4 {
		@Use
		public DP4(DP3 dp3, @RequireNew DP1 dp1) {
			System.out.println("DP4 constructed with: " + dp1 + " and " + dp3);
		}
	}

	private class DP5 {
		public DP5(DP1 dp1, DP2 dp2, DP3 dp3, DP4 dp4) {
			System.out.println("DP5 constructed with: " + dp1 + " and " + dp2 + " and " + dp3 + " and " + dp4);
		}
	}
}
