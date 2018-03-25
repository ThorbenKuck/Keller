package com.github.thorbenkuck.keller;

import com.github.thorbenkuck.keller.mvp.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class JavaFXStageControllerExample extends Application {

	private final ViewController controller = new AsynchronousViewController(this::runOnFXThread);

	public static void main(String[] args) {
		launch(args);
	}

	private void runOnFXThread(Runnable runnable) {
		if(Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	public JavaFXStageControllerExample() {
		controller.addFactory(ExampleView.class, () -> {
			ExamplePresenter examplePresenter = new ExamplePresenter();
			ExampleView exampleView = new ExampleView(examplePresenter);
			examplePresenter.instantiate(exampleView);

			return exampleView;
		});
	}

	/**
	 * The main entry point for all JavaFX applications.
	 * The start method is called after the init method has returned,
	 * and after the system is ready for the application to begin running.
	 * <p>
	 * <p>
	 * NOTE: This method is called on the JavaFX Application Thread.
	 * </p>
	 *
	 * @param primaryStage the primary stage for this application, onto which
	 *                     the application scene can be set. The primary stage will be embedded in
	 *                     the browser if the application was launched as an applet.
	 *                     Applications may create other stages, if needed, but they will not be
	 *                     primary stages and will not be embedded in the browser.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		controller.openMainStage(ExampleView.class);

		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				controller.openSeparateStage(ExampleView.class).synchronize();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, 1, 1, TimeUnit.SECONDS)
		;
	}

	private class ExamplePresenter implements Presenter<ExampleView> {

		private ExampleView view;

		@Override
		public void onClose() {
			System.out.println("sadly saying goodbye...");
		}

		@Override
		public void setView(final ExampleView exampleView) {
			this.view = exampleView;
		}

		@Override
		public ExampleView getView() {
			return view;
		}
	}

	private class ExampleView extends CustomStage implements View<ExamplePresenter> {

		private ExamplePresenter presenter;

		private ExampleView(final ExamplePresenter presenter) {
			this.presenter = presenter;
		}

		@Override
		public ExamplePresenter getPresenter() {
			return presenter;
		}

		@Override
		public void instantiate() throws InstantiationException {
			final Pane parent = new Pane();

			Scene scene = new Scene(parent);

			setScene(scene);
			setWidth(ThreadLocalRandom.current().nextInt(200, 600));
			setHeight(ThreadLocalRandom.current().nextInt(200, 600));
		}
	}

	private class CustomStage extends Stage implements Show { }

}
