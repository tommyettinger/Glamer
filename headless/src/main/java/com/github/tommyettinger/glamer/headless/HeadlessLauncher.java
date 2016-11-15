package com.github.tommyettinger.glamer.headless;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.github.tommyettinger.glamer.GlamerTool;

/** Launches the headless application. Can be converted into a utilities project or a server application. */
public class HeadlessLauncher {
    public static void main(String[] args) {
        createApplication(args);
    }

    private static Application createApplication(String[] args) {
        // Note: you can use a custom ApplicationListener implementation for the headless project instead of GlamerTool.
        return new HeadlessApplication(new GlamerTool(args), getDefaultConfiguration());
    }

    private static HeadlessApplicationConfiguration getDefaultConfiguration() {
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.renderInterval = -1f; // When this value is negative, GlamerTool#render() is never called.
        return configuration;
    }
}