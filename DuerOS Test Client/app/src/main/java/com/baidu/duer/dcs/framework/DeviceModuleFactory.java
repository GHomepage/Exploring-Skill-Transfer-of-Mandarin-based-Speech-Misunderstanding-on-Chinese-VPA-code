
package com.baidu.duer.dcs.framework;

import com.baidu.duer.dcs.devicemodule.alerts.AlertsDeviceModule;
import com.baidu.duer.dcs.devicemodule.audioplayer.AudioPlayerDeviceModule;
import com.baidu.duer.dcs.devicemodule.playbackcontroller.PlaybackControllerDeviceModule;
import com.baidu.duer.dcs.devicemodule.screen.ScreenDeviceModule;
import com.baidu.duer.dcs.devicemodule.screen.extend.card.ScreenExtendDeviceModule;
import com.baidu.duer.dcs.devicemodule.speakcontroller.SpeakerControllerDeviceModule;
import com.baidu.duer.dcs.devicemodule.system.SystemDeviceModule;
import com.baidu.duer.dcs.devicemodule.system.message.SetEndPointPayload;
import com.baidu.duer.dcs.devicemodule.system.message.ThrowExceptionPayload;
import com.baidu.duer.dcs.devicemodule.voiceinput.VoiceInputDeviceModule;
import com.baidu.duer.dcs.devicemodule.voiceoutput.VoiceOutputDeviceModule;
import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.systeminterface.IPlaybackController;
import com.baidu.duer.dcs.systeminterface.IWebView;
import com.baidu.duer.dcs.util.LogUtil;


public class DeviceModuleFactory {
    private static final String TAG = DeviceModuleFactory.class.getSimpleName();
    private final IDeviceModuleHandler deviceModuleHandler;
    private final IMediaPlayer dialogMediaPlayer;

    private VoiceInputDeviceModule voiceInputDeviceModule;
    private VoiceOutputDeviceModule voiceOutputDeviceModule;
    private SpeakerControllerDeviceModule speakerControllerDeviceModule;
    private AudioPlayerDeviceModule audioPlayerDeviceModule;
    private AlertsDeviceModule alertsDeviceModule;
    private SystemDeviceModule systemDeviceModule;
    private PlaybackControllerDeviceModule playbackControllerDeviceModule;
    private ScreenDeviceModule screenDeviceModule;
    private ScreenExtendDeviceModule screenExtendDeviceModule;

    private enum MediaChannel {
        SPEAK("dialog", 3),
        ALERT("alert", 2),
        AUDIO("audio", 1);

        private String channelName;
        private int priority;

        MediaChannel(String channelName, int priority) {
            this.channelName = channelName;
            this.priority = priority;
        }
    }

    public DeviceModuleFactory(final IDeviceModuleHandler deviceModuleHandler) {
        this.deviceModuleHandler = deviceModuleHandler;
        dialogMediaPlayer = deviceModuleHandler.getMultiChannelMediaPlayer()
                .addNewChannel(deviceModuleHandler.getPlatformFactory().createAudioTrackPlayer(),
                        MediaChannel.SPEAK.channelName,
                        MediaChannel.SPEAK.priority);
    }


    public void createVoiceInputDeviceModule() {

        voiceInputDeviceModule = new VoiceInputDeviceModule(
                dialogMediaPlayer, deviceModuleHandler.getMessageSender(),
                deviceModuleHandler.getPlatformFactory().getVoiceInput(),
                deviceModuleHandler.getDialogRequestIdHandler(),
                deviceModuleHandler.getResponseDispatcher());
        deviceModuleHandler.addDeviceModule(voiceInputDeviceModule);
    }

    public VoiceInputDeviceModule getVoiceInputDeviceModule() {
        return voiceInputDeviceModule;
    }

    public void createVoiceOutputDeviceModule() {
        voiceOutputDeviceModule = new VoiceOutputDeviceModule(dialogMediaPlayer,
                deviceModuleHandler.getMessageSender());
        voiceOutputDeviceModule.addVoiceOutputListener(new VoiceOutputDeviceModule.IVoiceOutputListener() {
            @Override
            public void onVoiceOutputStarted() {
                LogUtil.d(TAG, "DcsResponseBodyEnqueue-onVoiceOutputStarted ok ");
                deviceModuleHandler.getResponseDispatcher().blockDependentQueue();
            }

            @Override
            public void onVoiceOutputFinished() {
                LogUtil.d(TAG, "DcsResponseBodyEnqueue-onVoiceOutputFinished ok ");
                deviceModuleHandler.getResponseDispatcher().unBlockDependentQueue();
            }
        });

        deviceModuleHandler.addDeviceModule(voiceOutputDeviceModule);
    }

