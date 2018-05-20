package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.di.*;
import com.github.thorbenkuck.keller.di.annotations.Bind;
import com.github.thorbenkuck.keller.di.annotations.Cache;
import com.github.thorbenkuck.keller.di.annotations.RequireNew;
import org.junit.Test;

public class DITest {

	@Test
	public void test() {
		DependencyManager dependencyManager = DependencyManager.create();
		dependencyManager.inject(new DP2());
		dependencyManager.get(DP5.class);

		System.out.println("\n\n----");

		System.out.println("Injected Bindings:     " + dependencyManager.getBindings());
		System.out.println("Constructed Bindings:  " + dependencyManager.getConstructedBindings());
		System.out.println("All Bindings:          " + dependencyManager.getInjectedBinding());
	}

	private interface I1 {
	}

	@Cache
	private class DP1 {
		public DP1(DP2 dp2) {
			System.out.println("DP1 constructed with: " + dp2);
		}
	}

	@Cache
	private @Bind class DP2 implements @Bind I1 {
		public DP2() {
			System.out.println("DP2 constructed");
		}
	}

	@Cache
	private class DP3 {
		public DP3(DP1 dp1, DP2 dp2) {
			System.out.println("DP3 constructed with: " + dp1 + " and " + dp2);
		}
	}

	private class DP4 {
		public DP4(DP3 dp3, @RequireNew DP1 dp1) {
			System.out.println("DP4 constructed with: " + dp1 + " and " + dp3);
		}
	}

	private class DP5 {
		public DP5(DP1 dp1, I1 dp2, DP3 dp3, DP4 dp4) {
			System.out.println("DP5 constructed with: " + dp1 + " and " + dp2 + " and " + dp3 + " and " + dp4);
		}
	}
}
