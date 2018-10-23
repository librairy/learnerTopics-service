package org.librairy.service.learner.service;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.spotify.docker.client.AnsiProgressHandler;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.auth.ConfigFileRegistryAuthSupplier;
import com.spotify.docker.client.auth.MultiRegistryAuthSupplier;
import com.spotify.docker.client.auth.NoOpRegistryAuthSupplier;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.librairy.service.learner.model.DockerHubCredentials;
import org.librairy.service.learner.model.Export;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);
    private VelocityEngine velocityEngine;
    private ExecutorService dockerExecutor;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @PostConstruct
    public void setup() throws IOException {

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        dockerExecutor = Executors.newSingleThreadExecutor();

        LOG.info("Export Service initialized");
    }


    public boolean request(Export export) throws Exception {
        RegistryAuth credentials = new RegistryAuth() {
            @Override
            public String username() {
                return export.getCredentials().getUsername();
            }

            @Override
            public String password() {
                return export.getCredentials().getPassword();
            }

            @Override
            public String email() {
                return export.getCredentials().getEmail();
            }

            @Override
            public String serverAddress() {
                return export.getCredentials().getServer();
            }

            @Override
            public String identityToken() {
                return export.getCredentials().getToken();
            }

            @Override
            public Builder toBuilder() {
                return null;
            }
        };

        RegistryAuthSupplier registryAuth = new RegistryAuthSupplier() {
            @Override
            public RegistryAuth authFor(String s) throws DockerException {
                return credentials;
            }

            @Override
            public RegistryAuth authForSwarm() throws DockerException {
                return credentials;
            }

            @Override
            public RegistryConfigs authForBuild() throws DockerException {
                return null;
            }
        };

        DefaultDockerClient.Builder builder = DefaultDockerClient
                .fromEnv()
                .readTimeoutMillis(0);

        builder.uri("unix:///var/run/docker.sock");

        builder.registryAuthSupplier(authSupplier(export));

        DefaultDockerClient dockerClient = builder.build();

        LOG.info("Host: " + dockerClient.getHost());


//            int res = dockerClient.auth(credentials);

//            LOG.info("Credentials: " + credentials + " => " + res);

        Template t = velocityEngine.getTemplate("Dockerfile.vm");

        VelocityContext context = new VelocityContext();
        context.put("title", export.getTitle());
        context.put("description", export.getDescription());
        context.put("licenseName", export.getLicenseName());
        context.put("licenseUrl", export.getLicenseUrl());
        context.put("contactEmail", export.getContactEmail());
        context.put("contactName", export.getContactName());
        context.put("contactUrl", export.getContactUrl());

        String dockerFile = Paths.get(Paths.get(resourceFolder).getParent().toString(), "Dockerfile").toFile().getAbsolutePath();

        FileWriter fw = new FileWriter(dockerFile);
        t.merge(context, fw);
        fw.close();

        String imageName = export.getCredentials().getRepository();

        LOG.info("Bulding Docker Image from : " + export);

        final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();

        dockerClient.pull("librairy/modeler-topics-service");

        final String returnedImageId = dockerClient.build(
                Paths.get(resourceFolder).getParent(), imageName, new ProgressHandler() {
                    @Override
                    public void progress(ProgressMessage message) throws DockerException {
                        final String imageId = message.buildImageId();
                        if (imageId != null) {
                            imageIdFromMessage.set(imageId);
                        }
                    }
                });

        LOG.info("Image ID: " + returnedImageId);


        if (Strings.isNullOrEmpty(returnedImageId)) return false;

//            dockerClient.push(export.getCredentials().getRepository());

        dockerExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final AnsiProgressHandler ansiProgressHandler = new AnsiProgressHandler();
                    final DigestExtractingProgressHandler handler = new DigestExtractingProgressHandler(ansiProgressHandler);
                    try {
                        LOG.info("Pushing " + imageName);
                        dockerClient.push(imageName, handler, credentials);
                    } catch (Exception e) {
                        LOG.error("Error on push: ", e);
                    }

                    dockerClient.removeImage(imageName);

                } catch (Exception e) {
                    LOG.warn("Error pushing docker image", e);
                }
            }
        });
        return true;
    }

    private RegistryAuthSupplier authSupplier(Export export) throws Exception {

        final List<RegistryAuthSupplier> suppliers = new ArrayList<>();

        // prioritize the docker config file
        suppliers.add(new ConfigFileRegistryAuthSupplier());

        // then Google Container Registry support
//        final RegistryAuthSupplier googleRegistrySupplier = googleContainerRegistryAuthSupplier();
//        if (googleRegistrySupplier != null) {
//            suppliers.add(googleRegistrySupplier);
//        }

        // lastly, use any explicitly configured RegistryAuth as a catch-all
        final RegistryAuth registryAuth = registryAuth(export.getCredentials());
        if (registryAuth != null) {
            final RegistryConfigs configsForBuild = RegistryConfigs.create(ImmutableMap.of("dockerhub", registryAuth));
            suppliers.add(new NoOpRegistryAuthSupplier(registryAuth, configsForBuild));
        }

        LOG.info("Using authentication suppliers: " +
                Lists.transform(suppliers, new SupplierToClassNameFunction()));

        return new MultiRegistryAuthSupplier(suppliers);
    }


    protected RegistryAuth registryAuth(DockerHubCredentials credentials) throws Exception {
        final RegistryAuth.Builder registryAuthBuilder = RegistryAuth.builder();
        final String registryUrl = credentials.getServer();
        final String username = credentials.getUsername();
        String password = credentials.getPassword();
//        if (secDispatcher != null) {
//            try {
//                password = secDispatcher.decrypt(password);
//            } catch (SecDispatcherException ex) {
//                throw new MojoExecutionException("Cannot decrypt password from settings", ex);
//            }
//        }
        final String email = credentials.getEmail();

        if (!isNullOrEmpty(username)) {
            registryAuthBuilder.username(username);
        }
        if (!isNullOrEmpty(email)) {
            registryAuthBuilder.email(email);
        }
        if (!isNullOrEmpty(password)) {
            registryAuthBuilder.password(password);
        }
        if (!isNullOrEmpty(registryUrl)) {
            registryAuthBuilder.serverAddress(registryUrl);
        }

        return registryAuthBuilder.build();
    }

    private static class DigestExtractingProgressHandler implements ProgressHandler {

        private final ProgressHandler delegate;
        private String digest;

        DigestExtractingProgressHandler(final ProgressHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void progress(final ProgressMessage message) throws DockerException {
            if (message.digest() != null) {
                digest = message.digest();
            }

            delegate.progress(message);
        }

        public String digest() {
            return digest;
        }
    }


    private static class SupplierToClassNameFunction
            implements Function<RegistryAuthSupplier, String> {

        @Override
        public String apply( final RegistryAuthSupplier input) {
            return input.getClass().getSimpleName();
        }
    }


}
