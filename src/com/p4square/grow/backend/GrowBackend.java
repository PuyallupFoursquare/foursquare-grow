/*
 * Copyright 2012 Jesse Morgan
 */

package com.p4square.grow.backend;

import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.p4square.grow.config.Config;

import com.p4square.grow.backend.db.CassandraDatabase;
import com.p4square.grow.backend.db.CassandraKey;
import com.p4square.grow.backend.db.CassandraProviderImpl;

import com.p4square.grow.model.Question;

import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.QuestionProvider;

import com.p4square.grow.backend.resources.AccountResource;
import com.p4square.grow.backend.resources.BannerResource;
import com.p4square.grow.backend.resources.SurveyResource;
import com.p4square.grow.backend.resources.SurveyResultsResource;
import com.p4square.grow.backend.resources.TrainingRecordResource;
import com.p4square.grow.backend.resources.TrainingResource;

/**
 * Main class for the backend application.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowBackend extends Application {
    private static final String DEFAULT_COLUMN = "value";

    private final static Logger LOG = Logger.getLogger(GrowBackend.class);

    private final Config mConfig;
    private final CassandraDatabase mDatabase;

    private final Provider<String, Question> mQuestionProvider;

    public GrowBackend() {
        this(new Config());
    }

    public GrowBackend(Config config) {
        mConfig = config;
        mDatabase = new CassandraDatabase();

        mQuestionProvider = new QuestionProvider<CassandraKey>(new CassandraProviderImpl<Question>(mDatabase, "strings", Question.class)) {
            @Override
            public CassandraKey makeKey(String questionId) {
                return new CassandraKey("/questions/" + questionId, DEFAULT_COLUMN);
            }
        };
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Account API
        router.attach("/accounts/{userId}", AccountResource.class);

        // Survey API
        router.attach("/assessment/question/{questionId}", SurveyResource.class);

        router.attach("/accounts/{userId}/assessment", SurveyResultsResource.class);
        router.attach("/accounts/{userId}/assessment/answers/{questionId}",
                SurveyResultsResource.class);

        // Training API
        router.attach("/training/{level}", TrainingResource.class);
        router.attach("/training/{level}/videos/{videoId}", TrainingResource.class);

        router.attach("/accounts/{userId}/training", TrainingRecordResource.class);
        router.attach("/accounts/{userId}/training/videos/{videoId}",
                TrainingRecordResource.class);

        // Misc.
        router.attach("/banner", BannerResource.class);

        return router;
    }

    /**
     * Open the database.
     */
    @Override
    public void start() throws Exception {
        super.start();

        // Setup database
        mDatabase.setClusterName(mConfig.getString("clusterName", "Dev Cluster"));
        mDatabase.setKeyspaceName(mConfig.getString("keyspace", "GROW"));
        mDatabase.init();
    }

    /**
     * Close the database.
     */
    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down...");
        mDatabase.close();

        super.stop();
    }

    /**
     * @return the current database.
     */
    public CassandraDatabase getDatabase() {
        return mDatabase;
    }

    public Provider<String, Question> getQuestionProvider() {
        return mQuestionProvider;
    }

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) throws Exception {
        // Start the HTTP Server
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 9095);
        component.getClients().add(Protocol.HTTP);
        component.getDefaultHost().attach(new GrowBackend());

        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    component.stop();
                } catch (Exception e) {
                    LOG.error("Exception during cleanup", e);
                }
            }
        });

        LOG.info("Starting server...");

        try {
            component.start();
        } catch (Exception e) {
            LOG.fatal("Could not start: " + e.getMessage(), e);
        }
    }
}
