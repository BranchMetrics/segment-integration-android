package com.segment.analytics.android.integrations.tune;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.tune.Tune;
import com.tune.TuneEvent;
import com.tune.ma.application.TuneActivity;

/**
 * Created by johng on 4/12/16.
 */
public class TuneIntegration extends Integration<Tune> {
    private static final String TUNE_KEY = "MobileAppTracking";
    private Logger logger;

    final Tune tune;
    final String advertiserId;
    final String conversionKey;
    final boolean turnOnTMA;
    final String gcmSenderId;

    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            Logger logger = analytics.logger(TUNE_KEY);
            Context context = analytics.getApplication();
            String advertiserId = settings.getString("advertiserId");
            String conversionKey = settings.getString("conversionKey");
            boolean turnOnTMA = settings.getBoolean("turnOnTMA", false);
            String gcmSenderId = settings.getString("gcmSenderId");
            return new TuneIntegration(context, advertiserId, conversionKey, turnOnTMA, gcmSenderId, logger);
        }

        @Override
        public String key() {
            return TUNE_KEY;
        }
    };

    public TuneIntegration(Context context, String advertiserId, String conversionKey,
                           boolean turnOnTMA, String gcmSenderId, Logger logger) {
        this.logger = logger;
        logger.verbose("Initializing Tune Integration, advertiserId: %s, conversionKey: %s, " +
                "turnOnTMA: %b", advertiserId, conversionKey, turnOnTMA);
        this.advertiserId = advertiserId;
        this.conversionKey = conversionKey;
        this.turnOnTMA = turnOnTMA;
        this.gcmSenderId = gcmSenderId;
        this.tune = Tune.init(context, advertiserId, conversionKey, turnOnTMA);

        if (this.turnOnTMA) {
            if (!TextUtils.isEmpty(gcmSenderId)) {
                this.tune.setPushNotificationSenderId(gcmSenderId);
            }
        }
        logger.verbose("TuneIntegration initialized.");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        logger.verbose("TuneIntegration onActivityStarted");
        if (turnOnTMA) {
            TuneActivity.onStart(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        logger.verbose("TuneIntegration onActivityResumed: Calling Tune measureSession");
        if (turnOnTMA) {
            TuneActivity.onResume(activity);
        }
        tune.setReferralSources(activity);
        tune.measureSession();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        logger.verbose("TuneIntegration onActivityStopped");
        if (turnOnTMA) {
            TuneActivity.onStop(activity);
        }
    }

    // These are messages in the format of the Segment API.
    // You can refer to the Javadocs for each payload in more detail.
    // Semantically, they are the same as documented in our spec:
    // https://segment.com/docs/spec/.
    @Override
    public void identify(IdentifyPayload identify) {
        logger.verbose("TuneIntegration identify: Setting Tune user identifiers");
        tune.setUserId(identify.userId());
        tune.setPhoneNumber(identify.traits().phone());
        tune.setUserEmail(identify.traits().email());
        tune.setUserName(identify.traits().username());
    }

    @Override
    public void track(TrackPayload track) {
        logger.verbose("TuneIntegration track: Calling Tune measureEvent with %s", track.event());
        TuneEvent event = new TuneEvent(track.event());
        event.withRevenue(track.properties().revenue());
        event.withCurrencyCode(track.properties().currency());
        event.withAdvertiserRefId(track.properties().orderId());
        event.withContentId(track.properties().productId());
        event.withContentType(track.properties().category());
        tune.measureEvent(event);
    }

    // Reset is when you clear out any local user data.
    @Override
    public void reset() {
        logger.verbose("TuneIntegration reset: Clearing Tune user identifiers");
        tune.setUserId(null);
        tune.setPhoneNumber(null);
        tune.setUserEmail(null);
        tune.setUserName(null);
    }

    // This should return the same object your users would have to use if
    // they were to integrate natively.
    // e.g. Localytics would return `null`, but Mixpanel would return an instance
    // of a MixpanelAPI object.
    @Override
    public Tune getUnderlyingInstance() {
        return tune;
    }
}
