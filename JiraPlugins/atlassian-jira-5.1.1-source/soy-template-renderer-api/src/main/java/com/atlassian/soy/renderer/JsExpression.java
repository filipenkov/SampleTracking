package com.atlassian.soy.renderer;

public class JsExpression
{

    private final String text;

    public JsExpression(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
