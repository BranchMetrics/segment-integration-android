package com.segment.analytics.android.integrations.tune;

import android.app.Activity;
import android.app.Application;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import com.tune.Tune;
import com.tune.TuneEvent;
import com.tune.ma.application.TuneActivity;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest({Tune.class, TuneEvent.class, TuneIntegration.class})
public class TuneTest {

    @Rule public PowerMockRule rule = new PowerMockRule();
    @Mock Tune tune;
    @Mock TuneActivity tuneActivity;
    @Mock TuneEvent event;
    @Mock Application context;
    Logger logger;
    @Mock Analytics analytics;

    TuneIntegration integration;

    @Before public void setUp() {
        initMocks(this);
        mockStatic(Tune.class);
        logger = Logger.with(Analytics.LogLevel.DEBUG);
        when(Tune.init(context, "advertiser_id", "conversion_key", false)).thenReturn(tune);
        when(Tune.init(context, "advertiser_id", "conversion_key", true)).thenReturn(tune);
        when(analytics.logger("TUNE")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);

        integration = new TuneIntegration(context, "advertiser_id", "conversion_key", false, "gcm_sender_id", logger);
    }

    @Test public void factory() {
        ValueMap settings = new ValueMap().putValue("advertiserId", "advertiser_id")
                .putValue("conversionKey", "conversion_key")
                .putValue("turnOnTMA", false);

        TuneIntegration integration =
                (TuneIntegration) TuneIntegration.FACTORY.create(settings, analytics);

        verifyStatic();
        assertThat(integration.advertiserId).isEqualTo("advertiser_id");
        assertThat(integration.conversionKey).isEqualTo("conversion_key");
        assertThat(integration.turnOnTMA).isFalse();
        assertThat(integration.gcmSenderId).isNotEqualTo("gcm_sender_id");
        assertThat(integration.tune).isEqualTo(tune);
        assertThat(integration.tune).isNotNull();
    }

    @Test public void initializeWithTMA() throws IllegalStateException {
        ValueMap settings = new ValueMap().putValue("advertiserId", "advertiser_id")
                .putValue("conversionKey", "conversion_key")
                .putValue("turnOnTMA", true)
                .putValue("gcmSenderId", "gcm_sender_id");

        TuneIntegration integration =
                (TuneIntegration) TuneIntegration.FACTORY.create(settings, analytics);

        verifyStatic();
        assertThat(integration.advertiserId).isEqualTo("advertiser_id");
        assertThat(integration.conversionKey).isEqualTo("conversion_key");
        assertThat(integration.turnOnTMA).isTrue();
        assertThat(integration.gcmSenderId).isEqualTo("gcm_sender_id");
        assertThat(integration.tune).isEqualTo(tune);
        assertThat(integration.tune).isNotNull();
    }

    @Test public void activityStart() {
        // Enable TMA to check for TuneActivity
        ValueMap settings = new ValueMap().putValue("advertiserId", "advertiser_id")
                .putValue("conversionKey", "conversion_key")
                .putValue("turnOnTMA", true);

        TuneIntegration integration =
                (TuneIntegration) TuneIntegration.FACTORY.create(settings, analytics);

        Activity activity = mock(Activity.class);
        integration.onActivityStarted(activity);

        verifyStatic();
        TuneActivity.onStart(activity);
    }

    @Test public void activityResume() {
        // Enable TMA to check for TuneActivity
        ValueMap settings = new ValueMap().putValue("advertiserId", "advertiser_id")
                .putValue("conversionKey", "conversion_key")
                .putValue("turnOnTMA", true);

        TuneIntegration integration =
                (TuneIntegration) TuneIntegration.FACTORY.create(settings, analytics);

        Activity activity = mock(Activity.class);
        integration.onActivityResumed(activity);

        verify(tune).setReferralSources(activity);
        verify(tune).measureSession();
        verifyNoMoreTuneInteractions();

        verifyStatic();
        TuneActivity.onResume(activity);
    }

    @Test public void activityStop() {
        // Enable TMA to check for TuneActivity
        ValueMap settings = new ValueMap().putValue("advertiserId", "advertiser_id")
                .putValue("conversionKey", "conversion_key")
                .putValue("turnOnTMA", true);

        TuneIntegration integration =
                (TuneIntegration) TuneIntegration.FACTORY.create(settings, analytics);

        Activity activity = mock(Activity.class);
        integration.onActivityStopped(activity);
        verifyStatic();
        TuneActivity.onStop(activity);
    }

    @Test public void track() throws Exception {
        TuneEvent event = Mockito.mock(TuneEvent.class);
        whenNew(TuneEvent.class).withArguments(Mockito.anyString()).thenReturn(event);

        integration.track(new TrackPayloadBuilder().event("foo").build());
        verify(event).withRevenue(0);
        verify(event).withCurrencyCode(null);
        verify(event).withAdvertiserRefId(null);
        verify(event).withContentId(null);
        verify(event).withContentType(null);
        verify(tune).measureEvent(event);
        verifyNoMoreTuneInteractions();
    }

    @Test public void trackWithProperties() throws Exception {
        TuneEvent event = Mockito.mock(TuneEvent.class);
        whenNew(TuneEvent.class).withArguments(Mockito.anyString()).thenReturn(event);

        Properties properties = new Properties()
                .putRevenue(0.99)
                .putCurrency("USD")
                .putOrderId("1234")
                .putProductId("abc")
                .putCategory("produce");
        integration.track(new TrackPayloadBuilder().event("foo").properties(properties).build());

        verify(event).withRevenue(0.99);
        verify(event).withCurrencyCode("USD");
        verify(event).withAdvertiserRefId("1234");
        verify(event).withContentId("abc");
        verify(event).withContentType("produce");
        verify(tune).measureEvent(event);
        verifyNoMoreTuneInteractions();
    }

    @Test public void identify() {
        Traits traits = createTraits("foo");
        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());
        verify(tune).setUserId("foo");
        verify(tune).setPhoneNumber(null);
        verify(tune).setUserEmail(null);
        verify(tune).setUserName(null);
        verifyNoMoreTuneInteractions();
    }

    @Test public void identifyWithProperties() throws JSONException {
        Traits traits = createTraits("foo")
                .putEmail("friends@segment.com")
                .putPhone("1-844-611-0621")
                .putUsername("segmentio");

        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());
        verify(tune).setUserId("foo");
        verify(tune).setPhoneNumber("1-844-611-0621");
        verify(tune).setUserEmail("friends@segment.com");
        verify(tune).setUserName("segmentio");
        verifyNoMoreTuneInteractions();
    }

    @Test public void reset() {
        integration.reset();
        verify(tune).setUserId(null);
        verify(tune).setPhoneNumber(null);
        verify(tune).setUserEmail(null);
        verify(tune).setUserName(null);
        verifyNoMoreTuneInteractions();
    }

    private void verifyNoMoreTuneInteractions() {
        verifyNoMoreInteractions(tune);
    }
}
