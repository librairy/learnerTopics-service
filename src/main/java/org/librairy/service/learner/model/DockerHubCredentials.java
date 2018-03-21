package org.librairy.service.learner.model;

import com.google.common.base.Strings;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class DockerHubCredentials {

    private String username     = "";

    private String password     = "";

    private String email        = "";

    private String repository   = "";

    private String server       = "";//https://registry.hub.docker.com/";

    private String token        = "";

    public DockerHubCredentials() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEmpty(){
        return (Strings.isNullOrEmpty(username) && Strings.isNullOrEmpty(email)) || (Strings.isNullOrEmpty(password) && Strings.isNullOrEmpty(token)) || Strings.isNullOrEmpty(repository);
    }
}
