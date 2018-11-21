package org.librairy.service.learner.model;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Export {

    private DockerHubCredentials credentials;

    private String title        = "My Model";

    private String description  = "Built by using librAIry learner service";

    private String contactName  = "oeg-upm";

    private String contactEmail = "info@upm.es";

    private String contactUrl   = "http://librairy.linkeddata.es";

    private String licenseName  = "Apache License Version 2.0";

    private String licenseUrl   = "https://www.apache.org/licenses/LICENSE-2.0";

    private Boolean pushDockerHub   = true;

    private Boolean removeAfterPush = false;

    public Export() {
    }

    public DockerHubCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(DockerHubCredentials credentials) {
        this.credentials = credentials;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public Boolean getPushDockerHub() {
        return pushDockerHub;
    }

    public void setPushDockerHub(Boolean pushDockerHub) {
        this.pushDockerHub = pushDockerHub;
    }

    public Boolean getRemoveAfterPush() {
        return removeAfterPush;
    }

    public void setRemoveAfterPush(Boolean removeAfterPush) {
        this.removeAfterPush = removeAfterPush;
    }
}
