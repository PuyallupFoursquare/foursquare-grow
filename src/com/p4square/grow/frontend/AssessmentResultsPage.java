/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.Date;
import java.util.Map;

import com.p4square.f1oauth.FellowshipOneIntegrationDriver;
import freemarker.template.Template;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.fmfacade.json.JsonRequestClient;
import com.p4square.fmfacade.json.JsonResponse;
import com.p4square.fmfacade.json.ClientException;

import com.p4square.f1oauth.Attribute;
import com.p4square.f1oauth.F1API;
import com.p4square.f1oauth.F1User;

import com.p4square.grow.config.Config;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * This page fetches the user's final score and displays the transitional page between
 * the assessment and the videos.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AssessmentResultsPage extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(AssessmentResultsPage.class);

    private GrowFrontend mGrowFrontend;
    private Config mConfig;
    private JsonRequestClient mJsonClient;

    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mConfig = mGrowFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        Template t = mGrowFrontend.getTemplate("templates/assessment-results.ftl");

        try {
            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return ErrorPage.TEMPLATE_NOT_FOUND;
            }

            Map<String, Object> root = getRootObject();

            // Get the assessment results
            JsonResponse response = backendGet("/accounts/" + mUserId + "/assessment");
            if (!response.getStatus().isSuccess()) {
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return ErrorPage.BACKEND_ERROR;
            }

            final String score = (String) response.getMap().get("result");
            if (score == null) {
                // Odd... send them to the first questions
                String nextPage = mConfig.getString("dynamicRoot", "")
                    + "/account/assessment/question/first";
                getResponse().redirectSeeOther(nextPage);
                return new StringRepresentation("Redirecting to " + nextPage);
            }

            // Publish results in F1
            publishScoreInF1(response.getMap());

            root.put("stage", score);
            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    private void publishScoreInF1(Map results) {
        if (!(getRequest().getClientInfo().getUser() instanceof F1User)) {
            // Only useful if the user is from F1.
            return;
        }

        F1User user = (F1User) getRequest().getClientInfo().getUser();

        // Update the attribute.
        String attributeName = "Assessment Complete - " + results.get("result");

        try {
            Attribute attribute = new Attribute(attributeName);
            attribute.setStartDate(new Date());
            attribute.setComment(JsonEncodedProvider.MAPPER.writeValueAsString(results));

            F1API f1 = ((FellowshipOneIntegrationDriver) mGrowFrontend.getThirdPartyIntegrationFactory())
                            .getF1Access().getAuthenticatedApi(user);
            if (!f1.addAttribute(user.getIdentifier(), attribute)) {
                LOG.error("addAttribute failed for " + user.getIdentifier() 
                        + " with attribute " + attributeName);
            }
        } catch (Exception e) {
            LOG.error("addAttribute failed for " + user.getIdentifier() 
                    + " with attribute " + attributeName, e);
        }
    }

    /**
     * @return The backend endpoint URI
     */
    private String getBackendEndpoint() {
        return mConfig.getString("backendUri", "riap://component/backend");
    }

    /**
     * Helper method to send a GET to the backend.
     */
    private JsonResponse backendGet(final String uri) {
        LOG.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.get(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            LOG.warn("Error making backend request for '" + uri + "'. status = "
                    + response.getStatus().toString());
        }

        return response;
    }
}
