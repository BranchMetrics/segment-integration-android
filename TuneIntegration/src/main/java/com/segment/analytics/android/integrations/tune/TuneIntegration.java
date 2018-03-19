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
    private static final String TUNE_KEY = "TUNE";
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

            // Check that advertiserId, conversionKey were passed
            if (TextUtils.isEmpty(advertiserId)) {
                logger.info("Please add TUNE advertiser id in Segment settings.");
                return null;
            }

            if (TextUtils.isEmpty(conversionKey)) {
                logger.info("Please add TUNE conversion key in Segment settings.");
                return null;
            }

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
        logger.verbose("Initializing TuneIntegration, advertiserId: %s, conversionKey: %s", advertiserId, conversionKey);
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
    public void onActivityResumed(Activity activity) {
        logger.verbose("TuneIntegration onActivityResumed: Calling TUNE measureSession");
        if (turnOnTMA) {
            TuneActivity.onResume(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        logger.verbose("TuneIntegration onActivityPaused");
        if (turnOnTMA) {
            TuneActivity.onPause(activity);
        }
    }

    // These are messages in the format of the Segment API.
    // You can refer to the Javadocs for each payload in more detail.
    // Semantically, they are the same as documented in our spec:
    // https://segment.com/docs/spec/.
    @Override
    public void identify(IdentifyPayload identify) {
        logger.verbose("TuneIntegration identify: Setting TUNE user identifiers");
        tune.setUserId(identify.userId());
        tune.setPhoneNumber(identify.traits().phone());
        tune.setUserEmail(identify.traits().email());
        tune.setUserName(identify.traits().username());
    }

    @Override
    public void track(TrackPayload track) {
        String eventName = track.event();
        // Map Segment's "Completed Order" event to TUNE's "Purchase" event in order to record revenue
        if (eventName.equals("Completed Order")) {
            eventName = TuneEvent.PURCHASE;
        }

        logger.verbose("TuneIntegration track: Calling TUNE measureEvent with %s", eventName);
        TuneEvent event = new TuneEvent(eventName);
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
        logger.verbose("TuneIntegration reset: Clearing TUNE user identifiers");
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
