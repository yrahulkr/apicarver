package com.apicarv.testCarver.uitestrunner;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

public interface CrawlingRules {
    public void setCrawlingRules(CrawljaxConfigurationBuilder builder);

    public boolean getVisualData();
}
