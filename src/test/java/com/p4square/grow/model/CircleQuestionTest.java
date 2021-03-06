/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for CircleQuestion.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CircleQuestionTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(CircleQuestionTest.class.getName());
    }

    private CircleQuestion mQuestion;

    @Before
    public void setUp() {
        mQuestion = new CircleQuestion();

        Answer a1 = new Answer();
        a1.setScore(2);

        Answer a2 = new Answer();
        a2.setScore(4);

        mQuestion.getAnswers().put("1.00,0.00", a1);
        mQuestion.getAnswers().put("-1.00,0.00", a2);
    }

    /**
     * Verify the getters and setters function correctly.
     */
    @Test
    public void testGetAndSet() {
        mQuestion.setTopLeft("TopLeft String");
        assertEquals("TopLeft String", mQuestion.getTopLeft());

        mQuestion.setTopRight("TopRight String");
        assertEquals("TopRight String", mQuestion.getTopRight());

        mQuestion.setBottomRight("BottomRight String");
        assertEquals("BottomRight String", mQuestion.getBottomRight());

        mQuestion.setBottomLeft("BottomLeft String");
        assertEquals("BottomLeft String", mQuestion.getBottomLeft());
    }

    /**
     * The ScoringEngines are tested extensively independently, so simply
     * verify that we get the expected results for our input.
     */
    @Test
    public void testScoreAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();

        answer.setAnswerId("0.5,0.5");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(2, score.sum, DELTA);
        assertEquals(1, score.count);

        answer.setAnswerId("-0.5,-0.5");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(6, score.sum, DELTA);
        assertEquals(2, score.count);

        try {
            answer.setAnswerId("notAPoint");
            assertTrue(mQuestion.scoreAnswer(score, answer));
            fail("Should have thrown exception.");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verify the correct type string is returned.
     */
    @Test
    public void testType() {
        assertEquals("circle", mQuestion.getType().toString());
    }
}
