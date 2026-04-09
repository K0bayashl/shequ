package com.community.mvp.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "community-mvp")
public class CommunityMvpProperties {

    private final Runtime runtime = new Runtime();
    private final Security security = new Security();
    private final Cache cache = new Cache();
    private final Docs docs = new Docs();

    public Runtime getRuntime() {
        return runtime;
    }

    public Security getSecurity() {
        return security;
    }

    public Cache getCache() {
        return cache;
    }

    public Docs getDocs() {
        return docs;
    }

    public static class Runtime {

        private boolean databaseEnabled;

        public boolean isDatabaseEnabled() {
            return databaseEnabled;
        }

        public void setDatabaseEnabled(boolean databaseEnabled) {
            this.databaseEnabled = databaseEnabled;
        }
    }

    public static class Security {

        private boolean jwtEnabled;
        private String jwtSecret = "";

        public boolean isJwtEnabled() {
            return jwtEnabled;
        }

        public void setJwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
        }

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }
    }

    public static class Cache {

        private boolean redisEnabled;

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }
    }

    public static class Docs {

        private boolean openapiEnabled;

        public boolean isOpenapiEnabled() {
            return openapiEnabled;
        }

        public void setOpenapiEnabled(boolean openapiEnabled) {
            this.openapiEnabled = openapiEnabled;
        }
    }
}
