package app.we.go.firebase.rx2.remoteconfig;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * rxjava2 wrapper for fetching and reading firebase Remote Config values.<br/>
 * Clients must call {@link #fetch(Activity)} before calling any of the <code>get</code> methods,
 * failing to do so will result in an error. The <code>fetch</code> and <code>get</code>
 * calls can be chained as in the example:
 * <pre>
 *    remoteConfigHelper
 *       .fetch(getActivity())
 *       .andThen(remoteConfigHelper.getBoolean(PARAM_NAME))
 *       .subscribeWith(.....)
 * </pre>
 */
public class RemoteConfigHelper {

    private static final String TAG = "CONFIG";
    private static final long DEFAULT_CACHE_EXPIRATION = 100L;

    private FirebaseRemoteConfig remoteConfig;

    private boolean initialized;
    private long cacheExpiration;

    private RemoteConfigHelper() {
        remoteConfig = FirebaseRemoteConfig.getInstance();

        cacheExpiration = DEFAULT_CACHE_EXPIRATION;
    }

    private void setDefaultResources(@XmlRes int defaultResources) {
        remoteConfig.setDefaults(defaultResources);
    }

    private void setCacheExpiration(long cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    @NonNull
    public Completable fetch(@NonNull Activity activity) {

        return Completable.create(e ->
                remoteConfig.fetch(cacheExpiration)
                        .addOnCompleteListener(activity, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Config fetched successfully");
                                remoteConfig.activateFetched();
                                initialized = true;
                                e.onComplete();
                            } else {
                                Log.e(TAG, "Config fetch error");
                                e.onError(task.getException());
                            }
                        }));
    }

    @NonNull
    public Single<String> getString(@NonNull String key) {
        return Single.fromCallable(() -> {
            checkThatFetched();
            return remoteConfig.getString(key);
        });
    }

    @NonNull
    public Single<Boolean> getBoolean(@NonNull String key) {
        return Single.fromCallable(() -> {
            checkThatFetched();
            return remoteConfig.getBoolean(key);
        });
    }

    @NonNull
    public Single<Long> getLong(@NonNull String key) {
        return Single.fromCallable(() -> {
            checkThatFetched();
            return remoteConfig.getLong(key);
        });
    }

    @NonNull
    public Single<Double> getDouble(@NonNull String key) {
        return Single.fromCallable(() -> {
            checkThatFetched();
            return remoteConfig.getDouble(key);
        });
    }


    private void checkThatFetched() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized. Please call fetch() first.");
        }
    }

    /**
     * Builder pattern, allows the defaults and cache expiration to be set for the
     * {@link RemoteConfigHelper}
     */
    public static class Builder {

        private int defaultResources;
        private long cacheExpiration;
        private boolean developerModeEnabled;

        public Builder() {
            // nothing to initialize
        }

        public Builder withDefaults(@XmlRes int defaultResources) {
            this.defaultResources = defaultResources;
            return this;
        }

        public Builder withCacheExpiration(long cacheExpiration) {
            this.cacheExpiration = cacheExpiration;
            return this;
        }

        public Builder withDeveloperModeEnabled(boolean developerModeEnabled) {
            this.developerModeEnabled = developerModeEnabled;
            return this;
        }


        public RemoteConfigHelper build() {
            FirebaseRemoteConfigSettings settings =
                    new FirebaseRemoteConfigSettings.Builder()
                            .setDeveloperModeEnabled(developerModeEnabled)
                            .build();
            FirebaseRemoteConfig.getInstance().setConfigSettings(settings);

            RemoteConfigHelper configHelper = new RemoteConfigHelper();
            if (defaultResources != 0) {
                configHelper.setDefaultResources(defaultResources);
            }
            if (cacheExpiration > 0) {
                configHelper.setCacheExpiration(cacheExpiration);
            }

            return configHelper;
        }

    }

}