    public void createSpeakControllerDeviceModule() {
        BaseMultiChannelMediaPlayer.ISpeakerController speakerController =
                deviceModuleHandler.getMultiChannelMediaPlayer().getSpeakerController();
        speakerControllerDeviceModule =
                new SpeakerControllerDeviceModule(speakerController,
                        deviceModuleHandler.getMessageSender());
        deviceModuleHandler.addDeviceModule(speakerControllerDeviceModule);
    }

    public void createAudioPlayerDeviceModule() {
        IMediaPlayer mediaPlayer = deviceModuleHandler.getMultiChannelMediaPlayer()
                .addNewChannel(deviceModuleHandler.getPlatformFactory().createMediaPlayer(),
                        MediaChannel.AUDIO.channelName,
                        MediaChannel.AUDIO.priority);
        audioPlayerDeviceModule = new AudioPlayerDeviceModule(mediaPlayer,
                deviceModuleHandler.getMessageSender());
        deviceModuleHandler.addDeviceModule(audioPlayerDeviceModule);
    }

    public AudioPlayerDeviceModule getAudioPlayerDeviceModule() {
        return audioPlayerDeviceModule;
    }

    public void createAlertsDeviceModule() {
        IMediaPlayer mediaPlayer = deviceModuleHandler.getMultiChannelMediaPlayer()
                .addNewChannel(deviceModuleHandler.getPlatformFactory().createMediaPlayer(),
                        MediaChannel.ALERT.channelName,
                        MediaChannel.ALERT.priority);
        alertsDeviceModule = new AlertsDeviceModule(mediaPlayer,
                deviceModuleHandler.getPlatformFactory().createAlertsDataStore(),
                deviceModuleHandler.getMessageSender(),
                deviceModuleHandler.getPlatformFactory().getMainHandler());

        alertsDeviceModule.addAlertListener(new AlertsDeviceModule.IAlertListener() {
            @Override
            public void onAlertStarted(String alertToken) {
            }
        });

        deviceModuleHandler.addDeviceModule(alertsDeviceModule);
    }

    public void createSystemDeviceModule() {
        systemDeviceModule = new SystemDeviceModule(deviceModuleHandler.getMessageSender());
        systemDeviceModule.addModuleListener(new SystemDeviceModule.IDeviceModuleListener() {
            @Override
            public void onSetEndpoint(SetEndPointPayload endPointPayload) {
                if (null != endPointPayload) {
                    String endpoint = endPointPayload.getEndpoint();
                    if (null != endpoint && endpoint.length() > 0) {
                        HttpConfig.setEndpoint(endpoint);
                    }
                }
            }

            @Override
            public void onThrowException(ThrowExceptionPayload throwExceptionPayload) {
                LogUtil.v(TAG, throwExceptionPayload.toString());
            }
        });
        deviceModuleHandler.addDeviceModule(systemDeviceModule);
    }

    public SystemDeviceModule getSystemDeviceModule() {
        return systemDeviceModule;
    }

    public SystemDeviceModule.Provider getSystemProvider() {
        return systemDeviceModule.getProvider();
    }

    public void createPlaybackControllerDeviceModule() {
        IPlaybackController playback = deviceModuleHandler.getPlatformFactory().getPlayback();
        playbackControllerDeviceModule = new PlaybackControllerDeviceModule(playback,
                deviceModuleHandler.getMessageSender(), alertsDeviceModule);
        deviceModuleHandler.addDeviceModule(playbackControllerDeviceModule);
    }

    public void createScreenDeviceModule() {
        IWebView webView = deviceModuleHandler.getPlatformFactory().getWebView();
        screenDeviceModule = new ScreenDeviceModule(webView, deviceModuleHandler.getMessageSender());
        deviceModuleHandler.addDeviceModule(screenDeviceModule);
    }

    public void createScreenExtendDeviceModule() {
        screenExtendDeviceModule = new ScreenExtendDeviceModule(deviceModuleHandler.getMessageSender());
        deviceModuleHandler.addDeviceModule(screenExtendDeviceModule);
    }

    public ScreenExtendDeviceModule getScreenExtendDeviceModule() {
        return screenExtendDeviceModule;
    }

    public ScreenDeviceModule getScreenDeviceModule() {
        return screenDeviceModule;
    }

    public interface IDeviceModuleHandler {   //创建实现端能力的设备
        IPlatformFactory getPlatformFactory();

        DialogRequestIdHandler getDialogRequestIdHandler();

        IMessageSender getMessageSender();

        BaseMultiChannelMediaPlayer getMultiChannelMediaPlayer();

        void addDeviceModule(BaseDeviceModule deviceModule);

        DcsResponseDispatcher getResponseDispatcher();
    }
}
