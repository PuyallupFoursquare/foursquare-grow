/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model of an assessment question.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class Question {
    public static enum QuestionType {
        TEXT, IMAGE, SLIDER, QUAD, CIRCLE;
    }

    private final String mQuestionId;
    private final QuestionType mType;
    private final String mQuestionText;
    private Map<String, Answer> mAnswers;

    private final String mPreviousQuestionId;
    private final String mNextQuestionId;

    public Question(final Map<String, Object> map) {
        mQuestionId = (String) map.get("id");
        mType = QuestionType.valueOf(((String) map.get("type")).toUpperCase());

        mQuestionText = (String) map.get("text");

        mPreviousQuestionId = (String) map.get("previousQuestion");
        mNextQuestionId = (String) map.get("nextQuestion");

        mAnswers = new HashMap<String, Answer>();
        for (Map.Entry<String, Object> answer :
                ((Map<String, Object>) map.get("answers")).entrySet()) {

            final String id = answer.getKey();
            final Map<String, Object> answerMap = (Map<String, Object>) answer.getValue();
            final Answer answerObj = new Answer(id, answerMap);
            mAnswers.put(id, answerObj);
        }
    }

    public String getId() {
        return mQuestionId;
    }

    public QuestionType getType() {
        return mType;
    }

    public String getText() {
        return mQuestionText;
    }

    public String getPrevious() {
        return mPreviousQuestionId;
    }

    public String getNext() {
        return mNextQuestionId;
    }

    public Map<String, Answer> getAnswers() {
        return Collections.unmodifiableMap(mAnswers);
    }
}