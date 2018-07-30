package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Handlebars;

public class JzonbieHandlebars extends Handlebars {
    public JzonbieHandlebars() {
        super();
        registerHelper("jsonPath", new JsonPathHelper());
    }
}