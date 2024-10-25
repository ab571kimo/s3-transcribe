package controller;

import Model.TranscribeParam;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;
import utils.SecretsManagerUtils;

import java.util.UUID;

public class TranscribeController implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {

        S3EventNotificationRecord record = event.getRecords().getFirst();
        String srcBucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getUrlDecodedKey();
        String region = record.getAwsRegion();
        String fileUrl = "s3://" + srcBucket + "/" + srcKey;

        LambdaLogger logger = context.getLogger();
        logger.log("RECORD: " + record);
        logger.log("SOURCE BUCKET: " + srcBucket);
        logger.log("SOURCE KEY: " + srcKey);
        logger.log("S3 Region: " + region);
        logger.log("FILE URL: " + fileUrl);

        String jobName = UUID.randomUUID().toString();

        SecretsManagerUtils secretsManagerUtils = new SecretsManagerUtils();
        String secret = secretsManagerUtils.getSecret();
        logger.log("SECRET: " + secret);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TranscribeParam transcribeParam = gson.fromJson(secret, TranscribeParam.class);

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)

                .settings(Settings.builder()
                        .showSpeakerLabels(transcribeParam.isShowSpeakerLabels())
                        .maxSpeakerLabels(transcribeParam.getMaxSpeakerLabels())
                        .channelIdentification(transcribeParam.isChannelIdentification())
                        .showAlternatives(transcribeParam.isShowAlternatives())
                        .build())

                .mediaFormat("mp4")
                .media(Media.builder()
                        .mediaFileUri(fileUrl)
                        .build())

                .subtitles(Subtitles.builder()
                        .formats(SubtitleFormat.SRT, SubtitleFormat.VTT)
                        .build())

                .identifyMultipleLanguages(true)
                .languageOptions(LanguageCode.ZH_TW, LanguageCode.EN_US)

                .outputBucketName(srcBucket)
                .outputKey(srcKey + ".json")
                .build();

        TranscribeClient transcribeClient = TranscribeClient.builder().build();
        transcribeClient.startTranscriptionJob(request);

        return srcBucket + "/" + srcKey;

    }
}