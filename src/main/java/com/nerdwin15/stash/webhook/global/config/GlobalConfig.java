package com.nerdwin15.stash.webhook.global.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class GlobalConfig {

    @XmlElement
    private String executablePath;

    @XmlElement
    private String configHome;

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(final String executablePath) {
        this.executablePath = executablePath;
    }

    public String getConfigHome() {
        return configHome;
    }

    public void setConfigHome(final String configHome) {
        this.configHome = configHome;
    }
}
